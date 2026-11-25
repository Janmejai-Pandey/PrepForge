package prepforge.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import prepforge.exception.DataAccessException;
import prepforge.model.ApplicationRecord;
import prepforge.model.Company;
import prepforge.model.JobRole;
import prepforge.model.Skill;
import prepforge.util.StorageManager;

public class InMemoryCompanyDao implements CompanyDao {

    private final StorageManager.PersistentState state;

    public InMemoryCompanyDao() {
        this.state = StorageManager.getState();
        if (state.companies.isEmpty()) {
            seedInitialData();
        }
    }

    private void seedInitialData() {
        Skill java = new Skill(1, "Java", "Programming");
        Skill sql = new Skill(2, "SQL", "Database");
        Skill oop = new Skill(3, "OOP", "Computer Science");
        Skill dsa = new Skill(4, "Data Structures", "Algorithms");
        Skill python = new Skill(5, "Python", "Programming");

        Company google = new Company(1, "Google", "Multinational technology company", "Mountain View, CA", "Technology");
        Company ms = new Company(2, "Microsoft", "Global software and cloud leader", "Redmond, WA", "Cloud & Software");
        Company amz = new Company(3, "Amazon", "E-commerce and cloud computing giant", "Seattle, WA", "E-Commerce & Cloud");
        Company tcs = new Company(4, "Tata Consultancy Services", "Global IT services and consulting", "Mumbai, India", "IT Consulting");
        Company info = new Company(5, "Infosys", "Digital services and consulting leader", "Bengaluru, India", "IT Consulting");

        state.companies.put(google.getCompanyId(), google);
        state.companies.put(ms.getCompanyId(), ms);
        state.companies.put(amz.getCompanyId(), amz);
        state.companies.put(tcs.getCompanyId(), tcs);
        state.companies.put(info.getCompanyId(), info);

        JobRole r1 = new JobRole(1, "Software Development Engineer (SDE-1)", 8.5, google);
        r1.addRequiredSkill(java);
        r1.addRequiredSkill(dsa);
        r1.addRequiredSkill(oop);
        google.addJobRole(r1);
        state.jobRoles.put(r1.getRoleId(), r1);

        JobRole r2 = new JobRole(2, "Cloud Solutions Engineer", 8.0, ms);
        r2.addRequiredSkill(java);
        r2.addRequiredSkill(sql);
        ms.addJobRole(r2);
        state.jobRoles.put(r2.getRoleId(), r2);

        JobRole r3 = new JobRole(3, "Backend Systems Associate", 7.5, amz);
        r3.addRequiredSkill(java);
        r3.addRequiredSkill(sql);
        r3.addRequiredSkill(dsa);
        amz.addJobRole(r3);
        state.jobRoles.put(r3.getRoleId(), r3);

        JobRole r4 = new JobRole(4, "Systems Engineer", 6.5, tcs);
        r4.addRequiredSkill(java);
        tcs.addJobRole(r4);
        state.jobRoles.put(r4.getRoleId(), r4);

        JobRole r5 = new JobRole(5, "Software Specialist", 7.0, info);
        r5.addRequiredSkill(python);
        r5.addRequiredSkill(sql);
        info.addJobRole(r5);
        state.jobRoles.put(r5.getRoleId(), r5);

        StorageManager.saveState();
    }

    @Override
    public synchronized List<Company> findAllCompanies() throws DataAccessException {
        return new ArrayList<>(state.companies.values());
    }

    @Override
    public synchronized Company findCompanyById(int companyId) throws DataAccessException {
        return state.companies.get(companyId);
    }

    @Override
    public synchronized void saveCompany(Company company) throws DataAccessException {
        if (company == null) {
            throw new DataAccessException("Cannot persist a null company.");
        }
        if (company.getCompanyId() <= 0) {
            company.setCompanyId(state.companyIdCounter++);
        }
        state.companies.put(company.getCompanyId(), company);
        StorageManager.saveState();
    }

    @Override
    public synchronized List<JobRole> findAllJobRoles() throws DataAccessException {
        return new ArrayList<>(state.jobRoles.values());
    }

    @Override
    public synchronized JobRole findJobRoleById(int roleId) throws DataAccessException {
        return state.jobRoles.get(roleId);
    }

    @Override
    public synchronized void saveJobRole(JobRole role) throws DataAccessException {
        if (role == null) {
            throw new DataAccessException("Cannot persist a null job role.");
        }
        if (role.getRoleId() <= 0) {
            role.setRoleId(state.roleIdCounter++);
        }
        state.jobRoles.put(role.getRoleId(), role);
        if (role.getCompany() != null) {
            Company comp = state.companies.get(role.getCompany().getCompanyId());
            if (comp != null) {
                comp.addJobRole(role);
            }
        }
        StorageManager.saveState();
    }

    @Override
    public synchronized void saveApplication(int studentId, int roleId) throws DataAccessException {
        state.studentApplications.computeIfAbsent(studentId, k -> new HashSet<>()).add(roleId);
        StorageManager.saveState();
    }

    @Override
    public synchronized boolean hasApplied(int studentId, int roleId) throws DataAccessException {
        Set<Integer> applied = state.studentApplications.get(studentId);
        return applied != null && applied.contains(roleId);
    }

    @Override
    public synchronized List<Integer> findAppliedRoleIds(int studentId) throws DataAccessException {
        Set<Integer> applied = state.studentApplications.get(studentId);
        if (applied == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(applied);
    }

    @Override
    public synchronized List<ApplicationRecord> findAllApplications() throws DataAccessException {
        return new ArrayList<>(state.applicationRecords);
    }

    @Override
    public synchronized void updateApplicationStatus(int studentId, int roleId, String status) throws DataAccessException {
        for (ApplicationRecord rec : state.applicationRecords) {
            if (rec.getStudentId() == studentId && rec.getRoleId() == roleId) {
                rec.setStatus(status);
                break;
            }
        }
        StorageManager.saveState();
    }

    @Override
    public synchronized void saveApplicationRecord(ApplicationRecord record) throws DataAccessException {
        if (record == null) return;
        if (record.getApplicationId() <= 0) {
            record.setApplicationId(state.applicationIdCounter++);
        }
        state.applicationRecords.removeIf(r -> r.getStudentId() == record.getStudentId() && r.getRoleId() == record.getRoleId());
        state.applicationRecords.add(record);
        saveApplication(record.getStudentId(), record.getRoleId());
        StorageManager.saveState();
    }
}
