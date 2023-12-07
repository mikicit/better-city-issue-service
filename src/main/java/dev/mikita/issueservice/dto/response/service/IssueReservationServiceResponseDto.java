package dev.mikita.issueservice.dto.response.service;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * The type Issue reservation response dto.
 */
@Data
public class IssueReservationServiceResponseDto {
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
    String employeeId;
    /**
     * The Issue id.
     */
    Long issueId;
}
