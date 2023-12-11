package dev.mikita.issueservice.repository;

import dev.mikita.issueservice.entity.IssueReservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.time.LocalDateTime;
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

    @Query("SELECT ir FROM IssueReservation ir WHERE " +
            "(:serviceUid IS NULL OR ir.serviceUid = :serviceUid) AND " +
            "(:employeeUid IS NULL OR ir.employeeUid = :employeeUid) AND " +
            "(:departmentUid IS NULL OR ir.departmentUid = :departmentUid) AND " +
            "(:includeCategories = 0 OR ir.issue.category.id IN (:categories)) AND " +
            "(cast(:from as timestamp) IS NULL OR ir.creationDate >= :from) AND " +
            "(cast(:to as timestamp) IS NULL OR ir.creationDate <= :to)")
    Page<IssueReservation> getIssuesReservations(@Param("serviceUid") String serviceUid,
                                                 @Param("employeeUid") String employeeUid,
                                                 @Param("departmentUid") String departmentUid,
                                                 @Param("from") LocalDateTime from,
                                                 @Param("to") LocalDateTime to,
                                                 @Param("categories") List<Long> categories,
                                                 @Param("includeCategories") int includeCategories,
                                                 Pageable pageable);

    @Query("SELECT count(ir) FROM IssueReservation ir WHERE " +
            "(:serviceUid IS NULL OR ir.serviceUid = :serviceUid) AND " +
            "(:employeeUid IS NULL OR ir.employeeUid = :employeeUid) AND " +
            "(:departmentUid IS NULL OR ir.departmentUid = :departmentUid) AND " +
            "(:includeCategories = 0 OR ir.issue.category.id IN (:categories)) AND " +
            "(cast(:from as timestamp) IS NULL OR ir.creationDate >= :from) AND " +
            "(cast(:to as timestamp) IS NULL OR ir.creationDate <= :to)")
    Long getIssuesReservationsCount(@Param("serviceUid") String serviceUid,
                                    @Param("employeeUid") String employeeUid,
                                    @Param("departmentUid") String departmentUid,
                                    @Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to,
                                    @Param("categories") List<Long> categories,
                                    @Param("includeCategories") int includeCategories);
}
