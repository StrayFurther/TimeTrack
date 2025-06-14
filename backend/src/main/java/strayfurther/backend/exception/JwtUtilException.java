package strayfurther.backend.exception;

public class JwtUtilException extends RuntimeException {
    public JwtUtilException(String message) {
        super(message);
    }

    public JwtUtilException(String message, Throwable cause) {
        super(message, cause);
    }
}