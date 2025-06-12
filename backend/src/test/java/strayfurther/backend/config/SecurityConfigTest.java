package strayfurther.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldLoadAuthenticationManagerBean() {
        assertNotNull(authenticationManager, "AuthenticationManager bean should be loaded");
    }

    @Test
    void shouldLoadPasswordEncoderBean() {
        assertNotNull(passwordEncoder, "PasswordEncoder bean should be loaded");
    }

//    @Test
//    void shouldRedirectHttpToHttps() throws Exception {
//        mockMvc.perform(get("/user/exists").secure(false)) // Simulate an HTTP request
//                .andExpect(status().is3xxRedirection()) // Expect forbidden since HTTPS is required
//                .andExpect(result -> assertTrue(
//                        result.getResponse().getHeaderNames().contains("Strict-Transport-Security"),
//                        "Strict-Transport-Security header should be present"
//                ));
//
//        mockMvc.perform(get("/user/exists").secure(true)) // Simulate an HTTPS request
//                .andExpect(status().isOk()); // Expect 200 OK or the appropriate success status
//    }

//    @Test
//    void shouldAllowAccessToPublicEndpoints() throws Exception {
//        String registerRequest = """
//        {
//            "userName": "testUser",
//            "email": "test@example.com",
//            "password": "Password123!"
//        }
//        """;
//
//        mockMvc.perform(post("/user/register").secure(true)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(registerRequest))
//                .andExpect(status().isCreated());
//    }

    @Test
    void shouldRequireAuthenticationForProtectedEndpoints() throws Exception {
        mockMvc.perform(post("/user/profile-pic").secure(true))
                .andExpect(status().isForbidden());
    }
}