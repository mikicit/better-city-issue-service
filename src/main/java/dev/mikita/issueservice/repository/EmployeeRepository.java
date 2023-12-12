package dev.mikita.issueservice.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import dev.mikita.issueservice.entity.Employee;
import dev.mikita.issueservice.entity.UserStatus;
import dev.mikita.issueservice.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Repository
public class EmployeeRepository {
    private final CollectionReference collectionReference;
    private final FirebaseAuth firebaseAuth;

    public EmployeeRepository(Firestore firestore,
                              FirebaseAuth firebaseAuth,
                              @Value("${firebase.firestore.collections.employee}") String collectionName) {
        this.firebaseAuth = firebaseAuth;
        this.collectionReference = firestore.collection(collectionName);
    }

    public Employee find(String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        UserRecord userRecord = firebaseAuth.getUser(uid);

        if (userRecord.getCustomClaims().isEmpty()
                || !userRecord.getCustomClaims().get("role").toString().equals("EMPLOYEE")) {
            throw NotFoundException.create("Employee", uid);
        }

        return makeEmployee(userRecord, collectionReference.document(uid).get().get());
    }

    public List<Employee> findAllByDepartmentUid(String uid) throws ExecutionException, InterruptedException {
        List<Employee> employees = new ArrayList<>();
        collectionReference.whereEqualTo("departmentUid", uid).get().get().forEach(documentSnapshot -> {
            try {
                UserRecord userRecord = firebaseAuth.getUser(documentSnapshot.getId());
                employees.add(makeEmployee(userRecord, documentSnapshot));
            } catch (FirebaseAuthException e) {
                throw new RuntimeException(e);
            }
        });
        return employees;
    }

    public List<Employee> findAllByServiceUid(String uid) throws ExecutionException, InterruptedException {
        List<Employee> employees = new ArrayList<>();
        collectionReference.whereEqualTo("serviceUid", uid).get().get().forEach(documentSnapshot -> {
            try {
                UserRecord userRecord = firebaseAuth.getUser(documentSnapshot.getId());
                employees.add(makeEmployee(userRecord, documentSnapshot));
            } catch (FirebaseAuthException e) {
                throw new RuntimeException(e);
            }
        });
        return employees;
    }

    // snapshotToEntity
    private Employee makeEmployee(UserRecord userRecord, DocumentSnapshot snapshot) {
        Employee employee = new Employee();

        employee.setUid(userRecord.getUid());
        employee.setEmail(userRecord.getEmail());
        employee.setStatus(UserStatus.valueOf(userRecord.getCustomClaims().get("status").toString()));
        employee.setFirstName(snapshot.getString("firstName"));
        employee.setLastName(snapshot.getString("lastName"));
        employee.setDepartmentUid(snapshot.getString("departmentUid"));
        employee.setServiceUid(snapshot.getString("serviceUid"));

        return employee;
    }

    private Map<String, Object> entityToMap(Employee employee) {
        Map<String, Object> data = new HashMap<>();

        data.put("firstName", employee.getFirstName());
        data.put("lastName", employee.getLastName());
        data.put("serviceUid", employee.getServiceUid());
        data.put("departmentUid", employee.getDepartmentUid());

        return data;
    }
}
