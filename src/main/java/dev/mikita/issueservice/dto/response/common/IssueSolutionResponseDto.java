package dev.mikita.issueservice.dto.response.common;

import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "Issue solution id cannot be empty")
    Long id;
    /**
     * The Photo.
     */
    @NotBlank(message = "Issue solution photo cannot be empty")
    String photo;
    /**
     * The Description.
     */
    @NotBlank(message = "Issue solution description cannot be empty")
    String description;
    /**
     * The Creation date.
     */
    @NotBlank(message = "Issue solution creation date cannot be empty")
    LocalDateTime creationDate;
    /**
     * The Service id.
     */
    @NotBlank(message = "Issue solution service id cannot be empty")
    String serviceUid;
    /**
     * The Issue id.
     */
    @NotBlank(message = "Issue solution issue id cannot be empty")
    Long issueId;
}
