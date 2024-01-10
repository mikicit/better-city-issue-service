package dev.mikita.issueservice.dto.request;

import lombok.Data;
import lombok.NonNull;

import java.beans.ConstructorProperties;

/**
 * The type Create category request dto.
 */
@Data
public class CreateCategoryRequestDto {
    /**
     * The Name.
     */
    private String name;

    @ConstructorProperties({"name"})
    public CreateCategoryRequestDto(@NonNull String name) {
        this.name = name;
    }
}
