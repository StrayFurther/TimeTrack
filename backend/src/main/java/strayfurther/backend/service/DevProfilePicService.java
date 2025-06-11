package strayfurther.backend.service;

import org.apache.commons.io.FilenameUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.PathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import strayfurther.backend.exception.FileStorageException;

import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;
import java.util.UUID;
import org.springframework.core.io.Resource;
import strayfurther.backend.model.User;

@Profile("dev")
@Service
public class DevProfilePicService implements ProfilePicService {

    private final Path rootLocation = Paths.get("uploads/profile-pics");

    @Override
    public String getProfilePicPath(String fileName) {
        return rootLocation.resolve(fileName).toString();
    }

    @Override
    public String saveProfilePic(MultipartFile file) {
        try {
            Files.createDirectories(rootLocation);
            String fileName = UUID.randomUUID() + "." + FilenameUtils.getExtension(file.getOriginalFilename());
            Path filePath = rootLocation.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toString();
        } catch (IOException e) {
            throw new FileStorageException("Failed to save file", e);
        }
    }

    @Override
    public Resource loadFileAsResource(String filePath) throws FileStorageException {
        try {
            Path path = rootLocation.resolve(Paths.get(filePath)).normalize();
            if (!path.startsWith(rootLocation)) {
                throw new FileStorageException("Invalid file path: " + filePath);
            }
            if (Files.exists(path)) {
                return new PathResource(path);
            } else {
                throw new FileStorageException("File not found: " + filePath);
            }
        } catch (FileStorageException e) {
            throw new FileStorageException("Error loading file: " + filePath, e);
        }
    }
}