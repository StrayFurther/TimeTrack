package strayfurther.backend.config.whitelist;

import java.util.List;

public class PermittedEndpoints {
    public static final List<String> POST_ENDPOINTS = List.of(
            "/api/user/register",
            "/api/user/login",
            "/user/register",
            "/user/login"
    );
    public static final List<String> GET_ENDPOINTS = List.of(
            "/api/user/exists/**",
            "/user/exists/**"
    );
}