package dev.mikita.issueservice.dto.response.common;

import lombok.Data;
import lombok.NonNull;

import java.beans.ConstructorProperties;

@Data
public class CountResponseDto {
    private Long count;

    @ConstructorProperties({"count"})
    public CountResponseDto(@NonNull Long count) {
        this.count = count;
    }
}
