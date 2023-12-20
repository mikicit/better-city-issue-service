package dev.mikita.issueservice.dto;

import dev.mikita.issueservice.entity.IssueStatus;
import lombok.Data;

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
    public ChangeIssueStatusNotificationDto() {}
}
