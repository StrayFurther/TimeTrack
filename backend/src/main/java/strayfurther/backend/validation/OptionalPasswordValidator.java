
package strayfurther.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OptionalPasswordValidator implements ConstraintValidator<OptionalPasswordConstraint, String> {

    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-={}:;\"'\\[\\]|<>,.?/~`]).{8,}$";

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return true;
        }
        return password.matches(PASSWORD_PATTERN);
    }
}

