package dev.mikita.issueservice.repository;

import dev.mikita.issueservice.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.List;

/**
 * The interface Like repository.
 */
@RepositoryRestResource(exported = false)
public interface LikeRepository extends JpaRepository<Like, Long> {
    /**
     * Find all by issue id list.
     *
     * @param issueId the issue id
     * @return the list
     */
    List<Like> findAllByIssueId(Long issueId);

    /**
     * Find by issue id and resident id like.
     *
     * @param issueId    the issue id
     * @param residentId the resident id
     * @return the like
     */
    @Query("SELECT l FROM Like l WHERE l.issue.id = :issueId AND l.resident = :residentId")
    Like findByIssueIdAndResidentId(@Param("issueId") Long issueId, @Param("residentId") String residentId);

    /**
     * Gets like status.
     *
     * @param issueId    the issue id
     * @param residentId the resident id
     * @return the like status
     */
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END " +
            "FROM Like l WHERE l.issue.id = :issueId AND l.resident = :residentId")
    Boolean getLikeStatus(@Param("issueId") Long issueId, @Param("residentId") String residentId);
}
