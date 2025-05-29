package strayfurther.backend.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements Filter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${ratelimit.capacity}")
    private int capacity;

    @Value("${ratelimit.refill}")
    private int refill;

    @Value("${ratelimit.duration-minutes}")
    private int durationMinutes;

    @Autowired
    private Environment env;

    // Only rate limit these endpoints
    private static final String[] RATE_LIMITED_PATHS = {"/api/user/register", "/api/user/login"};

    private boolean isRateLimitedPath(String path) {
        return Arrays.stream(RATE_LIMITED_PATHS).anyMatch(path::startsWith);
    }

    private Bucket resolveBucket(String ip) {
        return buckets.computeIfAbsent(ip, k -> Bucket4j.builder()
                .addLimit(Bandwidth.classic(capacity, Refill.greedy(refill, Duration.ofMinutes(durationMinutes))))
                .build());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

        // Skip rate limiting for 'test' profile
        if (Arrays.asList(env.getActiveProfiles()).contains("test")) {
            chain.doFilter(request, response);
            return;
        }

        // Only apply rate limiting to certain endpoints
        if (!isRateLimitedPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();
        Bucket bucket = resolveBucket(ip);

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).setStatus(429);
            response.getWriter().write("Too Many Requests");
        }
    }
}