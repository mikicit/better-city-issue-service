package dev.mikita.issueservice.dto.response.service;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * The type Issue solution response dto.
 */
@Data
public class IssueSolutionServiceResponseDto {
    @NotBlank(message = "Issue solution id cannot be empty")
    Long id;

    @NotBlank(message = "Issue solution photo cannot be empty")
    String photo;

    @NotBlank(message = "Issue solution description cannot be empty")
    String description;

    @NotBlank(message = "Issue solution creation date cannot be empty")
    LocalDateTime creationDate;

    @NotBlank(message = "Issue solution service id cannot be empty")
    String employeeUid;

    @NotBlank(message = "Issue solution department id cannot be empty")
    String departmentUid;

    @NotBlank(message = "Issue solution issue id cannot be empty")
    Long issueId;
}
