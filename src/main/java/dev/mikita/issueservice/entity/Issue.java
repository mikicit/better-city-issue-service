package dev.mikita.issueservice.entity;

import org.locationtech.jts.geom.Point;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The type Issue.
 */
@Entity
@Table(name = "bc_issue")
public class Issue {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private IssueStatus status;

    @Column(name = "coordinates", nullable = false, columnDefinition = "geography")
    private Point coordinates;

    @Column(name = "photo", nullable = false)
    private String photo;

    @Column(name = "creation_date", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime creationDate = LocalDateTime.now();

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "title", nullable = false, length = 64)
    private String title;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "author_uid", nullable = false, length = 128)
    private String authorUid;

    @OneToOne(mappedBy = "issue", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private ModerationResponse moderationResponse;

    @OneToMany(mappedBy = "issue", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Like> likes = new ArrayList<>();

    @Transient
    private int likeCount;

    @OneToOne(mappedBy = "issue", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private IssueReservation issueReservation;

    @OneToOne(mappedBy = "issue", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private IssueSolution issueSolution;

    /**
     * Gets status.
     *
     * @return the status
     */
    public IssueStatus getStatus() {
        return status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(IssueStatus status) {
        Objects.requireNonNull(status);
        this.status = status;
    }

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
     * Gets coordinates.
     *
     * @return the coordinates
     */
    public Point getCoordinates() {
        return coordinates;
    }

    /**
     * Sets coordinates.
     *
     * @param coordinates the coordinates
     */
    public void setCoordinates(Point coordinates) {
        Objects.requireNonNull(coordinates);
        this.coordinates = coordinates;
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
     * Sets description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        Objects.requireNonNull(description);
        this.description = description;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        Objects.requireNonNull(title);
        this.title = title;
    }

    /**
     * Gets category.
     *
     * @return the category
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Sets category.
     *
     * @param category the category
     */
    public void setCategory(Category category) {
        Objects.requireNonNull(category);
        this.category = category;
    }

    /**
     * Gets author id.
     *
     * @return the author id
     */
    public String getAuthorUid() {
        return authorUid;
    }

    /**
     * Sets author id.
     *
     * @param author the author
     */
    public void setAuthorUid(String author) {
        Objects.requireNonNull(author);
        this.authorUid = author;
    }

    public int getLikeCount() {
        return likes.size();
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

    @Override
    public String toString() {
        return "Issue{" +
                "status=" + status +
                ", id=" + id +
                ", photo=" + photo +
                ", title='" + title +
                ", description='" + description +
                ", creationDate=" + creationDate +
                ", coordinates=" + coordinates +
                ", authorUid=" + authorUid +
                ", category=" + category +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Issue issue)) return false;
        return Objects.equals(id, issue.id) && status == issue.status && Objects.equals(coordinates, issue.coordinates) && Objects.equals(photo, issue.photo) && Objects.equals(creationDate, issue.creationDate) && Objects.equals(description, issue.description) && Objects.equals(title, issue.title) && Objects.equals(category, issue.category) && Objects.equals(authorUid, issue.authorUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, coordinates, photo, creationDate, description, title, category, authorUid);
    }
}
