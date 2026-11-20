package prepforge.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import prepforge.exception.DataAccessException;
import prepforge.model.Admin;
import prepforge.model.Skill;
import prepforge.model.Student;
import prepforge.model.User;

/**
 * Implementation: InMemoryUserDao
 * 
 * Implements the UserDao contract.
 * Acts as an in-memory repository (or mock data layer) adhering strictly
 * to the DAO pattern: UI -> Service -> DAO -> Database.
 */
public class InMemoryUserDao implements UserDao {

    private final Map<Integer, User> userDatabase = new HashMap<>();
    private final Map<String, User> emailIndex = new HashMap<>();
    private final Map<Integer, Skill> skillCatalog = new HashMap<>();
    private int nextUserId = 1;
    private int nextSkillId = 1;

    public InMemoryUserDao() {
        seedDefaultSkills();
    }

    private void seedDefaultSkills() {
        addCatalogSkill("Java", "Programming");
        addCatalogSkill("Python", "Programming");
        addCatalogSkill("SQL", "Database");
        addCatalogSkill("Spring Boot", "Backend");
        addCatalogSkill("React", "Frontend");
        addCatalogSkill("Data Structures", "Core CS");
    }

    private void addCatalogSkill(String name, String category) {
        Skill skill = new Skill(nextSkillId, name, category);
        skillCatalog.put(nextSkillId, skill);
        nextSkillId++;
    }

    @Override
    public User findByEmail(String email) throws DataAccessException {
        if (email == null) return null;
        return emailIndex.get(email.trim().toLowerCase());
    }

    @Override
    public void saveStudent(Student student, String passwordHash) throws DataAccessException {
        if (student == null) {
            throw new DataAccessException("Cannot save null student.");
        }
        if (emailIndex.containsKey(student.getEmail().toLowerCase())) {
            throw new DataAccessException("User with email '" + student.getEmail() + "' already exists.");
        }

        if (student.getId() <= 0) {
            student.setId(nextUserId++);
        }
        student.setPasswordHash(passwordHash);

        userDatabase.put(student.getId(), student);
        emailIndex.put(student.getEmail().toLowerCase(), student);
    }

    @Override
    public void saveAdmin(Admin admin, String passwordHash) throws DataAccessException {
        if (admin == null) {
            throw new DataAccessException("Cannot save null admin.");
        }
        if (emailIndex.containsKey(admin.getEmail().toLowerCase())) {
            throw new DataAccessException("User with email '" + admin.getEmail() + "' already exists.");
        }

        if (admin.getId() <= 0) {
            admin.setId(nextUserId++);
        }
        admin.setPasswordHash(passwordHash);

        userDatabase.put(admin.getId(), admin);
        emailIndex.put(admin.getEmail().toLowerCase(), admin);
    }

    @Override
    public Student findStudentById(int studentId) throws DataAccessException {
        User u = userDatabase.get(studentId);
        if (u instanceof Student) {
            return (Student) u;
        }
        return null;
    }

    @Override
    public void updateStudent(Student student) throws DataAccessException {
        if (student == null || !userDatabase.containsKey(student.getId())) {
            throw new DataAccessException("Student with ID " + (student != null ? student.getId() : "null") + " not found.");
        }
        userDatabase.put(student.getId(), student);
        emailIndex.put(student.getEmail().toLowerCase(), student);
    }

    @Override
    public void addStudentSkill(int studentId, int skillId, String level) throws DataAccessException {
        Student student = findStudentById(studentId);
        if (student == null) {
            throw new DataAccessException("Student not found with ID: " + studentId);
        }
        Skill skill = skillCatalog.get(skillId);
        if (skill == null) {
            throw new DataAccessException("Skill not found with ID: " + skillId);
        }
        student.addSkill(skill, level);
    }

    @Override
    public void removeStudentSkill(int studentId, int skillId) throws DataAccessException {
        Student student = findStudentById(studentId);
        if (student == null) {
            throw new DataAccessException("Student not found with ID: " + studentId);
        }
        student.removeSkill(skillId);
    }

    @Override
    public List<Skill> findAllSkills() throws DataAccessException {
        return new ArrayList<>(skillCatalog.values());
    }

    @Override
    public Skill findSkillById(int skillId) throws DataAccessException {
        return skillCatalog.get(skillId);
    }

    @Override
    public List<Student> findAllStudents() throws DataAccessException {
        List<Student> students = new ArrayList<>();
        for (User u : userDatabase.values()) {
            if (u instanceof Student) {
                students.add((Student) u);
            }
        }
        return students;
    }

    @Override
    public void deleteStudent(int studentId) throws DataAccessException {
        User u = userDatabase.remove(studentId);
        if (u != null) {
            emailIndex.remove(u.getEmail().toLowerCase());
        } else {
            throw new DataAccessException("Cannot delete: Student not found with ID " + studentId);
        }
    }
}
