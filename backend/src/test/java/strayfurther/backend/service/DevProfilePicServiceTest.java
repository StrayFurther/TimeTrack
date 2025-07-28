package strayfurther.backend.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import strayfurther.backend.exception.FileStorageException;

import java.io.File;
import java.nio.file.attribute.PosixFilePermissions;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

import strayfurther.backend.util.DirectoryInitializer;

@ActiveProfiles("dev")
@SpringBootTest(properties = {
        "profile.pics.location=/tmp/test-profile-pics",
}, classes = {strayfurther.backend.BackendApplication.class})
class DevProfilePicServiceTest {

    private static final String TEST_PROFILE_PICS_LOCATION = "/tmp/test-profile-pics";

    private DevProfilePicService devProfilePicService;
    private Path testDirectory;

    public DevProfilePicServiceTest() throws IOException {
        // Clean up the test directory before each test
        (new DirectoryInitializer(TEST_PROFILE_PICS_LOCATION)).initializeDirectory();
        testDirectory = Paths.get("/tmp/test-profile-pics");
        Files.createDirectories(testDirectory); // Ensure the directory exists
        Files.setPosixFilePermissions(testDirectory, PosixFilePermissions.fromString("rwxrwxrwx"));
        devProfilePicService = new DevProfilePicService(testDirectory.toAbsolutePath().normalize().toString());
        testDirectory = devProfilePicService.getBasePath();
    }

    public static boolean deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file); // Recursively delete contents
                }
            }
        }
        return directory.delete(); // Delete the empty directory or file
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(devProfilePicService.getBasePath())) {
            deleteDirectory(devProfilePicService.getBasePath().toFile());
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

        Path savedFilePath = testDirectory.resolve(fileName).normalize();
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
            assertTrue(matchingFile.toString().contains(fileName), "File should exist after saving");
            Path basePath = devProfilePicService.getBasePath();
            Files.setPosixFilePermissions(basePath, PosixFilePermissions.fromString("rwxrwxrwx"));
            Files.setPosixFilePermissions(basePath.resolve(fileName), PosixFilePermissions.fromString("rwxrwxrwx"));
            String fileContent = Files.readString(matchingFile);

            assertEquals(new String(file.getBytes()), fileContent, "File content should match the uploaded content");
        } catch (IOException e) {

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

        Resource resource = devProfilePicService.loadFileAsResource(fileName);

        assertNotNull(resource, "Resource should not be null");
        assertTrue(resource.exists(), "Resource should exist");
        assertEquals("test content", new String(resource.getInputStream().readAllBytes()), "Resource content should match");
    }

    @Test
    void testLoadFileAsResourceInvalidPath() {
        Exception exception = assertThrows(FileStorageException.class, () -> {
            System.out.println(devProfilePicService.loadFileAsResource("../invalid22.jpg"));
        });

        assertTrue(exception.getMessage().contains("File does not exist: ../invalid22.jpg"), "Exception message should indicate invalid path");
    }

    @Test
    void testLoadFileAsResourceFileNotFound() {
        Exception exception = assertThrows(FileStorageException.class, () -> {
            devProfilePicService.loadFileAsResource("nonexistent-file.jpg");
        });
        System.out.println("Exception message: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("File does not exist"), "Exception message should indicate file not found");
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
            assertTrue(matchingFile.toString().contains(fileName), "File should exist after saving");
            boolean result = devProfilePicService.deletePic(fileName);
            System.out.println("DID DELETE: " + result);
            assertTrue(result, "Did delete");
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
                fail("Error deleting file: " + e);
            }
        } catch (IOException e) {
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

    /* Test will fail since the permissions for directory will be adjusted in the deletePic method
     which means that i cannot reproduce the permission denied error
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
        Exception exception = assertThrows(FileStorageException.class, () -> {
            devProfilePicService.deletePic(savedFile);
        });

        assertTrue(exception.getMessage().contains("I/O error occurred while deleting file"),
                "Exception message should indicate permission denied");
    }
     */

    @Test
    void testDeleteNonExistentFile() throws IOException {
        Exception exception = assertThrows(FileStorageException.class, () -> {
            devProfilePicService.deletePic("nonexistent-file22.jpg");
        });

        assertTrue(exception.getMessage().contains("File does not exist"),
                "Exception message should indicate permission denied");
    }

}