package dev.mikita.issueservice.service;

import dev.mikita.issueservice.dto.ChangeIssueStatusNotificationDto;
import dev.mikita.issueservice.entity.Issue;
import dev.mikita.issueservice.entity.IssueReservation;
import dev.mikita.issueservice.entity.IssueSolution;
import dev.mikita.issueservice.entity.IssueStatus;
import dev.mikita.issueservice.exception.NotFoundException;
import dev.mikita.issueservice.repository.IssueRepository;
import dev.mikita.issueservice.repository.IssueReservationRepository;
import dev.mikita.issueservice.repository.IssueSolutionRepository;
import dev.mikita.issueservice.util.FirebaseStorageUtil;
import jakarta.security.auth.message.AuthException;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * The type Issue solution service.
 */
@Service
@Transactional(readOnly = true)
public class IssueSolutionService {
    private final IssueSolutionRepository issueSolutionRepository;
    private final IssueReservationRepository issueReservationRepository;
    private final IssueRepository issueRepository;
    private final FirebaseStorageUtil firebaseStorageUtil;

    private final KafkaTemplate<String, ChangeIssueStatusNotificationDto> kafkaTemplate;
    private static final String STATUS_CHANGE_TOPIC = "notifications";

    /**
     * Instantiates a new Issue solution service.
     *
     * @param issueSolutionRepository    the issue solution repository
     * @param issueReservationRepository the issue reservation repository
     * @param issueRepository            the issue repository
     * @param firebaseStorageUtil        the firebase storage util
     * @param kafkaTemplate              the kafka template
     */
    @Autowired
    public IssueSolutionService(IssueSolutionRepository issueSolutionRepository,
                                IssueReservationRepository issueReservationRepository,
                                IssueRepository issueRepository,
                                FirebaseStorageUtil firebaseStorageUtil,
                                KafkaTemplate<String, ChangeIssueStatusNotificationDto> kafkaTemplate) {
        this.issueSolutionRepository = issueSolutionRepository;
        this.issueReservationRepository = issueReservationRepository;
        this.issueRepository = issueRepository;
        this.firebaseStorageUtil = firebaseStorageUtil;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Gets issue solution by id.
     *
     * @param id the id
     * @return the issue solution by id
     */
    public IssueSolution getIssueSolution(Long id) {
        return issueSolutionRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Issue solution does not found."));
    }

    @SneakyThrows
    @Transactional
    public void createIssueSolution(Long issueId, String employeeUid, String description, MultipartFile photoFile) {
        // Get Issue
        Issue issue = issueRepository.findById(issueId).orElseThrow(
                () -> new NotFoundException("Issue does not found."));

        // Check status
        if (issue.getStatus() != IssueStatus.SOLVING) {
            throw new IllegalStateException("You cannot add a solution to the issue that is not solving.");
        }

        // Get Issue Reservation
        IssueReservation issueReservation = issueReservationRepository.getIssueReservationByIssueId(issueId);
        if (issueReservation == null) {
            throw new IllegalStateException("You cannot add a solution to the issue you didn't reserve.");
        }

        // Check if issue is reserved by the same employee
        if (!Objects.equals(issueReservation.getEmployeeUid(), employeeUid)) {
            throw new AuthException("You cannot add a solution to the issue you didn't reserve.");
        }

        // Set status
        issue.setStatus(IssueStatus.SOLVED);

        // Create Solution
        IssueSolution issueSolution = new IssueSolution();
        issueSolution.setDescription(description);
        issueSolution.setIssue(issue);
        issueSolution.setServiceUid(issueReservation.getServiceUid());
        issueSolution.setDepartmentUid(issueReservation.getDepartmentUid());
        issueSolution.setEmployeeUid(issueReservation.getEmployeeUid());

        // Photo
        String storagePath = firebaseStorageUtil.uploadImage(photoFile, "issues/%s/".formatted(issue.getId()));
        issueSolution.setPhoto(storagePath);
        issueSolutionRepository.save(issueSolution);

        // Send notification
        ChangeIssueStatusNotificationDto notificationDto = new ChangeIssueStatusNotificationDto(issueId, issue.getAuthorUid(), IssueStatus.SOLVED);
        kafkaTemplate.send(STATUS_CHANGE_TOPIC, notificationDto);
    }

    public Page<IssueSolution> getIssuesSolutions(
            String serviceId, String employeeId, String departmentId,
            LocalDate from, LocalDate to, List<Long> categories, Pageable pageable) {
        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.atStartOfDay();

        int includeCategories = categories == null ? 0 : 1;

        return issueSolutionRepository.getIssuesSolutions(
                serviceId, employeeId, departmentId, categories, fromDateTime, toDateTime, includeCategories, pageable);
    }

    public Long getIssuesSolutionsCount(
            String serviceId, String employeeId, String departmentId, LocalDate from, LocalDate to, List<Long> categories) {
        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.atStartOfDay();

        int includeCategories = categories == null ? 0 : 1;

        return issueSolutionRepository.getIssuesSolutionsCount(
                serviceId, employeeId, departmentId, categories, fromDateTime, toDateTime, includeCategories);
    }

    public Double getAverageSolutionsTime(
            String serviceId, String employeeId, String departmentId, List<Long> categories, LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.atStartOfDay();

        int includeCategories = categories == null ? 0 : 1;

        Double result = issueSolutionRepository.getAverageSolutionsTime(
                serviceId, employeeId, departmentId, categories, fromDateTime, toDateTime, includeCategories);

        return result == null ? 0 : result;
    }
}
