package dev.mikita.issueservice.repository;

import dev.mikita.issueservice.entity.IssueReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.List;

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

    List<IssueReservation> getIssueReservationByServiceId(String serviceId);
    List<IssueReservation> getIssueReservationByEmployeeId(String employeeId);
    List<IssueReservation> getIssueReservationByDepartmentId(String departmentId);

    /**
     * Count by service id long.
     *
     * @param serviceId the service id
     * @return the long
     */
    Long countByServiceId(String serviceId);
    Long countByEmployeeId(String employeeId);
    Long countByDepartmentId(String departmentId);
}
