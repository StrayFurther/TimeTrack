package strayfurther.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import strayfurther.backend.model.enums.Role;
import strayfurther.backend.validation.PasswordConstraint;


@Getter
@Setter
@Builder
public class UserRequestDTO {

    @NotBlank
    private String userName;

    @NotBlank
    @Email
    private String email;

    @PasswordConstraint
    private String password;

    @Builder.Default
    private Role role = Role.USER;
}
