package dev.mikita.issueservice.dto.response.common;

import lombok.Data;
import lombok.NonNull;

import java.beans.ConstructorProperties;

@Data
public class AvgTimeResponseDto {
    private Double avgTime;

    @ConstructorProperties({"avgTime"})
    public AvgTimeResponseDto(@NonNull Double avgTime) {
        this.avgTime = avgTime;
    }
}
