package dev.mikita.issueservice.controller;

import dev.mikita.issueservice.annotation.FirebaseAuthorization;
import dev.mikita.issueservice.dto.response.common.CategoryResponseDto;
import dev.mikita.issueservice.entity.Category;
import dev.mikita.issueservice.service.CategoryService;
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
    @GetMapping(produces = "application/json")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
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
    @GetMapping(path = "/{id}", produces = "application/json")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<CategoryResponseDto> getCategory(@PathVariable("id") Long id) {
        CategoryResponseDto response = new ModelMapper().map(
                categoryService.getCategoryById(id), CategoryResponseDto.class);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
