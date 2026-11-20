package prepforge.exception;

public class DataAccessException extends PrepForgeException {

    public DataAccessException() {
        super("A data access or database error occurred.");
    }

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
