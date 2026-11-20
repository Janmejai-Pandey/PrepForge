package prepforge.util;

import java.util.regex.Pattern;
import prepforge.exception.ValidationException;

/**
 * Utility Class: Validator
 * 
 * Provides static validation methods to verify user inputs across PrepForge.
 * 
 * Demonstrates:
 * 1. STATIC METHODS: Utility methods called using class name (Validator.validateEmail(...))
 *    without needing to create an object with 'new Validator()'.
 * 2. REGULAR EXPRESSIONS (Regex): Pattern matching for email addresses.
 * 3. EXCEPTION THROWING: Throws ValidationException if input rules are violated.
 */
public class Validator {

    // Regular expression for validating standard email formats
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    // Minimum password length requirement
    public static final int MIN_PASSWORD_LENGTH = 6;

    // Private constructor prevents instantiation of this utility class
    private Validator() {
    }

    // ---------------- Boolean Checkers ----------------
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.trim().length() >= MIN_PASSWORD_LENGTH;
    }

    public static boolean isValidCgpa(double cgpa) {
        return cgpa >= 0.0 && cgpa <= 10.0;
    }

    // ---------------- Enforcing Validation Methods (Throw Exceptions) ----------------
    public static void validateEmail(String email) throws ValidationException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email cannot be empty.");
        }
        if (!isValidEmail(email)) {
            throw new ValidationException("Invalid email format: '" + email + "'. Must be user@domain.com");
        }
    }

    public static void validatePassword(String password) throws ValidationException {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Password cannot be empty.");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
        }
    }

    public static void validateName(String name) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Name cannot be empty.");
        }
        if (name.trim().length() < 2) {
            throw new ValidationException("Name must be at least 2 characters long.");
        }
    }

    public static void validateCgpa(double cgpa) throws ValidationException {
        if (!isValidCgpa(cgpa)) {
            throw new ValidationException("Invalid CGPA: " + cgpa + ". CGPA must be between 0.0 and 10.0.");
        }
    }

    public static void validateEnrollment(String enrollment) throws ValidationException {
        if (enrollment == null || enrollment.trim().isEmpty()) {
            throw new ValidationException("Enrollment number cannot be empty.");
        }
    }

    public static void validatePositiveId(int id, String fieldName) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException(fieldName + " must be a positive integer greater than 0.");
        }
    }

    public static void validateNotNull(Object obj, String fieldName) throws ValidationException {
        if (obj == null) {
            throw new ValidationException(fieldName + " cannot be null.");
        }
    }
}
