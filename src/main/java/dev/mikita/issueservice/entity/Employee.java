package dev.mikita.issueservice.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Employee extends User {
    private String firstName;
    private String lastName;
    private String serviceUid;
    private String departmentUid;
}
