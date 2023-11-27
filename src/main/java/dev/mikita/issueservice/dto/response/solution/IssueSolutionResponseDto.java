package dev.mikita.issueservice.dto.response.solution;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * The type Issue solution response dto.
 */
@Data
public class IssueSolutionResponseDto {
    /**
     * The Id.
     */
    Long id;
    /**
     * The Photo.
     */
    String photo;
    /**
     * The Description.
     */
    String description;
    /**
     * The Creation date.
     */
    LocalDateTime creationDate;
    /**
     * The Service id.
     */
    String serviceId;
    /**
     * The Issue id.
     */
    Long issueId;
}
