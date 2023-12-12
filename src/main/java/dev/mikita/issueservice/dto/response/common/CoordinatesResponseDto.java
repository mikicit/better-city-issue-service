package dev.mikita.issueservice.dto.response.common;

import lombok.Data;
import org.locationtech.jts.geom.Point;

@Data
public class CoordinatesResponseDto {
    Point coordinates;
}
