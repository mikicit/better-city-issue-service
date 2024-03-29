package dev.mikita.issueservice.dto.response.common;

import dev.mikita.issueservice.entity.IssueStatus;
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
    Long id;
    /**
     * The Photo.
     */
    String photo;
    /**
     * The Coordinates.
     */
    Point coordinates;
    /**
     * The Description.
     */
    String description;
    /**
     * The Title.
     */
    String title;
    /**
     * The Author id.
     */
    String authorUid;
    /**
     * The Category id.
     */
    Long categoryId;
    /**
     * The Creation date.
     */
    LocalDateTime creationDate;
    /**
     * The Status.
     */
    IssueStatus status;
    int likeCount;
}
