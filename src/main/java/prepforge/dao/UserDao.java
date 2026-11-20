package prepforge.dao;

import java.util.List;
import prepforge.exception.DataAccessException;
import prepforge.model.Admin;
import prepforge.model.Skill;
import prepforge.model.Student;
import prepforge.model.User;

/**
 * DAO Interface: UserDao
 * 
 * Defines the contract for all persistence operations related to users,
 * students, admins, and skills.
 * 
 * Demonstrates:
 * 1. SEPARATION OF CONCERNS: The Service layer only talks to this interface.
 *    No SQL or storage details appear in the Service layer.
 * 2. DATA ACCESS EXCEPTION WRAPPING: Throws DataAccessException on failures.
 */
public interface UserDao {

    User findByEmail(String email) throws DataAccessException;

    void saveStudent(Student student, String passwordHash) throws DataAccessException;

    void saveAdmin(Admin admin, String passwordHash) throws DataAccessException;

    Student findStudentById(int studentId) throws DataAccessException;

    void updateStudent(Student student) throws DataAccessException;

    void addStudentSkill(int studentId, int skillId, String level) throws DataAccessException;

    void removeStudentSkill(int studentId, int skillId) throws DataAccessException;

    List<Skill> findAllSkills() throws DataAccessException;

    Skill findSkillById(int skillId) throws DataAccessException;

    List<Student> findAllStudents() throws DataAccessException;

    void deleteStudent(int studentId) throws DataAccessException;
}
