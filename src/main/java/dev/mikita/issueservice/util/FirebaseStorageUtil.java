package dev.mikita.issueservice.util;

import com.google.cloud.storage.Blob;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class FirebaseStorageUtil {
    private final StorageClient firebaseStorage;
    private final String BASE_URL;

    @Autowired
    public FirebaseStorageUtil(StorageClient firebaseStorage) {
        this.firebaseStorage = firebaseStorage;
        BASE_URL = "https://firebasestorage.googleapis.com/v0/b/" + firebaseStorage.bucket().getName() + "/o/";
    }

    public void deleteFile(String filePath) {
        Blob blob = firebaseStorage.bucket().get(URLDecoder.decode(filePath, StandardCharsets.UTF_8));
        if (blob != null) blob.delete();
    }

    public String parseFileName(String url) {
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    public String uploadImage(MultipartFile file, String dirPath) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("File name is null.");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IOException("File size is too large.");
        }

        String fileMimeType = file.getContentType();
        if (fileMimeType == null) {
            throw new IOException("File type is null.");
        }

        String fileExtension = switch (fileMimeType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            default -> throw new IOException("File type is not supported.");
        };

        String fileName = UUID.randomUUID() + fileExtension;
        String filePath = dirPath + fileName;
        Blob blob = firebaseStorage.bucket().create(filePath, file.getBytes(), fileMimeType);
        return BASE_URL + URLEncoder.encode(blob.getName(), StandardCharsets.UTF_8);
    }
}
