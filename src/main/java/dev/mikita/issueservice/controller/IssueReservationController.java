package dev.mikita.issueservice.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import dev.mikita.issueservice.annotation.FirebaseAuthorization;
import dev.mikita.issueservice.dto.response.common.CountResponseDto;
import dev.mikita.issueservice.dto.response.common.IssueReservationResponseDto;
import dev.mikita.issueservice.dto.response.service.IssueReservationServiceResponseDto;
import dev.mikita.issueservice.entity.IssueReservation;
import dev.mikita.issueservice.service.DepartmentService;
import dev.mikita.issueservice.service.EmployeeService;
import dev.mikita.issueservice.service.IssueReservationService;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping(path = "/api/v1/reservations")
public class IssueReservationController {
    private final IssueReservationService issueReservationService;
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    @Autowired
    public IssueReservationController(IssueReservationService issueReservationService,
                                      EmployeeService employeeService,
                                      DepartmentService departmentService) {
        this.issueReservationService = issueReservationService;
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }

    @GetMapping(path = "/{id}", produces = "application/json")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<IssueReservationResponseDto> getIssueReservation(@PathVariable Long id) {
        IssueReservationResponseDto response = new ModelMapper().map(
                issueReservationService.getIssueReservation(id), IssueReservationResponseDto.class);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(path = "/service/{uid}", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE"}, statuses = {"ACTIVE"})
    public ResponseEntity<List<IssueReservationServiceResponseDto>> getServiceReservations(
            @PathVariable String uid, HttpServletRequest request)
            throws AuthException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        if (!token.getUid().equals(uid)) {
            throw new AuthException("Unauthorized");
        }

        List<IssueReservation> response = issueReservationService.getIssuesReservationsByServiceId(uid);
        return ResponseEntity.ok(new ModelMapper().map(response, new ParameterizedTypeReference<List<IssueReservationServiceResponseDto>>() {}.getType()));
    }

    @GetMapping(path = "/employee/{uid}", produces = "application/json")
    @FirebaseAuthorization(roles = {"EMPLOYEE", "SERVICE"}, statuses = {"ACTIVE"})
    public ResponseEntity<List<IssueReservationResponseDto>> getEmployeeReservations(
            @PathVariable String uid, HttpServletRequest request)
            throws AuthException, ExecutionException, InterruptedException, FirebaseAuthException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        // Authorization
        switch (token.getClaims().get("role").toString()) {
            case "ROLE_SERVICE" -> {
                if (employeeService.isEmployeeInService(uid, token.getUid())) {
                    throw new AuthException("Unauthorized");
                }
            }
            case "ROLE_EMPLOYEE" -> {
                if (!token.getUid().equals(uid)) {
                    throw new AuthException("Unauthorized");
                }
            }
        }

        List<IssueReservation> response = issueReservationService.getIssuesReservationsByEmployeeId(uid);
        return ResponseEntity.ok(new ModelMapper().map(response, new ParameterizedTypeReference<List<IssueReservationResponseDto>>() {}.getType()));
    }

    @GetMapping(path = "/department/{uid}", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE"}, statuses = {"ACTIVE"})
    public ResponseEntity<List<IssueReservationResponseDto>> getDepartmentReservations(
            @PathVariable String uid, HttpServletRequest request)
            throws AuthException, ExecutionException, InterruptedException, FirebaseAuthException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        if (!departmentService.isServiceOwnerOfDepartment(token.getUid(), uid)) {
            throw new AuthException("Unauthorized");
        }

        List<IssueReservation> response = issueReservationService.getIssuesReservationsByDepartmentId(uid);
        return ResponseEntity.ok(new ModelMapper().map(response, new ParameterizedTypeReference<List<IssueReservationResponseDto>>() {}.getType()));
    }

    @GetMapping(path = "/service/{uid}/count", produces = "application/json")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<CountResponseDto> getServiceReservationsCount(@PathVariable String uid) {
        CountResponseDto response = new CountResponseDto();
        response.setCount(issueReservationService.getIssuesReservationsCountByServiceId(uid));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(path = "/employee/{uid}/count", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE", "EMPLOYEE"},statuses = {"ACTIVE"})
    public ResponseEntity<CountResponseDto> getEmployeeReservationsCount(
            @PathVariable String uid, HttpServletRequest request)
            throws AuthException, ExecutionException, InterruptedException, FirebaseAuthException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        // Authorization
        switch (token.getClaims().get("role").toString()) {
            case "SERVICE" -> {
                if (employeeService.isEmployeeInService(uid, token.getUid())) {
                    throw new AuthException("Unauthorized");
                }
            }
            case "EMPLOYEE" -> {
                if (!token.getUid().equals(uid)) {
                    throw new AuthException("Unauthorized");
                }
            }
        }

        CountResponseDto response = new CountResponseDto();
        response.setCount(issueReservationService.getIssuesReservationsCountByEmployeeId(uid));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(path = "/department/{uid}/count", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE"}, statuses = {"ACTIVE"})
    public ResponseEntity<CountResponseDto> getDepartmentReservationsCount(
            @PathVariable String uid,
            HttpServletRequest request)
            throws ExecutionException, InterruptedException, FirebaseAuthException, AuthException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        if (!departmentService.isServiceOwnerOfDepartment(token.getUid(), uid)) {
            throw new AuthException("Unauthorized");
        }

        CountResponseDto response = new CountResponseDto();
        response.setCount(issueReservationService.getIssuesReservationsCountByDepartmentId(uid));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}