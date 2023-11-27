package dev.mikita.issueservice.dto.response.likes;

import lombok.Data;

/**
 * The type Issue like status response dto.
 */
@Data
public class IssueLikeStatusResponseDto {
    /**
     * The Like status.
     */
    Boolean like_status;
}
