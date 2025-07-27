package strayfurther.backend.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class DirectoryInitializer {

    private final Path rootLocation;
    private final Path uploadDirectory;

    public DirectoryInitializer(@Value("${profile.pics.location}") String desiredUploadDirectory) {
        this.rootLocation = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        this.uploadDirectory = Paths.get(System.getProperty("user.dir") + "/" + desiredUploadDirectory).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void initializeDirectory() {
        try {
            DirectoryPermissionManager.writeWithPermissionChange(rootLocation, () -> {
                if (!Files.exists(uploadDirectory)) {
                    try {
                        Files.createDirectories(uploadDirectory);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Created directory: " + uploadDirectory);
                } else {
                    System.out.println("Directory already exists: " + uploadDirectory);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize directory: " + rootLocation, e);
        }
    }
}