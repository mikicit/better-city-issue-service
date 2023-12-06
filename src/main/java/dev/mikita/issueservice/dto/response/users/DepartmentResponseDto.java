package dev.mikita.issueservice.dto.response.users;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DepartmentResponseDto {
    private String uid;
    private String name;
    private String description;
    private String address;
    private String phoneNumber;
    private LocalDateTime creationDate;
    private List<Long> categories;
    private String serviceUid;
}
