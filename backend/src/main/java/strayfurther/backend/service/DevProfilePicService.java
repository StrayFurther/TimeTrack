package strayfurther.backend.service;

import org.apache.commons.io.FilenameUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.PathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import org.springframework.core.io.Resource;

@Profile("dev")
@Service
public class DevProfilePicService implements ProfilePicService {

    private final Path rootLocation = Paths.get("uploads/profile-pics");

    @Override
    public String getProfilePicPath(Long userId) {
        // Example: return the path based on userId
        return rootLocation.resolve(userId + ".jpg").toString();
    }

    @Override
    public void uploadProfilePic(Long userId, byte[] picData) {
        try {
            Files.createDirectories(rootLocation);
            Path filePath = rootLocation.resolve(userId + ".jpg");
            Files.write(filePath, picData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public String saveProfilePic(MultipartFile file) throws IOException {
        Files.createDirectories(rootLocation);
        String fileName = UUID.randomUUID() + "." + FilenameUtils.getExtension(file.getOriginalFilename());
        Path filePath = rootLocation.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return filePath.toString();
    }

    @Override
    public Resource loadFileAsResource(String filePath) throws IOException {
        Path path = rootLocation.resolve(Paths.get(filePath)).normalize();
        if (!path.startsWith(rootLocation)) {
            throw new IOException("Invalid file path: " + filePath);
        }
        if (Files.exists(path)) {
            return new PathResource(path);
        } else {
            throw new IOException("File not found: " + filePath);
        }
    }
}
