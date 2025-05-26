package strayfurther.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
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
}
