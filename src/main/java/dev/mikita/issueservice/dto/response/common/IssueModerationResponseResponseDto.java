package dev.mikita.issueservice.dto.response.common;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class IssueModerationResponseResponseDto {
    LocalDateTime creationDate;
    String comment;
}
