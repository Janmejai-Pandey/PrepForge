package prepforge;

import prepforge.dao.InMemoryPlacementDao;
import prepforge.dao.InMemoryUserDao;
import prepforge.dao.PlacementDao;
import prepforge.dao.UserDao;
import prepforge.exception.ValidationException;
import prepforge.model.Admin;
import prepforge.model.JobRole;
import prepforge.model.Skill;
import prepforge.model.Student;
import prepforge.model.User;
import prepforge.service.PlacementService;
import prepforge.service.UserService;
import prepforge.util.PasswordHasher;
import prepforge.util.Validator;

import java.util.List;

/**
 * Standalone Test Runner & Verification Suite
 * 
 * Executes comprehensive unit and integration tests covering:
 * - Validator & Regex tests
 * - PasswordHasher (PBKDF2) tests
 * - UserService tests
 * - PlacementService tests
 * 
 * Can be run directly from any terminal or IDE via standard 'java TestRunner'.
 */
public class TestRunner {

    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("       PREPFORGE AUTOMATED TEST SUITE             ");
        System.out.println("==================================================");

        runValidatorTests();
        runPasswordHasherTests();
        runUserServiceTests();
        runPlacementServiceTests();

        System.out.println("\n==================================================");
        System.out.println("TEST SUMMARY");
        System.out.println("  Passed: " + passedTests);
        System.out.println("  Failed: " + failedTests);
        System.out.println("  Total : " + (passedTests + failedTests));
        if (failedTests == 0) {
            System.out.println("  Status: ALL TESTS PASSED SUCCESSFULLY! [OK]");
        } else {
            System.out.println("  Status: SOME TESTS FAILED!");
        }
        System.out.println("==================================================");
    }

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            passedTests++;
        } else {
            System.err.println("  [FAIL] " + testName);
            failedTests++;
        }
    }

    // -------------------------------------------------------------
    // 1. VALIDATOR TESTS
    // -------------------------------------------------------------
    private static void runValidatorTests() {
        System.out.println("\n--- [Suite 1: Validator Tests] ---");

        assertTrue("Valid email accepted", Validator.isValidEmail("rahul@prepforge.com"));
        assertTrue("Invalid email without @ rejected", !Validator.isValidEmail("rahulprepforge.com"));
        assertTrue("Invalid email without domain rejected", !Validator.isValidEmail("rahul@"));
        assertTrue("Null email rejected", !Validator.isValidEmail(null));

        assertTrue("Valid password accepted", Validator.isValidPassword("secret123"));
        assertTrue("Short password rejected", !Validator.isValidPassword("12345"));

        assertTrue("Valid CGPA accepted", Validator.isValidCgpa(8.5));
        assertTrue("Negative CGPA rejected", !Validator.isValidCgpa(-1.0));
        assertTrue("CGPA > 10.0 rejected", !Validator.isValidCgpa(10.5));
    }

    // -------------------------------------------------------------
    // 2. PASSWORD HASHER TESTS
    // -------------------------------------------------------------
    private static void runPasswordHasherTests() {
        System.out.println("\n--- [Suite 2: PasswordHasher (PBKDF2) Tests] ---");
        try {
            String password = "StrongPassword@2026";
            String hash1 = PasswordHasher.hashPassword(password);
            String hash2 = PasswordHasher.hashPassword(password);

            assertTrue("Hash string format contains salt separator ':'", hash1.contains(":"));
            assertTrue("Unique salts produce different hashes for identical passwords", !hash1.equals(hash2));
            assertTrue("Password verification succeeds for correct password", PasswordHasher.verifyPassword(password, hash1));
            assertTrue("Password verification fails for incorrect password", !PasswordHasher.verifyPassword("WrongPassword", hash1));
        } catch (Exception e) {
            assertTrue("PasswordHasher exception: " + e.getMessage(), false);
        }
    }

    // -------------------------------------------------------------
    // 3. USER SERVICE TESTS
    // -------------------------------------------------------------
    private static void runUserServiceTests() {
        System.out.println("\n--- [Suite 3: UserService Tests] ---");
        try {
            UserDao dao = new InMemoryUserDao();
            UserService userService = new UserService(dao);

            // Register student
            Student s = new Student("Amit Kumar", "amit@prepforge.com", "EN101", "CSE", 8.4);
            userService.registerStudent(s, "AmitPass@123");
            assertTrue("Student registered and assigned ID", s.getId() > 0);

            // Login polymorphic test
            User user = userService.login("amit@prepforge.com", "AmitPass@123");
            assertTrue("Polymorphic login returned non-null user", user != null);
            assertTrue("User role is STUDENT", "STUDENT".equals(user.getRole()));

            // Wrong password check
            boolean wrongPassThrew = false;
            try {
                userService.login("amit@prepforge.com", "WrongPassword");
            } catch (ValidationException e) {
                wrongPassThrew = true;
            }
            assertTrue("Wrong password throws ValidationException", wrongPassThrew);

            // Add skill
            userService.addSkill(s.getId(), 1, "Advanced");
            Student updated = userService.getProfile(s.getId());
            assertTrue("Skill 'Java' added to student", updated.hasSkill(1));

            // Register Admin
            Admin admin = new Admin("Dr. Gupta", "admin@prepforge.com", "TPO Cell", "EMP1");
            userService.registerAdmin(admin, "AdminPass@123");
            User adminUser = userService.login("admin@prepforge.com", "AdminPass@123");
            assertTrue("Admin logged in with role ADMIN", "ADMIN".equals(adminUser.getRole()));

        } catch (Exception e) {
            assertTrue("UserService exception: " + e.getMessage(), false);
        }
    }

    // -------------------------------------------------------------
    // 4. PLACEMENT SERVICE TESTS
    // -------------------------------------------------------------
    private static void runPlacementServiceTests() {
        System.out.println("\n--- [Suite 4: PlacementService Tests] ---");
        try {
            UserDao userDao = new InMemoryUserDao();
            PlacementDao placementDao = new InMemoryPlacementDao();

            UserService userService = new UserService(userDao);
            PlacementService placementService = new PlacementService(placementDao, userDao);

            // Register qualified student (CGPA: 8.5, Skills: Java[1] and Data Structures[6])
            Student qualified = new Student("Pooja", "pooja@prep.com", "EN202", "CSE", 8.5);
            userService.registerStudent(qualified, "Pooja@123");
            userService.addSkill(qualified.getId(), 1, "Advanced"); // Java
            userService.addSkill(qualified.getId(), 6, "Advanced"); // Data Structures

            // Google SDE requires CGPA 8.0, Java, Data Structures
            List<JobRole> eligibleRoles = placementService.getEligibleRoles(qualified.getId());
            assertTrue("Qualified student matched to eligible job roles", eligibleRoles.size() >= 1);

            // Apply to Google SDE (Role ID 1)
            placementService.applyToRole(qualified.getId(), 1);
            assertTrue("Student successfully applied to role", qualified.hasAppliedToRole(1));

            // Duplicate application check
            boolean duplicateBlocked = false;
            try {
                placementService.applyToRole(qualified.getId(), 1);
            } catch (ValidationException e) {
                duplicateBlocked = true;
            }
            assertTrue("Duplicate application blocked by ValidationException", duplicateBlocked);

            // Ineligible student (CGPA 6.0 trying to apply to Google SDE)
            Student lowCgpaStudent = new Student("LowCgpa", "low@prep.com", "EN303", "IT", 6.0);
            userService.registerStudent(lowCgpaStudent, "LowPass@123");
            boolean ineligibleBlocked = false;
            try {
                placementService.applyToRole(lowCgpaStudent.getId(), 1);
            } catch (ValidationException e) {
                ineligibleBlocked = true;
            }
            assertTrue("Ineligible student application blocked", ineligibleBlocked);

        } catch (Exception e) {
            assertTrue("PlacementService exception: " + e.getMessage(), false);
        }
    }
}
