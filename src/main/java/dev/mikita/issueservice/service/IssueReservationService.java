package dev.mikita.issueservice.service;

import dev.mikita.issueservice.dto.ChangeIssueStatusNotificationDto;
import dev.mikita.issueservice.entity.Issue;
import dev.mikita.issueservice.entity.IssueReservation;
import dev.mikita.issueservice.entity.IssueStatus;
import dev.mikita.issueservice.exception.NotFoundException;
import dev.mikita.issueservice.repository.IssueRepository;
import dev.mikita.issueservice.repository.IssueReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Issue reservation service.
 */
@Service
public class IssueReservationService {
    private final IssueReservationRepository issueReservationRepository;
    private final IssueRepository issueRepository;

    private final KafkaTemplate<String, ChangeIssueStatusNotificationDto> kafkaTemplate;
    private static final String STATUS_CHANGE_TOPIC = "notifications";

    /**
     * Instantiates a new Issue reservation service.
     *
     * @param issueReservationRepository the issue reservation repository
     * @param issueRepository            the issue repository
     * @param kafkaTemplate              the kafka template
     */
    @Autowired
    public IssueReservationService(IssueReservationRepository issueReservationRepository,
                                   IssueRepository issueRepository,
                                   KafkaTemplate<String, ChangeIssueStatusNotificationDto> kafkaTemplate) {
        this.issueReservationRepository = issueReservationRepository;
        this.issueRepository = issueRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Create issue reservation.
     *
     * @param issueId   the issue id
     * @param serviceId the service id
     */
    @Transactional
    public void createIssueReservation(Long issueId, String serviceId) {
        // Get Issue
        Issue issue = issueRepository.findById(issueId).orElseThrow(
                () -> new NotFoundException("Issue is not found."));

        // Check status
        if (issue.getStatus() != IssueStatus.PUBLISHED) {
            throw new IllegalStateException("Issue is not published.");
        }

        // Set status
        issue.setStatus(IssueStatus.SOLVING);

        // Create reservation
        IssueReservation issueReservation = new IssueReservation();
        issueReservation.setIssue(issue);
        issueReservation.setServiceId(serviceId);

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

    /**
     * Gets issues reservations count.
     *
     * @param serviceId the service id
     * @return the issues reservations count
     */
    @Transactional(readOnly = true)
    public Long getIssuesReservationsCount(String serviceId) {
        return issueReservationRepository.countByServiceId(serviceId);
    }
}
