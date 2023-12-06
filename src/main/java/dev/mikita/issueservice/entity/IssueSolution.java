package dev.mikita.issueservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * The type Issue solution.
 */
@Entity
@Table(name = "bc_issue_solution")
public class IssueSolution {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "creation_date", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime creationDate = LocalDateTime.now();

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "photo", nullable = false)
    private String photo;

    @Column(name = "service_id", nullable = false, length = 128)
    private String serviceId;

    @Column(name = "employee_id", nullable = false, length = 128)
    private String employeeId;

    @Column(name = "department_id", nullable = false, length = 128)
    private String departmentId;

    @OneToOne
    @JoinColumn(name = "issue_id", nullable = false, unique = true)
    private Issue issue;

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
        Objects.requireNonNull(id);
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
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets photo.
     *
     * @return the photo
     */
    public String getPhoto() {
        return photo;
    }

    /**
     * Sets photo.
     *
     * @param photo the photo
     */
    public void setPhoto(String photo) {
        Objects.requireNonNull(photo);
        this.photo = photo;
    }

    /**
     * Sets description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        Objects.requireNonNull(description);
        this.description = description;
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

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        Objects.requireNonNull(employeeId);
        this.employeeId = employeeId;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        Objects.requireNonNull(departmentId);
        this.departmentId = departmentId;
    }

    @Override
    public String toString() {
        return "issueSolution{" +
                "id=" + id +
                ", creationDate=" + creationDate +
                ", description=" + description +
                ", photo=" + photo +
                ", serviceId=" + serviceId +
                ", issue=" + issue +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IssueSolution that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(creationDate, that.creationDate) && Objects.equals(description, that.description) && Objects.equals(photo, that.photo) && Objects.equals(serviceId, that.serviceId) && Objects.equals(issue, that.issue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creationDate, description, photo, serviceId, issue);
    }
}
