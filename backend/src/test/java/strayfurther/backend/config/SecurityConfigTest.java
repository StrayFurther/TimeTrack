package strayfurther.backend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import strayfurther.backend.repository.UserRepository;
import strayfurther.backend.service.ProfilePicService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

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

        @Value("${app.client.source.secret}")
        private String secret; // Load the key from application-test.properties

        @Test
        void shouldAllowAccessToPublicEndpointsWithHeaders() throws Exception {
            String nonce = System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString();
            String timestamp = java.time.Instant.now().toString();
            String signature = generateHmacSHA256Signature(nonce + ":" + timestamp, secret);

            mockMvc.perform(post("/user/register")
                            .header("X-Client-Nonce", nonce)
                            .header("X-Client-Timestamp", timestamp)
                            .header("X-Client-Signature", signature)
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

        private String generateHmacSHA256Signature(String data, String secret) {
            try {
                Mac mac = Mac.getInstance("HmacSHA256");
                SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
                mac.init(secretKeySpec);
                byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
                StringBuilder hexString = new StringBuilder();
                for (byte b : hmacBytes) {
                    hexString.append(String.format("%02x", b));
                }
                return hexString.toString();
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate HMAC SHA256 signature", e);
            }
        }
    }

}