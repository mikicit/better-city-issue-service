package dev.mikita.issueservice.dto.request;

import lombok.Data;
import lombok.NonNull;

import java.beans.ConstructorProperties;

@Data
public class DeclineIssueRequestDto {
    private String comment;

    @ConstructorProperties({"comment"})
    public DeclineIssueRequestDto(@NonNull String comment) {
        this.comment = comment;
    }
}
