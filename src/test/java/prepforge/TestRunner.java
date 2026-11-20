package prepforge;

import java.util.List;
import prepforge.dao.CompanyDao;
import prepforge.dao.InMemoryCompanyDao;
import prepforge.dao.InMemoryUserDao;
import prepforge.dao.UserDao;
import prepforge.exception.DataAccessException;
import prepforge.exception.ValidationException;
import prepforge.model.Admin;
import prepforge.model.Company;
import prepforge.model.JobRole;
import prepforge.model.Skill;
import prepforge.model.Student;
import prepforge.model.User;
import prepforge.service.PlacementService;
import prepforge.service.UserService;
import prepforge.util.PasswordHasher;
import prepforge.util.Validator;

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
        runDaoTests();

        System.out.println("\n==================================================");
        System.out.println("TEST SUMMARY");
        System.out.println("  Passed: " + passedTests);
        System.out.println("  Failed: " + failedTests);
        System.out.println("  Total : " + (passedTests + failedTests));
        System.out.println("==================================================");

        if (failedTests > 0) {
            System.exit(1);
        }
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

    private static void runValidatorTests() {
        System.out.println("\n>>> 1. RUNNING VALIDATOR TESTS");

        assertTrue("Valid email format accepted", Validator.isValidEmail("student@prepforge.com"));
        assertTrue("Invalid email without @ rejected", !Validator.isValidEmail("studentprepforge.com"));
        assertTrue("Invalid email without domain rejected", !Validator.isValidEmail("student@"));
        assertTrue("Empty email rejected", !Validator.isValidEmail(""));
        assertTrue("Null email rejected", !Validator.isValidEmail(null));

        assertTrue("Password >= 6 chars valid", Validator.isValidPassword("secret123"));
        assertTrue("Password < 6 chars invalid", !Validator.isValidPassword("12345"));
        assertTrue("Null password invalid", !Validator.isValidPassword(null));

        assertTrue("CGPA 8.5 in valid range (0-10)", Validator.isValidCgpa(8.5));
        assertTrue("CGPA 0.0 boundary valid", Validator.isValidCgpa(0.0));
        assertTrue("CGPA 10.0 boundary valid", Validator.isValidCgpa(10.0));
        assertTrue("CGPA -1.0 negative invalid", !Validator.isValidCgpa(-1.0));
        assertTrue("CGPA 10.5 above 10 invalid", !Validator.isValidCgpa(10.5));
    }

    private static void runPasswordHasherTests() {
        System.out.println("\n>>> 2. RUNNING PASSWORD HASHER (PBKDF2) TESTS");

        try {
            String rawPassword = "CandidatePassword@2024";
            String hash1 = PasswordHasher.hashPassword(rawPassword);
            String hash2 = PasswordHasher.hashPassword(rawPassword);

            assertTrue("Hash format is salt:hash", hash1.contains(":") && hash1.split(":").length == 2);
            assertTrue("Salts differ between separate hashes", !hash1.equals(hash2));
            assertTrue("Correct password verification succeeds", PasswordHasher.verifyPassword(rawPassword, hash1));
            assertTrue("Wrong password verification fails", !PasswordHasher.verifyPassword("WrongPassword!", hash1));
        } catch (Exception e) {
            assertTrue("PasswordHasher exception: " + e.getMessage(), false);
        }
    }

    private static void runUserServiceTests() {
        System.out.println("\n>>> 3. RUNNING USER SERVICE TESTS");

        UserDao userDao = new InMemoryUserDao();
        UserService userService = new UserService(userDao);

        try {
            Student s1 = new Student("Ananya Sharma", "ananya@prepforge.com", "EN2024_01", "CSE", 8.9);
            userService.registerStudent(s1, "AnanyaPass123");
            assertTrue("Student registration assigns positive ID", s1.getId() > 0);

            User loggedIn = userService.login("ananya@prepforge.com", "AnanyaPass123");
            assertTrue("Login retrieves valid user object", loggedIn != null);
            assertTrue("Polymorphic role is 'Student'", "Student".equals(loggedIn.getRole()));

            Admin admin = new Admin("Dr. Rajesh Gupta", "rajesh@prepforge.com", "ADM_101", "Head T&P");
            userService.registerAdmin(admin, "AdminGuptaPass");
            assertTrue("Admin registration assigns positive ID", admin.getId() > 0);

            User loggedAdmin = userService.login("rajesh@prepforge.com", "AdminGuptaPass");
            assertTrue("Admin login returns Admin role", "Admin".equals(loggedAdmin.getRole()));

            boolean duplicateRejected = false;
            try {
                Student dup = new Student("Fake Ananya", "ananya@prepforge.com", "EN2024_99", "IT", 7.0);
                userService.registerStudent(dup, "AnyPassword123");
            } catch (ValidationException ve) {
                duplicateRejected = true;
            }
            assertTrue("Duplicate email registration rejected", duplicateRejected);

            List<Skill> allSkills = userService.getAllSkills();
            assertTrue("Skill catalog available", allSkills.size() >= 2);
            userService.addSkill(s1.getId(), allSkills.get(0).getId(), "Advanced");
            Student updated = userService.getProfile(s1.getId());
            assertTrue("Student skill added successfully", updated.getSkills().size() == 1);

            userService.removeSkill(s1.getId(), allSkills.get(0).getId());
            updated = userService.getProfile(s1.getId());
            assertTrue("Student skill removed successfully", updated.getSkills().isEmpty());

            userService.deleteStudent(s1.getId());
            boolean deletedFound = false;
            try {
                userService.getProfile(s1.getId());
                deletedFound = true;
            } catch (ValidationException ve) {
                deletedFound = false;
            }
            assertTrue("Deleted student no longer retrievable", !deletedFound);

        } catch (Exception e) {
            assertTrue("UserService unexpected exception: " + e.getMessage(), false);
        }
    }

    private static void runPlacementServiceTests() {
        System.out.println("\n>>> 4. RUNNING PLACEMENT SERVICE TESTS");

        CompanyDao companyDao = new InMemoryCompanyDao();
        UserDao userDao = new InMemoryUserDao();
        PlacementService placementService = new PlacementService(companyDao, userDao);
        UserService userService = new UserService(userDao);

        try {
            assertTrue("Initial company catalog populated", placementService.getAllCompanies().size() >= 4);

            Student highCgpaStudent = new Student("Priya Patel", "priya@prepforge.com", "EN2024_02", "CSE", 9.2);
            userService.registerStudent(highCgpaStudent, "PriyaPass123");

            Skill javaSkill = null;
            for (Skill s : userService.getAllSkills()) {
                if ("Java".equalsIgnoreCase(s.getName())) {
                    javaSkill = s;
                    break;
                }
            }
            if (javaSkill != null) {
                userService.addSkill(highCgpaStudent.getId(), javaSkill.getId(), "Expert");
            }

            List<JobRole> eligibleRoles = placementService.getEligibleRoles(highCgpaStudent.getId());
            assertTrue("Eligible roles calculated correctly", !eligibleRoles.isEmpty());

            JobRole appliedRole = eligibleRoles.get(0);
            placementService.applyToRole(highCgpaStudent.getId(), appliedRole.getRoleId());
            assertTrue("Application recorded in DAO", placementService.hasApplied(highCgpaStudent.getId(), appliedRole.getRoleId()));

            boolean duplicateAppRejected = false;
            try {
                placementService.applyToRole(highCgpaStudent.getId(), appliedRole.getRoleId());
            } catch (ValidationException ve) {
                duplicateAppRejected = true;
            }
            assertTrue("Duplicate application to same role rejected", duplicateAppRejected);

        } catch (Exception e) {
            assertTrue("PlacementService unexpected exception: " + e.getMessage(), false);
        }
    }

    private static void runDaoTests() {
        System.out.println("\n>>> 5. RUNNING DAO TESTS (USERDAO & COMPANYDAO)");

        UserDao userDao = new InMemoryUserDao();
        CompanyDao companyDao = new InMemoryCompanyDao();

        try {
            Student s = new Student("Siddharth Singh", "sid@prepforge.com", "EN2024_10", "CSE", 8.7);
            userDao.saveStudent(s, "hash123");
            assertTrue("UserDao saves and assigns ID to student", s.getId() > 0);
            assertTrue("UserDao finds student by email", userDao.findByEmail("sid@prepforge.com") != null);
            assertTrue("UserDao finds student by ID", userDao.findStudentById(s.getId()) != null);

            Admin admin = new Admin("Dr. Neha Kapoor", "neha@prepforge.com", "ADM_202", "Training & Placement");
            userDao.saveAdmin(admin, "adminhash");
            assertTrue("UserDao saves admin", admin.getId() > 0);
            assertTrue("UserDao finds admin by email", userDao.findByEmail("neha@prepforge.com") != null);

            s.setCgpa(9.0);
            userDao.updateStudent(s);
            assertTrue("UserDao updates student record", userDao.findStudentById(s.getId()).getCgpa() == 9.0);

            List<Skill> skills = userDao.findAllSkills();
            assertTrue("UserDao finds all skills", skills.size() >= 2);
            userDao.addStudentSkill(s.getId(), skills.get(0).getId(), "Expert");
            assertTrue("UserDao associates skill with student", userDao.findStudentById(s.getId()).getSkills().size() == 1);

            userDao.removeStudentSkill(s.getId(), skills.get(0).getId());
            assertTrue("UserDao removes student skill", userDao.findStudentById(s.getId()).getSkills().isEmpty());

            userDao.deleteStudent(s.getId());
            assertTrue("UserDao deletes student", userDao.findStudentById(s.getId()) == null);

            List<Company> companies = companyDao.findAllCompanies();
            assertTrue("CompanyDao finds all companies", companies.size() >= 4);

            Company comp = new Company("Oracle", "Cloud database leader", "Austin, TX", "Database");
            companyDao.saveCompany(comp);
            assertTrue("CompanyDao persists company with generated ID", comp.getCompanyId() > 0);
            assertTrue("CompanyDao retrieves company by ID", companyDao.findCompanyById(comp.getCompanyId()) != null);

            List<JobRole> roles = companyDao.findAllJobRoles();
            assertTrue("CompanyDao finds all job roles", !roles.isEmpty());

            JobRole role = roles.get(0);
            int testStudentId = 777;
            assertTrue("CompanyDao checks not applied initially", !companyDao.hasApplied(testStudentId, role.getRoleId()));

            companyDao.saveApplication(testStudentId, role.getRoleId());
            assertTrue("CompanyDao checks hasApplied after save", companyDao.hasApplied(testStudentId, role.getRoleId()));
            assertTrue("CompanyDao returns applied role IDs", companyDao.findAppliedRoleIds(testStudentId).contains(role.getRoleId()));

            boolean duplicateEmailFails = false;
            try {
                Student sDup = new Student("Dup User", "neha@prepforge.com", "EN_DUP", "CSE", 8.0);
                userDao.saveStudent(sDup, "hash");
            } catch (DataAccessException dae) {
                duplicateEmailFails = true;
            }
            assertTrue("UserDao rejects duplicate email with DataAccessException", duplicateEmailFails);

        } catch (Exception e) {
            assertTrue("DaoTest unexpected exception: " + e.getMessage(), false);
        }
    }
}
