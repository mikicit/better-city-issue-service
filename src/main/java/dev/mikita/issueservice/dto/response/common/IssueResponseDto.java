package dev.mikita.issueservice.dto.response.common;

import dev.mikita.issueservice.entity.IssueStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;

/**
 * The type Issue response dto.
 */
@Data
public class IssueResponseDto {
    /**
     * The Id.
     */
    @NotBlank(message = "Issue id cannot be empty")
    Long id;
    /**
     * The Photo.
     */
    @NotBlank(message = "Issue photo cannot be empty")
    String photo;
    /**
     * The Coordinates.
     */
    @NotBlank(message = "Issue coordinates cannot be empty")
    Point coordinates;
    /**
     * The Description.
     */
    @NotBlank(message = "Issue description cannot be empty")
    String description;
    /**
     * The Title.
     */
    @NotBlank(message = "Issue title cannot be empty")
    String title;
    /**
     * The Author id.
     */
    @NotBlank(message = "Issue author id cannot be empty")
    String authorUid;
    /**
     * The Category id.
     */
    @NotBlank(message = "Issue category id cannot be empty")
    Long categoryId;
    /**
     * The Creation date.
     */
    @NotBlank(message = "Issue creation date cannot be empty")
    LocalDateTime creationDate;
    /**
     * The Status.
     */
    @NotBlank(message = "Issue status cannot be empty")
    IssueStatus status;

    @NotBlank(message = "Issue like count cannot be empty")
    int likeCount;
}
