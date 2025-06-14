package strayfurther.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HttpToHttpsRedirectConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRedirectHttpToHttps() throws Exception {
        mockMvc.perform(get("/user/exists?email=test@example.com").secure(false)) // Simulate HTTP request
                .andExpect(status().is3xxRedirection()); // Expect 302 redirect
    }

    @Test
    void shouldAllowHttpsRequests() throws Exception {
        mockMvc.perform(get("/user/exists?email=test@example.com").secure(true)) // Simulate HTTPS request
                .andExpect(status().isOk()); // Expect 200 OK or appropriate response
    }
}