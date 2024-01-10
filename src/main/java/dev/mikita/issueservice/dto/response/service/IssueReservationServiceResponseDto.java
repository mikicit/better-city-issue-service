package dev.mikita.issueservice.dto.response.service;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IssueReservationServiceResponseDto {
    Long id;

    LocalDateTime creationDate;

    String employeeUid;

    String departmentUid;

    Long issueId;
}
