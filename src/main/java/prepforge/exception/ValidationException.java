package prepforge.exception;

/**
 * ValidationException
 * 
 * Thrown whenever user input fails validation rules.
 * Examples: invalid email format, weak password, negative CGPA, empty names, duplicate email.
 */
public class ValidationException extends PrepForgeException {

    // Default constructor
    public ValidationException() {
        super("Input validation failed.");
    }

    // Constructor with a specific validation error message
    public ValidationException(String message) {
        super(message);
    }

    // Constructor with message and underlying cause
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
