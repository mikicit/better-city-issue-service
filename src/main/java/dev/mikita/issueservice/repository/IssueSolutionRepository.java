package dev.mikita.issueservice.repository;

import dev.mikita.issueservice.entity.IssueReservation;
import dev.mikita.issueservice.entity.IssueSolution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.List;

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

    List<IssueSolution> getIssueSolutionByServiceId(String serviceId);
    List<IssueSolution> getIssueSolutionByEmployeeId(String employeeId);
    List<IssueSolution> getIssueSolutionByDepartmentId(String departmentId);

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
