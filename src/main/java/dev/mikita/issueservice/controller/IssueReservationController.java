package dev.mikita.issueservice.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import dev.mikita.issueservice.annotation.FirebaseAuthorization;
import dev.mikita.issueservice.dto.response.common.CountResponseDto;
import dev.mikita.issueservice.dto.response.common.IssueReservationResponseDto;
import dev.mikita.issueservice.dto.response.employee.IssueReservationEmployeeResponseDto;
import dev.mikita.issueservice.dto.response.service.IssueReservationServiceResponseDto;
import dev.mikita.issueservice.entity.*;
import dev.mikita.issueservice.service.DepartmentService;
import dev.mikita.issueservice.service.EmployeeService;
import dev.mikita.issueservice.service.IssueReservationService;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/v1/reservations")
public class IssueReservationController {
    private final IssueReservationService issueReservationService;
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    @Getter
    public enum OrderBy {
        CREATION_DATE("creationDate");

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
    public IssueReservationController(IssueReservationService issueReservationService,
                                      EmployeeService employeeService,
                                      DepartmentService departmentService) {
        this.issueReservationService = issueReservationService;
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }

    @GetMapping(path = "/{id}", produces = "application/json")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<?> getIssueReservation(
            @PathVariable Long id,
            HttpServletRequest request) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        IssueReservation issueReservation = issueReservationService.getIssueReservation(id);

        Class<?> responseClass = switch ((UserRole) token.getClaims().get("role")) {
            case SERVICE -> IssueReservationServiceResponseDto.class;
            case EMPLOYEE -> {
                if (issueReservation.getEmployeeUid().equals(token.getUid())) {
                    yield IssueReservationEmployeeResponseDto.class;
                } else {
                    yield IssueReservationResponseDto.class;
                }
            }
            default -> IssueReservationResponseDto.class;
        };

        return ResponseEntity.ok(new ModelMapper().map(issueReservation, responseClass));
    }

