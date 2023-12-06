package dev.mikita.issueservice.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import dev.mikita.issueservice.annotation.FirebaseAuthorization;
import dev.mikita.issueservice.dto.response.CountResponseDto;
import dev.mikita.issueservice.dto.response.solution.IssueSolutionResponseDto;
import dev.mikita.issueservice.entity.IssueSolution;
import dev.mikita.issueservice.service.DepartmentService;
import dev.mikita.issueservice.service.EmployeeService;
import dev.mikita.issueservice.service.IssueSolutionService;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping(path = "/api/v1/solutions")
public class IssueSolutionController {
    private final IssueSolutionService issueSolutionService;
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    @Autowired
    public IssueSolutionController(IssueSolutionService issueSolutionService,
                                   EmployeeService employeeService,
                                   DepartmentService departmentService) {
        this.issueSolutionService = issueSolutionService;
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }

    @GetMapping(path = "/{id}")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<IssueSolutionResponseDto> getIssueSolution(@PathVariable Long id) {
        IssueSolutionResponseDto response = new ModelMapper().map(
                issueSolutionService.getIssueSolutionById(id), IssueSolutionResponseDto.class);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(path = "/service/{uid}")
    @FirebaseAuthorization(roles = {"ROLE_SERVICE"}, statuses = {"ACTIVE"})
    public ResponseEntity<List<IssueSolutionResponseDto>> getServiceSolutions(
            @PathVariable String uid, HttpServletRequest request)
            throws AuthException, ExecutionException, InterruptedException, FirebaseAuthException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        if (!token.getUid().equals(uid)) {
            throw new AuthException("Unauthorized");
        }

        List<IssueSolution> response = issueSolutionService.getIssueSolutionByEmployeeId(uid);
        return ResponseEntity.ok(new ModelMapper().map(response, new ParameterizedTypeReference<List<IssueSolutionResponseDto>>() {}.getType()));
    }

    @GetMapping(path = "/employee/{uid}")
    @FirebaseAuthorization(roles = {"ROLE_EMPLOYEE", "ROLE_SERVICE"}, statuses = {"ACTIVE"})
    public ResponseEntity<List<IssueSolutionResponseDto>> getEmployeeSolutions(
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

        List<IssueSolution> response = issueSolutionService.getIssueSolutionByEmployeeId(uid);
        return ResponseEntity.ok(new ModelMapper().map(response, new ParameterizedTypeReference<List<IssueSolutionResponseDto>>() {}.getType()));
    }

    @GetMapping(path = "/department/{uid}")
    @FirebaseAuthorization(roles = {"ROLE_SERVICE"}, statuses = {"ACTIVE"})
    public ResponseEntity<List<IssueSolutionResponseDto>> getDepartmentSolutions(
            @PathVariable String uid, HttpServletRequest request)
            throws AuthException, ExecutionException, InterruptedException, FirebaseAuthException {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        if (!departmentService.isServiceOwnerOfDepartment(token.getUid(), uid)) {
            throw new AuthException("Unauthorized");
        }

        List<IssueSolution> response = issueSolutionService.getIssueSolutionByDepartmentId(uid);
        return ResponseEntity.ok(new ModelMapper().map(response, new ParameterizedTypeReference<List<IssueSolutionResponseDto>>() {}.getType()));
    }

    /**
     * Gets issues solutions count.
     *
     * @param serviceId the service id
     * @return the issues solutions count
     */
    @GetMapping(path = "/solutions/count")
    @FirebaseAuthorization(statuses = {"ACTIVE"})
    public ResponseEntity<CountResponseDto> getIssuesSolutionsCount(
            @RequestParam(required = false) String serviceId) {
        CountResponseDto response = new CountResponseDto();
        response.setCount(issueSolutionService.getIssuesSolutionsCount());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
