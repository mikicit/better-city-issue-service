package dev.mikita.issueservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * The type Update category request dto.
 */
@Data
public class UpdateCategoryRequestDto {
    /**
     * The Name.
     */
    @NotBlank(message = "Category cannot be empty.")
    String name;
}
