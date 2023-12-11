package dev.mikita.issueservice.repository;

import dev.mikita.issueservice.entity.Issue;
import dev.mikita.issueservice.entity.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The interface Issue repository.
 */
@RepositoryRestResource(exported = false)
public interface IssueRepository extends JpaRepository<Issue, Long> {
    @Query("SELECT i FROM Issue i WHERE " +
            "(:includeStatuses = 0 OR i.status IN (:statuses)) AND " +
            "(:residentUid IS NULL OR i.authorUid = :residentUid) AND " +
            "(:includeCategories = 0 OR i.category.id IN (:categories)) AND " +
            "(cast(:from as timestamp) IS NULL OR i.creationDate >= :from) AND " +
            "(cast(:to as timestamp) IS NULL OR i.creationDate <= :to)")
    Page<Issue> findAll(@Param("statuses") List<IssueStatus> statuses,
                        @Param("residentUid") String residentUid,
                        @Param("categories") List<Long> categories,
                        @Param("from") LocalDateTime from,
                        @Param("to") LocalDateTime to,
                        @Param("includeCategories") int includeCategories,
                        @Param("includeStatuses") int includeStatuses,
                        Pageable pageable);

    @Query("SELECT i FROM Issue i WHERE " +
            "(:serviceUid IS NULL OR i.issueReservation.serviceUid = :serviceUid) AND " +
            "(:departmentUid IS NULL OR i.issueReservation.departmentUid = :departmentUid) AND " +
            "(:employeeUid IS NULL OR i.issueReservation.employeeUid = :employeeUid) AND " +
            "(:includeStatuses = 0 OR i.status IN (:statuses)) AND " +
            "(:residentUid IS NULL OR i.authorUid = :residentUid) AND " +
            "(:includeCategories = 0 OR i.category.id IN (:categories)) AND " +
            "(cast(:from as timestamp) IS NULL OR i.creationDate >= :from) AND " +
            "(cast(:to as timestamp) IS NULL OR i.creationDate <= :to)")
    Page<Issue> findAllByHolder(@Param("serviceUid") String serviceUid,
                                @Param("departmentUid") String departmentUid,
                                @Param("employeeUid") String employeeUid,
                                @Param("statuses") List<IssueStatus> statuses,
                                @Param("residentUid") String residentUid,
                                @Param("categories") List<Long> categories,
                                @Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to,
                                @Param("includeCategories") int includeCategories,
                                @Param("includeStatuses") int includeStatuses,
                                Pageable pageable);

    @Query("SELECT i FROM Issue i WHERE " +
            "(:includeStatuses = 0 OR i.status IN (:statuses)) AND " +
            "(:includeCategories = 0 OR i.category.id IN (:categories)) AND " +
            "(:coordinates IS NULL OR :distanceM IS NULL OR ST_DWithin(i.coordinates, ST_GeographyFromText(:coordinates), :distanceM) = true) AND" +
            "(cast(:from as timestamp) IS NULL OR i.creationDate >= :from) AND " +
            "(cast(:to as timestamp) IS NULL OR i.creationDate <= :to)")
    List<Issue> findAllByRadius(@Param("statuses") List<IssueStatus> statuses,
                                @Param("categories") List<Long> categories,
                                @Param("coordinates") String coordinates,
                                @Param("distanceM") Double distanceM,
                                @Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to,
                                @Param("includeCategories") int includeCategories,
                                @Param("includeStatuses") int includeStatuses);

    @Query("SELECT COUNT(i) FROM Issue i WHERE " +
            "(:residentUid IS NULL OR i.authorUid = :residentUid) AND " +
            "(:includeStatuses = 0 OR i.status IN (:statuses)) AND " +
            "(:includeCategories = 0 OR i.category.id IN (:categories)) AND " +
            "(cast(:from as timestamp) IS NULL OR i.creationDate >= :from) AND " +
            "(cast(:to as timestamp) IS NULL OR i.creationDate <= :to)")
    Long getIssuesCount(@Param("statuses") List<IssueStatus> statuses,
                        @Param("residentUid") String residentUid,
                        @Param("categories") List<Long> categories,
                        @Param("from") LocalDateTime from,
                        @Param("to") LocalDateTime to,
                        @Param("includeCategories") int includeCategories,
                        @Param("includeStatuses") int includeStatuses);

    @Query("SELECT COUNT(l) FROM Like l WHERE l.issue.id = :issueId")
    Long findLikeCountById(@Param("issueId") Long issueId);
}
