package dev.mikita.issueservice.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import dev.mikita.issueservice.dto.ChangeIssueStatusNotificationDto;
import dev.mikita.issueservice.entity.Department;
import dev.mikita.issueservice.entity.Issue;
import dev.mikita.issueservice.entity.IssueReservation;
import dev.mikita.issueservice.entity.IssueStatus;
import dev.mikita.issueservice.exception.NotFoundException;
import dev.mikita.issueservice.repository.DepartmentRepository;
import dev.mikita.issueservice.repository.EmployeeRepository;
import dev.mikita.issueservice.repository.IssueRepository;
import dev.mikita.issueservice.repository.IssueReservationRepository;
import jakarta.security.auth.message.AuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * The type Issue reservation service.
 */
@Service
@Transactional(readOnly = true)
public class IssueReservationService {
    private final IssueReservationRepository issueReservationRepository;
    private final IssueRepository issueRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final KafkaTemplate<String, ChangeIssueStatusNotificationDto> kafkaTemplate;
    private static final String STATUS_CHANGE_TOPIC = "notifications";

    @Autowired
    public IssueReservationService(IssueReservationRepository issueReservationRepository,
                                   IssueRepository issueRepository,
                                   EmployeeRepository employeeRepository,
                                   DepartmentRepository departmentRepository,
                                   KafkaTemplate<String, ChangeIssueStatusNotificationDto> kafkaTemplate) {
        this.issueReservationRepository = issueReservationRepository;
        this.issueRepository = issueRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void createIssueReservation(Long issueId, FirebaseToken token)
            throws AuthException, ExecutionException, FirebaseAuthException, InterruptedException {
        // Get Issue
        Issue issue = issueRepository.findById(issueId).orElseThrow(
                () -> new NotFoundException("Issue is not found."));

        // Check status
        if (issue.getStatus() != IssueStatus.PUBLISHED) {
            throw new IllegalStateException("Issue is not published.");
        }

        // Get Department
        Department department = departmentRepository.find(token.getClaims().get("departmentUid").toString());

        // Check department
        if (!department.getCategories().contains(issue.getCategory().getId())) {
            throw new AuthException("Employee has no access to this category.");
        }

        // Set status
        issue.setStatus(IssueStatus.SOLVING);

        // Create reservation
        IssueReservation issueReservation = new IssueReservation();
        issueReservation.setIssue(issue);
        issueReservation.setServiceUid(token.getClaims().get("serviceUid").toString());
        issueReservation.setEmployeeUid(token.getUid());
        issueReservation.setDepartmentUid(token.getClaims().get("departmentUid").toString());

        issueReservationRepository.save(issueReservation);

        // Send notification
        ChangeIssueStatusNotificationDto notificationDto = new ChangeIssueStatusNotificationDto(issueId, issue.getAuthorUid(), IssueStatus.SOLVING);
        kafkaTemplate.send(STATUS_CHANGE_TOPIC, notificationDto);
    }

    /**
     * Gets issue reservation.
     *
     * @param id the id
     * @return the issue reservation
     */
    public IssueReservation getIssueReservation(Long id) {
        return issueReservationRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Issue reservation is not found."));
    }

    public Page<IssueReservation> getIssuesReservations(
            String serviceId, String employeeId, String departmentId, LocalDate from, LocalDate to, List<Long> categories, Pageable pageable) {
        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.atStartOfDay();

        int includeCategories = categories == null ? 0 : 1;

        return issueReservationRepository.getIssuesReservations(
                serviceId, employeeId, departmentId, fromDateTime, toDateTime, categories, includeCategories, pageable);
    }

    public Long getIssuesReservationsCount(
            String serviceId, String employeeId, String departmentId , LocalDate from, LocalDate to, List<Long> categories) {
        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.atStartOfDay();

        int includeCategories = categories == null ? 0 : 1;

        return issueReservationRepository.getIssuesReservationsCount(
                serviceId, employeeId, departmentId, fromDateTime, toDateTime, categories, includeCategories);
    }
}
