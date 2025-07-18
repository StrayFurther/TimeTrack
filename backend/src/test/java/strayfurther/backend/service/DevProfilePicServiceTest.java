package strayfurther.backend.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import strayfurther.backend.exception.FileStorageException;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;

import java.io.IOException;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("dev")
@SpringBootTest
class DevProfilePicServiceTest {

    @Autowired
    private DevProfilePicService devProfilePicService;

    private Path testDirectory;

    @BeforeEach
    void setUp() throws IOException {
        testDirectory = Paths.get("/tmp/test-profile-pics");
        Files.createDirectories(testDirectory);
        Files.setPosixFilePermissions(testDirectory, PosixFilePermissions.fromString("rwxrwxrwx"));
        devProfilePicService = new DevProfilePicService(testDirectory.toString());
    }



    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(testDirectory)) {
            // Reset permissions to allow deletion
            Files.setPosixFilePermissions(testDirectory, PosixFilePermissions.fromString("rwxrwxrwx"));

            Files.walk(testDirectory)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (!file.delete()) {
                            System.err.println("Failed to delete: " + file.getAbsolutePath());
                        }
                    });
        }
    }

    @Test
    void testSaveProfilePicEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        Exception exception = assertThrows(FileStorageException.class, () -> {
            devProfilePicService.saveProfilePic(file);
        });

        assertTrue(exception.getMessage().contains("Empty files are not allowed."), "Exception message should indicate save failure");
    }

    @Test
    void testSaveProfilePicSuccess() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile-pic.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        String fileName = devProfilePicService.saveProfilePic(file);

        Path savedFilePath = testDirectory.resolve(fileName);
        assertTrue(Files.exists(savedFilePath), "File should exist after saving");
        assertEquals("test content", Files.readString(savedFilePath), "File content should match");
    }

    @Test
    void testSaveProfilePicNoExtension() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile-pic",
                "image/jpeg",
                "test content".getBytes()
        );

        String fileName = devProfilePicService.saveProfilePic(file);

        Path savedFilePath = testDirectory.resolve(fileName);
        assertTrue(Files.exists(savedFilePath), "File should exist after saving");
        assertEquals("test content", Files.readString(savedFilePath), "File content should match");
    }

    @Test
    void testSaveProfilePicPermissionDenied() throws IOException {
        // Set restrictive permissions on the directory
        Files.setPosixFilePermissions(testDirectory, PosixFilePermissions.fromString("r--r--r--"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile-pic.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        // Assert that a FileStorageException is thrown
        Exception exception = assertThrows(FileStorageException.class, () -> {
            devProfilePicService.saveProfilePic(file);
        });

        // Verify the exception type without relying on system-specific messages
        assertNotNull(exception, "Exception should not be null");
    }

    @Test
    void testLoadFileAsResourceSuccess() throws IOException {
        String fileName = "test-profile-pic.jpg";
        Path filePath = Files.createFile(testDirectory.resolve(fileName));
        Files.writeString(filePath, "test content");

        Resource resource = devProfilePicService.loadFileAsResource(fileName);

        assertNotNull(resource, "Resource should not be null");
        assertTrue(resource.exists(), "Resource should exist");
        assertEquals("test content", new String(resource.getInputStream().readAllBytes()), "Resource content should match");
    }

    @Test
    void testLoadFileAsResourceInvalidPath() {
        Exception exception = assertThrows(FileStorageException.class, () -> {
            devProfilePicService.loadFileAsResource("../invalid.jpg");
        });

        assertTrue(exception.getMessage().contains("Invalid file path"), "Exception message should indicate invalid path");
    }

    @Test
    void testLoadFileAsResourceFileNotFound() {
        Exception exception = assertThrows(FileStorageException.class, () -> {
            devProfilePicService.loadFileAsResource("nonexistent-file.jpg");
        });
        System.out.println(exception.getMessage());
        assertTrue(exception.getMessage().contains("File not found"), "Exception message should indicate file not found");
    }

    @Test
    void testDeletePicSuccess() throws IOException {
        // Create a temporary directory with proper permissions
        Path directoryPath = Paths.get("/tmp/test-profile-pics");
        Files.createDirectories(directoryPath);
        Files.setPosixFilePermissions(directoryPath, PosixFilePermissions.fromString("rwxrwxrwx"));

        // Create a temporary file in the directory
        Path filePath = Files.createFile(directoryPath.resolve("test-profile-pic.jpg"));

        // Ensure the file exists
        assertTrue(Files.exists(filePath), "File should exist before deletion");

        // Delete the file
        boolean result = devProfilePicService.deletePic(filePath.getFileName().toString());

        // Verify the file is deleted
        assertFalse(Files.exists(filePath), "File should not exist after deletion");
        assertTrue(result, "Method should return true for successful deletion");
    }

    @Test
    void testDeletePicParameterFailure() {
        // Test with null file name
        Exception exceptionNull = assertThrows(FileStorageException.class, () -> {
            devProfilePicService.deletePic(null);
        });
        assertTrue(exceptionNull.getMessage().contains("File name cannot be null or empty"),
                "Exception message should indicate null or empty file name");

        // Test with empty file name
        Exception exceptionEmpty = assertThrows(FileStorageException.class, () -> {
            devProfilePicService.deletePic("");
        });
        assertTrue(exceptionEmpty.getMessage().contains("File name cannot be null or empty"),
                "Exception message should indicate null or empty file name");
    }

    // IOException wont be testsd since not reproducable in this context

    @Test
    void testDeletePicInvalidFileName() {
        // Test with null file name
        Exception exceptionNull = assertThrows(FileStorageException.class, () -> {
            devProfilePicService.deletePic(null);
        });
        assertTrue(exceptionNull.getMessage().contains("File name cannot be null or empty"),
                "Exception message should indicate null or empty file name");

        // Test with empty file name
        Exception exceptionEmpty = assertThrows(FileStorageException.class, () -> {
            devProfilePicService.deletePic("");
        });
        assertTrue(exceptionEmpty.getMessage().contains("File name cannot be null or empty"),
                "Exception message should indicate null or empty file name");
    }

    @Test
    void testDeletePicSecurityException() throws IOException {

        // Create the file first
        Path filePath = Files.createFile(testDirectory.resolve("test-profile-pic5.jpg"));

        // Set restrictive permissions on the directory
        Files.setPosixFilePermissions(testDirectory, PosixFilePermissions.fromString("r--r--r--"));

        Exception exception = assertThrows(FileStorageException.class, () -> {
            devProfilePicService.deletePic(filePath.getFileName().toString());
        });

        assertTrue(exception.getMessage().contains("I/O error occurred while deleting file"),
                "Exception message should indicate permission denied");
    }

    @Test
    void testDeleteNonExistentFile() {
        boolean result = devProfilePicService.deletePic("nonexistent-file.jpg");
        assertFalse(result, "Method should return false for non-existent file");
    }

}