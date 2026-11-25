package prepforge.dao;

import java.util.ArrayList;
import java.util.List;
import prepforge.exception.DataAccessException;
import prepforge.model.Admin;
import prepforge.model.Skill;
import prepforge.model.Student;
import prepforge.model.User;
import prepforge.util.StorageManager;

public class InMemoryUserDao implements UserDao {

    private final StorageManager.PersistentState state;

    public InMemoryUserDao() {
        this.state = StorageManager.getState();
    }

    @Override
    public synchronized User findByEmail(String email) throws DataAccessException {
        if (email == null) return null;
        return state.emailIndex.get(email.trim().toLowerCase());
    }

    @Override
    public synchronized void saveStudent(Student student, String passwordHash) throws DataAccessException {
        if (student == null) {
            throw new DataAccessException("Cannot save null student.");
        }
        if (state.emailIndex.containsKey(student.getEmail().toLowerCase())) {
            throw new DataAccessException("User with email '" + student.getEmail() + "' already exists.");
        }
        int id = state.nextUserId++;
        student.setId(id);
        student.setUserId(id);
        student.setPasswordHash(passwordHash);

        state.userDatabase.put(id, student);
        state.emailIndex.put(student.getEmail().toLowerCase(), student);
        StorageManager.saveState();
    }

    @Override
    public synchronized void saveAdmin(Admin admin, String passwordHash) throws DataAccessException {
        if (admin == null) {
            throw new DataAccessException("Cannot save null admin.");
        }
        if (state.emailIndex.containsKey(admin.getEmail().toLowerCase())) {
            throw new DataAccessException("User with email '" + admin.getEmail() + "' already exists.");
        }
        int id = state.nextUserId++;
        admin.setId(id);
        admin.setUserId(id);
        admin.setPasswordHash(passwordHash);

        state.userDatabase.put(id, admin);
        state.emailIndex.put(admin.getEmail().toLowerCase(), admin);
        StorageManager.saveState();
    }

    @Override
    public synchronized Student findStudentById(int studentId) throws DataAccessException {
        User user = state.userDatabase.get(studentId);
        if (user instanceof Student) {
            return (Student) user;
        }
        return null;
    }

    @Override
    public synchronized void updateStudent(Student student) throws DataAccessException {
        if (student == null) {
            throw new DataAccessException("Cannot update null student.");
        }
        if (!state.userDatabase.containsKey(student.getId())) {
            throw new DataAccessException("Student with ID " + student.getId() + " not found.");
        }
        state.userDatabase.put(student.getId(), student);
        state.emailIndex.put(student.getEmail().toLowerCase(), student);
        StorageManager.saveState();
    }

    @Override
    public synchronized void addStudentSkill(int studentId, int skillId, String level) throws DataAccessException {
        Student student = findStudentById(studentId);
        if (student == null) {
            throw new DataAccessException("Student with ID " + studentId + " not found.");
        }
        Skill skill = findSkillById(skillId);
        if (skill == null) {
            throw new DataAccessException("Skill with ID " + skillId + " not found.");
        }
        student.addSkill(skill, level);
        StorageManager.saveState();
    }

    @Override
    public synchronized void removeStudentSkill(int studentId, int skillId) throws DataAccessException {
        Student student = findStudentById(studentId);
        if (student == null) {
            throw new DataAccessException("Student with ID " + studentId + " not found.");
        }
        student.removeSkill(skillId);
        StorageManager.saveState();
    }

    @Override
    public synchronized List<Skill> findAllSkills() throws DataAccessException {
        return new ArrayList<>(state.skillCatalog.values());
    }

    @Override
    public synchronized Skill findSkillById(int skillId) throws DataAccessException {
        return state.skillCatalog.get(skillId);
    }

    @Override
    public synchronized List<Student> findAllStudents() throws DataAccessException {
        List<Student> students = new ArrayList<>();
        for (User user : state.userDatabase.values()) {
            if (user instanceof Student) {
                students.add((Student) user);
            }
        }
        return students;
    }

    @Override
    public synchronized void deleteStudent(int studentId) throws DataAccessException {
        User user = state.userDatabase.remove(studentId);
        if (user != null) {
            state.emailIndex.remove(user.getEmail().toLowerCase());
            StorageManager.saveState();
        } else {
            throw new DataAccessException("Student with ID " + studentId + " not found.");
        }
    }
}
