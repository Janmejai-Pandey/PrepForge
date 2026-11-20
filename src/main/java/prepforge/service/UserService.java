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

public class UserService {

    private final UserDao userDao;

    public UserService() {
        this.userDao = new InMemoryUserDao();
    }

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User login(String email, String password) throws PrepForgeException {
        Validator.validateEmail(email);
        Validator.validatePassword(password);

        User user = userDao.findByEmail(email);
        if (user == null) {
            throw new ValidationException("Login failed: No account registered with email '" + email + "'.");
        }

        boolean passwordMatches = PasswordHasher.verifyPassword(password, user.getPasswordHash());
        if (!passwordMatches) {
            throw new ValidationException("Login failed: Incorrect password.");
        }
        return user;
    }

    public void registerStudent(Student s, String password) throws PrepForgeException {
        Validator.validateNotNull(s, "Student object");
        Validator.validateName(s.getName());
        Validator.validateEmail(s.getEmail());
        Validator.validateEnrollment(s.getEnrollmentNumber());
        Validator.validateCgpa(s.getCgpa());
        Validator.validatePassword(password);

        User existing = userDao.findByEmail(s.getEmail());
        if (existing != null) {
            throw new ValidationException("Registration rejected: Email '" + s.getEmail() + "' is already registered.");
        }

        String passwordHash = PasswordHasher.hashPassword(password);
        userDao.saveStudent(s, passwordHash);
    }

    public void registerAdmin(Admin a, String password) throws PrepForgeException {
        Validator.validateNotNull(a, "Admin object");
        Validator.validateName(a.getName());
        Validator.validateEmail(a.getEmail());
        Validator.validatePassword(password);

        User existing = userDao.findByEmail(a.getEmail());
        if (existing != null) {
            throw new ValidationException("Registration rejected: Email '" + a.getEmail() + "' is already registered.");
        }

        String passwordHash = PasswordHasher.hashPassword(password);
        userDao.saveAdmin(a, passwordHash);
    }

    public Student getProfile(int studentId) throws PrepForgeException {
        Validator.validatePositiveId(studentId, "Student ID");
        Student student = userDao.findStudentById(studentId);
        if (student == null) {
            throw new ValidationException("Student profile not found for ID: " + studentId);
        }
        return student;
    }

    public void updateProfile(Student s) throws PrepForgeException {
        Validator.validateNotNull(s, "Student");
        Validator.validatePositiveId(s.getId(), "Student ID");
        Validator.validateName(s.getName());
        Validator.validateEmail(s.getEmail());
        Validator.validateCgpa(s.getCgpa());

        Student existing = userDao.findStudentById(s.getId());
        if (existing == null) {
            throw new ValidationException("Cannot update profile: Student with ID " + s.getId() + " does not exist.");
        }

        userDao.updateStudent(s);
    }

    public void addSkill(int studentId, int skillId, String level) throws PrepForgeException {
        Validator.validatePositiveId(studentId, "Student ID");
        Validator.validatePositiveId(skillId, "Skill ID");
        if (level == null || level.trim().isEmpty()) {
            throw new ValidationException("Skill proficiency level cannot be empty.");
        }

        Student student = userDao.findStudentById(studentId);
        if (student == null) {
            throw new ValidationException("Student not found for ID: " + studentId);
        }

        Skill skill = userDao.findSkillById(skillId);
        if (skill == null) {
            throw new ValidationException("Skill not found for ID: " + skillId);
        }

        userDao.addStudentSkill(studentId, skillId, level.trim());
    }

    public void removeSkill(int studentId, int skillId) throws PrepForgeException {
        Validator.validatePositiveId(studentId, "Student ID");
        Validator.validatePositiveId(skillId, "Skill ID");

        Student student = userDao.findStudentById(studentId);
        if (student == null) {
            throw new ValidationException("Student not found for ID: " + studentId);
        }

        userDao.removeStudentSkill(studentId, skillId);
    }

    public List<Skill> getAllSkills() throws PrepForgeException {
        return userDao.findAllSkills();
    }

    public List<Student> getAllStudents() throws PrepForgeException {
        return userDao.findAllStudents();
    }

    public void deleteStudent(int studentId) throws PrepForgeException {
        Validator.validatePositiveId(studentId, "Student ID");

        Student student = userDao.findStudentById(studentId);
        if (student == null) {
            throw new ValidationException("Cannot delete: Student not found with ID: " + studentId);
        }

        userDao.deleteStudent(studentId);
    }
}
