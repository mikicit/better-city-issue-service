package dev.mikita.issueservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * The type Issue reservation.
 */
@Entity
@Table(name = "bc_issue_reservation")
public class IssueReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "creation_date", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime creationDate = LocalDateTime.now();

    @OneToOne
    @JoinColumn(name = "issue_id", nullable = false, unique = true)
    private Issue issue;

    @Column(name = "service_id", nullable = false, length = 128)
    private String serviceId;

    /**
     * Gets id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets creation date.
     *
     * @return the creation date
     */
    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * Sets creation date.
     *
     * @param creationDate the creation date
     */
    public void setCreationDate(LocalDateTime creationDate) {
        Objects.requireNonNull(creationDate);
        this.creationDate = creationDate;
    }

    /**
     * Gets issue.
     *
     * @return the issue
     */
    public Issue getIssue() {
        return issue;
    }

    /**
     * Sets issue.
     *
     * @param issue the issue
     */
    public void setIssue(Issue issue) {
        Objects.requireNonNull(issue);
        this.issue = issue;
    }

    /**
     * Gets service id.
     *
     * @return the service id
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Sets service id.
     *
     * @param service the service
     */
    public void setServiceId(String service) {
        Objects.requireNonNull(service);
        this.serviceId = service;
    }

    @Override
    public String toString() {
        return "IssueReservation{" +
                "id=" + id +
                ", creationDate=" + creationDate +
                ", issue=" + issue +
                ", serviceId=" + serviceId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IssueReservation that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(creationDate, that.creationDate) && Objects.equals(issue, that.issue) && Objects.equals(serviceId, that.serviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creationDate, issue, serviceId);
    }
}
