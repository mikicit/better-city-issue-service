package dev.mikita.issueservice.repository;

import dev.mikita.issueservice.entity.Issue;
import dev.mikita.issueservice.entity.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The interface Issue repository.
 */
@RepositoryRestResource(exported = false)
public interface IssueRepository extends JpaRepository<Issue, Long> {
    @Query("SELECT i FROM Issue i WHERE " +
            "(:status IS NULL OR i.status = :status) AND " +
            "(:residentId IS NULL OR i.authorId = :residentId) AND " +
            "(:includeCategories = 0 OR i.category.id IN (:categories)) AND " +
            "(cast(:from as timestamp) IS NULL OR i.creationDate >= :from) AND " +
            "(cast(:to as timestamp) IS NULL OR i.creationDate <= :to)")
    Page<Issue> findAll(@Param("status") IssueStatus status,
                        @Param("residentId") String residentId,
                        @Param("categories") List<Long> categories,
                        @Param("from") LocalDateTime from,
                        @Param("to") LocalDateTime to,
                        @Param("includeCategories") int includeCategories,
                        Pageable pageable);

    @Query("SELECT i FROM Issue i WHERE " +
            "(:status IS NULL OR i.status = :status) AND " +
            "(:includeCategories = 0 OR i.category.id IN (:categories)) AND " +
            "(:coordinates IS NULL OR :distanceM IS NULL OR ST_DWithin(i.coordinates, ST_GeographyFromText(:coordinates), :distanceM) = true) AND" +
            "(cast(:from as timestamp) IS NULL OR i.creationDate >= :from) AND " +
            "(cast(:to as timestamp) IS NULL OR i.creationDate <= :to)")
    List<Issue> findAllByRadius(@Param("status") IssueStatus status,
                              @Param("categories") List<Long> categories,
                              @Param("coordinates") String coordinates,
                              @Param("distanceM") Double distanceM,
                              @Param("from") LocalDateTime from,
                              @Param("to") LocalDateTime to,
                              @Param("includeCategories") int includeCategories);

    @Query("SELECT COUNT(l) FROM Like l WHERE l.issue.id = :issueId")
    Long findLikeCountById(@Param("issueId") Long issueId);

    @Query("SELECT COUNT(i) FROM Issue i WHERE " +
            "(:status IS NULL OR i.status = :status) AND " +
            "(:residentId IS NULL OR i.authorId = :residentId) AND " +
            "(:includeCategories = 0 OR i.category.id IN (:categories)) AND " +
            "(:from IS NULL OR i.creationDate >= :from) AND " +
            "(:to IS NULL OR i.creationDate <= :to)")
    Long getIssuesCount(@Param("status") IssueStatus status,
                        @Param("residentId") String residentId,
                        @Param("categories") List<Long> categories,
                        @Param("from") LocalDate from,
                        @Param("to") LocalDate to,
                        @Param("includeCategories") int includeCategories);
}
