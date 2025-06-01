package strayfurther.backend.service;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;
import software.amazon.awssdk.core.sync.RequestBody;

@Profile("prod")
@Service
public class ProdProfilePicService implements ProfilePicService {

    private final S3Client s3Client;
    private final String bucketName = "your-s3-bucket-name";

    public ProdProfilePicService() {
        this.s3Client = S3Client.builder()
                .region(Region.EU_CENTRAL_1) // Replace with your region
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();
    }

    @Override
    public String getProfilePicPath(Long userId) {
        return String.format("https://%s.s3.amazonaws.com/%d.jpg", bucketName, userId);
    }

    @Override
    public void uploadProfilePic(Long userId, byte[] picData) {
        String key = userId + ".jpg";
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build(),
                    Paths.get(key)
            );
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    @Override
    public String saveProfilePic(MultipartFile file) throws IOException {
        String key = UUID.randomUUID() + "-" + file.getOriginalFilename();
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
    }

    @Override
    public Resource loadFileAsResource(String filePath) throws IOException {
        // filePath should be the S3 key (e.g., "abc123.jpg")
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build();
            return new InputStreamResource(s3Client.getObject(getObjectRequest));
        } catch (NoSuchKeyException e) {
            throw new IOException("File not found in S3: " + filePath, e);
        } catch (S3Exception e) {
            throw new IOException("Failed to load file from S3: " + filePath, e);
        }
    }
}