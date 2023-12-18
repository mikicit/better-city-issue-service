package dev.mikita.issueservice.dto.response.common;

import lombok.Data;
import lombok.NonNull;

import java.beans.ConstructorProperties;

@Data
public class IssueLikesResponseDto {
    private Long count;

    @ConstructorProperties({"count"})
    public IssueLikesResponseDto(@NonNull Long count) {
        this.count = count;
    }
}
