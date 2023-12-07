package dev.mikita.issueservice.service;

import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import dev.mikita.issueservice.dto.ChangeIssueStatusNotificationDto;
import dev.mikita.issueservice.entity.*;
import dev.mikita.issueservice.exception.NotFoundException;
import dev.mikita.issueservice.repository.*;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The type Issue service.
 */
@Service
public class IssueService {
    private final IssueRepository issueRepository;
    private final LikeRepository likeRepository;
    private final IssueReservationRepository reservationRepository;
    private final IssueSolutionRepository solutionRepository;
    private final CategoryRepository categoryRepository;
    private final ModerationResponseRepository moderationResponseRepository;
    private final StorageClient firebaseStorage;

    private final KafkaTemplate<String, ChangeIssueStatusNotificationDto> kafkaTemplate;
    private static final String STATUS_CHANGE_TOPIC = "notifications";

    /**
     * Instantiates a new Issue service.
     *
     * @param repository            the repository
     * @param likeRepository        the like repository
     * @param reservationRepository the reservation repository
     * @param solutionRepository    the solution repository
     * @param categoryRepository    the category repository
     * @param firebaseStorage       the firebase storage
     * @param kafkaTemplate         the kafka template
     */
    @Autowired
    public IssueService(IssueRepository repository,
                        LikeRepository likeRepository,
                        IssueReservationRepository reservationRepository,
                        IssueSolutionRepository solutionRepository,
                        CategoryRepository categoryRepository,
                        ModerationResponseRepository moderationResponseRepository,
                        StorageClient firebaseStorage,
                        KafkaTemplate<String, ChangeIssueStatusNotificationDto> kafkaTemplate) {
        this.issueRepository = repository;
        this.likeRepository = likeRepository;
        this.reservationRepository = reservationRepository;
        this.solutionRepository = solutionRepository;
        this.categoryRepository = categoryRepository;
        this.moderationResponseRepository = moderationResponseRepository;
        this.firebaseStorage = firebaseStorage;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional(readOnly = true)
    public Page<Issue> getIssues(Pageable pageable, Map<String, Object> filters) {
        IssueStatus status = filters.get("status") == null ? null : (IssueStatus) filters.get("status");
        String authorId = filters.get("authorId") == null ? null : (String) filters.get("authorId");
        List<Long> categories = filters.get("categories") == null ? null : (List<Long>) filters.get("categories");
        LocalDateTime from = filters.get("from") == null ? null : ((LocalDate) filters.get("from")).atStartOfDay();
        LocalDateTime to = filters.get("to") == null ? null : ((LocalDate) filters.get("to")).atStartOfDay();

        int includeCategories = categories == null ? 0 : 1;

        return issueRepository.findAll(status, authorId, categories, from, to, includeCategories, pageable);
    }

    @Transactional(readOnly = true)
    public List<Issue> getIssuesInRadius(Map<String, Object> filters) {
        IssueStatus status = filters.get("status") == null ? null : (IssueStatus) filters.get("status");
        List<Long> categories = filters.get("categories") == null ? null : (List<Long>) filters.get("categories");
        LocalDateTime from = filters.get("from") == null ? null : ((LocalDate) filters.get("from")).atStartOfDay();
        LocalDateTime to = filters.get("to") == null ? null : ((LocalDate) filters.get("to")).atStartOfDay();
        Point coordinates =  filters.get("coordinates") == null ? null : (Point) filters.get("coordinates");
        String coordinatesString = null;

        if (coordinates != null) {
            coordinatesString = String.format("POINT(%s %s)", coordinates.getX(), coordinates.getY());
        }

        Double distanceM = (Double) filters.get("distanceM");
        int includeCategories = categories == null ? 0 : 1;

        return issueRepository.findAllByRadius(status, categories, coordinatesString, distanceM, from, to, includeCategories);
    }

    /**
     * Find issue by id issue.
     *
     * @param id the id
     * @return the issue
     */
    @Transactional(readOnly = true)
    public Issue findIssueById(Long id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Issue is not found."));
    }

    @Transactional
    public void approveIssue(Long issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException("Issue is not found."));

        if (issue.getStatus() != IssueStatus.MODERATION) {
            throw new IllegalStateException("Issue is not in moderation.");
        }

        issue.setStatus(IssueStatus.PUBLISHED);
        issueRepository.save(issue);

        // Send notification
        ChangeIssueStatusNotificationDto statusChangeNotification = new ChangeIssueStatusNotificationDto();
        statusChangeNotification.setIssueId(issueId);
        statusChangeNotification.setUserId(issue.getAuthorId());
        statusChangeNotification.setStatus(IssueStatus.PUBLISHED);

        kafkaTemplate.send(STATUS_CHANGE_TOPIC, statusChangeNotification);
    }

