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
        student.setId(nextUserId);
        student.setUserId(nextUserId);
        student.setPasswordHash(passwordHash);

        userDatabase.put(nextUserId, student);
        emailIndex.put(student.getEmail().toLowerCase(), student);
        nextUserId++;
    }

    @Override
    public void saveAdmin(Admin admin, String passwordHash) throws DataAccessException {
        if (admin == null) {
            throw new DataAccessException("Cannot save null admin.");
        }
        if (emailIndex.containsKey(admin.getEmail().toLowerCase())) {
            throw new DataAccessException("User with email '" + admin.getEmail() + "' already exists.");
        }
        admin.setId(nextUserId);
        admin.setUserId(nextUserId);
        admin.setPasswordHash(passwordHash);

        userDatabase.put(nextUserId, admin);
        emailIndex.put(admin.getEmail().toLowerCase(), admin);
        nextUserId++;
    }

    @Override
    public Student findStudentById(int studentId) throws DataAccessException {
        User user = userDatabase.get(studentId);
        if (user instanceof Student) {
            return (Student) user;
        }
        return null;
    }

    @Override
    public void updateStudent(Student student) throws DataAccessException {
        if (student == null) {
            throw new DataAccessException("Cannot update null student.");
        }
        if (!userDatabase.containsKey(student.getId())) {
            throw new DataAccessException("Student with ID " + student.getId() + " not found.");
        }
        userDatabase.put(student.getId(), student);
        emailIndex.put(student.getEmail().toLowerCase(), student);
    }

    @Override
    public void addStudentSkill(int studentId, int skillId, String level) throws DataAccessException {
        Student student = findStudentById(studentId);
        if (student == null) {
            throw new DataAccessException("Student with ID " + studentId + " not found.");
        }
        Skill skill = findSkillById(skillId);
        if (skill == null) {
            throw new DataAccessException("Skill with ID " + skillId + " not found.");
        }
        student.addSkill(skill, level);
    }

    @Override
    public void removeStudentSkill(int studentId, int skillId) throws DataAccessException {
        Student student = findStudentById(studentId);
        if (student == null) {
            throw new DataAccessException("Student with ID " + studentId + " not found.");
        }
        Skill skill = findSkillById(skillId);
        if (skill != null) {
            student.getSkills().remove(skill);
        }
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
        for (User user : userDatabase.values()) {
            if (user instanceof Student) {
                students.add((Student) user);
            }
        }
        return students;
    }

    @Override
    public void deleteStudent(int studentId) throws DataAccessException {
        User user = userDatabase.remove(studentId);
        if (user != null) {
            emailIndex.remove(user.getEmail().toLowerCase());
        } else {
            throw new DataAccessException("Student with ID " + studentId + " not found.");
        }
    }
}
