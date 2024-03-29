package dev.mikita.issueservice.entity;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * The type Like.
 */
@Entity
@Table(name = "bc_like",
        uniqueConstraints = {
        @UniqueConstraint(name="unique_issue_resident", columnNames = {"issue_id", "resident_uid"})
})
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "issue_id")
    private Issue issue;

    @Column(name = "resident_uid", nullable = false, length = 128)
    private String residentUid;

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
     * Gets resident.
     *
     * @return the resident
     */
    public String getResidentUid() {
        return residentUid;
    }

    /**
     * Sets resident.
     *
     * @param residentUid the resident
     */
    public void setResidentUid(String residentUid) {
        Objects.requireNonNull(residentUid);
        this.residentUid = residentUid;
    }

    @Override
    public String toString() {
        return "Like{" +
                "id=" + id +
                ", issue=" + issue +
                ", residentUid=" + residentUid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Like like)) return false;
        return Objects.equals(id, like.id) && Objects.equals(issue, like.issue) && Objects.equals(residentUid, like.residentUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, issue, residentUid);
    }
}