package dev.mikita.issueservice.controller;

import com.google.firebase.auth.FirebaseToken;
import dev.mikita.issueservice.annotation.FirebaseAuthorization;
import dev.mikita.issueservice.dto.response.CountResponseDto;
import dev.mikita.issueservice.dto.response.issue.IssueResponseDto;
import dev.mikita.issueservice.dto.response.likes.IssueLikesResponseDto;
import dev.mikita.issueservice.dto.response.likes.IssueLikeStatusResponseDto;
import dev.mikita.issueservice.dto.response.reservation.IssueReservationResponseDto;
import dev.mikita.issueservice.dto.response.solution.IssueSolutionResponseDto;
import dev.mikita.issueservice.entity.*;
import dev.mikita.issueservice.service.CategoryService;
import dev.mikita.issueservice.service.IssueReservationService;
import dev.mikita.issueservice.service.IssueService;
import dev.mikita.issueservice.service.IssueSolutionService;
import jakarta.servlet.http.HttpServletRequest;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import java.io.IOException;
import java.util.*;

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
     * @param categoryService         the category service
     * @param issueSolutionService    the issue solution service
     */
    @Autowired
    public IssueController( IssueService issueService,
                            IssueReservationService issueReservationService,
                            CategoryService categoryService,
                            IssueSolutionService issueSolutionService
    ) {
        this.issueService = issueService;
        this.issueReservationService = issueReservationService;
        this.issueSolutionService = issueSolutionService;
    }

    /**
     * Gets issues.
     *
     * @param status    the status
     * @param authorId  the author id
     * @param distanceM the distance m
     * @param latitude  the latitude
     * @param longitude the longitude
     * @return the issues
     */
    @GetMapping
    @FirebaseAuthorization
    public ResponseEntity<List<IssueResponseDto>> getIssues(@RequestParam(required = false) IssueStatus status,
                                                            @RequestParam(required = false) String authorId,
                                                            @RequestParam(required = false) Double distanceM,
                                                            @RequestParam(required = false) Double latitude,
                                                            @RequestParam(required = false) Double longitude) {
        // Filters
        Map<String, Object> filters = new HashMap<>();
        if (status != null) filters.put("status", status);
        if (authorId != null) filters.put("authorId", authorId);
        if (latitude != null && longitude != null && distanceM != null) {
            GeometryFactory geometryFactory = new GeometryFactory();
            Coordinate coordinate = new Coordinate(longitude, latitude);
            Point coordinates = geometryFactory.createPoint(coordinate);
            filters.put("coordinates", coordinates);
            filters.put("distanceM", distanceM);
        }

        List<Issue> issues = issueService.findIssues(filters);

        ModelMapper modelMapper = new ModelMapper();
        List<IssueResponseDto> response = issues.stream().
                map(issue -> modelMapper.map(issue, IssueResponseDto.class)).toList();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Gets issue by id.
     *
     * @param id the id
     * @return the issue by id
     */
    @GetMapping(path = "/{id}")
    @FirebaseAuthorization
    public ResponseEntity<IssueResponseDto> getIssueById(@PathVariable("id") Long id) {
        return new ResponseEntity<>(new ModelMapper()
                .map(issueService.findIssueById(id), IssueResponseDto.class), HttpStatus.OK);
    }

    /**
     * Update issue status.
     *
     * @param id     the id
     * @param status the status
     */
    @PutMapping(path = "/{id}/status")
    @FirebaseAuthorization(roles = {"ROLE_MODERATOR", "ROLE_ADMIN"})
    public void updateIssueStatus(@PathVariable("id") Long id, @RequestParam IssueStatus status) {
        issueService.updateIssueStatus(id, status);
    }

    /**
     * Create issue.
     *
     * @param data    the data
     * @param request the request
     * @throws IOException the io exception
     */
// TODO Add validation
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @FirebaseAuthorization(roles = {"ROLE_RESIDENT"})
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

        // Firebase token
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        issueService.createIssue(token.getUid(), title, description, categoryId, coordinates, photoFile);
    }

    /**
     * Gets issues count.
     *
     * @param authorId the author id
     * @return the issues count
     */
    @GetMapping(path = "/count")
    @FirebaseAuthorization
    public ResponseEntity<CountResponseDto> getIssuesCount(@RequestParam(required = false) String authorId) {
        CountResponseDto response = new CountResponseDto();
        response.setCount(issueService.getIssuesCount(authorId));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Gets likes count.
     *
     * @param id the id
     * @return the likes count
     */
    @GetMapping("/{id}/likes/count")
    @FirebaseAuthorization
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
    @GetMapping(path = "/{id}/like/status")
    @FirebaseAuthorization(roles = {"ROLE_RESIDENT"})
    public ResponseEntity<IssueLikeStatusResponseDto> getLikeStatus(@PathVariable("id") Long id,
                                                                    HttpServletRequest request) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        IssueLikeStatusResponseDto response = new ModelMapper().map(
                issueService.getLikeStatus(id, token.getUid()), IssueLikeStatusResponseDto.class);

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
    @FirebaseAuthorization(roles = {"ROLE_RESIDENT"})
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
    @FirebaseAuthorization(roles = {"ROLE_RESIDENT"})
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
    @GetMapping(path = "/{id}/reservation")
    @FirebaseAuthorization
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
    @FirebaseAuthorization(roles = {"ROLE_SERVICE"})
    @PostMapping(path = "/{id}/reservation")
    public void createIssueReservation(@PathVariable("id") Long id, HttpServletRequest request) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        issueReservationService.createIssueReservation(id, token.getUid());
    }

    /**
     * Gets issues reservations count.
     *
     * @param serviceId the service id
     * @return the issues reservations count
     */
    @GetMapping(path = "/reservations/count")
    @FirebaseAuthorization
    public ResponseEntity<CountResponseDto> getIssuesReservationsCount(
            @RequestParam(required = false) String serviceId) {
        CountResponseDto response = new CountResponseDto();
        response.setCount(issueReservationService.getIssuesReservationsCount(serviceId));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Gets issue solution.
     *
     * @param id the id
     * @return the issue solution
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "/{id}/solution")
    @FirebaseAuthorization
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
// TODO Add validation
    @ResponseStatus(HttpStatus.CREATED)
    @FirebaseAuthorization(roles = {"ROLE_SERVICE"})
    @PostMapping(path = "/{id}/solution")
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

    /**
     * Gets issues solutions count.
     *
     * @param serviceId the service id
     * @return the issues solutions count
     */
    @GetMapping(path = "/solutions/count")
    @FirebaseAuthorization
    public ResponseEntity<CountResponseDto> getIssuesSolutionsCount(
            @RequestParam(required = false) String serviceId) {
        CountResponseDto response = new CountResponseDto();
        response.setCount(issueSolutionService.getIssuesSolutionsCount(serviceId));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
