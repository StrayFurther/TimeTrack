package strayfurther.backend.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class DirectoryPermissionManager {

    public static void writeWithPermissionChange(Path directory, Runnable writeOperation) throws IOException {
        // Save current permissions
        Set<PosixFilePermission> originalPermissions = Files.getPosixFilePermissions(directory.toAbsolutePath());

        try {
            // Change permissions to writable for all
            Set<PosixFilePermission> writablePermissions = PosixFilePermissions.fromString("rwxrwxrwx");
            Files.setPosixFilePermissions(directory, writablePermissions);

            // Perform the write operation
            writeOperation.run();
        } finally {
            // Restore original permissions
            Files.setPosixFilePermissions(directory, originalPermissions);
        }
    }
}