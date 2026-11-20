package prepforge.service;

import java.util.List;
import prepforge.dao.InMemoryUserDao;
import prepforge.dao.UserDao;
import prepforge.exception.DataAccessException;
import prepforge.exception.PrepForgeException;
import prepforge.exception.ValidationException;
import prepforge.model.Admin;
import prepforge.model.Skill;
import prepforge.model.Student;
import prepforge.model.User;
import prepforge.util.PasswordHasher;
import prepforge.util.Validator;

/**
 * Service Class: UserService
 * 
 * Implements core business logic for user management, student profiles,
 * authentication, and skills management.
 * 
 * Strict Architectural Rules Followed:
 * 1. UI -> Service -> DAO -> Database (Zero SQL in this class).
 * 2. Input validation on every public method using Validator.
 * 3. Secure password hashing using PasswordHasher (PBKDF2).
 * 4. Checked custom exceptions (ValidationException, DataAccessException, PrepForgeException).
 */
public class UserService {

    // DAO dependency (Separation of Concerns: Service delegates database operations to DAO)
    private final UserDao userDao;

    // Default constructor uses standard DAO implementation
    public UserService() {
        this.userDao = new InMemoryUserDao();
    }

    // Constructor with Dependency Injection (useful for unit testing with mocks)
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    // ------------------------------------------------------------------------
    // 1. LOGIN
    // ------------------------------------------------------------------------
    public User login(String email, String password) throws PrepForgeException {
        // Step 1: Input Validation
        Validator.validateEmail(email);
        Validator.validatePassword(password);

        // Step 2: Fetch user record via DAO (No SQL in Service!)
        User user = userDao.findByEmail(email);
        if (user == null) {
            throw new ValidationException("Login failed: No account registered with email '" + email + "'.");
        }

        // Step 3: Verify PBKDF2 password hash
        boolean passwordMatches = PasswordHasher.verifyPassword(password, user.getPasswordHash());
        if (!passwordMatches) {
            throw new ValidationException("Login failed: Incorrect password.");
        }

        // Step 4: Return authenticated polymorphic User object (Student or Admin)
        return user;
    }

    // ------------------------------------------------------------------------
    // 2. REGISTER STUDENT
    // ------------------------------------------------------------------------
    public void registerStudent(Student s, String password) throws PrepForgeException {
        // Step 1: Validate all student inputs
        Validator.validateNotNull(s, "Student");
        Validator.validateName(s.getName());
        Validator.validateEmail(s.getEmail());
        if (s.getEnrollmentNumber() != null && !s.getEnrollmentNumber().trim().isEmpty()) {
            Validator.validateEnrollment(s.getEnrollmentNumber());
        }
        Validator.validateCgpa(s.getCgpa());
        Validator.validatePassword(password);

        // Step 2: Check for existing account
        if (userDao.findByEmail(s.getEmail()) != null) {
            throw new ValidationException("Registration failed: Email '" + s.getEmail() + "' is already registered.");
        }

        // Step 3: Hash password with PBKDF2 and random salt
        String secureHash = PasswordHasher.hashPassword(password);

        // Step 4: Delegate saving to DAO
        userDao.saveStudent(s, secureHash);
    }

    // ------------------------------------------------------------------------
    // 3. REGISTER ADMIN
    // ------------------------------------------------------------------------
    public void registerAdmin(Admin a, String password) throws PrepForgeException {
        // Step 1: Validate admin inputs
        Validator.validateNotNull(a, "Admin");
        Validator.validateName(a.getName());
        Validator.validateEmail(a.getEmail());
        Validator.validatePassword(password);
        if (a.getDepartment() == null || a.getDepartment().trim().isEmpty()) {
            throw new ValidationException("Department cannot be empty.");
        }
        if (a.getEmployeeCode() == null || a.getEmployeeCode().trim().isEmpty()) {
            throw new ValidationException("Employee code cannot be empty.");
        }

        // Step 2: Check for existing account
        if (userDao.findByEmail(a.getEmail()) != null) {
            throw new ValidationException("Registration failed: Email '" + a.getEmail() + "' is already registered.");
        }

        // Step 3: Hash password
        String secureHash = PasswordHasher.hashPassword(password);

        // Step 4: Save via DAO
        userDao.saveAdmin(a, secureHash);
    }

    // ------------------------------------------------------------------------
    // 4. GET STUDENT PROFILE
    // ------------------------------------------------------------------------
    public Student getProfile(int studentId) throws PrepForgeException {
        Validator.validatePositiveId(studentId, "Student ID");

        Student student = userDao.findStudentById(studentId);
        if (student == null) {
            throw new ValidationException("Student profile not found for ID: " + studentId);
        }
        return student;
    }

    // ------------------------------------------------------------------------
    // 5. UPDATE PROFILE
    // ------------------------------------------------------------------------
    public void updateProfile(Student s) throws PrepForgeException {
        Validator.validateNotNull(s, "Student");
        Validator.validatePositiveId(s.getId(), "Student ID");
        Validator.validateName(s.getName());
        Validator.validateEmail(s.getEmail());
        if (s.getEnrollmentNumber() != null && !s.getEnrollmentNumber().trim().isEmpty()) {
            Validator.validateEnrollment(s.getEnrollmentNumber());
        }
        Validator.validateCgpa(s.getCgpa());

        userDao.updateStudent(s);
    }

    // ------------------------------------------------------------------------
    // 6. ADD SKILL TO STUDENT
    // ------------------------------------------------------------------------
    public void addSkill(int studentId, int skillId, String level) throws PrepForgeException {
        Validator.validatePositiveId(studentId, "Student ID");
        Validator.validatePositiveId(skillId, "Skill ID");
        if (level == null || level.trim().isEmpty()) {
            level = "Beginner";
        }

        userDao.addStudentSkill(studentId, skillId, level);
    }

    // ------------------------------------------------------------------------
    // 7. REMOVE SKILL FROM STUDENT
    // ------------------------------------------------------------------------
    public void removeSkill(int studentId, int skillId) throws PrepForgeException {
        Validator.validatePositiveId(studentId, "Student ID");
        Validator.validatePositiveId(skillId, "Skill ID");

        userDao.removeStudentSkill(studentId, skillId);
    }

    // ------------------------------------------------------------------------
    // 8. GET ALL SKILLS
    // ------------------------------------------------------------------------
    public List<Skill> getAllSkills() throws PrepForgeException {
        return userDao.findAllSkills();
    }

    // ------------------------------------------------------------------------
    // 9. GET ALL STUDENTS
    // ------------------------------------------------------------------------
    public List<Student> getAllStudents() throws PrepForgeException {
        return userDao.findAllStudents();
    }

    // ------------------------------------------------------------------------
    // 10. DELETE STUDENT
    // ------------------------------------------------------------------------
    public void deleteStudent(int studentId) throws PrepForgeException {
        Validator.validatePositiveId(studentId, "Student ID");
        userDao.deleteStudent(studentId);
    }
}
