package dev.mikita.issueservice.dto.request;

import lombok.Data;
import lombok.NonNull;

import java.beans.ConstructorProperties;

/**
 * The type Update category request dto.
 */
@Data
public class UpdateCategoryRequestDto {
    /**
     * The Name.
     */
    private String name;

    /**
     * Instantiates a new Update category request dto.
     *
     * @param name the name
     */
    @ConstructorProperties({"name"})
    public UpdateCategoryRequestDto(@NonNull String name) {
        this.name = name;
    }
}
