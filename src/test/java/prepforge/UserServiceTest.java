package prepforge;

import java.util.List;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserServiceTest {

    private UserService userService;
    private UserDao userDao;

    @BeforeEach
    public void setUp() {
        userDao = new InMemoryUserDao();
        userService = new UserService(userDao);
    }

    @Test
    public void testRegisterStudent_Success() throws PrepForgeException {
        Student s = new Student("Amit Kumar", "amit@prepforge.com", "EN2024_01", "CSE", 8.5);
        userService.registerStudent(s, "Password@123");

        assertTrue(s.getId() > 0);
        User fetched = userService.login("amit@prepforge.com", "Password@123");
        assertNotNull(fetched);
        assertEquals("Student", fetched.getRole());
        assertEquals("Amit Kumar", fetched.getName());
    }

    @Test
    public void testRegisterStudent_DuplicateEmail_ThrowsValidationException() throws PrepForgeException {
        Student s1 = new Student("Sneha Rao", "sneha@prepforge.com", "EN2024_02", "ECE", 9.1);
        userService.registerStudent(s1, "SecurePass1");

        Student s2 = new Student("Sneha Duplicate", "sneha@prepforge.com", "EN2024_03", "IT", 8.0);
        assertThrows(ValidationException.class, () -> {
            userService.registerStudent(s2, "SecurePass2");
        });
    }

    @Test
    public void testRegisterAdmin_SuccessAndLogin() throws PrepForgeException {
        Admin admin = new Admin("Dr. Sharma", "sharma@prepforge.com", "ADM_001", "Dean Placement");
        userService.registerAdmin(admin, "Admin@Pass2024");

        assertTrue(admin.getId() > 0);
        User loggedAdmin = userService.login("sharma@prepforge.com", "Admin@Pass2024");
        assertNotNull(loggedAdmin);
        assertEquals("Admin", loggedAdmin.getRole());
        assertEquals("Dean Placement", ((Admin) loggedAdmin).getDepartment());
    }

    @Test
    public void testLogin_WrongPassword_ThrowsValidationException() throws PrepForgeException {
        Student s = new Student("Vikas Patel", "vikas@prepforge.com", "EN2024_04", "MECH", 7.8);
        userService.registerStudent(s, "CorrectPassword");

        assertThrows(ValidationException.class, () -> {
            userService.login("vikas@prepforge.com", "WrongPassword");
        });
    }

    @Test
    public void testLogin_NonExistentEmail_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> {
            userService.login("nonexistent@domain.com", "AnyPassword123");
        });
    }

    @Test
    public void testUpdateProfile_Success() throws PrepForgeException {
        Student s = new Student("Karan Mehra", "karan@prepforge.com", "EN2024_05", "CSE", 7.2);
        userService.registerStudent(s, "Password123");

        s.setCgpa(8.4);
        s.setBranch("AI & Data Science");
        userService.updateProfile(s);

        Student updated = userService.getProfile(s.getId());
        assertEquals(8.4, updated.getCgpa(), 0.001);
        assertEquals("AI & Data Science", updated.getBranch());
    }

    @Test
    public void testAddAndRemoveSkill() throws PrepForgeException {
        Student s = new Student("Pooja Verma", "pooja@prepforge.com", "EN2024_06", "CSE", 8.8);
        userService.registerStudent(s, "Password123");

        List<Skill> skills = userService.getAllSkills();
        assertTrue(skills.size() >= 2);

        int javaSkillId = skills.get(0).getId();
        userService.addSkill(s.getId(), javaSkillId, "Advanced");

        Student profile = userService.getProfile(s.getId());
        assertEquals(1, profile.getSkills().size());

        userService.removeSkill(s.getId(), javaSkillId);
        profile = userService.getProfile(s.getId());
        assertEquals(0, profile.getSkills().size());
    }

    @Test
    public void testDeleteStudent_AdminCapability() throws PrepForgeException {
        Student s = new Student("Rohan Roy", "rohan@prepforge.com", "EN2024_07", "EE", 7.0);
        userService.registerStudent(s, "Password123");

        int studentId = s.getId();
        userService.deleteStudent(studentId);

        assertThrows(ValidationException.class, () -> {
            userService.getProfile(studentId);
        });
    }
}
