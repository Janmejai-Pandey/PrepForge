package prepforge.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import prepforge.exception.DataAccessException;
import prepforge.model.Company;
import prepforge.model.JobRole;
import prepforge.model.Skill;

public class InMemoryCompanyDao implements CompanyDao {

    private final Map<Integer, Company> companies = new ConcurrentHashMap<>();
    private final Map<Integer, JobRole> jobRoles = new ConcurrentHashMap<>();
    private final Map<Integer, Set<Integer>> studentApplications = new ConcurrentHashMap<>();

    private final AtomicInteger companyIdCounter = new AtomicInteger(100);
    private final AtomicInteger roleIdCounter = new AtomicInteger(200);

    public InMemoryCompanyDao() {
        seedInitialData();
    }

    private void seedInitialData() {
        Skill java = new Skill(1, "Java", "Programming");
        Skill sql = new Skill(2, "SQL", "Database");
        Skill oop = new Skill(3, "OOP", "Computer Science");
        Skill dsa = new Skill(4, "Data Structures", "Algorithms");
        Skill python = new Skill(5, "Python", "Programming");
        Skill web = new Skill(6, "Web Development", "Frontend");

        Company google = new Company(1, "Google", "Multinational technology company", "Mountain View, CA", "Technology");
        Company ms = new Company(2, "Microsoft", "Global software and cloud leader", "Redmond, WA", "Cloud & Software");
        Company amz = new Company(3, "Amazon", "E-commerce and cloud computing giant", "Seattle, WA", "E-Commerce & Cloud");
        Company tcs = new Company(4, "Tata Consultancy Services", "Global IT services and consulting", "Mumbai, India", "IT Consulting");
        Company info = new Company(5, "Infosys", "Digital services and consulting leader", "Bengaluru, India", "IT Consulting");

        companies.put(google.getCompanyId(), google);
        companies.put(ms.getCompanyId(), ms);
        companies.put(amz.getCompanyId(), amz);
        companies.put(tcs.getCompanyId(), tcs);
        companies.put(info.getCompanyId(), info);

        JobRole r1 = new JobRole(1, "Software Development Engineer (SDE-1)", 8.5, google);
        r1.addRequiredSkill(java);
        r1.addRequiredSkill(dsa);
        r1.addRequiredSkill(oop);
        google.addJobRole(r1);
        jobRoles.put(r1.getRoleId(), r1);

        JobRole r2 = new JobRole(2, "Cloud Solutions Engineer", 8.0, ms);
        r2.addRequiredSkill(java);
        r2.addRequiredSkill(sql);
        ms.addJobRole(r2);
        jobRoles.put(r2.getRoleId(), r2);

        JobRole r3 = new JobRole(3, "Backend Systems Associate", 7.5, amz);
        r3.addRequiredSkill(java);
        r3.addRequiredSkill(sql);
        r3.addRequiredSkill(dsa);
        amz.addJobRole(r3);
        jobRoles.put(r3.getRoleId(), r3);

        JobRole r4 = new JobRole(4, "Systems Engineer", 6.5, tcs);
        r4.addRequiredSkill(java);
        tcs.addJobRole(r4);
        jobRoles.put(r4.getRoleId(), r4);

        JobRole r5 = new JobRole(5, "Software Specialist", 7.0, info);
        r5.addRequiredSkill(python);
        r5.addRequiredSkill(sql);
        info.addJobRole(r5);
        jobRoles.put(r5.getRoleId(), r5);
    }

    @Override
    public List<Company> findAllCompanies() throws DataAccessException {
        return new ArrayList<>(companies.values());
    }

    @Override
    public Company findCompanyById(int companyId) throws DataAccessException {
        return companies.get(companyId);
    }

    @Override
    public void saveCompany(Company company) throws DataAccessException {
        if (company == null) {
            throw new DataAccessException("Cannot persist a null company.");
        }
        if (company.getCompanyId() <= 0) {
            company.setCompanyId(companyIdCounter.incrementAndGet());
        }
        companies.put(company.getCompanyId(), company);
    }

    @Override
    public List<JobRole> findAllJobRoles() throws DataAccessException {
        return new ArrayList<>(jobRoles.values());
    }

    @Override
    public JobRole findJobRoleById(int roleId) throws DataAccessException {
        return jobRoles.get(roleId);
    }

    @Override
    public void saveJobRole(JobRole role) throws DataAccessException {
        if (role == null) {
            throw new DataAccessException("Cannot persist a null job role.");
        }
        if (role.getRoleId() <= 0) {
            role.setRoleId(roleIdCounter.incrementAndGet());
        }
        jobRoles.put(role.getRoleId(), role);
        if (role.getCompany() != null) {
            Company comp = companies.get(role.getCompany().getCompanyId());
            if (comp != null) {
                comp.addJobRole(role);
            }
        }
    }

    @Override
    public void saveApplication(int studentId, int roleId) throws DataAccessException {
        studentApplications.computeIfAbsent(studentId, k -> ConcurrentHashMap.newKeySet()).add(roleId);
    }

    @Override
    public boolean hasApplied(int studentId, int roleId) throws DataAccessException {
        Set<Integer> applied = studentApplications.get(studentId);
        return applied != null && applied.contains(roleId);
    }

    @Override
    public List<Integer> findAppliedRoleIds(int studentId) throws DataAccessException {
        Set<Integer> applied = studentApplications.get(studentId);
        if (applied == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(applied);
    }
}
