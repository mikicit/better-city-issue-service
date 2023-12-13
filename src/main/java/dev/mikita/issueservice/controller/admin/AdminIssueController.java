package dev.mikita.issueservice.controller.admin;

import com.google.firebase.auth.FirebaseToken;
import dev.mikita.issueservice.annotation.FirebaseAuthorization;
import dev.mikita.issueservice.dto.request.DeclineIssueRequestDto;
import dev.mikita.issueservice.dto.response.common.IssueResponseDto;
import dev.mikita.issueservice.entity.Issue;
import dev.mikita.issueservice.entity.IssueStatus;
import dev.mikita.issueservice.service.IssueService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Getter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/v1/admin/issues")
public class AdminIssueController {
    private final IssueService issueService;

    @Getter
    public enum OrderBy {
        CREATION_DATE("creationDate"),
        STATUS("status"),
        TITLE("title"),
        CATEGORY("category"),
        LIKES("likes");

        private final String fieldName;

        OrderBy(String fieldName) {
            this.fieldName = fieldName;
        }
    }

    public enum Order {
        ASC,
        DESC
    }

    @Autowired
    public AdminIssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping(path = "", produces = "application/json")
    @FirebaseAuthorization(roles = {"MODERATOR", "ADMIN"})
    public ResponseEntity<Map<String, Object>> getIssues(
            @RequestParam(required = false) List<IssueStatus> statuses,
            @RequestParam(name = "author", required = false) String authorUid,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(name = "order_by", required = false) OrderBy orderBy,
            @RequestParam(required = false) Order order
    ) {
        // Default values
        if (statuses == null) statuses = List.of(IssueStatus.MODERATION);

        // Pagination and sorting
        if (orderBy == null) orderBy = OrderBy.CREATION_DATE;
        if (order == null) order = Order.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Direction.fromString(order.toString()), orderBy.getFieldName()));
        Page<Issue> pageIssues = issueService.getIssues(statuses, authorUid, categories, from, to, pageable);
        List<Issue> issues = pageIssues.getContent();

        // Collect result
        ModelMapper modelMapper = new ModelMapper();

        Map<String, Object> response = new HashMap<>();
        response.put("issues", issues.stream()
                .map(issue -> modelMapper.map(issue, IssueResponseDto.class))
                .collect(Collectors.toList()));
        response.put("currentPage", pageIssues.getNumber());
        response.put("totalItems", pageIssues.getTotalElements());
        response.put("totalPages", pageIssues.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(path = "/{id}", produces = "application/json")
    @FirebaseAuthorization(roles = {"MODERATOR", "ADMIN"})
    public ResponseEntity<IssueResponseDto> getIssue(@PathVariable Long id) {
        return ResponseEntity.ok(new ModelMapper()
                .map(issueService.findIssueById(id), IssueResponseDto.class));
    }

    @PutMapping(path = "/{id}/approve")
    @FirebaseAuthorization(roles = {"MODERATOR", "ADMIN"})
    public void approveIssue(@PathVariable("id") Long id) {
        issueService.approveIssue(id);
    }

    @PutMapping(path = "/{id}/decline", consumes = "application/json")
    @FirebaseAuthorization(roles = {"MODERATOR", "ADMIN"})
    public void declineIssue(@PathVariable("id") Long id, HttpServletRequest request,
                             @Valid @RequestBody DeclineIssueRequestDto declineIssueRequest) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        issueService.declineIssue(id, token.getUid(), declineIssueRequest.getComment());
    }
}
