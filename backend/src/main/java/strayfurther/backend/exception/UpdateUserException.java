package strayfurther.backend.exception;

public class UpdateUserException extends RuntimeException {
    public UpdateUserException(String message) {
        super(message);
    }

    public UpdateUserException(String message, Throwable cause) {
        super(message, cause);
    }
}