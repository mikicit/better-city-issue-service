package dev.mikita.issueservice.dto.response.common;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.beans.ConstructorProperties;

@Data
public class AvgTimeResponseDto {
    private Double avgTime;

    @ConstructorProperties({"avgTime"})
    public AvgTimeResponseDto(@NotBlank(message = "avgTime cannot be empty") Double avgTime) {
        this.avgTime = avgTime;
    }
}
