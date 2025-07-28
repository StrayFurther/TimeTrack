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
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import strayfurther.backend.util.DirectoryInitializer;
import strayfurther.backend.util.DirectoryPermissionManager;

@Profile("dev")
@Service
public class DevProfilePicService implements ProfilePicService {

    private final Path directory;

    private final Set<PosixFilePermission> initalPermissions;

    public DevProfilePicService(@Value("${profile.pics.location:uploads/profile-pics}") String uploadLocation) throws IOException {
        Path rootLocation = Paths.get(System.getProperty("user.dir"));
        this.directory = Paths.get(rootLocation.toAbsolutePath() + "/" + uploadLocation);
        initalPermissions = Files.getPosixFilePermissions(directory);
    }

    public void changePermissions(Set<PosixFilePermission> permissions) {
        try {
            Files.setPosixFilePermissions(directory, permissions);
        } catch (IOException e) {
            throw new FileStorageException("Failed to change permissions for directory: " + directory, e);
        }
    }

    @Override
    public String getProfilePicPath(String fileName) {
        return directory.resolve(fileName).toString();
    }

    @Override
    public String saveProfilePic(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("Empty files are not allowed.");
        }
        try {
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }
            if (!Files.isWritable(directory)) {
                changePermissions(PosixFilePermissions.fromString("rwxrwxrwx"));
            }

            String fileName = UUID.randomUUID() + "." + FilenameUtils.getExtension(file.getOriginalFilename());
            Path filePath = directory.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            if (!Files.getPosixFilePermissions(directory).equals(initalPermissions)) {
                Files.setPosixFilePermissions(filePath, initalPermissions);
            }
            return fileName;
        } catch (IOException e) {
            throw new FileStorageException("Error saving file:", e);
        } catch (SecurityException e) {
            throw new FileStorageException("Permission denied:", e);
        } catch (FileStorageException e) {
            throw new FileStorageException("Earlier Error:", e);
        } catch (Exception e) {
            throw new FileStorageException("Failed to save file due to an unexpected error.", e);
        } finally {
            try {
                Files.setPosixFilePermissions(directory, initalPermissions);
            } catch (IOException e) {
                throw new FileStorageException("Failed to reset permissions for directory: " + directory, e);
            }
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName) throws FileStorageException {
        System.out.println("Loading file: " + fileName);
        try {
            if (fileName == null || fileName.isEmpty()) {
                throw new FileStorageException("Invalid file path: " + fileName);
            }
            if (isFileSaved(fileName)) {
                System.out.println("File exists: " + fileName);
                return new PathResource(directory.resolve(Paths.get(fileName)).normalize());
            } else {
                System.out.println("File does not exist: " + fileName);
                throw new FileStorageException("File does not exist: " + fileName);
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
        if (!isFileSaved(fileName)) {
            throw new FileStorageException("File does not exist: " + fileName);
        }
        // catching not empty directory would be never happen since we are only deleting files
        try {
            Path fileToDelete = Paths.get(getProfilePicPath(fileName));
            DirectoryPermissionManager.writeWithPermissionChange(directory, () -> {
               try {
                   Files.deleteIfExists(fileToDelete);
               } catch (Exception e) {
                   throw new FileStorageException("Failed to delete file: " + fileName, e);
               }
            });
            return !isFileSaved(fileName);
        } catch (IOException e) {
            throw new FileStorageException("I/O error occurred while deleting file: " + fileName, e);
        } catch (SecurityException e) {
            throw new FileStorageException("Permission denied while deleting file: " + fileName, e);
        } catch (Exception e) {
            throw new FileStorageException("Unexpected error occurred while deleting file: " + fileName, e);
        }
    }

    public Path getBasePath() {
        return directory;
    }

    @Override
    public boolean isFileSaved(String fileName) throws FileStorageException {
        if (fileName == null || fileName.isEmpty()) {
            throw new FileStorageException("File name cannot be null or empty");
        }
        try (Stream<Path> files = Files.list(directory)) {
            return files.anyMatch(path -> path.getFileName().toString().equals(fileName));
        } catch (IOException e) {
            throw new FileStorageException("Error checking if file is saved: " + fileName, e);
        }
    }
}