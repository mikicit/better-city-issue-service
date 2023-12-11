package dev.mikita.issueservice.dto.response.employee;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IssueReservationEmployeeResponseDto {
    Long id;
    LocalDateTime creationDate;
    String serviceUid;
    String departmentUid;
    Long issueId;
}
