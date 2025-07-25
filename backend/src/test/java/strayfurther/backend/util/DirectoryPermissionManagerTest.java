package strayfurther.backend.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DirectoryPermissionManagerTest {

    private Path tempDirectory;

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary directory
        Path rootLocation = Paths.get(System.getProperty("user.dir"));
        tempDirectory = Paths.get(rootLocation.toAbsolutePath() + "/testDir");
        if (!Files.exists(tempDirectory)) {
            Files.createDirectory(tempDirectory.toAbsolutePath());
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        // Delete the temporary directory
        Set<PosixFilePermission> resetPermissions = PosixFilePermissions.fromString("rwxrwxrwx");
        Files.setPosixFilePermissions(tempDirectory, resetPermissions);

        List<Path> pathsToDelete;
        try (var paths = Files.walk(tempDirectory)) {
            pathsToDelete = paths.sorted((path1, path2) -> path2.getNameCount() - path1.getNameCount())
                    .toList();
        }

        // Delete each path
        for (Path path : pathsToDelete) {
            try {
                if (Files.exists(path)) {
                    Files.setPosixFilePermissions(path, resetPermissions);
                    Files.delete(path);
                }
            } catch (IOException e) {
                System.err.println("Failed to delete: " + path + " - " + e.getMessage());
            }
        }
    }

    @Test
    void testWriteWithPermissionChange() throws IOException {
        // Arrange
        Set<PosixFilePermission> initialPermissions = PosixFilePermissions.fromString("r--r--r--");
        Files.setPosixFilePermissions(tempDirectory, initialPermissions);

        // Act
        DirectoryPermissionManager.writeWithPermissionChange(tempDirectory, () -> {
            try {
                Path file = tempDirectory.resolve("testFile.txt");
                Files.writeString(file, "Test content");
                assertTrue(Files.exists(file), "File should be created during the write operation");
            } catch (IOException e) {
                fail("Write operation failed: " + e.getMessage());
            }
        });

        // Assert
        Set<PosixFilePermission> restoredPermissions = Files.getPosixFilePermissions(tempDirectory);
        assertEquals(initialPermissions, restoredPermissions, "Permissions should be restored to the original state");
    }

    @Test
    void testWithNestedDirectories() throws IOException {
        Set<PosixFilePermission> resetPermissions = PosixFilePermissions.fromString("rwxrwxrwx");
        Files.setPosixFilePermissions(tempDirectory, resetPermissions);
        Path subDir = tempDirectory.resolve("subDir");
        Files.createDirectory(subDir);
        Path fileInSubDir = subDir.resolve("nestedFile.txt");
        Files.createFile(fileInSubDir);
        resetPermissions = PosixFilePermissions.fromString("r--r--r--");
        Files.setPosixFilePermissions(tempDirectory, resetPermissions);

        DirectoryPermissionManager.writeWithPermissionChange(tempDirectory, () -> {
            try {
                Files.writeString(fileInSubDir, "Nested content");
                assertTrue(Files.exists(fileInSubDir), "File in subdirectory should exist");
            } catch (IOException e) {
                fail("Write operation in nested directory failed: " + e.getMessage());
            }
        });
    }

}