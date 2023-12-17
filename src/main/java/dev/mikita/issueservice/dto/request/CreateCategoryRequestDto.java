package dev.mikita.issueservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

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
    public CreateCategoryRequestDto(@NotBlank(message = "Category cannot be empty.") String name) {
        this.name = name;
    }
}
