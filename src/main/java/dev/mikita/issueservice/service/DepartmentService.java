package dev.mikita.issueservice.service;

import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.issueservice.entity.Department;
import dev.mikita.issueservice.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutionException;

@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public Department getDepartment(String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        return departmentRepository.find(uid);
    }

    public boolean isServiceOwnerOfDepartment(String serviceUid, String departmentUid)
            throws ExecutionException, InterruptedException, FirebaseAuthException {
        return departmentRepository.find(departmentUid).getServiceUid().equals(serviceUid);
    }
}
