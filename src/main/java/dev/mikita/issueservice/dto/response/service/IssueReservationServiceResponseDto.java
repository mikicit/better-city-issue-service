package dev.mikita.issueservice.dto.response.service;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IssueReservationServiceResponseDto {
    @NotBlank(message = "Issue reservation service id cannot be empty")
    Long id;

    @NotBlank(message = "Issue reservation service creation date cannot be empty")
    LocalDateTime creationDate;

    @NotBlank(message = "Issue reservation service employee id cannot be empty")
    String employeeUid;

    @NotBlank(message = "Issue reservation service department id cannot be empty")
    String departmentUid;

    @NotBlank(message = "Issue reservation service issue id cannot be empty")
    Long issueId;
}
