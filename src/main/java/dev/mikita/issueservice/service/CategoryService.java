package dev.mikita.issueservice.service;

import dev.mikita.issueservice.entity.Category;
import dev.mikita.issueservice.exception.NotFoundException;
import dev.mikita.issueservice.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * The type Category service.
 */
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    /**
     * Instantiates a new Category service.
     *
     * @param categoryRepository the category repository
     */
    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Gets categories.
     *
     * @return the categories
     */
    @Transactional(readOnly = true)
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Gets category by id.
     *
     * @param categoryId the category id
     * @return the category by id
     */
    @Transactional(readOnly = true)
    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(
                () -> new NotFoundException("Category does not found."));
    }

    /**
     * Create category.
     *
     * @param category the category
     */
    @Transactional
    public void createCategory(Category category) {
        categoryRepository.save(category);
    }

    /**
     * Update category.
     *
     * @param categoryId the category id
     * @param name       the name
     */
    @Transactional
    public void updateCategory(Long categoryId, String name) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(
                () -> new NotFoundException("Category with id " + categoryId + " does not exists."));

        category.setName(name);
        categoryRepository.save(category);
    }

    /**
     * Delete category.
     *
     * @param categoryId the category id
     */
    @Transactional
    public void deleteCategory(Long categoryId) {
        boolean exists = categoryRepository.existsById(categoryId);
        if (!exists) {
            throw new NotFoundException("Category with id " + categoryId + " does not exists.");
        }
        categoryRepository.deleteById(categoryId);
    }
}
