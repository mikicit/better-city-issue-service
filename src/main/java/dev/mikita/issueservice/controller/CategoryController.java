package dev.mikita.issueservice.controller;

import dev.mikita.issueservice.annotation.FirebaseAuthorization;
import dev.mikita.issueservice.dto.request.CreateCategoryRequestDto;
import dev.mikita.issueservice.dto.request.UpdateCategoryRequestDto;
import dev.mikita.issueservice.dto.response.category.CategoryResponseDto;
import dev.mikita.issueservice.entity.Category;
import dev.mikita.issueservice.service.CategoryService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * The type Category controller.
 */
@RestController
@RequestMapping(path = "/api/v1/categories")
public class CategoryController {
    private final CategoryService categoryService;

    /**
     * Instantiates a new Category controller.
     *
     * @param service the service
     */
    @Autowired
    public CategoryController(CategoryService service) {
        this.categoryService = service;
    }

    /**
     * Gets categories.
     *
     * @return the categories
     */
    @GetMapping
    @FirebaseAuthorization
    public ResponseEntity<List<CategoryResponseDto>> getCategories() {
        List<Category> categories = categoryService.getCategories();

        ModelMapper modelMapper = new ModelMapper();
        List<CategoryResponseDto> response = categories.stream()
                .map(category -> modelMapper.map(category, CategoryResponseDto.class))
                .toList();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Gets category by id.
     *
     * @param id the id
     * @return the category by id
     */
    @GetMapping(path = "/{id}")
    @FirebaseAuthorization
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable("id") Long id) {
        CategoryResponseDto response = new ModelMapper().map(
                categoryService.getCategoryById(id), CategoryResponseDto.class);
        return new ResponseEntity<>(response, HttpStatus.OK);
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
