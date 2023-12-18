package dev.mikita.issueservice.dto.response.common;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryResponseDto {
    @NotBlank(message = "Category id cannot be empty")
    Long id;
    @NotBlank(message = "Category name cannot be empty")
    String name;
}
