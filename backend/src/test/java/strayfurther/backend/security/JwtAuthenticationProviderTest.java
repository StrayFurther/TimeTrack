package strayfurther.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.ActiveProfiles;
import strayfurther.backend.util.JwtUtil;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
class JwtAuthenticationProviderTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtAuthenticationProvider provider;

    @Test
    void shouldAuthenticateWithValidToken() {
        // Simulate user registration
        String email = "user@example.com";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

        // Generate token for the user
        String token = jwtUtil.generateToken(email, userAgent);

        // Authenticate using the generated token
        Authentication authentication = provider.authenticate(new JwtAuthenticationToken(null, token, null));

        // Validate authentication
        assertNotNull(authentication, "Authentication should not be null");
        assertEquals(token, authentication.getCredentials(), "Credentials should match the token");
    }

    @Test
    void shouldThrowExceptionForInvalidToken() {
        String token = "invalidToken";

        assertThrows(AuthenticationException.class, () ->
                        provider.authenticate(new JwtAuthenticationToken(null, token, null)),
                "Should throw AuthenticationException for invalid token"
        );
    }

    // Test for expired token
    @Test
    void shouldThrowExceptionForExpiredToken() {
        // Simulate user registration
        String email = "user@example.com";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

        // Generate token with the current system time
        String token = jwtUtil.generateToken(email, userAgent);

        // Forward the system time by 31 days
        Instant forwardedTime = Instant.now().plus(Duration.ofDays(31));
        Clock.fixed(forwardedTime, ZoneId.systemDefault());

        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(new JwtUtil());

        // Attempt to authenticate using the expired token
        assertThrows(AuthenticationException.class, () ->
                        provider.authenticate(new JwtAuthenticationToken(null, token, null)),
                "Should throw AuthenticationException for expired token"
        );
    }

    // Test for unsupported authentication type
    @Test
    void shouldNotSupportOtherAuthenticationTypes() {
        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(mock(JwtUtil.class));

        assertFalse(provider.supports(String.class), "Should not support non-JWT authentication types");
    }

    @Test
    void shouldSupportJwtAuthenticationToken() {
        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(mock(JwtUtil.class));

        assertTrue(provider.supports(JwtAuthenticationToken.class), "Should support JwtAuthenticationToken class");
    }
}