package dev.mikita.issueservice.service;

import com.google.firebase.cloud.StorageClient;
import dev.mikita.issueservice.dto.ChangeIssueStatusNotificationDto;
import dev.mikita.issueservice.entity.Issue;
import dev.mikita.issueservice.entity.IssueReservation;
import dev.mikita.issueservice.entity.IssueSolution;
import com.google.cloud.storage.Bucket;
import dev.mikita.issueservice.entity.IssueStatus;
import dev.mikita.issueservice.exception.NotFoundException;
import dev.mikita.issueservice.repository.IssueRepository;
import dev.mikita.issueservice.repository.IssueReservationRepository;
import dev.mikita.issueservice.repository.IssueSolutionRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.Objects;
import java.util.UUID;

/**
 * The type Issue solution service.
 */
@Service
public class IssueSolutionService {
    private final IssueSolutionRepository issueSolutionRepository;
    private final IssueReservationRepository issueReservationRepository;
    private final IssueRepository issueRepository;
    private final StorageClient firebaseStorage;

    private final KafkaTemplate<String, ChangeIssueStatusNotificationDto> kafkaTemplate;
    private static final String STATUS_CHANGE_TOPIC = "notifications";

    /**
     * Instantiates a new Issue solution service.
     *
     * @param issueSolutionRepository    the issue solution repository
     * @param issueReservationRepository the issue reservation repository
     * @param issueRepository            the issue repository
     * @param firebaseStorage            the firebase storage
     * @param kafkaTemplate              the kafka template
     */
    @Autowired
    public IssueSolutionService(IssueSolutionRepository issueSolutionRepository,
                                IssueReservationRepository issueReservationRepository,
                                IssueRepository issueRepository,
                                StorageClient firebaseStorage,
                                KafkaTemplate<String, ChangeIssueStatusNotificationDto> kafkaTemplate) {
        this.issueSolutionRepository = issueSolutionRepository;
        this.issueReservationRepository = issueReservationRepository;
        this.issueRepository = issueRepository;
        this.firebaseStorage = firebaseStorage;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Gets issue solution by id.
     *
     * @param id the id
     * @return the issue solution by id
     */
    @Transactional(readOnly = true)
    public IssueSolution getIssueSolutionById(Long id) {
        return issueSolutionRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Issue solution does not found."));
    }

    /**
     * Create issue solution.
     *
     * @param issueId     the issue id
     * @param serviceId   the service id
     * @param description the description
     * @param photoFile   the photo file
     */
    @SneakyThrows
    @Transactional
    public void createIssueSolution(Long issueId, String serviceId, String description, MultipartFile photoFile) {
        // Get Issue
        Issue issue = issueRepository.findById(issueId).orElseThrow(
                () -> new NotFoundException("Issue does not found."));

        // Get Issue Reservation
        IssueReservation issueReservation = issueReservationRepository.getIssueReservationByIssueId(issueId);

        if (issueReservation == null) {
            throw new IllegalStateException("You cannot add a solution to the issue you didn't reserve.");
        }

        // Check if issue is reserved by the same service
        if (!Objects.equals(issueReservation.getServiceId(), serviceId)) {
            throw new IllegalStateException("You cannot add a solution to the issue you didn't reserve.");
        }

        // Create Solution
        IssueSolution issueSolution = new IssueSolution();
        issueSolution.setDescription(description);
        issueSolution.setIssue(issue);
        issueSolution.setServiceId(serviceId);

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

        issueSolution.setPhoto(photoUrl);
        issueSolution.getIssue().setStatus(IssueStatus.SOLVED);

        issueSolutionRepository.save(issueSolution);

        // Send notification
        ChangeIssueStatusNotificationDto notificationDto = new ChangeIssueStatusNotificationDto();
        notificationDto.setIssueId(issueId);
        notificationDto.setUserId(issue.getAuthorId());
        notificationDto.setStatus(IssueStatus.SOLVED);
        kafkaTemplate.send(STATUS_CHANGE_TOPIC, notificationDto);
    }

    /**
     * Gets issues solutions count.
     *
     * @param serviceId the service id
     * @return the issues solutions count
     */
    @Transactional(readOnly = true)
    public Long getIssuesSolutionsCount(String serviceId) {
        return issueSolutionRepository.countByServiceId(serviceId);
    }
}
