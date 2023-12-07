package dev.mikita.issueservice.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * The type User.
 */
@Data
public class User {
    private String uid;
    private String email;
    private String phoneNumber;
    private String password;
    private UserRole role;
    private UserStatus status;
    private String photo;
    private LocalDateTime creationDate;
}