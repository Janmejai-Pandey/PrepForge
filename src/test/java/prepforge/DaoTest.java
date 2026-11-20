package prepforge;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import prepforge.dao.CompanyDao;
import prepforge.dao.InMemoryCompanyDao;
import prepforge.dao.InMemoryUserDao;
import prepforge.dao.UserDao;
import prepforge.exception.DataAccessException;
import prepforge.model.Admin;
import prepforge.model.Company;
import prepforge.model.JobRole;
import prepforge.model.Skill;
import prepforge.model.Student;
import prepforge.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DaoTest {

    private UserDao userDao;
    private CompanyDao companyDao;

    @BeforeEach
    public void setUp() {
        userDao = new InMemoryUserDao();
        companyDao = new InMemoryCompanyDao();
    }

    @Test
    public void testUserDao_SaveAndFindStudent() throws DataAccessException {
        Student student = new Student("Siddharth Singh", "sid@prepforge.com", "EN2024_10", "CSE", 8.7);
        userDao.saveStudent(student, "hash123");

        assertTrue(student.getId() > 0);
        User byEmail = userDao.findByEmail("sid@prepforge.com");
        assertNotNull(byEmail);
        assertEquals("sid@prepforge.com", byEmail.getEmail());

        Student byId = userDao.findStudentById(student.getId());
        assertNotNull(byId);
        assertEquals("Siddharth Singh", byId.getName());
    }

    @Test
    public void testUserDao_SaveAdmin() throws DataAccessException {
        Admin admin = new Admin("Dr. Neha Kapoor", "neha@prepforge.com", "ADM_202", "Training & Placement");
        userDao.saveAdmin(admin, "adminhash");

        assertTrue(admin.getId() > 0);
        User byEmail = userDao.findByEmail("neha@prepforge.com");
        assertNotNull(byEmail);
        assertEquals("Admin", byEmail.getRole());
    }

    @Test
    public void testUserDao_UpdateStudent() throws DataAccessException {
        Student student = new Student("Ankit Varma", "ankit@prepforge.com", "EN2024_11", "IT", 7.9);
        userDao.saveStudent(student, "hash123");

        student.setCgpa(8.2);
        student.setBranch("CSE");
        userDao.updateStudent(student);

        Student updated = userDao.findStudentById(student.getId());
        assertEquals(8.2, updated.getCgpa(), 0.001);
        assertEquals("CSE", updated.getBranch());
    }

    @Test
    public void testUserDao_StudentSkills() throws DataAccessException {
        Student student = new Student("Neha Sen", "nehasen@prepforge.com", "EN2024_12", "CSE", 8.4);
        userDao.saveStudent(student, "hash123");

        List<Skill> skills = userDao.findAllSkills();
        assertTrue(skills.size() >= 2);

        int skillId = skills.get(0).getId();
        userDao.addStudentSkill(student.getId(), skillId, "Intermediate");

        Student withSkill = userDao.findStudentById(student.getId());
        assertEquals(1, withSkill.getSkills().size());

        userDao.removeStudentSkill(student.getId(), skillId);
        Student withoutSkill = userDao.findStudentById(student.getId());
        assertEquals(0, withoutSkill.getSkills().size());
    }

    @Test
    public void testUserDao_DeleteStudent() throws DataAccessException {
        Student student = new Student("Tarun Gill", "tarun@prepforge.com", "EN2024_13", "MECH", 6.8);
        userDao.saveStudent(student, "hash123");

        int id = student.getId();
        userDao.deleteStudent(id);

        assertNull(userDao.findStudentById(id));
        assertNull(userDao.findByEmail("tarun@prepforge.com"));
    }

    @Test
    public void testCompanyDao_FindAllAndById() throws DataAccessException {
        List<Company> list = companyDao.findAllCompanies();
        assertTrue(list.size() >= 4);

        Company first = list.get(0);
        Company byId = companyDao.findCompanyById(first.getCompanyId());
        assertNotNull(byId);
        assertEquals(first.getName(), byId.getName());
    }

    @Test
    public void testCompanyDao_SaveCompany() throws DataAccessException {
        Company comp = new Company("Oracle", "Cloud database leader", "Austin, TX", "Database");
        companyDao.saveCompany(comp);

        assertTrue(comp.getCompanyId() > 0);
        Company retrieved = companyDao.findCompanyById(comp.getCompanyId());
        assertNotNull(retrieved);
        assertEquals("Oracle", retrieved.getName());
    }

    @Test
    public void testCompanyDao_JobRolesAndApplications() throws DataAccessException {
        List<JobRole> roles = companyDao.findAllJobRoles();
        assertTrue(!roles.isEmpty());

        JobRole role = roles.get(0);
        assertNotNull(companyDao.findJobRoleById(role.getRoleId()));

        int studentId = 501;
        assertFalse(companyDao.hasApplied(studentId, role.getRoleId()));

        companyDao.saveApplication(studentId, role.getRoleId());
        assertTrue(companyDao.hasApplied(studentId, role.getRoleId()));

        List<Integer> applied = companyDao.findAppliedRoleIds(studentId);
        assertTrue(applied.contains(role.getRoleId()));
    }

    @Test
    public void testUserDao_SaveDuplicateEmail_ThrowsDataAccessException() throws DataAccessException {
        Student s1 = new Student("User One", "unique@prepforge.com", "EN_01", "CSE", 8.0);
        userDao.saveStudent(s1, "hash1");

        Student s2 = new Student("User Two", "unique@prepforge.com", "EN_02", "IT", 7.5);
        assertThrows(DataAccessException.class, () -> {
            userDao.saveStudent(s2, "hash2");
        });
    }
}
