package dev.mikita.issueservice.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import dev.mikita.issueservice.annotation.FirebaseAuthorization;
import dev.mikita.issueservice.dto.response.common.AvgTimeResponseDto;
import dev.mikita.issueservice.dto.response.common.CountResponseDto;
import dev.mikita.issueservice.dto.response.common.IssueSolutionResponseDto;
import dev.mikita.issueservice.dto.response.employee.IssueSolutionEmployeeResponseDto;
import dev.mikita.issueservice.dto.response.service.IssueSolutionServiceResponseDto;
import dev.mikita.issueservice.entity.IssueSolution;
import dev.mikita.issueservice.service.DepartmentService;
import dev.mikita.issueservice.service.EmployeeService;
import dev.mikita.issueservice.service.IssueSolutionService;
import dev.mikita.issueservice.entity.UserRole;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/v1/solutions")
public class IssueSolutionController {
    private final IssueSolutionService issueSolutionService;
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
    public IssueSolutionController(IssueSolutionService issueSolutionService,
                                   EmployeeService employeeService,
                                   DepartmentService departmentService) {
        this.issueSolutionService = issueSolutionService;
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }

    @GetMapping(path = "/{id}", produces = "application/json")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<?> getIssueSolution(
            @PathVariable Long id,
            HttpServletRequest request) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        IssueSolution issueSolution = issueSolutionService.getIssueSolution(id);

        Class<?> responseClass = switch ((UserRole) token.getClaims().get("role")) {
            case SERVICE -> IssueSolutionServiceResponseDto.class;
            case EMPLOYEE -> {
                if (issueSolution.getEmployeeUid().equals(token.getUid())) {
                    yield IssueSolutionEmployeeResponseDto.class;
                } else {
                    yield IssueSolutionResponseDto.class;
                }
            }
            default -> IssueSolutionResponseDto.class;
        };

