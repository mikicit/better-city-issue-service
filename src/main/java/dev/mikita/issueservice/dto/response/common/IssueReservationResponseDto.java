package dev.mikita.issueservice.dto.response.common;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * The type Issue reservation response dto.
 */
@Data
public class IssueReservationResponseDto {
    /**
     * The Id.
     */
    @NotBlank(message = "Issue reservation id cannot be empty")
    Long id;
    /**
     * The Creation date.
     */
    @NotBlank(message = "Issue reservation creation date cannot be empty")
    LocalDateTime creationDate;
    /**
     * The Service id.
     */
    @NotBlank(message = "Issue reservation service id cannot be empty")
    String serviceUid;
    /**
     * The Issue id.
     */
    @NotBlank(message = "Issue reservation issue id cannot be empty")
    Long issueId;
}
