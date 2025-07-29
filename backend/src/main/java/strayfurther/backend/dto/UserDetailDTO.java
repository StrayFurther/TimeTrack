package strayfurther.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import strayfurther.backend.model.enums.Role;

@Getter
@Setter
@Builder
public class UserDetailDTO {
    @NotBlank
    @Email
    String email;

    @NotBlank
    String userName;

    Role role;
}
