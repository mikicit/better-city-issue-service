package dev.mikita.issueservice.entity;

import lombok.Data;

/**
 * The type User.
 */
@Data
public class User {
    private String uid;
    private String email;
    private String password;
    private UserRole role;
    private UserStatus status;
    private String photo;
}