    @GetMapping(produces = "application/json")
    @FirebaseAuthorization(roles = {"ANALYST"}, statuses = {"ACTIVE"})
    public ResponseEntity<Map<String, Object>> getIssuesReservations(
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(name = "order_by",required = false) OrderBy orderBy,
            @RequestParam(required = false) Order order) {
        // Pagination and sorting
        if (orderBy == null) orderBy = OrderBy.CREATION_DATE;
        if (order == null) order = Order.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Direction.fromString(order.toString()), orderBy.getFieldName()));

        Page<IssueReservation> pageReservations = issueReservationService
                .getIssuesReservations(null, null, null, from, to, categories, pageable);
        List<IssueReservation> reservations = pageReservations.getContent();

        // Collect result
        ModelMapper modelMapper = new ModelMapper();

        Map<String, Object> response = new HashMap<>();
        response.put("reservations", reservations.stream()
                .map(reservation -> modelMapper.map(reservation, IssueReservationResponseDto.class))
                .collect(Collectors.toList()));
        response.put("currentPage", pageReservations.getNumber());
        response.put("totalItems", pageReservations.getTotalElements());
        response.put("totalPages", pageReservations.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/service/{uid}", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE", "ANALYST"}, statuses = {"ACTIVE"})
    public ResponseEntity<Map<String, Object>> getIssuesReservationsByService(
            @PathVariable String uid,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(name = "order_by",required = false) OrderBy orderBy,
            @RequestParam(required = false) Order order,
            HttpServletRequest request) throws AuthException {
        // Authorization
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        if (token.getClaims().get("role").toString().equals(UserRole.SERVICE.toString()) && !token.getUid().equals(uid)) {
            throw new AuthException("You are not authorized to access this resource");
        }

        // Pagination and sorting
        if (orderBy == null) orderBy = OrderBy.CREATION_DATE;
        if (order == null) order = Order.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Direction.fromString(order.toString()), orderBy.getFieldName()));

        Page<IssueReservation> pageReservations = issueReservationService
                .getIssuesReservations(uid, null, null, from, to, categories, pageable);
        List<IssueReservation> reservations = pageReservations.getContent();

        // Collect result
        ModelMapper modelMapper = new ModelMapper();

        Class<?> responseClass = token.getClaims().get("role").toString().equals(UserRole.SERVICE.toString()) ?
                IssueReservationServiceResponseDto.class : IssueReservationResponseDto.class;

        Map<String, Object> response = new HashMap<>();
        response.put("reservations", reservations.stream()
                .map(reservation -> modelMapper.map(reservation, responseClass))
                .collect(Collectors.toList()));
        response.put("currentPage", pageReservations.getNumber());
        response.put("totalItems", pageReservations.getTotalElements());
        response.put("totalPages", pageReservations.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/department/{uid}", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE", "EMPLOYEE"}, statuses = {"ACTIVE"})
    public ResponseEntity<Map<String, Object>> getIssuesReservationsByDepartment(
            @PathVariable String uid,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(name = "order_by",required = false) OrderBy orderBy,
            @RequestParam(required = false) Order order,
            HttpServletRequest request) throws AuthException, ExecutionException, InterruptedException, FirebaseAuthException {

        // Authorization
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        if (token.getClaims().get("role").toString().equals(UserRole.SERVICE.toString())) {
            if (!departmentService.isServiceOwnerOfDepartment(token.getUid(), uid)) {
                throw new AuthException("You are not authorized to access this resource");
            }
        } else if (token.getClaims().get("role").toString().equals(UserRole.EMPLOYEE.toString())) {
            if (!employeeService.isEmployeeInDepartment(token.getUid(), uid)) {
                throw new AuthException("You are not authorized to access this resource");
            }
        }

        // Pagination and sorting
        if (orderBy == null) orderBy = OrderBy.CREATION_DATE;
        if (order == null) order = Order.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Direction.fromString(order.toString()), orderBy.getFieldName()));

        Page<IssueReservation> pageReservations = issueReservationService
                .getIssuesReservations(null, null, uid, from, to, categories, pageable);
        List<IssueReservation> reservations = pageReservations.getContent();

        // Collect result
        ModelMapper modelMapper = new ModelMapper();

        Class<?> responseClass = switch (UserRole.valueOf(token.getClaims().get("role").toString())) {
            case SERVICE -> IssueReservationServiceResponseDto.class;
            case EMPLOYEE -> IssueReservationEmployeeResponseDto.class;
            default -> IssueReservationResponseDto.class;
        };

        Map<String, Object> response = new HashMap<>();
        response.put("reservations", reservations.stream()
                .map(reservation -> modelMapper.map(reservation, responseClass))
                .collect(Collectors.toList()));
        response.put("currentPage", pageReservations.getNumber());
        response.put("totalItems", pageReservations.getTotalElements());
        response.put("totalPages", pageReservations.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/employee/{uid}", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE", "EMPLOYEE"}, statuses = {"ACTIVE"})
    public ResponseEntity<Map<String, Object>> getIssuesReservationsByEmployee(
            @PathVariable String uid,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(name = "order_by",required = false) OrderBy orderBy,
            @RequestParam(required = false) Order order,
            HttpServletRequest request) throws AuthException, ExecutionException, InterruptedException, FirebaseAuthException {

        // Authorization
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        if (token.getClaims().get("role").toString().equals(UserRole.SERVICE.toString())) {
            if (!employeeService.isEmployeeInService(uid, token.getUid())) {
                throw new AuthException("You are not authorized to access this resource");
            }
        } else if (token.getClaims().get("role").toString().equals(UserRole.EMPLOYEE.toString())) {
            if (!token.getUid().equals(uid)) {
                throw new AuthException("You are not authorized to access this resource");
            }
        }

        // Pagination and sorting
        if (orderBy == null) orderBy = OrderBy.CREATION_DATE;
        if (order == null) order = Order.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Direction.fromString(order.toString()), orderBy.getFieldName()));

        Page<IssueReservation> pageReservations = issueReservationService
                .getIssuesReservations(null, uid, null, from, to, categories, pageable);
        List<IssueReservation> reservations = pageReservations.getContent();

        // Collect result
        ModelMapper modelMapper = new ModelMapper();

        Class<?> responseClass = switch (UserRole.valueOf(token.getClaims().get("role").toString())) {
            case SERVICE -> IssueReservationServiceResponseDto.class;
            case EMPLOYEE -> IssueReservationEmployeeResponseDto.class;
            default -> IssueReservationResponseDto.class;
        };

        Map<String, Object> response = new HashMap<>();
        response.put("reservations", reservations.stream()
                .map(reservation -> modelMapper.map(reservation, responseClass))
                .collect(Collectors.toList()));
        response.put("currentPage", pageReservations.getNumber());
        response.put("totalItems", pageReservations.getTotalElements());
        response.put("totalPages", pageReservations.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/count", produces = "application/json")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<CountResponseDto> getIssuesReservationsCount(
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        CountResponseDto response = new CountResponseDto(issueReservationService.getIssuesReservationsCount(null, null, null, from, to, categories));
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/service/{uid}/count", produces = "application/json")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<CountResponseDto> getIssuesReservationsCountByService(
            @PathVariable String uid,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        CountResponseDto response = new CountResponseDto(issueReservationService.getIssuesReservationsCount(uid, null, null, from, to, categories));
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/department/{uid}/count", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE", "EMPLOYEE"}, statuses = {"ACTIVE"})
    public ResponseEntity<CountResponseDto> getIssuesReservationsCountByDepartment(
            @PathVariable String uid,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) throws AuthException, ExecutionException, InterruptedException, FirebaseAuthException {
        // Authorization
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        if (token.getClaims().get("role").toString().equals(UserRole.SERVICE.toString())) {
            if (!departmentService.isServiceOwnerOfDepartment(token.getUid(), uid)) {
                throw new AuthException("You are not authorized to access this resource");
            }
        } else if (token.getClaims().get("role").toString().equals(UserRole.EMPLOYEE.toString())) {
            if (!employeeService.isEmployeeInDepartment(token.getUid(), uid)) {
                throw new AuthException("You are not authorized to access this resource");
            }
        }

        CountResponseDto response = new CountResponseDto(issueReservationService.getIssuesReservationsCount(null, null, uid, from, to, categories));
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/employee/{uid}/count", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE", "EMPLOYEE"}, statuses = {"ACTIVE"})
    public ResponseEntity<CountResponseDto> getIssuesReservationsCountByEmployee(
            @PathVariable String uid,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) throws AuthException, ExecutionException, InterruptedException, FirebaseAuthException {
        // Authorization
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        if (token.getClaims().get("role").toString().equals(UserRole.SERVICE.toString())) {
            if (!employeeService.isEmployeeInService(uid, token.getUid())) {
                throw new AuthException("You are not authorized to access this resource");
            }
        } else if (token.getClaims().get("role").toString().equals(UserRole.EMPLOYEE.toString())) {
            if (!token.getUid().equals(uid)) {
                throw new AuthException("You are not authorized to access this resource");
            }
        }

        CountResponseDto response = new CountResponseDto(issueReservationService.getIssuesReservationsCount(null, uid, null, from, to, categories));
        return ResponseEntity.ok(response);
    }
}