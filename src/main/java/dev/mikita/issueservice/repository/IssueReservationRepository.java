package dev.mikita.issueservice.repository;

import dev.mikita.issueservice.entity.IssueReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * The interface Issue reservation repository.
 */
@RepositoryRestResource(exported = false)
public interface IssueReservationRepository extends JpaRepository<IssueReservation, Long> {
    /**
     * Gets issue reservation by issue id.
     *
     * @param issueId the issue id
     * @return the issue reservation by issue id
     */
    IssueReservation getIssueReservationByIssueId(Long issueId);

    /**
     * Count by service id long.
     *
     * @param serviceId the service id
     * @return the long
     */
    Long countByServiceId(String serviceId);
}
