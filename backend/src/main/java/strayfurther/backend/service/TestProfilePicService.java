package strayfurther.backend.service;

import org.apache.commons.io.FilenameUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import strayfurther.backend.exception.FileStorageException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Profile("test")
@Service
public class TestProfilePicService implements ProfilePicService {

    private final Map<String, byte[]> inMemoryStorage = new HashMap<>();

    @Override
    public String getProfilePicPath(String fileName) {
        // Return a virtual path for testing purposes
        return "in-memory://" + fileName;
    }

    @Override
    public String saveProfilePic(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "." + FilenameUtils.getExtension(file.getOriginalFilename());
            inMemoryStorage.put(fileName, file.getBytes());
            return fileName;
        } catch (IOException e) {
            throw new FileStorageException("Failed to save file in memory", e);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName) throws FileStorageException {
        byte[] fileContent = inMemoryStorage.get(fileName);
        if (fileContent == null) {
            throw new FileStorageException("File not found: " + fileName);
        }
        return new ByteArrayResource(fileContent);
    }

    @Override
    public boolean deletePic(String fileName) throws FileStorageException {
        if (inMemoryStorage.containsKey(fileName)) {
            inMemoryStorage.remove(fileName);
            return true;
        }
        return false;
    }
}