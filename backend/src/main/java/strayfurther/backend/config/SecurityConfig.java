package strayfurther.backend.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.ForwardedHeaderFilter;
import strayfurther.backend.config.whitelist.PermittedEndpoints;
import strayfurther.backend.filter.CorsPreflightFilter;
import strayfurther.backend.filter.JwtAuthenticationFilter;
import strayfurther.backend.filter.RequestOriginValidationFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider jwtAuthenticationProvider;
    private final RequestOriginValidationFilter requestOriginValidationFilter;
    private final CorsPreflightFilter corsPreflightFilter;
    private final Environment environment;
    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          AuthenticationProvider jwtAuthenticationProvider,
                          RequestOriginValidationFilter requestOriginValidationFilter,
                          CorsPreflightFilter corsFilter,
                          Environment environment) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationProvider = jwtAuthenticationProvider;
        this.requestOriginValidationFilter = requestOriginValidationFilter;
        this.corsPreflightFilter = corsFilter;
        this.environment = environment;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(Collections.singletonList(jwtAuthenticationProvider));
    }

    @Bean
    @Profile({"dev", "test"})
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("RUNNING IN DEV OR TEST PROFILE: " + activeProfile);
        System.out.println("Active Profile: " + environment.getActiveProfiles()[0]);
        // Disable CSRF protection for development
        http.cors().and().csrf(csrf -> csrf.disable());

        // Explicitly allow OPTIONS requests for CORS preflight
        http.authorizeHttpRequests(auth -> {
            System.out.println("AUTHORIZED IN DEV OR TEST PROFILE");
            auth.requestMatchers(HttpMethod.POST, PermittedEndpoints.POST_ENDPOINTS.toArray(String[]::new)).permitAll();
            auth.requestMatchers(HttpMethod.GET, PermittedEndpoints.GET_ENDPOINTS.toArray(String[]::new)).permitAll();
            auth.anyRequest().authenticated();
        });
        System.out.println("RUNNING IN TEST PROFILE");
        http.addFilterBefore(corsPreflightFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(requestOriginValidationFilter, UsernamePasswordAuthenticationFilter.class);

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Profile("prod")
    public SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) throws Exception {
        // Disable CSRF protection. Using JWT for authentication, CSRF protection is not needed.
        http.csrf(csrf -> csrf.disable());

        // Block HTTP requests explicitly
        http.addFilterBefore((request, response, chain) -> {
            if (!request.isSecure()) {
                if (response instanceof HttpServletResponse) {
                    ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "HTTP requests are not allowed");
                }
            } else {
                chain.doFilter(request, response);
            }
        }, UsernamePasswordAuthenticationFilter.class);

        http.authorizeHttpRequests(auth -> {
            PermittedEndpoints.POST_ENDPOINTS.forEach(endpoint -> {
                System.out.println("POST_ENDPOINT: " + endpoint);
                auth.requestMatchers(HttpMethod.POST, endpoint).permitAll();

            });
            PermittedEndpoints.GET_ENDPOINTS.forEach(endpoint -> {
                System.out.println("GET_ENDPOINT: " + endpoint);
                auth.requestMatchers(HttpMethod.GET, endpoint).permitAll();
            });
            auth.anyRequest().authenticated();
        });

        // Remove requiresSecure() and block HTTP requests explicitly
        http.addFilterBefore(corsPreflightFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(requestOriginValidationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}