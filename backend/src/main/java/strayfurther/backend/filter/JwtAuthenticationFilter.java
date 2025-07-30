package strayfurther.backend.filter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import strayfurther.backend.config.whitelist.PermittedEndpoints;
import strayfurther.backend.security.JwtAuthenticationToken;
import strayfurther.backend.util.JwtUtil;

import java.io.IOException;
import java.util.List;

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
            System.out.println("Extracted JWT token: " + token);
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                String email = jwtUtil.extractJWTDetails(token).getEmail();
                if (email != null && jwtUtil.isTokenValid(token, email, request.getHeader("User-Agent"))) {
                    System.out.println("JWT is valid");
                    var authToken = new JwtAuthenticationToken(email, token, List.of(new SimpleGrantedAuthority("ROLE_USER")));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    authToken.setAuthenticated(true);
                    System.out.println("Authentication set in SecurityContextHolder: " + SecurityContextHolder.getContext().getAuthentication());

                    System.out.println("So far so good");
                }
            }
        } catch (JwtException e) {
            // no need to do something here, just let the filter chain continue
            System.out.println("JWT validation failed: " + e.getMessage());
        }
        System.out.println("Continuing filter chain for request: " + request.getRequestURI());
        filterChain.doFilter(request, response);
        System.out.println("After filter: SecurityContextHolder contains: " + SecurityContextHolder.getContext().getAuthentication());
        System.out.println("Continuing filter chain for request (AFTER DO FILTER CHAIN): " + request.getRequestURI());
    }


//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String path = request.getRequestURI();
//        String method = request.getMethod();
//        boolean isPermitted =
//                (method.equals("POST") && PermittedEndpoints.POST_ENDPOINTS.stream().anyMatch(endpoint -> pathMatcher.match(endpoint, path)))
//                || (method.equals("GET") && PermittedEndpoints.GET_ENDPOINTS.stream().anyMatch(endpoint -> pathMatcher.match(endpoint, path)));
//        System.out.println("isPermitted: " + isPermitted);
//        return isPermitted;
//    }
}