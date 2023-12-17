package dev.mikita.issueservice.dto.response.common;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.beans.ConstructorProperties;

@Data
public class CountResponseDto {
    private Long count;

    @ConstructorProperties({"count"})
    public CountResponseDto(@NotBlank(message = "count cannot be null") Long count) {
        this.count = count;
    }
}
