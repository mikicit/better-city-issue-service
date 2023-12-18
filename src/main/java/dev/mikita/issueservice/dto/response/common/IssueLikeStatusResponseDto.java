package dev.mikita.issueservice.dto.response.common;

import lombok.Data;
import lombok.NonNull;

import java.beans.ConstructorProperties;

@Data
public class IssueLikeStatusResponseDto {
    private Boolean likeStatus;

    @ConstructorProperties({"likeStatus"})
    public IssueLikeStatusResponseDto(@NonNull Boolean likeStatus) {
        this.likeStatus = likeStatus;
    }
}
