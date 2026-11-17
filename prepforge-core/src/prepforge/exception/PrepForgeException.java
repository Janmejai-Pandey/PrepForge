package prepforge.exception;

/**
 * PrepForgeException
 * 
 * Base checked exception class for the entire PrepForge application.
 * All custom business and application exceptions extend this class.
 */
public class PrepForgeException extends Exception {

    // Default constructor
    public PrepForgeException() {
        super("An error occurred in the PrepForge application.");
    }

    // Constructor that accepts a custom error message
    public PrepForgeException(String message) {
        super(message);
    }

    // Constructor that accepts both a message and the underlying cause (exception chaining)
    public PrepForgeException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor that accepts only the underlying cause
    public PrepForgeException(Throwable cause) {
        super(cause);
    }
}
