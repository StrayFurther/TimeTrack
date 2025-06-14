package strayfurther.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import strayfurther.backend.util.JwtUtil;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class JwtAuthenticationProviderTest {

    @Test
    void shouldAuthenticateWithValidToken() {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(jwtUtil);

        String token = "validToken";
        String email = "user@example.com";

        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(jwtUtil.isTokenValid(token, email)).thenReturn(true);

        Authentication authentication = provider.authenticate(new JwtAuthenticationToken(null, token, null));

        assertNotNull(authentication, "Authentication should not be null");
        assertEquals(email, authentication.getPrincipal(), "Principal should match extracted email");
        assertEquals(token, authentication.getCredentials(), "Credentials should match the token");
    }

    @Test
    void shouldThrowExceptionForInvalidToken() {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(jwtUtil);

        String token = "invalidToken";

        when(jwtUtil.extractEmail(token)).thenReturn(null);

        assertThrows(AuthenticationException.class, () ->
                        provider.authenticate(new JwtAuthenticationToken(null, token, null)),
                "Should throw AuthenticationException for invalid token"
        );
    }

    // Test for expired token
    @Test
    void shouldThrowExceptionForExpiredToken() {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(jwtUtil);

        String token = "expiredToken";

        when(jwtUtil.extractEmail(token)).thenReturn("user@example.com");
        when(jwtUtil.isTokenValid(token, "user@example.com")).thenReturn(false);

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