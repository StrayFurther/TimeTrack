package strayfurther.backend.util;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import strayfurther.backend.exception.JwtUtilException;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    private static final long EXPIRATION_TIME = 1000L * 60 * 60 * 24 * 30; // 30 days

    public String generateToken(String email) throws JwtUtilException {
        if (email == null || email.isEmpty()) {
            throw new JwtUtilException("Email cannot be null or empty");
        }
        if (secretKey == null || secretKey.isEmpty() || secretKey.length() < 32) {
            throw new JwtUtilException("Secret key cannot be null or empty");
        }

        Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) throws JwtUtilException {
        if (token == null || token.isEmpty()) {
            throw new JwtUtilException("Token cannot be null or empty");
        }
        if (secretKey == null || secretKey.isEmpty()) {
            throw new JwtUtilException("Secret key cannot be null or empty");
        }
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject(); // The email is stored as the subject
    }

    public boolean isTokenValid(String token, String email) throws JwtUtilException {
        if (token == null || token.isEmpty()) {
            throw new JwtUtilException("Token cannot be null or empty");
        }
        if (email == null || email.isEmpty()) {
            throw new JwtUtilException("Email cannot be null or empty");
        }
        try {
            String extractedEmail = extractEmail(token);
            return extractedEmail.equals(email) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        if (token == null || token.isEmpty()) {
            throw new JwtUtilException("Token cannot be null or empty");
        }
        try {
            Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            throw new JwtUtilException("Token validation failed", e);
        }

    }

    public String extractTokenFromHeader(String authHeader) throws JwtUtilException {
        if (authHeader == null || authHeader.isEmpty()) {
            throw new JwtUtilException("Authorization header cannot be null or empty");
        }
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new JwtUtilException("Authorization header must start with 'Bearer '");
    }
}

