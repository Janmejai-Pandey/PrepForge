package prepforge;

import org.junit.jupiter.api.Test;
import prepforge.exception.ValidationException;
import prepforge.util.Validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidatorTest {

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
        assertThrows(ValidationException.class, () -> {
            Validator.validateEmail("");
        });
        assertThrows(ValidationException.class, () -> {
            Validator.validateEmail(null);
        });
    }

    @Test
    public void testPasswordValidation_MinimumLengthEnforced() {
        assertFalse(Validator.isValidPassword("12345"));
        assertTrue(Validator.isValidPassword("123456"));
        assertTrue(Validator.isValidPassword("SecureP@ssword2024"));
    }

    @Test
    public void testValidatePassword_ThrowsValidationExceptionOnShort() {
        assertThrows(ValidationException.class, () -> {
            Validator.validatePassword("abc");
        });
        assertThrows(ValidationException.class, () -> {
            Validator.validatePassword("");
        });
        assertThrows(ValidationException.class, () -> {
            Validator.validatePassword(null);
        });
    }

    @Test
    public void testCgpaValidation_BoundaryChecks() {
        assertTrue(Validator.isValidCgpa(0.0));
        assertTrue(Validator.isValidCgpa(7.5));
        assertTrue(Validator.isValidCgpa(10.0));

        assertFalse(Validator.isValidCgpa(-0.1));
        assertFalse(Validator.isValidCgpa(10.01));
        assertFalse(Validator.isValidCgpa(12.5));
    }

    @Test
    public void testValidateCgpa_ThrowsValidationExceptionOnOutOfRange() {
        assertThrows(ValidationException.class, () -> {
            Validator.validateCgpa(-1.0);
        });
        assertThrows(ValidationException.class, () -> {
            Validator.validateCgpa(10.5);
        });
    }

    @Test
    public void testValidateName_ThrowsValidationExceptionOnEmptyOrShort() {
        assertThrows(ValidationException.class, () -> {
            Validator.validateName("");
        });
        assertThrows(ValidationException.class, () -> {
            Validator.validateName("A");
        });
        assertThrows(ValidationException.class, () -> {
            Validator.validateName(null);
        });
    }

    @Test
    public void testValidatePositiveId_ThrowsValidationExceptionOnNonPositive() {
        assertThrows(ValidationException.class, () -> {
            Validator.validatePositiveId(0, "Test ID");
        });
        assertThrows(ValidationException.class, () -> {
            Validator.validatePositiveId(-5, "Test ID");
        });
    }
}
