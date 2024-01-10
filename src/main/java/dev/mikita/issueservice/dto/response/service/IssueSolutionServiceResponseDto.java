package dev.mikita.issueservice.dto.response.service;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * The type Issue solution response dto.
 */
@Data
public class IssueSolutionServiceResponseDto {
    Long id;

    String photo;

    String description;

    LocalDateTime creationDate;

    String employeeUid;

    String departmentUid;

    Long issueId;
}
