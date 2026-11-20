package prepforge.exception;

/**
 * DataAccessException
 * 
 * Thrown whenever a database or persistence error occurs in the DAO layer.
 * Examples: database connection failure, SQL execution error, entity not found.
 * 
 * By wrapping low-level SQLExceptions into DataAccessException, we prevent
 * database-specific details from leaking into our Service and UI layers.
 */
public class DataAccessException extends PrepForgeException {

    // Default constructor
    public DataAccessException() {
        super("A data access or database error occurred.");
    }

    // Constructor with a specific error message
    public DataAccessException(String message) {
        super(message);
    }

    // Constructor with message and underlying cause (e.g. wrapping SQLException)
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
