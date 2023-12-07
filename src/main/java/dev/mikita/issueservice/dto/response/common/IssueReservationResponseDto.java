package dev.mikita.issueservice.dto.response.common;

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
    Long id;
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
