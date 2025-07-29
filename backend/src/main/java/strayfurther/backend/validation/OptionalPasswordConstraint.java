package strayfurther.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = OptionalPasswordValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionalPasswordConstraint {
    String message() default "Password must be at least 8 characters, include a number and a special character";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}