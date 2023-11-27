package dev.mikita.issueservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import java.io.IOException;
import java.io.InputStream;

/**
 * The type Firebase config.
 */
@Configuration
public class FirebaseConfig {
    /**
     * Firebase app.
     *
     * @return the firebase app
     * @throws IOException the io exception
     */
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        Resource resource = new ClassPathResource("keys/firebase-service-account-key.json");
        InputStream inputStream = resource.getInputStream();
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(inputStream))
                .setStorageBucket("ctu-nss.appspot.com")
                .build();
        return FirebaseApp.initializeApp(options);
    }

    /**
     * Firebase auth.
     *
     * @param firebaseApp the firebase app
     * @return the firebase auth
     */
    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }

    /**
     * Firestore firestore.
     *
     * @param firebaseApp the firebase app
     * @return the firestore
     */
    @Bean
    public Firestore firestore(FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore(firebaseApp);
    }

    /**
     * Firebase storage storage client.
     *
     * @param firebaseApp the firebase app
     * @return the storage client
     */
    @Bean
    public StorageClient firebaseStorage(FirebaseApp firebaseApp) {
        return StorageClient.getInstance(firebaseApp);
    }
}
