package dev.mikita.issueservice.dto.response.employee;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IssueReservationEmployeeResponseDto {
    @NotBlank(message = "Issue reservation employee id cannot be empty")
    Long id;

    @NotBlank(message = "Issue reservation employee creation date cannot be empty")
    LocalDateTime creationDate;

    @NotBlank(message = "Issue reservation employee service id cannot be empty")
    String serviceUid;

    @NotBlank(message = "Issue reservation employee department id cannot be empty")
    String departmentUid;

    @NotBlank(message = "Issue reservation employee issue id cannot be empty")
    Long issueId;
}
