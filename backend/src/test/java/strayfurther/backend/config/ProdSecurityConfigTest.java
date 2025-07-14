package strayfurther.backend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import strayfurther.backend.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("prod")
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=true",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.h2.console.enabled=true",
        "server.ssl.enabled=true"
}, classes = {strayfurther.backend.BackendApplication.class})
@AutoConfigureMockMvc
@Import(StubAmazonS3TestConfig.class)
class ProdSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void contextLoads() {
        assertNotNull(mockMvc, "MockMvc should be initialized");
    }

    @Test
    void testSecurityFilterChainWithNonTestProfile() throws Exception {
        // Act: Build the security filter chain
        SecurityFilterChain filterChain = securityConfig.prodSecurityFilterChain(null);

        // Assert: Ensure the filter chain is not null
        assertNotNull(filterChain, "SecurityFilterChain should be created successfully");
    }

    @Test
    void shouldRejectHttpRequests() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "userName": "testUser",
                            "email": "test@example.com",
                            "password": "Password123!"
                        }
                        """)
                        .secure(false))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAcceptHttpsRequests() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "userName": "testUser",
                            "email": "test@example.com",
                            "password": "Password123!"
                        }
                        """)
                        .secure(true))
                .andExpect(status().isCreated());
    }
}