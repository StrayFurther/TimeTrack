package strayfurther.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import strayfurther.backend.validation.OptionalPasswordConstraint;

@Getter
@Setter
@Builder
public class UpdateUserDTO {

    @NotBlank
    String userName;

    @OptionalPasswordConstraint
    String password;
}
