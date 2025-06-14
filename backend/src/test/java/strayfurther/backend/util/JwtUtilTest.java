package strayfurther.backend.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import strayfurther.backend.exception.JwtUtilException;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
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
        String token = jwtUtil.generateToken(email);

        assertNotNull(token, "Token should not be null");
        assertTrue(token.startsWith("eyJ"), "Token should be a valid JWT");
    }

    @Test
    void testGenerateTokenFailsNoEmail() {
        String email = null;

        Exception exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.generateToken(email);
        });

        assertEquals("Email cannot be null or empty", exception.getMessage(), "Exception message should indicate the email is null or empty");
    }

    @Test
    void testGenerateTokenFailsEmptyEmail() {
        String email = "";

        Exception exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.generateToken(email);
        });

        assertEquals("Email cannot be null or empty", exception.getMessage(), "Exception message should indicate the email is null or empty");
    }

    @Test
    void testExtractEmail() {
        String email = "test@example.com";
        String token = jwtUtil.generateToken(email);

        String extractedEmail = jwtUtil.extractEmail(token);
        assertEquals(email, extractedEmail, "Extracted email should match the original email");
    }

    @Test
    void testExtractEmptyEmailFails() {
        String email = "";

        Exception exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.generateToken(email);
        });

        assertEquals("Email cannot be null or empty", exception.getMessage(), "Exception message should indicate the email is null or empty");
    }

    @Test
    void testExtractNullEmailFails() {
        String email = null;

        Exception exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.generateToken(email);
        });

        assertEquals("Email cannot be null or empty", exception.getMessage(), "Exception message should indicate the email is null or empty");
    }

    @Test
    void testIsTokenValid() {
        String email = "test@example.com";
        String token = jwtUtil.generateToken(email);

        assertTrue(jwtUtil.isTokenValid(token, email), "Token should be valid for the given email");
    }

    @Test
    void testIsTokenValidExpiredToken() throws Exception {
        String email = "test@example.com";

        // Access the private `secretKey` field using reflection
        Field secretKeyField = JwtUtil.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        String secretKey = (String) secretKeyField.get(jwtUtil);

        String expiredToken = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24)) // Issued 1 day ago
                .setExpiration(new Date(System.currentTimeMillis() - 1000L * 60 * 60)) // Expired 1 hour ago
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();

        assertFalse(jwtUtil.isTokenValid(expiredToken, email), "Expired token should not be valid");
    }

    @Test
    void testIsTokenValidEmptyOrNullEmailParameter() {
        String email = "test@example.com";
        String token = jwtUtil.generateToken(email);

        Exception exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.isTokenValid(token, null);
        });

        assertEquals("Email cannot be null or empty", exception.getMessage(), "Exception message should indicate the email is null or empty");

        exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.isTokenValid(token, "");
        });

        assertEquals("Email cannot be null or empty", exception.getMessage(), "Exception message should indicate the email is null or empty");

    }

    @Test
    void testIsTokenValidEmptyOrNullTokenParameter() {
        String email = "test@example.com";

        Exception exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.isTokenValid(null, email);
        });

        assertEquals("Token cannot be null or empty", exception.getMessage(), "Exception message should indicate the Token is null or empty");

        exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.isTokenValid("", "");
        });

        assertEquals("Token cannot be null or empty", exception.getMessage(), "Exception message should indicate the Token is null or empty");

    }

    @Test
    void testIsTokenValidWrongEmail() {
        String email = "test@example.com";
        String token = jwtUtil.generateToken(email);

        assertEquals(false, jwtUtil.isTokenValid(token, "bro@mail.com"), "Token and email combination should not be valid for a different email");
    }

    @Test
    void testGenerateTokenWithNullOrEmptySecretKey() throws Exception {
        // Access the private `secretKey` field using reflection
        Field secretKeyField = JwtUtil.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);

        // Set the `secretKey` to null
        secretKeyField.set(jwtUtil, null);

        Exception exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.generateToken("test@example.com");
        });
        assertEquals("Secret key cannot be null or empty", exception.getMessage(), "Exception message should indicate the secret key is null or empty");

        // Set the `secretKey` to an empty string
        secretKeyField.set(jwtUtil, "");
        exception = assertThrows(JwtUtilException.class, () -> {
            jwtUtil.generateToken("test@example.com");
        });
        assertEquals("Secret key cannot be null or empty", exception.getMessage(), "Exception message should indicate the secret key is null or empty");
    }
    @Test
    void testIsTokenValidWrongTokenForEmail() {
        String email = "test@example.com";
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0LndhbGRyYWZmOTVAZ214LmRlIiwiaWF0IjoxNzQ5NzIwNDMxLCJleHAiOjE3NTIzMTI0MzF9.drrJPrHrdAbcbYqMaAqueA-Pqo4DK5JjHqTszjKJ1GM";

        assertEquals(false, jwtUtil.isTokenValid(token, "bro@mail.com"), "Token and email combination should not be valid for an unmatching token");
    }

    @Test
    void testIsTokenValidInvalidToken() {
        String email = "test@example.com";
        String token = "some sicko token that is not valid";

        assertEquals(false, jwtUtil.isTokenValid(token, "bro@mail.com"), "Token and email combination should not be valid for an invalid token");
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