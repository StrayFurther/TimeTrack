package strayfurther.backend.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import strayfurther.backend.exception.FileStorageException;

import java.io.File;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("dev")
@SpringBootTest
class DevProfilePicServiceTest {

    private DevProfilePicService devProfilePicService;
    private Path testDirectory;

    public DevProfilePicServiceTest() throws IOException {
        // Clean up the test directory before each test
        testDirectory = Paths.get("/tmp/test-profile-pics");
        Files.createDirectories(testDirectory); // Ensure the directory exists
        Files.setPosixFilePermissions(testDirectory, PosixFilePermissions.fromString("rwxrwxrwx"));
        System.out.println("Test directory: " + testDirectory.toAbsolutePath().normalize().toString());
        devProfilePicService = new DevProfilePicService(testDirectory.toAbsolutePath().normalize().toString());
        testDirectory = devProfilePicService.getBasePath();
    }

    void tearDown() throws IOException {
        if (Files.exists(testDirectory)) {
            System.out.println("Deleting test directory: " + testDirectory.toAbsolutePath().normalize().toString());
            testDirectory.toFile().delete();
            testDirectory.getParent().toFile().delete();
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
        System.out.println("Test directory: " + testDirectory.toAbsolutePath());
        System.out.println("Service base path: " + devProfilePicService.getBasePath());
        String fileName = devProfilePicService.saveProfilePic(file);

        Path savedFilePath = testDirectory.resolve(fileName).normalize();
        System.out.println("Saved file: " + savedFilePath.toAbsolutePath().normalize().toString());
        System.out.println("Saved file path: " + Paths.get("" + devProfilePicService.getBasePath().resolve(fileName).toAbsolutePath()));
        assertTrue(Files.exists(Paths.get("" + devProfilePicService.getBasePath().resolve(fileName).toAbsolutePath())), "File should exist after saving");
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
        try (Stream<Path> paths = Files.list(devProfilePicService.getBasePath())) {
            Path matchingFile = paths
                    .filter(path -> path.getFileName().toString().equals(fileName))
                    .findFirst()
                    .orElse(null);
            if (matchingFile == null) {
                fail("File should have been saved but was not found");
            }
            System.out.println("Saved file: " + matchingFile.toAbsolutePath() + " at " + fileName);
            assertTrue(matchingFile.toString().contains(fileName), "File should exist after saving");
            Path basePath = devProfilePicService.getBasePath();
            Files.setPosixFilePermissions(basePath, PosixFilePermissions.fromString("rwxrwxrwx"));
            Files.setPosixFilePermissions(basePath.resolve(fileName), PosixFilePermissions.fromString("rwxrwxrwx"));
            String fileContent = Files.readString(matchingFile);

            assertEquals(new String(file.getBytes()), fileContent, "File content should match the uploaded content");
        } catch (IOException e) {

            System.out.println("Error reading file: " + e);
            fail("Error reading file: " + e);
        }
    }

    // test gotta go since we now make the directory writtable every time
    /*
    @Test
    void testSaveProfilePicPermissionDenied() throws IOException {
        // Set restrictive permissions on the directory
        Files.setPosixFilePermissions(devProfilePicService.getBasePath(), PosixFilePermissions.fromString("r--r--r--"));

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
    } */


    @Test
    void testLoadFileAsResourceSuccess() throws IOException {
        String fileName = UUID.randomUUID() + ".jpg";
        Path filePath = Files.createFile(Paths.get(testDirectory.toAbsolutePath().normalize().toString() + "/" + fileName));
        Files.writeString(filePath, "test content");
        System.out.println("File created at: " + filePath.toAbsolutePath());

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
        Files.createDirectories(testDirectory.toAbsolutePath());
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",                     // Name of the parameter
                "example.jpg",              // Original file name
                "image/jpeg",               // Content type
                "sample file content".getBytes() // File content as bytes
        );

        String fileName = devProfilePicService.saveProfilePic(mockFile);

        // Ensure the file exists
        try (Stream<Path> paths = Files.list(devProfilePicService.getBasePath())) {
            Path matchingFile = paths
                    .filter(path -> path.getFileName().toString().equals(fileName))
                    .findFirst()
                    .orElse(null);
            if (matchingFile == null) {
                fail("File should have been saved but was not found");
            }
            System.out.println("Saved file: " + matchingFile.toAbsolutePath() + " at " + fileName);
            assertTrue(matchingFile.toString().contains(fileName), "File should exist after saving");
            boolean result = devProfilePicService.deletePic(fileName);
            try (Stream<Path> paths2 = Files.list(devProfilePicService.getBasePath())) {
                Path matchingFile2 = paths2
                        .filter(path -> path.getFileName().toString().equals(fileName))
                        .findFirst()
                        .orElse(null);
                if (matchingFile2 != null) {
                    fail("File was not deleted successfully");
                }
                assertNull(matchingFile2, "File should not exist after deleting");
            } catch (IOException e) {
                System.out.println("Error deleting file: " + e);
                fail("Error deleting file: " + e);
            }
        } catch (IOException e) {

            System.out.println("Error reading file: " + e);
            fail("Error reading file: " + e);
        }
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
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile-pic.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        // Create the file first
        String savedFile = devProfilePicService.saveProfilePic(file);

        // Set restrictive permissions on the directory
        Files.setPosixFilePermissions(testDirectory, PosixFilePermissions.fromString("r--r--r--"));
        System.out.println("File saved: " + savedFile);
        Exception exception = assertThrows(FileStorageException.class, () -> {
            devProfilePicService.deletePic(savedFile);
        });

        assertTrue(exception.getMessage().contains("I/O error occurred while deleting file"),
                "Exception message should indicate permission denied");
    }

    @Test
    void testDeleteNonExistentFile() throws IOException {
        try {
            boolean result = devProfilePicService.deletePic("nonexistent-file.jpg");
            assertFalse(result, "Method should return false for non-existent file");
        }  catch (FileStorageException e) {
            // If the file does not exist, we expect an exception to be thrown
        } finally {
            tearDown();
        }
    }

}