    @Transactional
    public void declineIssue(Long issueId, String moderatorId, String response) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException("Issue is not found."));

        if (issue.getStatus() != IssueStatus.MODERATION) {
            throw new IllegalStateException("Issue is not in moderation.");
        }

        issue.setStatus(IssueStatus.DELETED);
        issueRepository.save(issue);
        ModerationResponse moderationResponse = new ModerationResponse();
        moderationResponse.setIssue(issue);
        moderationResponse.setModeratorId(moderatorId);
        moderationResponse.setComment(response);
        moderationResponseRepository.save(moderationResponse);

        // Send notification
        ChangeIssueStatusNotificationDto statusChangeNotification = new ChangeIssueStatusNotificationDto();
        statusChangeNotification.setIssueId(issueId);
        statusChangeNotification.setUserId(issue.getAuthorId());
        statusChangeNotification.setStatus(IssueStatus.DELETED);

        kafkaTemplate.send(STATUS_CHANGE_TOPIC, statusChangeNotification);
    }

    /**
     * Create issue.
     *
     * @param residentId  the resident id
     * @param title       the title
     * @param description the description
     * @param categoryId  the category id
     * @param coordinates the coordinates
     * @param photoFile   the photo file
     * @throws IOException the io exception
     */
    @Transactional
    public void createIssue(String residentId,
                            String title,
                            String description,
                            Long categoryId,
                            Point coordinates,
                            MultipartFile photoFile) throws IOException {
        // Get Category
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category is not found."));

        // Create issue
        Issue issue = new Issue();
        issue.setTitle(title);
        issue.setDescription(description);
        issue.setCategory(category);
        issue.setCoordinates(coordinates);
        issue.setAuthorId(residentId);

        // Photo name
        String originalFilename = photoFile.getOriginalFilename();
        assert originalFilename != null;
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String uniqueFilename = UUID.randomUUID() + fileExtension;

        // Firebase Storage
        Bucket bucket = firebaseStorage.bucket();
        String storagePath = "issues/" + uniqueFilename;
        bucket.create(storagePath, photoFile.getBytes(), photoFile.getContentType());
        String photoUrl = "https://storage.googleapis.com/" + bucket.getName() + "/" + storagePath;

        issue.setStatus(IssueStatus.MODERATION);
        issue.setPhoto(photoUrl);
        issueRepository.save(issue);
    }

    /**
     * Find issue reservation.
     *
     * @param issueId the issue id
     * @return the issue reservation
     */
    @Transactional(readOnly = true)
    public IssueReservation findIssueReservation(Long issueId) {
        IssueReservation reservation = reservationRepository.getIssueReservationByIssueId(issueId);

        if (reservation == null) {
            throw new NotFoundException("Reservation is not found.");
        }

        return reservationRepository.getIssueReservationByIssueId(issueId);
    }

    /**
     * Find issue solution.
     *
     * @param issueId the issue id
     * @return the issue solution
     */
    @Transactional(readOnly = true)
    public IssueSolution findIssueSolution(Long issueId) {
        IssueSolution solution = solutionRepository.getIssueSolutionByIssueId(issueId);

        if (solution == null) {
            throw new NotFoundException("Solution is not found.");
        }

        return solutionRepository.getIssueSolutionByIssueId(issueId);
    }

    /**
     * Like issue.
     *
     * @param issueId    the issue id
     * @param residentId the resident id
     */
    @Transactional
    public void likeIssue(Long issueId, String residentId) {
        Issue issue = issueRepository.findById(issueId).orElseThrow(
                () -> new NotFoundException("Issue is not found."));

        if (issue.getStatus() == IssueStatus.MODERATION || issue.getStatus() == IssueStatus.DELETED) {
            throw new IllegalStateException("You can't like this issue.");
        }

        if (likeRepository.getLikeStatus(issueId, residentId)) {
            throw new IllegalStateException("You have already liked this issue.");
        }

        Like like = new Like();
        like.setIssue(issue);
        like.setResident(residentId);
        likeRepository.save(like);
    }

    /**
     * Delete like issue.
     *
     * @param issueId    the issue id
     * @param residentId the resident id
     */
    @Transactional
    public void deleteLikeIssue(Long issueId, String residentId) {
        Issue issue = issueRepository.findById(issueId).orElseThrow(
                () -> new NotFoundException("Issue is not found."));

        if (issue.getStatus() == IssueStatus.MODERATION || issue.getStatus() == IssueStatus.DELETED) {
            throw new IllegalStateException("You can't unlike this issue.");
        }

        if (!likeRepository.getLikeStatus(issueId, residentId)) {
            throw new IllegalStateException("There is no like on this issue.");
        }

        Like like = likeRepository.findByIssueIdAndResidentId(issueId, residentId);
        likeRepository.delete(like);
    }

    /**
     * Gets like status.
     *
     * @param id         the id
     * @param residentId the resident id
     * @return the like status
     */
    @Transactional(readOnly = true)
    public Boolean getLikeStatus(Long id, String residentId) {
        if (!issueRepository.existsById(id)) {
            throw new NotFoundException("Issue is not found.");
        }

        return likeRepository.getLikeStatus(id, residentId);
    }

    /**
     * Gets likes count.
     *
     * @param issueId the issue id
     * @return the likes count
     */
    @Transactional(readOnly = true)
    public Long getLikesCount(Long issueId) {
        if (!issueRepository.existsById(issueId)) {
            throw new NotFoundException("Issue is not found.");
        }

        return issueRepository.findLikeCountById(issueId);
    }

    @Transactional
    public Long getIssuesCount(Map<String, Object> filters) {
        IssueStatus status = (IssueStatus) filters.get("status");
        String authorId = (String) filters.get("authorId");
        List<Long> categories = (List<Long>) filters.get("categories");
        LocalDate from = (LocalDate) filters.get("from");
        LocalDate to = (LocalDate) filters.get("to");

        int includeCategories = categories == null ? 0 : 1;

        return issueRepository.getIssuesCount(status, authorId, categories, from, to, includeCategories);
    }

    @Transactional
    public ModerationResponse getModerationResponseByIssueId(Long issueId) {
        if (!moderationResponseRepository.existsById(issueId)) {
            throw new NotFoundException("Moderation response is not found.");
        }

        return moderationResponseRepository.getModerationResponseByIssueId(issueId);
    }
}
