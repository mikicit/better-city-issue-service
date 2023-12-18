package dev.mikita.issueservice.dto.response.common;

import dev.mikita.issueservice.entity.IssueStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.locationtech.jts.geom.Point;
@Data
public class IssueShortResponseDto {
    @NotBlank(message = "Issue short id cannot be empty")
    Long id;

    @NotBlank(message = "Issue short coordinates cannot be empty")
    Point coordinates;

    @NotBlank(message = "Issue short title cannot be empty")
    String title;

    @NotBlank(message = "Issue short category id cannot be empty")
    Long categoryId;

    @NotBlank(message = "Issue short status cannot be empty")
    IssueStatus status;
}