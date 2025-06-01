package strayfurther.backend.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProfilePicService {
    // Define methods for profile picture service
    public String getProfilePicPath(Long userId);
    public void uploadProfilePic(Long userId, byte[] picData);
    public String saveProfilePic(MultipartFile file) throws IOException;
    public Resource loadFileAsResource(String filePath) throws IOException;
}


