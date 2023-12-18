package dev.mikita.issueservice.dto.response.common;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.locationtech.jts.geom.Point;

@Data
public class CoordinatesResponseDto {
    @NotBlank(message = "Coordinates cannot be empty")
    Point coordinates;
}
