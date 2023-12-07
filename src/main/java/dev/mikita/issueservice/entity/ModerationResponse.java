package dev.mikita.issueservice.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "bc_moderation_response")
public class ModerationResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "moderator_id", nullable = false, length = 128)
    private String moderatorId;

    @OneToOne
    @JoinColumn(name = "issue_id", nullable = false, unique = true)
    private Issue issue;

    @Column(name = "moderation_response", nullable = false, length = 1000)
    private String comment;

    @Column(name = "creation_date", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime creationDate = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        Objects.requireNonNull(id);
        this.id = id;
    }

    public String getModeratorId() {
        return moderatorId;
    }

    public void setModeratorId(String moderatorId) {
        Objects.requireNonNull(moderatorId);
        this.moderatorId = moderatorId;
    }

    public Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        Objects.requireNonNull(issue);
        this.issue = issue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        Objects.requireNonNull(comment);
        this.comment = comment;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        Objects.requireNonNull(creationDate);
        this.creationDate = creationDate;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ModerationResponse that = (ModerationResponse) object;
        return Objects.equals(id, that.id) && Objects.equals(moderatorId, that.moderatorId) && Objects.equals(issue, that.issue) && Objects.equals(comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, moderatorId, issue, comment);
    }

    @Override
    public String toString() {
        return "ModerationResponse{" +
                "id=" + id +
                ", moderatorId='" + moderatorId + '\'' +
                ", issue=" + issue +
                ", moderationResponse='" + comment + '\'' +
                '}';
    }
}
