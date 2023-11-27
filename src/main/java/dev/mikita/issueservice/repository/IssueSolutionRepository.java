package dev.mikita.issueservice.repository;

import dev.mikita.issueservice.entity.IssueSolution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * The interface Issue solution repository.
 */
@RepositoryRestResource(exported = false)
public interface IssueSolutionRepository extends JpaRepository<IssueSolution, Long> {
    /**
     * Gets issue solution by issue id.
     *
     * @param issueId the issue id
     * @return the issue solution by issue id
     */
    IssueSolution getIssueSolutionByIssueId(Long issueId);

    /**
     * Count by service id long.
     *
     * @param serviceId the service id
     * @return the long
     */
    Long countByServiceId(String serviceId);
}
