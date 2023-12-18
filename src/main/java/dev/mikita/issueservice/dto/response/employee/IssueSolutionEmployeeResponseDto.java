package dev.mikita.issueservice.dto.response.employee;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IssueSolutionEmployeeResponseDto {
    @NotBlank(message = "Issue solution employee id cannot be empty")
    Long id;

    @NotBlank(message = "Issue solution employee photo cannot be empty")
    String photo;

    @NotBlank(message = "Issue solution employee description cannot be empty")
    String description;

    @NotBlank(message = "Issue solution employee creation date cannot be empty")
    LocalDateTime creationDate;

    @NotBlank(message = "Issue solution employee service id cannot be empty")
    String serviceUid;

    @NotBlank(message = "Issue solution employee department id cannot be empty")
    String departmentUid;

    @NotBlank(message = "Issue solution employee issue id cannot be empty")
    Long issueId;
}
