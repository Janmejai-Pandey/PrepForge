package prepforge.exception;

public class PrepForgeException extends Exception {

    public PrepForgeException() {
        super("An error occurred in the PrepForge application.");
    }

    public PrepForgeException(String message) {
        super(message);
    }

    public PrepForgeException(String message, Throwable cause) {
        super(message, cause);
    }

    public PrepForgeException(Throwable cause) {
        super(cause);
    }
}
