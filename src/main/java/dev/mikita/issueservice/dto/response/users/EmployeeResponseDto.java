package dev.mikita.issueservice.dto.response.users;

import lombok.Data;

@Data
public class EmployeeResponseDto {
    String uid;
    String email;
    String firstName;
    String lastName;
    String serviceUid;
    String departmentUid;
}
