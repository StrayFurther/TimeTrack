package strayfurther.backend.util;

import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Component
public class TestUtil {

    @Value("${app.client.source.secret}")
    private String secret;

    private String generateHmacSHA256Signature(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC SHA256 signature", e);
        }
    }

    public HttpHeaders generateHeaders() {
        String nonce = System.currentTimeMillis() + "-" + UUID.randomUUID();
        String timestamp = Instant.now().toString();
        String signature = generateHmacSHA256Signature(nonce + ":" + timestamp);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Client-Nonce", nonce);
        headers.add("X-Client-Timestamp", timestamp);
        headers.add("X-Client-Signature", signature);
        headers.add("User-Agent", "IntegrationTestAgent/1.0");
        return headers;
    }
}