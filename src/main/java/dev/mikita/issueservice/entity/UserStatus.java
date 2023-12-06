package dev.mikita.issueservice.entity;

/**
 * The enum User status.
 */
public enum UserStatus {
    /**
     * Active user status.
     */
    ACTIVE("ACTIVE"),
    /**
     * Inactive user status.
     */
    INACTIVE("BANNED"),
    /**
     * Deleted user status.
     */
    DELETED("DELETED");

    private final String name;

    UserStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
