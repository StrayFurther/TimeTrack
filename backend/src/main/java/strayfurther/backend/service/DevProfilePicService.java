package strayfurther.backend.service;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.PathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import strayfurther.backend.exception.FileStorageException;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import org.springframework.core.io.Resource;

@Profile("dev")
@Service
public class DevProfilePicService implements ProfilePicService {

    private final Path rootLocation;

    public DevProfilePicService(@Value("${profile.pics.location:uploads/profile-pics}") String rootLocation) {
        this.rootLocation = Paths.get(rootLocation).toAbsolutePath().normalize();
    }

    @Override
    public String getProfilePicPath(String fileName) {
        return rootLocation.resolve(fileName).toString();
    }

    @Override
    public String saveProfilePic(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("Empty files are not allowed.");
        }
        try {
            Files.createDirectories(rootLocation);
            String fileName = UUID.randomUUID() + "." + FilenameUtils.getExtension(file.getOriginalFilename());
            Path filePath = rootLocation.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new FileStorageException("Error saving file:", e);
        } catch (SecurityException e) {
            throw new FileStorageException("Permission denied:", e);
        } catch (Exception e) {
            throw new FileStorageException("Failed to save file due to an unexpected error.", e);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName) throws FileStorageException {
        try {
            Path path = rootLocation.resolve(Paths.get(fileName)).normalize();
            if (!path.startsWith(rootLocation)) {
                throw new FileStorageException("Invalid file path: " + fileName);
            }
            if (Files.exists(path)) {
                return new PathResource(path);
            } else {
                throw new FileStorageException("File not found: " + fileName);
            }
        } catch (FileStorageException e) {
            throw e; // Preserve specific error messages
        } catch (Exception e) {
            throw new FileStorageException("Error loading file: " + fileName + "\n " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deletePic(String fileName) throws FileStorageException {
        if (fileName == null || fileName.isEmpty()) {
            throw new FileStorageException("File name cannot be null or empty");
        }
        // catching not empty directory would be never happen since we are only deleting files
        try {
            return Files.deleteIfExists(Paths.get(getProfilePicPath(fileName)));
        } catch (IOException e) {
            throw new FileStorageException("I/O error occurred while deleting file: " + fileName, e);
        } catch (SecurityException e) {
            throw new FileStorageException("Permission denied while deleting file: " + fileName, e);
        } catch (Exception e) {
            throw new FileStorageException("Unexpected error occurred while deleting file: " + fileName, e);
        }
    }
}