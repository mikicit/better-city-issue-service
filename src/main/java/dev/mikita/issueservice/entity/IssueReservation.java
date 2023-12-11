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

    @Column(name = "service_uid", nullable = false, length = 128)
    private String serviceUid;

    @Column(name = "employee_uid", nullable = false, length = 128)
    private String employeeUid;

    @Column(name = "department_uid", nullable = false, length = 128)
    private String departmentUid;

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
    public String getServiceUid() {
        return serviceUid;
    }



    /**
     * Sets service id.
     *
     * @param service the service
     */
    public void setServiceUid(String service) {
        Objects.requireNonNull(service);
        this.serviceUid = service;
    }

    public String getEmployeeUid() {
        return employeeUid;
    }

    public void setEmployeeUid(String employeeUid) {
        Objects.requireNonNull(employeeUid);
        this.employeeUid = employeeUid;
    }

    public String getDepartmentUid() {
        return departmentUid;
    }

    public void setDepartmentUid(String departmentUid) {
        Objects.requireNonNull(departmentUid);
        this.departmentUid = departmentUid;
    }


    @Override
    public String toString() {
        return "IssueReservation{" +
                "id=" + id +
                ", creationDate=" + creationDate +
                ", issue=" + issue +
                ", serviceUid='" + serviceUid + '\'' +
                ", employeeUid='" + employeeUid + '\'' +
                ", departmentUid='" + departmentUid + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        IssueReservation that = (IssueReservation) object;
        return Objects.equals(id, that.id) && Objects.equals(creationDate, that.creationDate) && Objects.equals(issue, that.issue) && Objects.equals(serviceUid, that.serviceUid) && Objects.equals(employeeUid, that.employeeUid) && Objects.equals(departmentUid, that.departmentUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creationDate, issue, serviceUid, employeeUid, departmentUid);
    }
}
