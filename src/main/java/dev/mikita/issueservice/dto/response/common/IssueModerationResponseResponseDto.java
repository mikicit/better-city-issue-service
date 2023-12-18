package dev.mikita.issueservice.dto.response.common;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IssueModerationResponseResponseDto {
    @NotBlank(message = "Issue moderation creation date cannot be empty")
    LocalDateTime creationDate;

    @NotBlank(message = "Issue moderation comment cannot be empty")
    String comment;
}
