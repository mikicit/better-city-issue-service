package dev.mikita.issueservice.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import dev.mikita.issueservice.annotation.FirebaseAuthorization;
import dev.mikita.issueservice.dto.response.common.*;
import dev.mikita.issueservice.dto.response.common.IssueLikeStatusResponseDto;
import dev.mikita.issueservice.dto.response.common.IssueLikesResponseDto;
import dev.mikita.issueservice.dto.response.common.IssueModerationResponseResponseDto;
import dev.mikita.issueservice.entity.*;
import dev.mikita.issueservice.service.IssueReservationService;
import dev.mikita.issueservice.service.IssueService;
import dev.mikita.issueservice.service.IssueSolutionService;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * The type Issue controller.
 */
@RestController
@RequestMapping(path = "/api/v1/issues")
public class IssueController {
    private final IssueService issueService;
    private final IssueReservationService issueReservationService;
    private final IssueSolutionService issueSolutionService;

    /**
     * Instantiates a new Issue controller.
     *
     * @param issueService            the issue service
     * @param issueReservationService the issue reservation service
     * @param issueSolutionService    the issue solution service
     */
    @Autowired
    public IssueController( IssueService issueService,
                            IssueReservationService issueReservationService,
                            IssueSolutionService issueSolutionService) {
        this.issueService = issueService;
        this.issueReservationService = issueReservationService;
        this.issueSolutionService = issueSolutionService;
    }

