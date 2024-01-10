package dev.mikita.issueservice.dto.response.employee;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IssueSolutionEmployeeResponseDto {
    Long id;

    String photo;

    String description;

    LocalDateTime creationDate;

    String serviceUid;

    String departmentUid;

    Long issueId;
}
