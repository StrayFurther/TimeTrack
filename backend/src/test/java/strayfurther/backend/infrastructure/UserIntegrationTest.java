package strayfurther.backend.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import strayfurther.backend.repository.UserRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureMockMvc
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
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
                .andExpect(jsonPath("$.value").value(true));
    }

    @Test
    void shouldReturnFalseIfEmailDoesNotExist() throws Exception {
        mockMvc.perform(get("/user/exists").param("email", "notfound@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(false));
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
                .andExpect(jsonPath("$.value").value(true));
    }



}
