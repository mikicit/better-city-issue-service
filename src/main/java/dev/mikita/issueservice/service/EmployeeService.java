package dev.mikita.issueservice.service;

import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.issueservice.entity.Employee;
import dev.mikita.issueservice.exception.NotFoundException;
import dev.mikita.issueservice.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Employee getEmployee(String uid)
            throws FirebaseAuthException, ExecutionException, InterruptedException {
        Employee employee = employeeRepository.find(uid);
        if (employee == null) {
            throw new NotFoundException("Employee not found");
        }

        return employee;
    }

    public List<Employee> getEmployeesByServiceUid(String serviceUid)
            throws ExecutionException, InterruptedException {
        return employeeRepository.findAllByServiceUid(serviceUid);
    }

    public List<Employee> getEmployeesByDepartmentUid(String departmentUid)
            throws ExecutionException, InterruptedException {
        return employeeRepository.findAllByDepartmentUid(departmentUid);
    }

    public boolean isEmployeeInDepartment(String employeeUid, String departmentUid)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        Employee employee = employeeRepository.find(employeeUid);
        return employee.getDepartmentUid().equals(departmentUid);
    }

    public boolean isEmployeeInService(String employeeUid, String serviceUid)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        Employee employee = employeeRepository.find(employeeUid);
        return employee.getServiceUid().equals(serviceUid);
    }
}
