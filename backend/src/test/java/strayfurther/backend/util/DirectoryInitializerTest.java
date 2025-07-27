package strayfurther.backend.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(classes = DirectoryInitializer.class)
class DirectoryInitializerTest {

    private DirectoryInitializer directoryInitializer;
    private final String mockUploadDir = "testUploads";
    @Autowired
    private ApplicationContext context;

    @BeforeEach
    void setUp() throws IOException {
        directoryInitializer = new DirectoryInitializer(mockUploadDir);

        Path mockPath = Path.of(System.getProperty("user.dir"), mockUploadDir);
        if (Files.exists(mockPath)) {
            Files.walk(mockPath)
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (!file.delete()) {
                            throw new RuntimeException("Failed to delete file: " + file.getAbsolutePath());
                        }
                    });
            Files.deleteIfExists(mockPath);
        }
    }

    @Test
    void testInitializeDirectory_CreatesDirectoryIfNotExists() throws IOException {
        Path mockPath = Path.of(System.getProperty("user.dir"), mockUploadDir);

        assertFalse(Files.exists(mockPath), "Directory should not exist before initialization");

        directoryInitializer.initializeDirectory();

        assertTrue(Files.exists(mockPath), "Directory should exist after initialization");
    }

    @Test
    void testInitializeDirectory_DoesNotCreateDirectoryIfExists() throws IOException {
        Path mockPath = Path.of(System.getProperty("user.dir"), mockUploadDir);
        Files.createDirectories(mockPath);

        assertTrue(Files.exists(mockPath), "Directory should exist before initialization");

        directoryInitializer.initializeDirectory();

        assertTrue(Files.exists(mockPath), "Directory should still exist after initialization");
    }

    @Test
    void testInitializeDirectory_ThrowsRuntimeExceptionOnFailure() {
        Path mockPath = Path.of("mockDirectory");

        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            // Mock Files.setPosixFilePermissions to throw an IOException
            mockedFiles.when(() -> Files.setPosixFilePermissions(Mockito.any(), Mockito.any())).thenThrow(new IOException("Mocked IOException"));

            DirectoryInitializer directoryInitializer = new DirectoryInitializer(mockPath.toString());

            // Assert that a RuntimeException is thrown
            RuntimeException exception = assertThrows(RuntimeException.class, directoryInitializer::initializeDirectory);
            assertTrue(exception.getMessage().contains("Failed to initialize directory"));
        }
    }
}