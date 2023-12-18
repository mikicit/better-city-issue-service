package dev.mikita.issueservice.dto;

import dev.mikita.issueservice.entity.IssueStatus;
import lombok.Data;
import lombok.NonNull;

import java.beans.ConstructorProperties;

/**
 * The type Change issue status notification dto.
 */
@Data
public class ChangeIssueStatusNotificationDto {
    private Long issueId;
    private String userId;
    private IssueStatus status;

    /**
     * Instantiates a new Change issue status notification dto.
     */
    @ConstructorProperties({"issueId", "userId", "status"})
    public ChangeIssueStatusNotificationDto(
            @NonNull Long issueId,
            @NonNull String userId,
            @NonNull IssueStatus status) {
        this.issueId = issueId;
        this.userId = userId;
        this.status = status;
    }
}
