package prepforge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import prepforge.dao.InMemoryUserDao;
import prepforge.dao.UserDao;
import prepforge.exception.PrepForgeException;
import prepforge.exception.ValidationException;
import prepforge.model.Admin;
import prepforge.model.Skill;
import prepforge.model.Student;
import prepforge.model.User;
import prepforge.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Test: UserServiceTest
 * 
 * Verifies business logic in UserService: registration, login, profile updates,
 * skill assignments, and deletion.
 * 
 * Demonstrates:
 * 1. TEST ISOLATION: @BeforeEach creates a fresh UserService with a fresh DAO
 *    before every single test so tests do not interfere with each other.
 * 2. ASSERTIONS: assertNotNull, assertEquals, assertTrue, assertThrows.
 * 3. BUSINESS RULES: Verifies duplicate rejection, password hashing verification, and student profiles.
 */
public class UserServiceTest {

    private UserService userService;
    private UserDao userDao;

    @BeforeEach
    public void setUp() {
        // Fresh isolated DAO and Service before each test
        userDao = new InMemoryUserDao();
        userService = new UserService(userDao);
    }

    // ---------------- Registration Tests ----------------
    @Test
    public void testRegisterStudent_Success() throws PrepForgeException {
        Student s = new Student("Amit Kumar", "amit@prepforge.com", "EN2024_01", "CSE", 8.5);
        userService.registerStudent(s, "Password@123");

        assertTrue(s.getId() > 0, "Student ID should be assigned upon registration.");

        Student fetched = userService.getProfile(s.getId());
        assertNotNull(fetched);
        assertEquals("Amit Kumar", fetched.getName());
        assertEquals("amit@prepforge.com", fetched.getEmail());
    }

    @Test
    public void testRegisterStudent_DuplicateEmail_ThrowsValidationException() throws PrepForgeException {
        Student s1 = new Student("First User", "duplicate@prepforge.com", "EN1", "CSE", 8.0);
        userService.registerStudent(s1, "Pass@123");

        Student s2 = new Student("Second User", "duplicate@prepforge.com", "EN2", "IT", 7.5);

        assertThrows(ValidationException.class, () -> {
            userService.registerStudent(s2, "Pass@456");
        });
    }

    @Test
    public void testRegisterAdmin_Success() throws PrepForgeException {
        Admin admin = new Admin("Dr. Sharma", "tpo@prepforge.com", "Placement Cell", "EMP_101");
        userService.registerAdmin(admin, "AdminPass@2026");

        assertTrue(admin.getId() > 0);
        User loggedIn = userService.login("tpo@prepforge.com", "AdminPass@2026");
        assertEquals("ADMIN", loggedIn.getRole());
    }

    // ---------------- Login Tests ----------------
    @Test
    public void testLogin_Success_ReturnsPolymorphicUser() throws PrepForgeException {
        Student s = new Student("Sneha Roy", "sneha@prepforge.com", "EN2024_03", "ECE", 9.1);
        userService.registerStudent(s, "SnehaSecret@1");

        User user = userService.login("sneha@prepforge.com", "SnehaSecret@1");
        assertNotNull(user);
        assertEquals("Sneha Roy", user.getName());
        assertEquals("STUDENT", user.getRole());
    }

    @Test
    public void testLogin_WrongPassword_ThrowsValidationException() throws PrepForgeException {
        Student s = new Student("Vikram", "vikram@prepforge.com", "EN2024_04", "ME", 7.8);
        userService.registerStudent(s, "CorrectPassword@1");

        assertThrows(ValidationException.class, () -> {
            userService.login("vikram@prepforge.com", "WrongPassword@1");
        });
    }

    @Test
    public void testLogin_UnregisteredEmail_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> {
            userService.login("nobody@prepforge.com", "AnyPassword@123");
        });
    }

    // ---------------- Profile & Skills Tests ----------------
    @Test
    public void testUpdateProfile_Success() throws PrepForgeException {
        Student s = new Student("Rohan", "rohan@prepforge.com", "EN2024_05", "CSE", 7.0);
        userService.registerStudent(s, "RohanPass@1");

        // Update profile details
        s.setName("Rohan Verma");
        s.setCgpa(8.2);
        userService.updateProfile(s);

        Student updated = userService.getProfile(s.getId());
        assertEquals("Rohan Verma", updated.getName());
        assertEquals(8.2, updated.getCgpa(), 0.001);
    }

    @Test
    public void testAddAndRemoveSkill() throws PrepForgeException {
        Student s = new Student("Neha", "neha@prepforge.com", "EN2024_06", "IT", 8.8);
        userService.registerStudent(s, "NehaPass@1");

        // Skill 1 is "Java" in our seeded catalog
        userService.addSkill(s.getId(), 1, "Advanced");

        Student withSkill = userService.getProfile(s.getId());
        assertTrue(withSkill.hasSkill(1));
        assertEquals("Advanced", withSkill.getSkillLevel(1));

        // Remove the skill
        userService.removeSkill(s.getId(), 1);
        Student afterRemove = userService.getProfile(s.getId());
        assertFalse(afterRemove.hasSkill(1));
    }

    @Test
    public void testGetAllStudentsAndSkills() throws PrepForgeException {
        List<Skill> allSkills = userService.getAllSkills();
        assertTrue(allSkills.size() >= 5, "Initial skill catalog should contain standard skills.");

        Student s1 = new Student("Test1", "test1@prep.com", "EN_T1", "CSE", 8.0);
        Student s2 = new Student("Test2", "test2@prep.com", "EN_T2", "IT", 7.5);
        userService.registerStudent(s1, "Pass@123");
        userService.registerStudent(s2, "Pass@123");

        List<Student> allStudents = userService.getAllStudents();
        assertTrue(allStudents.size() >= 2);
    }

    @Test
    public void testDeleteStudent_Success() throws PrepForgeException {
        Student s = new Student("DeleteMe", "delete@prep.com", "EN_DEL", "CSE", 6.5);
        userService.registerStudent(s, "DeletePass@1");

        userService.deleteStudent(s.getId());

        assertThrows(ValidationException.class, () -> {
            userService.getProfile(s.getId());
        });
    }
}
