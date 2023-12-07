package dev.mikita.issueservice.dto.response.service;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * The type Issue solution response dto.
 */
@Data
public class IssueSolutionServiceResponseDto {
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
    String employeeId;
    /**
     * The Issue id.
     */
    Long issueId;
}
