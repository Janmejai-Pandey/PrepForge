package prepforge.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import prepforge.dao.CompanyDao;
import prepforge.dao.InMemoryCompanyDao;
import prepforge.dao.InMemoryUserDao;
import prepforge.dao.UserDao;
import prepforge.exception.PrepForgeException;
import prepforge.exception.ValidationException;
import prepforge.model.ApplicationRecord;
import prepforge.model.Company;
import prepforge.model.JobRole;
import prepforge.model.Skill;
import prepforge.model.Student;
import prepforge.util.Validator;

public class PlacementService {

    private final CompanyDao companyDao;
    private final UserDao userDao;

    public PlacementService() {
        this.companyDao = new InMemoryCompanyDao();
        this.userDao = new InMemoryUserDao();
    }

    public PlacementService(CompanyDao companyDao, UserDao userDao) {
        this.companyDao = companyDao;
        this.userDao = userDao;
    }

    public List<Company> getAllCompanies() throws PrepForgeException {
        return companyDao.findAllCompanies();
    }

    public List<JobRole> getEligibleRoles(int studentId) throws PrepForgeException {
        Validator.validatePositiveId(studentId, "Student ID");

        Student student = userDao.findStudentById(studentId);
        if (student == null) {
            throw new ValidationException("Student not found with ID: " + studentId);
        }

        List<JobRole> allRoles = companyDao.findAllJobRoles();
        List<JobRole> eligibleRoles = new ArrayList<>();

        for (JobRole role : allRoles) {
            if (student.getCgpa() < role.getMinCgpa()) {
                continue;
            }

            Set<Skill> requiredSkills = role.getRequiredSkills();
            boolean hasAllSkills = true;

            if (requiredSkills != null && !requiredSkills.isEmpty()) {
                List<Skill> studentSkills = student.getSkills();
                for (Skill req : requiredSkills) {
                    boolean matched = false;
                    for (Skill s : studentSkills) {
                        if (s.getId() == req.getId() || s.getName().equalsIgnoreCase(req.getName())) {
                            matched = true;
                            break;
                        }
                    }
                    if (!matched) {
                        hasAllSkills = false;
                        break;
                    }
                }
            }

            if (hasAllSkills) {
                eligibleRoles.add(role);
            }
        }

        return eligibleRoles;
    }

    public void applyToRole(int studentId, int roleId) throws PrepForgeException {
        Validator.validatePositiveId(studentId, "Student ID");
        Validator.validatePositiveId(roleId, "Job Role ID");

        Student student = userDao.findStudentById(studentId);
        if (student == null) {
            throw new ValidationException("Application rejected: Student ID " + studentId + " not found.");
        }

        JobRole role = companyDao.findJobRoleById(roleId);
        if (role == null) {
            throw new ValidationException("Application rejected: Job role ID " + roleId + " not found.");
        }

        if (companyDao.hasApplied(studentId, roleId)) {
            throw new ValidationException("You have already submitted an application for '" + role.getTitle() + "'.");
        }

        if (student.getCgpa() < role.getMinCgpa()) {
            throw new ValidationException("Application rejected: CGPA cutoff not met. Required: "
                    + role.getMinCgpa() + ", Your CGPA: " + student.getCgpa());
        }

        Set<Skill> requiredSkills = role.getRequiredSkills();
        if (requiredSkills != null && !requiredSkills.isEmpty()) {
            List<Skill> studentSkills = student.getSkills();
            for (Skill req : requiredSkills) {
                boolean matched = false;
                for (Skill s : studentSkills) {
                    if (s.getId() == req.getId() || s.getName().equalsIgnoreCase(req.getName())) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    throw new ValidationException("Application rejected: Missing mandatory skill '" + req.getName() + "'.");
                }
            }
        }

        String companyName = role.getCompany() != null ? role.getCompany().getName() : "Company";
        ApplicationRecord app = new ApplicationRecord(
                0, studentId, student.getName(), student.getEmail(), student.getBranch(),
                student.getCgpa(), roleId, role.getTitle(), companyName, "APPLIED"
        );
        companyDao.saveApplicationRecord(app);
    }

    public void saveCompany(Company c) throws PrepForgeException {
        Validator.validateNotNull(c, "Company");
        Validator.validateName(c.getName());
        companyDao.saveCompany(c);
    }

    public void saveJobRole(JobRole r) throws PrepForgeException {
        Validator.validateNotNull(r, "JobRole");
        if (r.getTitle() == null || r.getTitle().trim().isEmpty()) {
            throw new ValidationException("Job role title cannot be empty.");
        }
        Validator.validateCgpa(r.getMinCgpa());
        Validator.validateNotNull(r.getCompany(), "Hiring Company");
        companyDao.saveJobRole(r);
    }

    public List<JobRole> getAllJobRoles() throws PrepForgeException {
        return companyDao.findAllJobRoles();
    }

    public boolean hasApplied(int studentId, int roleId) throws PrepForgeException {
        return companyDao.hasApplied(studentId, roleId);
    }

    public List<Integer> getAppliedRoleIds(int studentId) throws PrepForgeException {
        return companyDao.findAppliedRoleIds(studentId);
    }

    public List<ApplicationRecord> getAllApplications() throws PrepForgeException {
        return companyDao.findAllApplications();
    }

    public void updateApplicationStatus(int studentId, int roleId, String status) throws PrepForgeException {
        Validator.validatePositiveId(studentId, "Student ID");
        Validator.validatePositiveId(roleId, "Job Role ID");
        if (status == null || status.trim().isEmpty()) {
            throw new ValidationException("Status cannot be empty.");
        }
        companyDao.updateApplicationStatus(studentId, roleId, status.trim().toUpperCase());
    }
}
