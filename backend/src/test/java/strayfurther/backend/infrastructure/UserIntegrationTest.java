package strayfurther.backend.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import strayfurther.backend.repository.UserRepository;
import strayfurther.backend.util.TestUtil;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestUtil testUtil;

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
                        .content(registerRequest)
                        .headers(testUtil.generateHeaders()))
                .andExpect(status().isCreated());

            String loginRequest = """
        {
            "email": "test@example.com",
            "password": "Password123!"
        }
        """;

        String jsonResponse = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest)
                        .headers(testUtil.generateHeaders()))
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
                        .content(requestBody)
                        .headers(testUtil.generateHeaders()))
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
                        .content(requestBody)
                        .headers(testUtil.generateHeaders()))
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
                        .content(requestBody)
                        .headers(testUtil.generateHeaders()))
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
                        .content(requestBody)
                        .headers(testUtil.generateHeaders()))
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
                        .content(requestBody)
                        .headers(testUtil.generateHeaders()))
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
                        .content(requestBody)
                        .headers(testUtil.generateHeaders()))
                .andExpect(status().isCreated());


        requestBody = """
                    {
                        "email": "example@mail.com",
                        "password": "Encodedpass123-"
                    }
                """;

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .headers(testUtil.generateHeaders()))
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
                        .content(requestBody)
                        .headers(testUtil.generateHeaders()))
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
                        .content(requestBody)
                        .headers(testUtil.generateHeaders()))
                .andExpect(status().isCreated());


        requestBody = """
                    {
                        "email": "example@mail.com",
                        "password": "Encodedpass123"
                    }
                """;

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .headers(testUtil.generateHeaders()))
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
                        .content(registerRequest)
                        .headers(testUtil.generateHeaders()))
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
                        .content(loginRequest)
                        .headers(testUtil.generateHeaders()))
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
                        .content(requestBody)
                        .headers(testUtil.generateHeaders()))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/user/exists")
                        .param("email", "exists@example.com")
                        .headers(testUtil.generateHeaders()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void shouldReturnFalseIfEmailDoesNotExist() throws Exception {
        mockMvc.perform(get("/user/exists").param("email", "notfound@example.com")
                        .headers(testUtil.generateHeaders()))
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
                        .content(requestBody)
                        .headers(testUtil.generateHeaders()))
                .andExpect(status().isCreated());

        // Check existence with lowercase email (URL-encoded)

        mockMvc.perform(get("/user/exists")
                        .param("email", "casesensitive@example.com")
                        .headers(testUtil.generateHeaders()))
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
                        .header("Authorization", "Bearer " + token)
                        .headers(testUtil.generateHeaders()))
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
                        .header("Authorization", "Bearer " + token)
                        .headers(testUtil.generateHeaders()))
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
                        .header("Authorization", "Bearer " + token)
                        .headers(testUtil.generateHeaders()))
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
                        .file(file)
                        .headers(testUtil.generateHeaders())
                )
                .andExpect(status().isUnauthorized());
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
                        .header("Authorization", "Bearer " + token)
                        .headers(testUtil.generateHeaders()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filePath").exists());

        mockMvc.perform(get("/user/profile-pic")
                        .header("Authorization", "Bearer " + token)
                        .headers(testUtil.generateHeaders()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }

    @Test
    void shouldFailToGetProfilePicIfNotFound() throws Exception {
        String token = authenticateTestUser();

        mockMvc.perform(get("/user/profile-pic")
                        .header("Authorization", "Bearer " + token)
                        .headers(testUtil.generateHeaders()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Profile picture not found"));
    }

    @Test
    void shouldFailToGetProfilePicWithInvalidPossiblyRealToken() throws Exception {
        // Provide a properly formatted but invalid JWT
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalidPayload.invalidSignature";

        mockMvc.perform(get("/user/profile-pic")
                        .header("Authorization", "Bearer " + invalidToken)
                        .headers(testUtil.generateHeaders()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailToGetProfilePicWithInvalidToken() throws Exception {
        mockMvc.perform(get("/user/profile-pic")
                        .header("Authorization", "Bearer invalidToken")
                        .headers(testUtil.generateHeaders()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailToGetProfilePicWithoutToken() throws Exception {
        mockMvc.perform(get("/user/profile-pic")
                        .headers(testUtil.generateHeaders()))
                .andExpect(status().isUnauthorized());
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
                        .header("Authorization", "Bearer " + token)
                        .headers(testUtil.generateHeaders()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Profile picture not found"));
    }

    @Test
    void shouldReturnUserSuccessfully() throws Exception {
        String token = authenticateTestUser();

        MvcResult result = mockMvc.perform(get("/user/details")
                        .header("Authorization", "Bearer " + token)
                        .headers(testUtil.generateHeaders()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("testUser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER")).andReturn();
        System.out.println(result.getResponse().getContentAsString());
    }

    @Test
    void shouldFailToReturnUserIfNotFound() throws Exception {
        String token = authenticateTestUser();

        // Simulate a user deletion
        userRepository.deleteAll();

        mockMvc.perform(get("/user/details")
                        .header("Authorization", "Bearer " + token)
                        .headers(testUtil.generateHeaders()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found: User not found or invalid token: User not found with email: test@example.com"));
    }

}
