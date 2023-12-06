package dev.mikita.issueservice.controller.admin;

import dev.mikita.issueservice.annotation.FirebaseAuthorization;
import dev.mikita.issueservice.entity.IssueStatus;
import dev.mikita.issueservice.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/admin/issues")
public class AdminIssueController {
    private final IssueService issueService;

    @Autowired
    public AdminIssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    // TODO: add comment
    @PutMapping(path = "/{id}/status")
    @FirebaseAuthorization(roles = {"ROLE_MODERATOR", "ROLE_ADMIN"})
    public void updateIssueStatus(@PathVariable("id") Long id, @RequestParam IssueStatus status) {
        issueService.updateIssueStatus(id, status);
    }
}
