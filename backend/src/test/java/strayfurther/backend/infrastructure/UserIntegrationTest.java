package strayfurther.backend.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import strayfurther.backend.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    String authenticateTestUser() throws Exception {
        // Register and login to get a token
        String registerRequest = """
        {
            "userName": "testUser",
            "email": "test@example.com",
            "password": "Password123!"
        }
        """;

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

            String loginRequest = """
        {
            "email": "test@example.com",
            "password": "Password123!"
        }
        """;

        String jsonResponse = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonResponse);
        return jsonNode.get("token").asText();
    }

    @AfterEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        String requestBody = """
            {
                "userName": "testUser",
                "email": "test@example.com",
                "password": "securePassword123-"
            }
        """;

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        assertTrue(userRepository.existsByEmail("test@example.com"));
    }

    @Test
    void shouldFailOnDuplicateEmail() throws Exception {
        String requestBody = """
            {
                "userName": "loginUser",
                "email": "duplicate@example.com",
                "password": "Encodedpass123-"
            }
        """;

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        requestBody = """
            {
                "userName": "newUser",
                "email": "duplicate@example.com",
                "password": "anotherPass123-"
            }
        """;

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldFailWithInvalidInputEmail() throws Exception {
        String requestBody = """
            {
                "userName": "invalidMailUser",
                "email": "invalidemail",
                "password": "PLainPassword123-"
            }
        """;

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWithInvalidInputPW() throws Exception {
        String requestBody = """
            {
                "userName": "invalidPWUser",
                "email": "valid@email.com",
                "password": "PLainPassword123"
            }
        """;

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBeAbleToLoginWithValidCredentials() throws Exception {
        // need to register first
        // pw wont encoded otherwise since encoding happens before saving method
        String requestBody = """
            {
                "userName": "loginUser",
                "email": "example@mail.com",
                "password": "Encodedpass123-"
            }
        """;

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());


        requestBody = """
                    {
                        "email": "example@mail.com",
                        "password": "Encodedpass123-"
                    }
                """;

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void shouldFailToLoginWithNoUserRegistered() throws Exception {
        String requestBody = """
                    {
                        "email": "user@mail.com",
                        "password": "WrongPassword123-"
                    }
                """;
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    void shouldFailWithWrongCredentialsForUser() throws Exception {
        // need to register first
        String requestBody = """
            {
                "userName": "loginUser",
                "email": "example@mail.com",
                "password": "Encodedpass123-"
            }
        """;

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());


        requestBody = """
                    {
                        "email": "example@mail.com",
                        "password": "Encodedpass123"
                    }
                """;

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRegisterAndLoginWithCaseInsensitiveEmail() throws Exception {
        // Register with uppercase email
        String registerRequest = """
        {
            "userName": "caseUser",
            "email": "CaseUser@Example.com",
            "password": "StrongPass123!"
        }
    """;

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated());

        // Login with lowercase email
        String loginRequest = """
        {
            "email": "caseuser@example.com",
            "password": "StrongPass123!"
        }
    """;

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void shouldReturnTrueIfEmailExists() throws Exception {
        // Register a user
        String requestBody = """
        {
            "userName": "existsUser",
            "email": "exists@example.com",
            "password": "Password123-"
        }
    """;

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/user/exists")
                        .param("email", "exists@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void shouldReturnFalseIfEmailDoesNotExist() throws Exception {
        mockMvc.perform(get("/user/exists").param("email", "notfound@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void shouldReturnTrueForCaseInsensitiveEmail() throws Exception {
        // Register with mixed case email
        String requestBody = """
        {
            "userName": "caseUser",
            "email": "CaseSensitive@Example.com",
            "password": "Password123-"
        }
    """;

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        // Check existence with lowercase email (URL-encoded)

        mockMvc.perform(get("/user/exists")
                        .param("email", "casesensitive@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void shouldUploadProfilePicSuccessfully() throws Exception {
        String token = authenticateTestUser();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile-pic.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/user/profile-pic")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filePath").exists());
    }

    @Test
    void shouldFailToUploadProfilePicIfFileTooLarge() throws Exception {
        String token = authenticateTestUser();

        byte[] largeFileContent = new byte[3 * 1024 * 1024]; // 3MB file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large-profile-pic.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                largeFileContent
        );

        mockMvc.perform(multipart("/user/profile-pic")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File too large"));
    }

    @Test
    void shouldFailToUploadProfilePicIfInvalidFileType() throws Exception {
        String token = authenticateTestUser();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invalid-file.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "invalid content".getBytes()
        );

        mockMvc.perform(multipart("/user/profile-pic")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid file type"));
    }

    @Test
    void shouldFailToUploadProfilePicWithoutToken() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile-pic.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/user/profile-pic")
                        .file(file))
                .andExpect(status().isForbidden()); // Expect 403 for missing token
    }

    @Test
    void shouldGetProfilePicSuccessfully() throws Exception {
        String token = authenticateTestUser();

        // Simulate a profile picture upload
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile-pic.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/user/profile-pic")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filePath").exists());

        mockMvc.perform(get("/user/profile-pic")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }

    @Test
    void shouldFailToGetProfilePicIfNotFound() throws Exception {
        String token = authenticateTestUser();

        mockMvc.perform(get("/user/profile-pic")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Profile picture not found"));
    }

    @Test
    void shouldFailToGetProfilePicWithInvalidPossiblyRealToken() throws Exception {
        // Provide a properly formatted but invalid JWT
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalidPayload.invalidSignature";

        mockMvc.perform(get("/user/profile-pic")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailToGetProfilePicWithInvalidToken() throws Exception {
        mockMvc.perform(get("/user/profile-pic")
                        .header("Authorization", "Bearer invalidToken"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailToGetProfilePicWithoutToken() throws Exception {
        mockMvc.perform(get("/user/profile-pic"))
                .andExpect(status().isForbidden()); // Expect 403 for missing token
    }

    @Test
    void shouldFailToGetProfilePicIfFileNameIsNull() throws Exception {
        String token = authenticateTestUser();

        // Simulate a user with a null profilePic
        userRepository.findAll().forEach(user -> {
            user.setProfilePic(null);
            userRepository.save(user);
        });

        mockMvc.perform(get("/user/profile-pic")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Profile picture not found"));
    }

}