    // TODO: 07.12.2023 Add filters permissions check
    // TODO: 07.12.2023 Add sorting by likes count
    @GetMapping(path = "", produces = "application/json")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<Map<String, Object>> getIssues(@RequestParam(required = false) IssueStatus status,
                                                         @RequestParam(required = false) String authorId,
                                                         @RequestParam(required = false) List<Long> categories,
                                                         @RequestParam(required = false)
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                         @RequestParam(required = false)
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size,
                                                         @RequestParam(required = false) OrderBy orderBy,
                                                         @RequestParam(required = false) Order order
    ) {
        // Filters
        Map<String, Object> filters = new HashMap<>();
        if (status != null) filters.put("status", status);
        if (authorId != null) filters.put("authorId", authorId);
        if (categories != null) filters.put("categories", categories);
        if (from != null) filters.put("from", from);
        if (to != null) filters.put("to", to);

        // Pagination and sorting
        if (orderBy == null) orderBy = OrderBy.CREATION_DATE;
        if (order == null) order = Order.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Direction.fromString(order.toString()), orderBy.getFieldName()));
        Page<Issue> pageIssues = issueService.getIssues(pageable, filters);
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

    @GetMapping(path = "/radius", produces = "application/json")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<List<IssueShortResponseDto>> getIssuesInRadius(
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Double distanceM,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        // Filters
        Map<String, Object> filters = new HashMap<>();
        if (status != null) filters.put("status", status);
        if (categories != null) filters.put("categories", categories);
        if (from != null) filters.put("from", from);
        if (to != null) filters.put("to", to);
        if (latitude != null && longitude != null && distanceM != null) {
            GeometryFactory geometryFactory = new GeometryFactory();
            Coordinate coordinate = new Coordinate(longitude, latitude);
            Point coordinates = geometryFactory.createPoint(coordinate);
            filters.put("coordinates", coordinates);
            filters.put("distanceM", distanceM);
        }

        List<Issue> issues = issueService.getIssuesInRadius(filters);

        return ResponseEntity.ok(new ModelMapper().map(
                issues, new ParameterizedTypeReference<List<IssueShortResponseDto>>() {}.getType()));
    }

    /**
     * Gets issue by id.
     *
     * @param id the id
     * @return the issue by id
     */
    @GetMapping(path = "/{id}", produces = "application/json")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<IssueResponseDto> getIssue(@PathVariable Long id, HttpServletRequest request)
            throws AuthException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        Issue issue = issueService.findIssueById(id);

        if ((issue.getStatus() == IssueStatus.DELETED || issue.getStatus() == IssueStatus.MODERATION)
            && !token.getUid().equals(issue.getAuthorId())) {
            throw new AuthException("Unauthorized");
        }

        return new ResponseEntity<>(new ModelMapper().map(issue, IssueResponseDto.class), HttpStatus.OK);
    }

    /**
     * Create issue.
     *
     * @param data    the data
     * @param request the request
     * @throws IOException the io exception
     */

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @FirebaseAuthorization(roles = {"RESIDENT"}, statuses = {"ACTIVE"})
    public void createIssue(MultipartHttpServletRequest data, HttpServletRequest request) throws IOException {
        // Attributes
        String title = data.getParameter("title");
        String description = data.getParameter("description");
        Long categoryId = Long.valueOf(data.getParameter("categoryId"));

        // Coordinates
        double latitude = Double.parseDouble(data.getParameter("latitude"));
        double longitude = Double.parseDouble(data.getParameter("longitude"));
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate coordinate = new Coordinate(longitude, latitude);
        Point coordinates = geometryFactory.createPoint(coordinate);

        // Photo
        MultipartFile photoFile = data.getFile("photo");
        if (photoFile == null) {
            throw new IllegalArgumentException("Photo is required");
        }

        // Firebase token
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        issueService.createIssue(token.getUid(), title, description, categoryId, coordinates, photoFile);
    }

    /**
     * Gets likes count.
     *
     * @param id the id
     * @return the likes count
     */
    @GetMapping(value = "/{id}/likes/count", produces = "application/json")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<IssueLikesResponseDto> getLikesCount(@PathVariable Long id) {
        IssueLikesResponseDto response = new IssueLikesResponseDto();
        response.setCount(issueService.getLikesCount(id));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Gets like status.
     *
     * @param id      the id
     * @param request the request
     * @return the like status
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{id}/like/status", produces = "application/json")
    @FirebaseAuthorization(roles = {"RESIDENT"}, statuses = {"ACTIVE"})
    public ResponseEntity<IssueLikeStatusResponseDto> getLikeStatus(@PathVariable("id") Long id,
                                                                    HttpServletRequest request) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        Boolean likeStatus = issueService.getLikeStatus(id, token.getUid());
        IssueLikeStatusResponseDto response = new IssueLikeStatusResponseDto();
        response.setLikeStatus(likeStatus);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Like issue.
     *
     * @param id      the id
     * @param request the request
     */
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/{id}/like")
    @FirebaseAuthorization(roles = {"RESIDENT"}, statuses = {"ACTIVE"})
    public void likeIssue(@PathVariable("id") Long id,
                          HttpServletRequest request) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        issueService.likeIssue(id, token.getUid());
    }

    /**
     * Delete like issue.
     *
     * @param issueId the issue id
     * @param request the request
     */
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(path = "/{id}/like")
    @FirebaseAuthorization(roles = {"RESIDENT"}, statuses = {"ACTIVE"})
    public void deleteLikeIssue(@PathVariable("id") Long issueId,
                                HttpServletRequest request) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        issueService.deleteLikeIssue(issueId, token.getUid());
    }

    /**
     * Gets issue reservation.
     *
     * @param id the id
     * @return the issue reservation
     */
    @GetMapping(path = "/{id}/reservation", produces = "application/json")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<IssueReservationResponseDto> getIssueReservation(@PathVariable("id") Long id) {
        IssueReservationResponseDto response = new ModelMapper().map(
                issueService.findIssueReservation(id), IssueReservationResponseDto.class);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Create issue reservation.
     *
     * @param id      the id
     * @param request the request
     */
    @ResponseStatus(HttpStatus.CREATED)
    @FirebaseAuthorization(roles = {"EMPLOYEE"}, statuses = {"ACTIVE"})
    @PostMapping(path = "/{id}/reservation", consumes = "application/json")
    public void createIssueReservation(@PathVariable("id") Long id, HttpServletRequest request)
            throws AuthException, ExecutionException, FirebaseAuthException, InterruptedException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        issueReservationService.createIssueReservation(id, token);
    }

    @GetMapping(path = "/resident/me", produces = "application/json")
    @FirebaseAuthorization(roles = {"RESIDENT"}, statuses = {"ACTIVE"})
    public ResponseEntity<Map<String, Object>> getCurrentResidentIssues(
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) OrderBy orderBy,
            @RequestParam(required = false) Order order,
            HttpServletRequest request) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        // Filters
        Map<String, Object> filters = new HashMap<>();
        filters.put("authorId", token.getUid());
        if (status != null) filters.put("status", status);
        if (orderBy == null) orderBy = OrderBy.CREATION_DATE;
        if (order == null) order = Order.DESC;

        // Pagination and sorting
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Direction.fromString(order.toString()), orderBy.getFieldName()));
        Page<Issue> pageIssues = issueService.getIssues(pageable, filters);
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

    @GetMapping(path = "/resident/{uid}/count", produces = "application/json")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<CountResponseDto> getResidentIssuesCount(@PathVariable String uid) {
        CountResponseDto response = new CountResponseDto();

        Map<String, Object> filters = new HashMap<>();
        filters.put("authorId", uid);

        response.setCount(issueService.getIssuesCount(filters));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(path = "/{id}/moderation", produces = "application/json")
    @FirebaseAuthorization(roles = {"RESIDENT", "MODERATOR"}, statuses = {"ACTIVE"})
    public ResponseEntity<IssueModerationResponseResponseDto> getModerationResponse(@PathVariable Long id,
                                                                                    HttpServletRequest request)
            throws AuthException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        if (token.getClaims().get("role").toString().equals("RESIDENT")) {
            if (!issueService.findIssueById(id).getAuthorId().equals(token.getUid())) {
                throw new AuthException("Unauthorized");
            }
        }

        IssueModerationResponseResponseDto response = new ModelMapper().map(
                issueService.getModerationResponseByIssueId(id), IssueModerationResponseResponseDto.class);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(path = "/count", produces = "application/json")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<CountResponseDto> getIssuesCount(@RequestParam(required = false) IssueStatus status,
                                                           @RequestParam(required = false) String authorId,
                                                           @RequestParam(required = false) List<Long> categories,
                                                           @RequestParam(required = false)
                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                           @RequestParam(required = false)
                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to)
    {
        // Filters
        Map<String, Object> filters = new HashMap<>();
        if (status != null) filters.put("status", status);
        if (authorId != null) filters.put("authorId", authorId);
        if (categories != null) filters.put("categories", categories);
        if (from != null && to != null) {
            filters.put("from", from);
            filters.put("to", to);
        }

        CountResponseDto response = new CountResponseDto();
        response.setCount(issueService.getIssuesCount(filters));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Gets issue solution.
     *
     * @param id the id
     * @return the issue solution
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{id}/solution", produces = "application/json")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<IssueSolutionResponseDto> getIssueSolution(@PathVariable("id") Long id) {
        IssueSolutionResponseDto issueSolutionResponseDto = new ModelMapper().map(
                issueService.findIssueSolution(id), IssueSolutionResponseDto.class);
        return ResponseEntity.ok(issueSolutionResponseDto);
    }

    /**
     * Create issue solution.
     *
     * @param id      the id
     * @param data    the data
     * @param request the request
     */
    @ResponseStatus(HttpStatus.CREATED)
    @FirebaseAuthorization(roles = {"EMPLOYEE"}, statuses = {"ACTIVE"})
    @PostMapping(path = "/{id}/solution", consumes = "application/json")
    public void createIssueSolution(@PathVariable("id") Long id,
                                    MultipartHttpServletRequest data,
                                    HttpServletRequest request) {
        // Attributes
        String description = data.getParameter("description");
        MultipartFile photoFile = data.getFile("photo");

        // Token
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        issueSolutionService.createIssueSolution(id, token.getUid(), description, photoFile);
    }
}
