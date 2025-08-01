package strayfurther.backend.validation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
class OptionalPasswordValidatorTest {

    private final OptionalPasswordValidator validator = new OptionalPasswordValidator();

    @Test
    void testValidPassword() {
        assertTrue(validator.isValid("Password1!", null)); // Valid password
    }

    @Test
    void testInvalidPasswordTooShort() {
        assertFalse(validator.isValid("Pass1!", null)); // Too short
    }

    @Test
    void testInvalidPasswordNoNumber() {
        assertFalse(validator.isValid("Password!", null)); // No number
    }

    @Test
    void testInvalidPasswordNoSpecialCharacter() {
        assertFalse(validator.isValid("Password1", null)); // No special character
    }

    @Test
    void testNullPassword() {
        assertTrue(validator.isValid(null, null)); // Null password
    }
}