package strayfurther.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JWTContentDTO {
    private String email;
    private String userAgent;
}
