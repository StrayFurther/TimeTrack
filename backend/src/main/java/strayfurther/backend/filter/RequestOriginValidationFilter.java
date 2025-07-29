package strayfurther.backend.filter;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RequestOriginValidationFilter extends OncePerRequestFilter {

    private static final long TIMESTAMP_THRESHOLD_SECONDS = 300; // 5 minutes

    @Value("${app.client.source.secret}") // Inject the secret from application properties
    private String clientSecret;

    private final Map<String, Instant> usedNonces = new ConcurrentHashMap<>();

    @PostConstruct
    public void logSecret() {
        System.out.println("Resolved secret: " + clientSecret);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println("RequestOriginValidationFilter.doFilterInternal called");

        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.contains("Postman")) {
            filterChain.doFilter(request, response);
            return;
        }


        String nonce = request.getHeader("X-Client-Nonce");
        String timestamp = request.getHeader("X-Client-Timestamp");
        String signature = request.getHeader("X-Client-Signature");
        System.out.println("Nonce: " + nonce);
        if (nonce == null || timestamp == null || signature == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required headers");
            return;
        }

        try {
            Instant requestTimestamp = Instant.parse(timestamp);

            if (!isValidNonce(nonce) || !isValidTimestamp(requestTimestamp) || !isValidSignature(nonce, timestamp, signature)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid request");
                return;
            }
            System.out.println("RequestOriginValidationFilter.doFilterInternal called");
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid header format");
        }
    }

    private boolean isValidNonce(String nonce) {
        if (usedNonces.containsKey(nonce)) {
            return false; // Nonce already used
        }
        usedNonces.put(nonce, Instant.now());
        // Clean up expired nonces
        usedNonces.entrySet().removeIf(entry -> Instant.now().minusSeconds(TIMESTAMP_THRESHOLD_SECONDS).isAfter(entry.getValue()));
        return true;
    }

    private boolean isValidTimestamp(Instant timestamp) {
        return !Instant.now().minusSeconds(TIMESTAMP_THRESHOLD_SECONDS).isAfter(timestamp);
    }

    private boolean isValidSignature(String nonce, String timestamp, String signature) {
        String data = nonce + ":" + timestamp;
        String expectedSignature = generateHmacSHA256(data, clientSecret);
        return expectedSignature.equals(signature);
    }

    private String generateHmacSHA256(String data, String secret) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hmacBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC", e);
        }
    }
}