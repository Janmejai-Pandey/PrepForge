package prepforge.exception;

public class ValidationException extends PrepForgeException {

    public ValidationException() {
        super("Input validation failed.");
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
