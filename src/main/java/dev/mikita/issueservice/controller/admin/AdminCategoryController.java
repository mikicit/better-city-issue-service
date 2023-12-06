package dev.mikita.issueservice.controller.admin;

import dev.mikita.issueservice.annotation.FirebaseAuthorization;
import dev.mikita.issueservice.dto.request.CreateCategoryRequestDto;
import dev.mikita.issueservice.dto.request.UpdateCategoryRequestDto;
import dev.mikita.issueservice.entity.Category;
import dev.mikita.issueservice.service.CategoryService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/admin/categories")
public class AdminCategoryController {
    private final CategoryService categoryService;

    @Autowired
    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Create category.
     *
     * @param createCategoryRequestDto the create category request dto
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @FirebaseAuthorization(roles = {"ROLE_MODERATOR", "ROLE_ADMIN"})
    public void createCategory(@Valid @RequestBody CreateCategoryRequestDto createCategoryRequestDto) {
        ModelMapper modelMapper = new ModelMapper();
        Category category = modelMapper.map(createCategoryRequestDto, Category.class);
        categoryService.createCategory(category);
    }

    /**
     * Update category.
     *
     * @param id              the id
     * @param categoryRequest the category request
     */
    @ResponseStatus(HttpStatus.OK)
    @PutMapping(path = "/{id}")
    @FirebaseAuthorization(roles = {"ROLE_MODERATOR", "ROLE_ADMIN"})
    public void updateCategory(@PathVariable("id") Long id,
                               @Valid @RequestBody UpdateCategoryRequestDto categoryRequest) {
        categoryService.updateCategory(id, categoryRequest.getName());
    }

    /**
     * Delete category.
     *
     * @param id the id
     */
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(path = "/{id}")
    @FirebaseAuthorization(roles = {"ROLE_MODERATOR", "ROLE_ADMIN"})
    public void deleteCategory(@PathVariable("id") Long id) {
        categoryService.deleteCategory(id);
    }
}
