package strayfurther.backend.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import strayfurther.backend.model.User;
import strayfurther.backend.repository.UserRepository;

import java.util.Optional;

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
        User existingUser = new User();
        existingUser.setUserName("existingUser");
        existingUser.setEmail("duplicate@example.com");
        existingUser.setPassword("Encodedpass123-");
        userRepository.save(existingUser);

        String requestBody = """
            {
                "userName": "newUser",
                "email": "duplicate@example.com",
                "password": "anotherPass123"
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
    void shouldFailToLoginWithInvalidCredentials() throws Exception {
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
}
