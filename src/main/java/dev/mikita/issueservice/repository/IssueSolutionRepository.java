package dev.mikita.issueservice.repository;

import dev.mikita.issueservice.entity.IssueSolution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.time.LocalDateTime;
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

    @Query("SELECT isol FROM IssueSolution isol WHERE " +
            "(:serviceUid IS NULL OR isol.serviceUid = :serviceUid) AND " +
            "(:employeeUid IS NULL OR isol.employeeUid = :employeeUid) AND " +
            "(:departmentUid IS NULL OR isol.departmentUid = :departmentUid) AND " +
            "(:includeCategories = 0 OR isol.issue.category.id IN (:categories)) AND " +
            "(cast(:from as timestamp) IS NULL OR isol.creationDate >= :from) AND " +
            "(cast(:to as timestamp) IS NULL OR isol.creationDate <= :to)")
    Page<IssueSolution> getIssuesSolutions(@Param("serviceUid") String serviceUid,
                                           @Param("employeeUid") String employeeUid,
                                           @Param("departmentUid") String departmentUid,
                                           @Param("categories") List<Long> categories,
                                           @Param("from") LocalDateTime from,
                                           @Param("to") LocalDateTime to,
                                           @Param("includeCategories") int includeCategories,
                                           Pageable pageable);

    @Query("SELECT count(isol) FROM IssueSolution isol WHERE " +
            "(:serviceUid IS NULL OR isol.serviceUid = :serviceUid) AND " +
            "(:employeeUid IS NULL OR isol.employeeUid = :employeeUid) AND " +
            "(:departmentUid IS NULL OR isol.departmentUid = :departmentUid) AND " +
            "(:includeCategories = 0 OR isol.issue.category.id IN (:categories)) AND " +
            "(cast(:from as timestamp) IS NULL OR isol.creationDate >= :from) AND " +
            "(cast(:to as timestamp) IS NULL OR isol.creationDate <= :to)")
    Long getIssuesSolutionsCount(@Param("serviceUid") String serviceUid,
                                 @Param("employeeUid") String employeeUid,
                                 @Param("departmentUid") String departmentUid,
                                 @Param("categories") List<Long> categories,
                                 @Param("from") LocalDateTime from,
                                 @Param("to") LocalDateTime to,
                                 @Param("includeCategories") int includeCategories);

    @Query("SELECT AVG(CAST(DATE_PART('EPOCH', isol.creationDate) - DATE_PART('EPOCH', isol.issue.issueReservation.creationDate) as biginteger)) " +
            "FROM IssueSolution isol " +
            "WHERE " +
            "(:serviceUid IS NULL OR isol.serviceUid = :serviceUid) AND " +
            "(:employeeUid IS NULL OR isol.employeeUid = :employeeUid) AND " +
            "(:departmentUid IS NULL OR isol.departmentUid = :departmentUid) AND " +
            "(:includeCategories = 0 OR isol.issue.category.id IN (:categories)) AND " +
            "(cast(:from as timestamp) IS NULL OR isol.creationDate >= :from) AND " +
            "(cast(:to as timestamp) IS NULL OR isol.creationDate <= :to)")
    Double getAverageSolutionsTime(@Param("serviceUid") String serviceUid,
                                   @Param("employeeUid") String employeeUid,
                                   @Param("departmentUid") String departmentUid,
                                   @Param("categories") List<Long> categories,
                                   @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to,
                                   @Param("includeCategories") int includeCategories);
}
