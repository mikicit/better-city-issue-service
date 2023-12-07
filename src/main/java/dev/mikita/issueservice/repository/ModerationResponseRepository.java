package dev.mikita.issueservice.repository;

import dev.mikita.issueservice.entity.ModerationResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModerationResponseRepository extends JpaRepository<ModerationResponse, Long> {
    ModerationResponse getModerationResponseByIssueId(Long issueId);
}
