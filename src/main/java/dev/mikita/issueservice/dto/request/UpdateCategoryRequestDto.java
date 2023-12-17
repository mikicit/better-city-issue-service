package dev.mikita.issueservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

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
    public UpdateCategoryRequestDto(@NotBlank(message = "Category cannot be empty.") String name) {
        this.name = name;
    }
}
