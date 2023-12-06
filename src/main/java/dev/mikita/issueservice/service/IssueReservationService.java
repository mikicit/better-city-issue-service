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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * The type Issue reservation service.
 */
@Service
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
        Department department = departmentRepository.find(token.getClaims().get("departmentId").toString());

        // Check department
        if (!department.getCategories().contains(issue.getCategory().getId())) {
            throw new AuthException("Employee has no access to this category.");
        }

        // Set status
        issue.setStatus(IssueStatus.SOLVING);

        // Create reservation
        IssueReservation issueReservation = new IssueReservation();
        issueReservation.setIssue(issue);
        issueReservation.setServiceId(token.getClaims().get("serviceId").toString());
        issueReservation.setEmployeeId(token.getUid());
        issueReservation.setDepartmentId(token.getClaims().get("departmentId").toString());

        issueReservationRepository.save(issueReservation);

        // Send notification
        ChangeIssueStatusNotificationDto notificationDto = new ChangeIssueStatusNotificationDto();
        notificationDto.setIssueId(issueId);
        notificationDto.setUserId(issue.getAuthorId());
        notificationDto.setStatus(IssueStatus.SOLVING);
        kafkaTemplate.send(STATUS_CHANGE_TOPIC, notificationDto);
    }

    /**
     * Gets issue reservation.
     *
     * @param id the id
     * @return the issue reservation
     */
    @Transactional(readOnly = true)
    public IssueReservation getIssueReservation(Long id) {
        return issueReservationRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Issue reservation is not found."));
    }

    @Transactional(readOnly = true)
    public IssueReservation getIssueReservationByIssueId(Long issueId) {
        IssueReservation issueReservation = issueReservationRepository.getIssueReservationByIssueId(issueId);

        if (issueReservation == null) {
            throw new NotFoundException("Issue reservation is not found.");
        }

        return issueReservationRepository.getIssueReservationByIssueId(issueId);
    }

    @Transactional(readOnly = true)
    public List<IssueReservation> getIssueReservationByServiceId(String serviceId) {
        return issueReservationRepository.getIssueReservationByServiceId(serviceId);
    }

    @Transactional(readOnly = true)
    public List<IssueReservation> getIssueReservationByEmployeeId(String employeeId) {
        return issueReservationRepository.getIssueReservationByEmployeeId(employeeId);
    }

    @Transactional(readOnly = true)
    public List<IssueReservation> getIssueReservationByDepartmentId(String departmentId) {
        return issueReservationRepository.getIssueReservationByDepartmentId(departmentId);
    }

    @Transactional(readOnly = true)
    public Long getIssuesReservationsCount() {
        return issueReservationRepository.count();
    }

    @Transactional(readOnly = true)
    public Long getIssuesReservationsCountByServiceId(String serviceUid) {
        return issueReservationRepository.countByServiceId(serviceUid);
    }

    @Transactional(readOnly = true)
    public Long getIssuesReservationsCountByEmployeeId(String employeeUid) {
        return issueReservationRepository.countByEmployeeId(employeeUid);
    }

    @Transactional(readOnly = true)
    public Long getIssuesReservationsCountByDepartmentId(String departmentUid) {
        return issueReservationRepository.countByDepartmentId(departmentUid);
    }
}
