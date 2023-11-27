package dev.mikita.issueservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * The type Create category request dto.
 */
@Data
public class CreateCategoryRequestDto {
    /**
     * The Name.
     */
    @NotBlank(message = "Category cannot be empty.")
    String name;
}
