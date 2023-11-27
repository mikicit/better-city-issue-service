package dev.mikita.issueservice.entity;

/**
 * The enum Issue status.
 */
public enum IssueStatus {
    /**
     * Moderation issue status.
     */
    MODERATION("MODERATION"),
    /**
     * Published issue status.
     */
    PUBLISHED("PUBLISHED"),
    /**
     * Deleted issue status.
     */
    DELETED("DELETED"),
    /**
     * Solved issue status.
     */
    SOLVED("SOLVED"),
    /**
     * Solving issue status.
     */
    SOLVING("SOLVING");

    private final String issueStatus;

    IssueStatus(String issueStatus) {
        this.issueStatus = issueStatus;
    }

    @Override
    public String toString() {
        return issueStatus;
    }
}
