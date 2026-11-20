package prepforge;

import org.junit.jupiter.api.Test;
import prepforge.exception.ValidationException;
import prepforge.util.Validator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test: ValidatorTest
 * 
 * Verifies the correctness of all input validation methods in Validator.java.
 * 
 * Demonstrates:
 * 1. JUNIT 5: Uses @Test annotation to mark test cases.
 * 2. ASSERTIONS: assertTrue, assertFalse, assertThrows.
 * 3. EDGE CASES: Tests valid inputs, empty strings, malformed formats, and boundary values.
 */
public class ValidatorTest {

    // ---------------- Email Validation Tests ----------------
    @Test
    public void testValidEmail_ShouldPass() {
        assertTrue(Validator.isValidEmail("rahul@prepforge.com"));
        assertTrue(Validator.isValidEmail("student.101@college.edu.in"));
        assertTrue(Validator.isValidEmail("admin_user@company.org"));
    }

    @Test
    public void testInvalidEmail_MissingAtSymbol_ShouldFail() {
        assertFalse(Validator.isValidEmail("rahulprepforge.com"));
    }

    @Test
    public void testInvalidEmail_MissingDomain_ShouldFail() {
        assertFalse(Validator.isValidEmail("rahul@"));
        assertFalse(Validator.isValidEmail("rahul@domain"));
    }

    @Test
    public void testInvalidEmail_NullOrEmpty_ShouldFail() {
        assertFalse(Validator.isValidEmail(null));
        assertFalse(Validator.isValidEmail(""));
        assertFalse(Validator.isValidEmail("   "));
    }

    @Test
    public void testValidateEmail_ThrowsValidationExceptionOnInvalid() {
        assertThrows(ValidationException.class, () -> {
            Validator.validateEmail("invalid-email-address");
        });
    }

    // ---------------- Password Validation Tests ----------------
    @Test
    public void testValidPassword_MeetsLengthRequirement() {
        assertTrue(Validator.isValidPassword("password123"));
        assertTrue(Validator.isValidPassword("secret")); // 6 chars
    }

    @Test
    public void testInvalidPassword_TooShort_ShouldFail() {
        assertFalse(Validator.isValidPassword("12345")); // Only 5 chars
        assertFalse(Validator.isValidPassword("a"));
        assertFalse(Validator.isValidPassword(""));
        assertFalse(Validator.isValidPassword(null));
    }

    @Test
    public void testValidatePassword_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> {
            Validator.validatePassword("123");
        });
    }

    // ---------------- CGPA Validation Tests ----------------
    @Test
    public void testValidCgpa_WithinRange() {
        assertTrue(Validator.isValidCgpa(0.0));
        assertTrue(Validator.isValidCgpa(7.5));
        assertTrue(Validator.isValidCgpa(10.0));
    }

    @Test
    public void testInvalidCgpa_Negative_ShouldFail() {
        assertFalse(Validator.isValidCgpa(-0.1));
        assertThrows(ValidationException.class, () -> {
            Validator.validateCgpa(-1.0);
        });
    }

    @Test
    public void testInvalidCgpa_ExceedsTen_ShouldFail() {
        assertFalse(Validator.isValidCgpa(10.1));
        assertThrows(ValidationException.class, () -> {
            Validator.validateCgpa(10.5);
        });
    }

    // ---------------- Name and Identifier Validation Tests ----------------
    @Test
    public void testValidateName_ThrowsOnEmptyOrNull() {
        assertThrows(ValidationException.class, () -> Validator.validateName(""));
        assertThrows(ValidationException.class, () -> Validator.validateName(null));
        assertThrows(ValidationException.class, () -> Validator.validateName("A")); // Only 1 char
    }

    @Test
    public void testValidatePositiveId_ThrowsOnZeroOrNegative() {
        assertThrows(ValidationException.class, () -> Validator.validatePositiveId(0, "ID"));
        assertThrows(ValidationException.class, () -> Validator.validatePositiveId(-5, "ID"));
    }
}
