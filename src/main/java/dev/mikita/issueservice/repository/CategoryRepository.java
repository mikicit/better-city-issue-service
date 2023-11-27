package dev.mikita.issueservice.repository;

import dev.mikita.issueservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * The interface Category repository.
 */
@RepositoryRestResource(exported = false)
public interface CategoryRepository extends JpaRepository<Category, Long> {
    /**
     * Find by name category.
     *
     * @param name the name
     * @return the category
     */
    Category findByName(String name);
}
