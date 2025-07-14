package strayfurther.backend.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import strayfurther.backend.dto.JWTContentDTO;
import strayfurther.backend.exception.JwtUtilException;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.secret}")
    private String oldSecretKey;

    @BeforeEach
    void setUp() throws Exception {
        // Reset the secretKey to the old value before each test
        Field secretKeyField = JwtUtil.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        secretKeyField.set(jwtUtil, oldSecretKey);
    }

    @Test
    void testGenerateToken() {
        String email = "test@example.com";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        String token = jwtUtil.generateToken(email, userAgent);

        assertNotNull(token, "Token should not be null");
        assertTrue(token.startsWith("eyJ"), "Token should be a valid JWT");
    }

    @Test
    void testGenerateTokenFailsNoEmail() {
        String email = null;
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

        Exception exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.generateToken(email, userAgent);
        });

        assertEquals("Email cannot be null or empty", exception.getMessage(), "Exception message should indicate the email is null or empty");
    }

    @Test
    void testGenerateTokenFailsEmptyEmail() {
        String email = "";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

        Exception exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.generateToken(email, userAgent);
        });

        assertEquals("Email cannot be null or empty", exception.getMessage(), "Exception message should indicate the email is null or empty");
    }

    @Test
    void testGenerateTokenFailsNoUserAgent() {
        String email = "test@mail.com";

        Exception exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.generateToken(email, null);
        });

        assertEquals("userAgent cannot be null or empty", exception.getMessage(), "Exception message should indicate the email is null or empty");
    }

    @Test
    void testGenerateTokenFailsEmptyUserAgent() {
        String email = "test@mail.com";

        Exception exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.generateToken(email, "");
        });

        assertEquals("userAgent cannot be null or empty", exception.getMessage(), "Exception message should indicate the email is null or empty");
    }

    @Test
    void testExtractDetails() {
        String email = "test@example.com";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        String token = jwtUtil.generateToken(email, userAgent);

        JWTContentDTO jwtContentDTO = jwtUtil.extractJWTDetails(token);
        assertEquals(email, jwtContentDTO.getEmail(), "Extracted email should match the original email");
        assertEquals(userAgent, jwtContentDTO.getUserAgent(), "Extracted user agent should match the original user agent");
    }

    @Test
    void testEactractDetailsWithNullOrEmptyToken() {
        Exception exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.extractJWTDetails(null);
        });

        assertEquals("Token cannot be null or empty", exception.getMessage(), "Exception message should indicate the token is null or empty");

        exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.extractJWTDetails("");
        });

        assertEquals("Token cannot be null or empty", exception.getMessage(), "Exception message should indicate the token is null or empty");
    }

    @Test
    void testIsTokenValid() {
        String email = "test@example.com";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        String token = jwtUtil.generateToken(email, userAgent);

        assertTrue(jwtUtil.isTokenValid(token, email, userAgent), "Token should be valid for the given email");
    }

    @Test
    void testIsTokenValidExpiredToken() {
        String email = "test@example.com";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

        // Generate a valid token
        String token = jwtUtil.generateToken(email, userAgent);

        // Forward the system time to simulate token expiration
        Instant expiredTime = Instant.now().plus(Duration.ofDays(31));
        Clock.fixed(expiredTime, ZoneId.systemDefault());

        // Create a new temporary JwtUtil object
        JwtUtil tempJwtUtil = new JwtUtil();

        // Validate the token using the temporary JwtUtil object
        assertFalse(tempJwtUtil.isTokenValid(token, email, userAgent), "Expired token should not be valid");
    }

    @Test
    void testIsTokenValidEmptyOrNullEmailParameter() {
        String email = "test@example.com";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        String token = jwtUtil.generateToken(email, userAgent);

        Exception exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.isTokenValid(token, null, userAgent);
        });

        assertEquals("Email cannot be null or empty", exception.getMessage(), "Exception message should indicate the email is null or empty");

        exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.isTokenValid(token, "", userAgent);
        });

        assertEquals("Email cannot be null or empty", exception.getMessage(), "Exception message should indicate the email is null or empty");

    }

    @Test
    void testIsTokenValidEmptyOrNullUserAgentParameter() {
        String email = "test@example.com";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        String token = jwtUtil.generateToken(email, userAgent);

        Exception exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.isTokenValid(token, email, null);
        });

        assertEquals("userAgent cannot be null or empty", exception.getMessage(), "Exception message should indicate the User-Agent is null or empty");

        exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.isTokenValid(token, email, "");
        });

        assertEquals("userAgent cannot be null or empty", exception.getMessage(), "Exception message should indicate the User-Agent is null or empty");
    }

    @Test
    void testIsTokenValidEmptyOrNullTokenParameter() {
        String email = "test@example.com";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

        Exception exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.isTokenValid(null, email, userAgent);
        });

        assertEquals("Token cannot be null or empty", exception.getMessage(), "Exception message should indicate the Token is null or empty");

        exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.isTokenValid("", "", userAgent);
        });

        assertEquals("Token cannot be null or empty", exception.getMessage(), "Exception message should indicate the Token is null or empty");

        exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.isTokenValid(" ", email, "");
        });

        assertEquals("userAgent cannot be null or empty", exception.getMessage(), "Exception message should indicate the User-Agent is null or empty");

        exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.isTokenValid(" ", email, null);
        });
        assertEquals("userAgent cannot be null or empty", exception.getMessage(), "Exception message should indicate the User-Agent is null or empty");

    }

    @Test
    void testIsTokenValidWrongEmail() {
        String email = "test@example.com";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        String token = jwtUtil.generateToken(email, userAgent);

        assertEquals(false, jwtUtil.isTokenValid(token, "bro@mail.com", userAgent), "Token and email combination should not be valid for a different email");
    }

    @Test
    void testGenerateTokenWithNullOrEmptySecretKey() throws Exception {
        // Access the private `secretKey` field using reflection
        Field secretKeyField = JwtUtil.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

        // Set the `secretKey` to null
        secretKeyField.set(jwtUtil, null);

        Exception exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.generateToken("test@example.com", userAgent);
        });
        assertEquals("Secret key cannot be null or empty", exception.getMessage(), "Exception message should indicate the secret key is null or empty");

        // Set the `secretKey` to an empty string
        secretKeyField.set(jwtUtil, "");
        exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.generateToken("test@example.com", userAgent);
        });
        assertEquals("Secret key cannot be null or empty", exception.getMessage(), "Exception message should indicate the secret key is null or empty");
    }
    @Test
    void testIsTokenValidWrongTokenForEmail() {
        String email = "test@example.com";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        String token = jwtUtil.generateToken(email, userAgent);

        assertEquals(false, jwtUtil.isTokenValid(token, "bro@mail.com", userAgent), "Token and email combination should not be valid for an unmatching token");
    }

    @Test
    void testIsTokenValidWrongTokenForUserAgent() {
        String email = "test@example.com";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        String token = jwtUtil.generateToken(email, userAgent);

        assertEquals(false, jwtUtil.isTokenValid(token, email, "PostmanRuntime/7.28.4"), "Token and user agent combination should not be valid for an unmatching user agent");
    }

    @Test
    void testIsTokenValidInvalidToken() {
        String email = "test@example.com";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
        String token = "some sicko token that is not valid";

        assertEquals(false, jwtUtil.isTokenValid(token, email, userAgent), "Token and email combination should not be valid for an invalid token");
    }

    @Test
    void testExtractTokenFromHeader() {
        String authHeader = "Bearer testToken123";
        String token = jwtUtil.extractTokenFromHeader(authHeader);

        assertEquals("testToken123", token, "Extracted token should match the token in the header");
    }

    @Test
    void testExtractTokenFromHeaderInvalid() {
        String authHeader = "InvalidHeader";

        Exception exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.extractTokenFromHeader(authHeader);
        });

        assertEquals("Authorization header must start with 'Bearer '", exception.getMessage(), "Exception message should indicate the header format is invalid");
    }

    @Test
    void testExtractTokenFromHeaderEmpty() {
        String authHeader = "";

        Exception exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.extractTokenFromHeader(authHeader);
        });

        assertEquals("Authorization header cannot be null or empty", exception.getMessage(), "Exception message should indicate the header is empty or null");
    }

    @Test
    void testExtractTokenFromHeaderNull() {
        String authHeader = null;

        Exception exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.extractTokenFromHeader(authHeader);
        });

        assertEquals("Authorization header cannot be null or empty", exception.getMessage(), "Exception message should indicate the header is empty or null");
    }
}