package strayfurther.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import strayfurther.backend.model.enums.Role;
import strayfurther.backend.validation.OptionalPasswordConstraint;

@Getter
@Setter
@Builder
public class AdminsUpdateUserDTO {

    @NotBlank
    String userName;

    @NotBlank
    Role role;

    @OptionalPasswordConstraint
    String password;
}
