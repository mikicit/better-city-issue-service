package dev.mikita.issueservice.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class Department {
    private String uid;
    private String name;
    private String description;
    private String address;
    private String phoneNumber;
    private LocalDateTime creationDate;
    private List<Long> categories;
    private String serviceUid;
}
