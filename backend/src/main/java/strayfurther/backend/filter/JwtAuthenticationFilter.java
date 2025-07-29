package strayfurther.backend.filter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import strayfurther.backend.config.whitelist.PermittedEndpoints;
import strayfurther.backend.security.JwtAuthenticationToken;
import strayfurther.backend.util.JwtUtil;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();


    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println("Running JwtAuthenticationFilter for request: " + request.getRequestURI());
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                String email = jwtUtil.extractJWTDetails(token).getEmail();
                if (email != null && jwtUtil.isTokenValid(token, email, request.getHeader("User-Agent"))) {
                    System.out.println("JWT is valid");
                    var authToken = new JwtAuthenticationToken(email, token, null);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("So far so good");
                }
            }
        } catch (JwtException e) {
            // no need to do something here, just let the filter chain continue
        }

        filterChain.doFilter(request, response);
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        System.out.println("Checking if path is whitelisted: '" + path + "'");
        boolean isPermitted = PermittedEndpoints.POST_ENDPOINTS.stream().anyMatch(endpoint -> pathMatcher.match(endpoint, path)) ||
                PermittedEndpoints.GET_ENDPOINTS.stream().anyMatch(endpoint -> pathMatcher.match(endpoint, path));
        System.out.println("Path is whitelisted: " + isPermitted);
        return isPermitted;
    }
}