package prepforge.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import prepforge.dao.InMemoryPlacementDao;
import prepforge.dao.InMemoryUserDao;
import prepforge.dao.PlacementDao;
import prepforge.dao.UserDao;
import prepforge.exception.PrepForgeException;
import prepforge.exception.ValidationException;
import prepforge.model.Company;
import prepforge.model.JobRole;
import prepforge.model.Skill;
import prepforge.model.Student;
import prepforge.util.Validator;

/**
 * Service Class: PlacementService
 * 
 * Manages company recruitment drives, job role postings, student eligibility matching,
 * and placement applications.
 * 
 * Eligibility Rule:
 * 1. Student CGPA >= JobRole minimum CGPA
 *    AND
 * 2. Student possesses ALL required skills for that JobRole.
 */
public class PlacementService {

    private final PlacementDao placementDao;
    private final UserDao userDao;

    // Default constructor
    public PlacementService() {
        this.placementDao = new InMemoryPlacementDao();
        this.userDao = new InMemoryUserDao();
    }

    // Constructor with Dependency Injection
    public PlacementService(PlacementDao placementDao, UserDao userDao) {
        this.placementDao = placementDao;
        this.userDao = userDao;
    }

    // ------------------------------------------------------------------------
    // 1. GET ALL COMPANIES
    // ------------------------------------------------------------------------
    public List<Company> getAllCompanies() throws PrepForgeException {
        return placementDao.findAllCompanies();
    }

    // ------------------------------------------------------------------------
    // 2. GET ELIGIBLE ROLES FOR STUDENT
    // ------------------------------------------------------------------------
    public List<JobRole> getEligibleRoles(int studentId) throws PrepForgeException {
        // Step 1: Validate student ID
        Validator.validatePositiveId(studentId, "Student ID");

        // Step 2: Fetch student details
        Student student = userDao.findStudentById(studentId);
        if (student == null) {
            throw new ValidationException("Cannot evaluate eligibility: Student not found with ID " + studentId);
        }

        // Step 3: Fetch all posted job roles
        List<JobRole> allRoles = placementDao.findAllJobRoles();
        List<JobRole> eligibleRoles = new ArrayList<>();

        // Step 4: Evaluate eligibility criteria for each job role
        for (JobRole role : allRoles) {
            if (isStudentEligible(student, role)) {
                eligibleRoles.add(role);
            }
        }

        return eligibleRoles;
    }

    // ------------------------------------------------------------------------
    // 3. APPLY TO ROLE
    // ------------------------------------------------------------------------
    public void applyToRole(int studentId, int roleId) throws PrepForgeException {
        Validator.validatePositiveId(studentId, "Student ID");
        Validator.validatePositiveId(roleId, "Job Role ID");

        Student student = userDao.findStudentById(studentId);
        if (student == null) {
            throw new ValidationException("Application failed: Student not found with ID " + studentId);
        }

        JobRole role = placementDao.findJobRoleById(roleId);
        if (role == null) {
            throw new ValidationException("Application failed: Job Role not found with ID " + roleId);
        }

        // Check if already applied
        if (placementDao.hasApplied(studentId, roleId) || student.hasAppliedToRole(roleId)) {
            throw new ValidationException("Application failed: Student has already applied for '" + role.getTitle() + "'.");
        }

        // Check placement eligibility
        if (!isStudentEligible(student, role)) {
            throw new ValidationException("Application rejected: Student does not meet the eligibility criteria for '"
                    + role.getTitle() + "'. Required CGPA: " + role.getMinCgpa() + " (Student CGPA: " + student.getCgpa() + ")");
        }

        // Record application
        placementDao.saveApplication(studentId, roleId);
        student.applyToRole(roleId);
    }

    // ------------------------------------------------------------------------
    // 4. SAVE COMPANY
    // ------------------------------------------------------------------------
    public void saveCompany(Company c) throws PrepForgeException {
        Validator.validateNotNull(c, "Company");
        Validator.validateName(c.getName());

        placementDao.saveCompany(c);
    }

    // ------------------------------------------------------------------------
    // 5. SAVE JOB ROLE
    // ------------------------------------------------------------------------
    public void saveJobRole(JobRole r) throws PrepForgeException {
        Validator.validateNotNull(r, "JobRole");
        if (r.getTitle() == null || r.getTitle().trim().isEmpty()) {
            throw new ValidationException("Job role title cannot be empty.");
        }
        Validator.validateCgpa(r.getMinCgpa());
        Validator.validateNotNull(r.getCompany(), "Hiring Company");

        placementDao.saveJobRole(r);
    }

    // ------------------------------------------------------------------------
    // HELPER: ELIGIBILITY CHECKING ALGORITHM
    // ------------------------------------------------------------------------
    private boolean isStudentEligible(Student student, JobRole role) {
        // Condition 1: CGPA Eligibility Check
        if (student.getCgpa() < role.getMinCgpa()) {
            return false;
        }

        // Condition 2: Skills Eligibility Check
        Set<Skill> requiredSkills = role.getRequiredSkills();
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            return true; // No specific skills demanded
        }

        // The student must possess EVERY skill required by the role
        for (Skill requiredSkill : requiredSkills) {
            boolean hasThisSkill = false;

            // Check against student's acquired skills
            for (Skill studentSkill : student.getSkills()) {
                if (studentSkill.equals(requiredSkill)
                        || studentSkill.getSkillId() == requiredSkill.getSkillId()
                        || studentSkill.getSkillName().equalsIgnoreCase(requiredSkill.getSkillName())) {
                    hasThisSkill = true;
                    break;
                }
            }

            if (!hasThisSkill) {
                return false; // Student is missing at least one required skill
            }
        }

        return true;
    }
}
