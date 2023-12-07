package dev.mikita.issueservice.entity;

/**
 * The enum User role.
 */
public enum UserRole {
    /**
     * Admin user role.
     */
    ADMIN("ADMIN"),
    /**
     * Moderator user role.
     */
    MODERATOR("MODERATOR"),
    /**
     * Resident user role.
     */
    RESIDENT("RESIDENT"),
    /**
     * Service user role.
     */
    SERVICE("SERVICE"),
    EMPLOYEE("EMPLOYEE"),
    ANALYST("ANALYST");

    private final String name;

    UserRole(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