        return ResponseEntity.ok(new ModelMapper().map(issueSolution, responseClass));
    }

    @GetMapping(produces = "application/json")
    @FirebaseAuthorization(roles = {"ANALYST"}, statuses = {"ACTIVE"})
    public ResponseEntity<Map<String, Object>> getIssuesSolutions(
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

        Page<IssueSolution> pageSolutions = issueSolutionService
                .getIssuesSolutions(null, null, null, from, to, categories, pageable);
        List<IssueSolution> solutions = pageSolutions.getContent();

        // Collect result
        ModelMapper modelMapper = new ModelMapper();

        Map<String, Object> response = new HashMap<>();
        response.put("reservations", solutions.stream()
                .map(solution -> modelMapper.map(solution, IssueSolutionResponseDto.class))
                .collect(Collectors.toList()));
        response.put("currentPage", pageSolutions.getNumber());
        response.put("totalItems", pageSolutions.getTotalElements());
        response.put("totalPages", pageSolutions.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/service/{uid}", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE", "ANALYST"}, statuses = {"ACTIVE"})
    public ResponseEntity<Map<String, Object>> getIssuesSolutionsByService(
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

        Page<IssueSolution> pageSolutions = issueSolutionService
                .getIssuesSolutions(uid, null, null, from, to, categories, pageable);
        List<IssueSolution> solutions = pageSolutions.getContent();

        // Collect result
        ModelMapper modelMapper = new ModelMapper();

        Class<?> responseClass = token.getClaims().get("role").toString().equals(UserRole.SERVICE.toString()) ?
                IssueSolutionServiceResponseDto.class : IssueSolutionResponseDto.class;

        Map<String, Object> response = new HashMap<>();
        response.put("reservations", solutions.stream()
                .map(solution -> modelMapper.map(solution, responseClass))
                .collect(Collectors.toList()));
        response.put("currentPage", pageSolutions.getNumber());
        response.put("totalItems", pageSolutions.getTotalElements());
        response.put("totalPages", pageSolutions.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/department/{uid}", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE", "EMPLOYEE"}, statuses = {"ACTIVE"})
    public ResponseEntity<Map<String, Object>> getIssuesSolutionsByDepartment(
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

        Page<IssueSolution> pageSolutions = issueSolutionService
                .getIssuesSolutions(null, null, uid, from, to, categories, pageable);
        List<IssueSolution> solutions = pageSolutions.getContent();

        // Collect result
        ModelMapper modelMapper = new ModelMapper();

        Class<?> responseClass = switch (UserRole.valueOf(token.getClaims().get("role").toString())) {
            case SERVICE -> IssueSolutionServiceResponseDto.class;
            case EMPLOYEE -> IssueSolutionEmployeeResponseDto.class;
            default -> IssueSolutionResponseDto.class;
        };

        Map<String, Object> response = new HashMap<>();
        response.put("reservations", solutions.stream()
                .map(solution -> modelMapper.map(solution, responseClass))
                .collect(Collectors.toList()));
        response.put("currentPage", pageSolutions.getNumber());
        response.put("totalItems", pageSolutions.getTotalElements());
        response.put("totalPages", pageSolutions.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/employee/{uid}", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE", "EMPLOYEE"}, statuses = {"ACTIVE"})
    public ResponseEntity<Map<String, Object>> getIssuesSolutionsByEmployee(
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

        Page<IssueSolution> pageSolutions = issueSolutionService
                .getIssuesSolutions(null, uid, null, from, to, categories, pageable);
        List<IssueSolution> solutions = pageSolutions.getContent();

        // Collect result
        ModelMapper modelMapper = new ModelMapper();

        Class<?> responseClass = switch (UserRole.valueOf(token.getClaims().get("role").toString())) {
            case SERVICE -> IssueSolutionServiceResponseDto.class;
            case EMPLOYEE -> IssueSolutionEmployeeResponseDto.class;
            default -> IssueSolutionResponseDto.class;
        };

        Map<String, Object> response = new HashMap<>();
        response.put("reservations", solutions.stream()
                .map(solution -> modelMapper.map(solution, responseClass))
                .collect(Collectors.toList()));
        response.put("currentPage", pageSolutions.getNumber());
        response.put("totalItems", pageSolutions.getTotalElements());
        response.put("totalPages", pageSolutions.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/count", produces = "application/json")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<CountResponseDto> getIssuesSolutionsCount(
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        CountResponseDto response = new CountResponseDto();
        response.setCount(issueSolutionService.getIssuesSolutionsCount(null, null, null, from, to, categories));
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/service/{uid}/count", produces = "application/json")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<CountResponseDto> getIssuesSolutionsCountByService(
            @PathVariable String uid,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        CountResponseDto response = new CountResponseDto();
        response.setCount(issueSolutionService.getIssuesSolutionsCount(uid, null, null, from, to, categories));
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/department/{uid}/count", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE", "EMPLOYEE"}, statuses = {"ACTIVE"})
    public ResponseEntity<CountResponseDto> getIssuesSolutionsCountByDepartment(
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

        CountResponseDto response = new CountResponseDto();
        response.setCount(issueSolutionService.getIssuesSolutionsCount(null, null, uid, from, to, categories));
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/employee/{uid}/count", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE", "EMPLOYEE"}, statuses = {"ACTIVE"})
    public ResponseEntity<CountResponseDto> getIssuesSolutionsCountByEmployee(
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

        CountResponseDto response = new CountResponseDto();
        response.setCount(issueSolutionService.getIssuesSolutionsCount(null, uid, null, from, to, categories));
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/avg-time", produces = "application/json")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<AvgTimeResponseDto> getAvgTimeSolutions(
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        AvgTimeResponseDto response = new AvgTimeResponseDto();
        response.setAvgTime(issueSolutionService.getAverageSolutionsTime(
                null, null, null, categories, from, to));
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/service/{uid}/avg-time", produces = "application/json")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<AvgTimeResponseDto> getAvgTimeSolutionsByService(
            @PathVariable String uid,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        AvgTimeResponseDto response = new AvgTimeResponseDto();
        response.setAvgTime(issueSolutionService.getAverageSolutionsTime(
                uid, null, null, categories, from, to));
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/department/{uid}/avg-time", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE", "EMPLOYEE"}, statuses = {"ACTIVE"})
    public ResponseEntity<AvgTimeResponseDto> getAvgTimeSolutionsByDepartment(
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

        AvgTimeResponseDto response = new AvgTimeResponseDto();
        response.setAvgTime(issueSolutionService.getAverageSolutionsTime(
                null, null, uid, categories, from, to));
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/employee/{uid}/avg-time", produces = "application/json")
    @FirebaseAuthorization(roles = {"SERVICE", "EMPLOYEE"}, statuses = {"ACTIVE"})
    public ResponseEntity<AvgTimeResponseDto> getAvgTimeSolutionsByEmployee(
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

        AvgTimeResponseDto response = new AvgTimeResponseDto();
        response.setAvgTime(issueSolutionService.getAverageSolutionsTime(
                null, uid, null, categories, from, to));
        return ResponseEntity.ok(response);
    }
}
