package dev.mikita.issueservice.repository;

import dev.mikita.issueservice.entity.Issue;
import dev.mikita.issueservice.entity.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * The interface Issue repository.
 */
@RepositoryRestResource(exported = false)
public interface IssueRepository extends JpaRepository<Issue, Long> {
    /**
     * Find like count by id long.
     *
     * @param issueId the issue id
     * @return the long
     */
    @Query("SELECT COUNT(l) FROM Like l WHERE l.issue.id = :issueId")
    Long findLikeCountById(@Param("issueId") Long issueId);

    /**
     * Find all list.
     *
     * @param status      the status
     * @param residentId  the resident id
     * @param coordinates the coordinates
     * @param distanceM   the distance m
     * @return the list
     */
    @Query("SELECT i FROM Issue i WHERE " +
            "(:status IS NULL OR i.status = :status) AND " +
            "(:residentId IS NULL OR i.authorId = :residentId) AND " +
            "(:coordinates IS NULL OR :distanceM IS NULL OR ST_DWithin(i.coordinates, ST_GeographyFromText(:coordinates), :distanceM) = true)")
    List<Issue> findAll(@Param("status") IssueStatus status,
                        @Param("residentId") String residentId,
                        @Param("coordinates") String coordinates,
                        @Param("distanceM") Double distanceM);

    /**
     * Count issues by author id long.
     *
     * @param authorId the author id
     * @return the long
     */
    @Query("SELECT COUNT(i) FROM Issue i WHERE i.authorId = :authorId OR :authorId IS NULL")
    Long countIssuesByAuthorId(@Param("authorId") String authorId);
}
