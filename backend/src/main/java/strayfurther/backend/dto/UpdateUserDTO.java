package strayfurther.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import strayfurther.backend.validation.OptionalPasswordConstraint;

@Setter
@Getter
@Builder
public class UpdateUserDTO {

    @NotBlank
    String userName;

    @NotBlank
    @OptionalPasswordConstraint
    String password;
}
