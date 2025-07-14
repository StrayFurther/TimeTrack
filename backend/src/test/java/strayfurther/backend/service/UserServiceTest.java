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
import strayfurther.backend.exception.JwtUtilException;
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

        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        String token = userService.loginUser(loginRequest, userAgent);

        assertNotNull(token);
    }


    @Test
    void testLoginUserWithInvalidCredentials() {
        UserRequestDTO userRequest = UserRequestDTO.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
        userService.registerUser(userRequest);

        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

        // Wrong password
        LoginRequestDTO wrongPasswordRequest = LoginRequestDTO.builder()
                .email("test@example.com")
                .password("wrongpassword")
                .build();
        assertNull(userService.loginUser(wrongPasswordRequest, userAgent));

        // Non-existent user
        LoginRequestDTO nonExistentUserRequest = LoginRequestDTO.builder()
                .email("nonexistent@example.com")
                .password("password123")
                .build();
        assertNull(userService.loginUser(nonExistentUserRequest, userAgent));
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
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        String token = userService.loginUser(LoginRequestDTO.builder()
                .email("test@example.com")
                .password("password123")
                .build(), userAgent);
        userService.uploadProfilePic(token, file);

        Resource resource = userService.getUserProfilePic(token);

        assertNotNull(resource);
    }

    @Test
    void testUploadProfilePicWithExistingPic() throws FileStorageException {
        UserRequestDTO userRequest = UserRequestDTO.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("password123")
                .build();
        userService.registerUser(userRequest);

        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        String token = userService.loginUser(LoginRequestDTO.builder()
                .email("test@example.com")
                .password("password123")
                .build(), userAgent);

        String oldFileName = userService.uploadProfilePic(token, new MockMultipartFile("file", "old-pic.jpg", "image/jpeg", "test content".getBytes()));

        MultipartFile newFile = new MockMultipartFile("file", "new-pic.jpg", "image/jpeg", "test content".getBytes());
        String newFileName = userService.uploadProfilePic(token, newFile);

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
        assertTrue(exception.getMessage().contains("User not found or invalid token: JWT strings must contain exactly 2 period characters"));
    }

    @Test
    void testLoginUserWithMissingOrWrongUserAgent() {
        String userEmail = "test@example.com";
        String userPasword = "password123";
        UserRequestDTO userRequest = UserRequestDTO.builder()
                .userName("testuser")
                .email(userEmail)
                .password(userPasword)
                .build();
        userService.registerUser(userRequest);

        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .email(userEmail)
                .password(userPasword)
                .build();

        // Test with empty or missing User-Agent
        JwtUtilException missingUserAgentException = assertThrows(JwtUtilException.class, () ->
                userService.loginUser(loginRequest, null)
        );
        assertEquals("userAgent cannot be null or empty", missingUserAgentException.getMessage());

        missingUserAgentException = assertThrows(JwtUtilException.class, () ->
                userService.loginUser(loginRequest, "")
        );
        assertEquals("userAgent cannot be null or empty", missingUserAgentException.getMessage());

        // Test with incorrect User-Agent
        String incorrectUserAgent = "InvalidUserAgent";
        String token = userService.loginUser(loginRequest, "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        assertEquals(false, jwtUtil.isTokenValid(token, userEmail, incorrectUserAgent));
    }

    @Test
    void testGetUserFromTokenWithExpiredToken() {
        // Arrange
        String email = "test@example.com";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        String token = jwtUtil.generateToken(email, userAgent); // Generate a valid token

        // Simulate time forwarding by changing the system time zone
        String originalTimeZone = System.getProperty("user.timezone");
        System.setProperty("user.timezone", "GMT+2");
        TimeZone.setDefault(null); // Refresh the default time zone

        try {
            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getUserFromToken(token));
            assertEquals("User not found or invalid token: User not found with email: test@example.com", exception.getMessage());
        } finally {
            // Reset the time zone to the original
            System.setProperty("user.timezone", originalTimeZone);
            TimeZone.setDefault(null); // Refresh the default time zone
        }
    }

}