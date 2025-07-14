package strayfurther.backend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import strayfurther.backend.repository.UserRepository;
import strayfurther.backend.service.ProfilePicService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private ProfilePicService profilePicService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldLoadAuthenticationManagerBean() {
        assertNotNull(authenticationManager, "AuthenticationManager bean should be loaded");
    }

    @Test
    void shouldLoadPasswordEncoderBean() {
        assertNotNull(passwordEncoder, "PasswordEncoder bean should be loaded");
    }

    @Nested
    class DevProfileTests {

        @Test
        void testSecurityFilterChainWithNonTestProfile() throws Exception {
            // Act: Build the security filter chain
            SecurityFilterChain filterChain = securityConfig.devSecurityFilterChain(null);

            // Assert: Ensure the filter chain is not null
            assertNotNull(filterChain, "SecurityFilterChain should be created successfully");
        }

        @Test
        void shouldAllowAccessToPublicEndpoints() throws Exception {
            mockMvc.perform(post("/user/register")
                            .contentType("application/json")
                            .content("""
                            {
                                "userName": "testUser",
                                "email": "test@example.com",
                                "password": "Password123!"
                            }
                            """))
                    .andExpect(status().isCreated());
        }

        @Test
        void shouldRequireAuthenticationForProtectedEndpoints() throws Exception {
            mockMvc.perform(post("/user/profile-pic").secure(true))
                    .andExpect(status().isForbidden());
        }

    }

}