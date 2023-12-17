package dev.mikita.issueservice.service;

import dev.mikita.issueservice.dto.ChangeIssueStatusNotificationDto;
import dev.mikita.issueservice.dto.request.GetIssuesInSquareRequestDto;
import dev.mikita.issueservice.entity.*;
import dev.mikita.issueservice.exception.NotFoundException;
import dev.mikita.issueservice.repository.*;
import dev.mikita.issueservice.entity.IssueStatus;
import dev.mikita.issueservice.util.FirebaseStorageUtil;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
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

/**
 * The type Issue service.
 */
@Service
@Transactional(readOnly = true)
public class IssueService {
    private final IssueRepository issueRepository;
    private final LikeRepository likeRepository;
    private final IssueReservationRepository reservationRepository;
    private final IssueSolutionRepository solutionRepository;
    private final CategoryRepository categoryRepository;
    private final ModerationResponseRepository moderationResponseRepository;
    private final FirebaseStorageUtil firebaseStorageUtil;

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
     * @param firebaseStorageUtil   the firebase storage util
     * @param kafkaTemplate         the kafka template
     */
    @Autowired
    public IssueService(IssueRepository repository,
                        LikeRepository likeRepository,
                        IssueReservationRepository reservationRepository,
                        IssueSolutionRepository solutionRepository,
                        CategoryRepository categoryRepository,
                        ModerationResponseRepository moderationResponseRepository,
                        FirebaseStorageUtil firebaseStorageUtil,
                        KafkaTemplate<String, ChangeIssueStatusNotificationDto> kafkaTemplate) {
        this.issueRepository = repository;
        this.likeRepository = likeRepository;
        this.reservationRepository = reservationRepository;
        this.solutionRepository = solutionRepository;
        this.categoryRepository = categoryRepository;
        this.moderationResponseRepository = moderationResponseRepository;
        this.firebaseStorageUtil = firebaseStorageUtil;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Page<Issue> getIssues(
            List<IssueStatus> statuses, String authorUid, List<Long> categories, LocalDate from, LocalDate to, Pageable pageable) {
        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.atStartOfDay();

        int includeStatuses = statuses == null ? 0 : 1;
        int includeCategories = categories == null ? 0 : 1;

        return issueRepository.findAll(
                statuses, authorUid, categories, fromDateTime, toDateTime, includeCategories, includeStatuses, pageable);
    }

    public List<Issue> getIssuesInRadius(
            List<IssueStatus> statuses, List<Long> categories, LocalDate from, LocalDate to, Double distance, Double latitude, Double longitude) {
        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.atStartOfDay();
        String coordinatesString = createCoordinatesString(latitude, longitude);

        int includeStatuses = statuses == null ? 0 : 1;
        int includeCategories = categories == null ? 0 : 1;

        return issueRepository.findAllByRadius(
                statuses, categories, coordinatesString, distance, fromDateTime, toDateTime, includeCategories, includeStatuses);
    }

    public List<Issue> getIssuesInSquare(GetIssuesInSquareRequestDto requestDto) {
        LocalDateTime fromDateTime = requestDto.getFrom() == null ? null : requestDto.getFrom().atStartOfDay();
        LocalDateTime toDateTime = requestDto.getTo() == null ? null : requestDto.getTo().atStartOfDay();

        int includeStatuses = requestDto.getStatuses() == null ? 0 : 1;
        int includeCategories = requestDto.getCategories() == null ? 0 : 1;

        return issueRepository.findAllBySquare(
                requestDto.getStatuses(),
                requestDto.getCategories(),
                requestDto.getMinLongitude(),
                requestDto.getMinLatitude(),
                requestDto.getMaxLongitude(),
                requestDto.getMaxLatitude(),
                fromDateTime,
                toDateTime,
                includeCategories,
                includeStatuses);
    }

    public Page<Issue> getIssuesByHolder(
            String serviceUid, String departmentUid, String employeeUid,
            List<IssueStatus> statuses, String authorUid, List<Long> categories, LocalDate from, LocalDate to, Pageable pageable) {
        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.atStartOfDay();

        int includeStatuses = statuses == null ? 0 : 1;
        int includeCategories = categories == null ? 0 : 1;

        return issueRepository.findAllByHolder(
                serviceUid, departmentUid, employeeUid, statuses, authorUid, categories, fromDateTime, toDateTime, includeCategories, includeStatuses, pageable);
    }

    /**
     * Find issue by id issue.
     *
     * @param id the id
     * @return the issue
     */
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
        statusChangeNotification.setUserId(issue.getAuthorUid());
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
        moderationResponse.setModeratorUid(moderatorId);
        moderationResponse.setComment(response);
        moderationResponseRepository.save(moderationResponse);

        // Send notification
        ChangeIssueStatusNotificationDto statusChangeNotification = new ChangeIssueStatusNotificationDto();
        statusChangeNotification.setIssueId(issueId);
        statusChangeNotification.setUserId(issue.getAuthorUid());
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
        issue.setAuthorUid(residentId);
        issue.setStatus(IssueStatus.MODERATION);
        issueRepository.save(issue);

        // Photo
        if (photoFile == null) {
            throw new IOException("File is null.");
        }

        String storagePath = firebaseStorageUtil.uploadImage(photoFile, "issues/%s/".formatted(issue.getId()));
        issue.setPhoto(storagePath);
        issueRepository.save(issue);
    }

    /**
     * Find issue reservation.
     *
     * @param issueId the issue id
     * @return the issue reservation
     */
    public IssueReservation findIssueReservation(Long issueId) {
        IssueReservation reservation = reservationRepository.getIssueReservationByIssueId(issueId);

        if (reservation == null) {
            throw new NotFoundException("Reservation is not found.");
        }

        return reservation;
    }

    /**
     * Find issue solution.
     *
     * @param issueId the issue id
     * @return the issue solution
     */
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
        like.setResidentUid(residentId);
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
    public Long getLikesCount(Long issueId) {
        if (!issueRepository.existsById(issueId)) {
            throw new NotFoundException("Issue is not found.");
        }

        return issueRepository.findLikeCountById(issueId);
    }

    public Long getIssuesCount(List<IssueStatus> statuses, String authorUid, List<Long> categories, LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.atStartOfDay();

        int includeCategories = categories == null ? 0 : 1;
        int includeStatuses = statuses == null ? 0 : 1;

        return issueRepository.getIssuesCount(
                statuses, authorUid, categories, fromDateTime, toDateTime, includeCategories, includeStatuses);
    }

    public Long getIssuesCountByHolder(String serviceUid, String departmentUid, String employeeUid,
                                       List<IssueStatus> statuses, String authorUid, List<Long> categories, LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.atStartOfDay();

        int includeCategories = categories == null ? 0 : 1;
        int includeStatuses = statuses == null ? 0 : 1;

        return issueRepository.getIssuesCountByHolder(
                serviceUid, departmentUid, employeeUid, statuses, authorUid, categories, fromDateTime, toDateTime, includeCategories, includeStatuses);
    }

    public ModerationResponse getModerationResponseByIssueId(Long issueId) {
        if (!moderationResponseRepository.existsById(issueId)) {
            throw new NotFoundException("Moderation response is not found.");
        }

        return moderationResponseRepository.getModerationResponseByIssueId(issueId);
    }

    private String createCoordinatesString(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }

        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate coordinate = new Coordinate(longitude, latitude);
        Point coordinates = geometryFactory.createPoint(coordinate);
        return String.format("POINT(%s %s)", coordinates.getX(), coordinates.getY());
    }
}
