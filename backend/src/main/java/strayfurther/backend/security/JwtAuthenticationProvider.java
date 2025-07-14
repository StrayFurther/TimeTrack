package strayfurther.backend.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import strayfurther.backend.dto.JWTContentDTO;
import strayfurther.backend.util.JwtUtil;

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationProvider(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String token = (String) authentication.getCredentials();
        try {
            JWTContentDTO jwtDetails = jwtUtil.extractJWTDetails(token);
            String email = jwtDetails.getEmail();
            String userAgent = jwtDetails.getUserAgent();

            if (email == null || userAgent == null || !jwtUtil.isTokenValid(token, email, userAgent)) {
                throw new AuthenticationException("Invalid JWT token") {};
            }

            return new JwtAuthenticationToken(null, token, null); // No authorities needed
        } catch (Exception e) {
            throw new AuthenticationException("Token validation failed", e) {};
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}