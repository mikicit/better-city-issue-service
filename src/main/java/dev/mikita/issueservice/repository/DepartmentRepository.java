package dev.mikita.issueservice.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuthException;
import dev.mikita.issueservice.entity.Department;
import dev.mikita.issueservice.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Repository
public class DepartmentRepository {
    private final CollectionReference collectionReference;

    @Autowired
    public DepartmentRepository(Firestore firestore,
                                @Value("${firebase.firestore.collections.department}") String collectionName) {
        this.collectionReference = firestore.collection(collectionName);
    }

    public List<Department> findByServiceUid(String uid) throws ExecutionException, InterruptedException {
        List<Department> departments = new ArrayList<>();
        collectionReference.whereEqualTo("serviceUid", uid).get().get().forEach(documentSnapshot -> {
            departments.add(snapshotToEntity(documentSnapshot));
        });
        return departments;
    }

    public Department find(String uid) throws FirebaseAuthException, ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = collectionReference.document(uid).get().get();
        if (!snapshot.exists()) throw new NotFoundException("Department is not found.");
        return snapshotToEntity(snapshot);
    }

    public boolean exists(String uid) throws ExecutionException, InterruptedException {
        return collectionReference.document(uid).get().get().exists();
    }

    private Department snapshotToEntity(DocumentSnapshot snapshot) {
        Department department = new Department();

        department.setUid(snapshot.getId());
        department.setName(snapshot.getString("name"));
        department.setDescription(snapshot.getString("description"));
        department.setAddress(snapshot.getString("address"));
        department.setPhoneNumber(snapshot.getString("phoneNumber"));
        department.setCreationDate(LocalDateTime.ofInstant(
                Objects.requireNonNull(snapshot.getDate("creationDate")).toInstant(), ZoneId.systemDefault()));
        department.setServiceUid(snapshot.getString("serviceUid"));
        department.setCategories((List<Long>) snapshot.get("categories"));

        return department;
    }

    private Map<String, Object> entityToMap(Department department) {
        Map<String, Object> data = new HashMap<>();

        data.put("name", department.getName());
        data.put("description", department.getDescription());
        data.put("address", department.getAddress());
        data.put("phoneNumber", department.getPhoneNumber());
        data.put("creationDate", Date.from(department.getCreationDate().atZone(ZoneId.systemDefault()).toInstant()));
        data.put("categories", department.getCategories());
        data.put("serviceUid", department.getServiceUid());

        return data;
    }
}
