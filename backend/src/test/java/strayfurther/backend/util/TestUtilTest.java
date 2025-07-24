package strayfurther.backend.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import strayfurther.backend.service.ProfilePicService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = "app.client.source.secret=test-secret")
class TestUtilTest {

    // dont know why but is needed by spring context to load the beans properly
    @MockBean
    private ProfilePicService profilePicService;

    @Autowired
    private TestUtil testUtil;

    private Method generateHmacSHA256SignatureMethod;

    @BeforeEach
    void setUp() throws Exception {
        // Set the secret value for testing
        generateHmacSHA256SignatureMethod = TestUtil.class.getDeclaredMethod("generateHmacSHA256Signature", String.class);
        generateHmacSHA256SignatureMethod.setAccessible(true);
    }

    @Test
    void testGenerateHmacSHA256Signature() throws Exception {
        // Arrange
        String data = "test-data";

        // Act
        String signature = (String) generateHmacSHA256SignatureMethod.invoke(testUtil, data);

        // Assert
        assertNotNull(signature, "Signature should not be null");
        assertFalse(signature.isEmpty(), "Signature should not be empty");
    }

    @Test
    void testValidHmacSHA256Signature() throws Exception {
        // Arrange
        String data = "test-data";
        String expectedSignature;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec("test-secret".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                hexString.append(String.format("%02x", b));
            }
            expectedSignature = hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate expected HMAC SHA256 signature", e);
        }

        // Act
        String actualSignature = (String) generateHmacSHA256SignatureMethod.invoke(testUtil, data);

        // Assert
        assertEquals(expectedSignature, actualSignature, "Generated signature should match the expected signature");
    }

    @Test
    void testGenerateHeaders() {
        // Act
        HttpHeaders headers = testUtil.generateHeaders();

        // Assert
        assertNotNull(headers, "Headers should not be null");
        assertTrue(headers.containsKey("X-Client-Nonce"), "Headers should contain X-Client-Nonce");
        assertTrue(headers.containsKey("X-Client-Timestamp"), "Headers should contain X-Client-Timestamp");
        assertTrue(headers.containsKey("X-Client-Signature"), "Headers should contain X-Client-Signature");
        assertTrue(headers.containsKey("User-Agent"), "Headers should contain User-Agent");

        // Validate header values
        String nonce = headers.getFirst("X-Client-Nonce");
        String timestamp = headers.getFirst("X-Client-Timestamp");
        String signature = headers.getFirst("X-Client-Signature");

        assertNotNull(nonce, "Nonce should not be null");
        assertNotNull(timestamp, "Timestamp should not be null");
        assertNotNull(signature, "Signature should not be null");

        // Ensure timestamp is a valid ISO-8601 string
        assertDoesNotThrow(() -> Instant.parse(timestamp), "Timestamp should be a valid ISO-8601 string");
    }

    @Test
    void testGenerateHeadersLogic() throws Exception {
        // Act
        HttpHeaders headers = testUtil.generateHeaders();

        // Extract header values
        String nonce = headers.getFirst("X-Client-Nonce");
        String timestamp = headers.getFirst("X-Client-Timestamp");
        String signature = headers.getFirst("X-Client-Signature");

        // Assert headers are not null
        assertNotNull(nonce, "Nonce should not be null");
        assertNotNull(timestamp, "Timestamp should not be null");
        assertNotNull(signature, "Signature should not be null");

        // Validate nonce format (e.g., contains a UUID)
        assertTrue(nonce.matches("\\d+-[a-f0-9\\-]+"), "Nonce should follow the expected format");

        // Validate timestamp is a valid ISO-8601 string
        assertDoesNotThrow(() -> Instant.parse(timestamp), "Timestamp should be a valid ISO-8601 string");

        // Validate the signature logic
        String expectedSignature = (String) generateHmacSHA256SignatureMethod.invoke(testUtil, nonce + ":" + timestamp);
        assertEquals(expectedSignature, signature, "Generated signature should match the expected logic");
    }
}