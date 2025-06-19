package strayfurther.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import strayfurther.backend.dto.LoginRequestDTO;
import strayfurther.backend.dto.UserRequestDTO;
import strayfurther.backend.exception.EmailAlreadyUsedException;
import strayfurther.backend.exception.FileStorageException;
import strayfurther.backend.model.User;
import strayfurther.backend.repository.UserRepository;
import strayfurther.backend.util.JwtUtil;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll(); // Clean up the database before each test
    }

    @Test
    void testRegisterUserSuccess() {
        UserRequestDTO userRequest = UserRequestDTO.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        userService.registerUser(userRequest);

        User savedUser = userRepository.findByEmail("test@example.com").orElse(null);
        assertNotNull(savedUser);
        assertEquals("testuser", savedUser.getUserName());
    }

    @Test
    void testRegisterUserEmailAlreadyUsed() {
        UserRequestDTO userRequest = UserRequestDTO.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        userService.registerUser(userRequest);

        assertThrows(EmailAlreadyUsedException.class, () -> userService.registerUser(userRequest));
    }

    @Test
    void testLoginUserSuccess() {
        UserRequestDTO userRequest = UserRequestDTO.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
        userService.registerUser(userRequest);

        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        String token = userService.loginUser(loginRequest);

        assertNotNull(token);
    }

    @Test
    void testLoginUserWithInvalidCredentials() {
        // Arrange
        UserRequestDTO userRequest = UserRequestDTO.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
        userService.registerUser(userRequest);

        // Act & Assert: Wrong password
        LoginRequestDTO wrongPasswordRequest = LoginRequestDTO.builder()
                .email("test@example.com")
                .password("wrongpassword")
                .build();
        assertNull(userService.loginUser(wrongPasswordRequest));

        // Act & Assert: Non-existent user
        LoginRequestDTO nonExistentUserRequest = LoginRequestDTO.builder()
                .email("nonexistent@example.com")
                .password("password123")
                .build();
        assertNull(userService.loginUser(nonExistentUserRequest));
    }

    @Test
    void testGetUserProfilePicSuccess() throws FileStorageException {
        UserRequestDTO userRequest = UserRequestDTO.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
        userService.registerUser(userRequest);

        MultipartFile file = new MockMultipartFile("file", "profile-pic.jpg", "image/jpeg", "test content".getBytes());
        String token = userService.loginUser(LoginRequestDTO.builder()
                .email("test@example.com")
                .password("password123")
                .build());
        userService.uploadProfilePic(token, file);

        Resource resource = userService.getUserProfilePic(token);

        assertNotNull(resource);
    }

    @Test
    void testUploadProfilePicWithExistingPic() throws FileStorageException {
        // Step 1: Register a user
        UserRequestDTO userRequest = UserRequestDTO.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
        userService.registerUser(userRequest);

        // Step 2: Log in the user to get a token
        String token = userService.loginUser(LoginRequestDTO.builder()
                .email("test@example.com")
                .password("password123")
                .build());

        // Step 3: Set an existing profile picture for the user
        User user = userRepository.findByEmail("test@example.com").orElseThrow();
        String oldFileName = userService.uploadProfilePic(token, new MockMultipartFile("file", "old-pic.jpg", "image/jpeg", "test content".getBytes()));

        // Step 4: Upload a new profile picture
        MultipartFile newFile = new MockMultipartFile("file", "new-pic.jpg", "image/jpeg", "test content".getBytes());
        String newFileName = userService.uploadProfilePic(token, newFile);

        // Assertions
        assertNotNull(newFileName);
        assertNotEquals(oldFileName, newFileName);
        User updatedUser = userRepository.findByEmail("test@example.com").orElseThrow();
        assertEquals(newFileName, updatedUser.getProfilePic());
    }

    @Test
    void testGetUserFromTokenWithInvalidOrExpiredToken() {
        // Arrange
        String invalidToken = "invalid_or_expired_token";

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getUserFromToken(invalidToken));
        assertEquals("Invalid or expired token", exception.getMessage());
    }

    @Test
    void testGetUserFromTokenWithExpiredToken() {
        // Arrange
        String email = "test@example.com";
        String token = jwtUtil.generateToken(email); // Generate a valid token

        // Simulate time forwarding by changing the system time zone
        String originalTimeZone = System.getProperty("user.timezone");
        System.setProperty("user.timezone", "GMT+2");
        TimeZone.setDefault(null); // Refresh the default time zone

        try {
            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getUserFromToken(token));
            assertEquals("Invalid or expired token", exception.getMessage());
        } finally {
            // Reset the time zone to the original
            System.setProperty("user.timezone", originalTimeZone);
            TimeZone.setDefault(null); // Refresh the default time zone
        }
    }

}