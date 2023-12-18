package dev.mikita.issueservice.dto.response.common;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.beans.ConstructorProperties;

@Data
public class IssueLikeStatusResponseDto {
    private Boolean likeStatus;

    @ConstructorProperties({"likeStatus"})
    public IssueLikeStatusResponseDto(@NotBlank(message = "like status cannot be null") Boolean likeStatus) {
        this.likeStatus = likeStatus;
    }
}
