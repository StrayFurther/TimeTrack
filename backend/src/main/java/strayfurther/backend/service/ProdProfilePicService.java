package strayfurther.backend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import strayfurther.backend.exception.FileStorageException;

import java.io.IOException;
import java.util.UUID;

@Profile("prod")
@Service
public class ProdProfilePicService implements ProfilePicService {

    private final AmazonS3 s3Client;
    private final String bucketName = "your-s3-bucket-name";

    public ProdProfilePicService(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String getProfilePicPath(String fileName) {
        return "profile-pics/" + fileName;
    }

    @Override
    public String saveProfilePic(MultipartFile file) {
        try {
            String fileName = "profile-pics/" + UUID.randomUUID() + "." + file.getOriginalFilename();
            s3Client.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), null)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            return s3Client.getUrl(bucketName, fileName).toString();
        } catch (IOException e) {
            throw new FileStorageException("Failed to save file to S3", e);
        }
    }

    @Override
    public Resource loadFileAsResource(String filePath) {
        try {
            S3Object s3Object = s3Client.getObject(bucketName, filePath);
            return new InputStreamResource(s3Object.getObjectContent());
        } catch (Exception e) {
            throw new FileStorageException("Failed to load file from S3", e);
        }
    }

    @Override
    public boolean deletePic(String fileName) throws FileStorageException {
        try {
            String filePath = getProfilePicPath(fileName);
            s3Client.deleteObject(bucketName, filePath);
            return true;
        } catch (Exception e) {
            throw new FileStorageException("Failed to delete file from S3: " + fileName, e);
        }
    }
}