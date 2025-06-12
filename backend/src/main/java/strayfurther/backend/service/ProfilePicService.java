package strayfurther.backend.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import strayfurther.backend.exception.FileStorageException;

public interface ProfilePicService {
    String getProfilePicPath(String fileName);
    String saveProfilePic(MultipartFile file) throws FileStorageException;
    Resource loadFileAsResource(String filePath) throws FileStorageException;
    boolean deletePic(String fileName) throws FileStorageException;
}


