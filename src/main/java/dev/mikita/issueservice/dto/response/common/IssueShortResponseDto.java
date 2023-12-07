package dev.mikita.issueservice.dto.response.common;

import dev.mikita.issueservice.entity.IssueStatus;
import lombok.Data;
import org.locationtech.jts.geom.Point;
@Data
public class IssueShortResponseDto {
    Long id;
    Point coordinates;
    String title;
    Long categoryId;
    IssueStatus status;
}
