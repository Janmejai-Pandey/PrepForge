package prepforge.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import prepforge.exception.DataAccessException;
import prepforge.model.Company;
import prepforge.model.JobRole;
import prepforge.model.Skill;

/**
 * Implementation: InMemoryPlacementDao
 * 
 * In-memory repository implementing PlacementDao for companies,
 * job roles, and applications.
 */
public class InMemoryPlacementDao implements PlacementDao {

    private final Map<Integer, Company> companyMap = new HashMap<>();
    private final Map<Integer, JobRole> jobRoleMap = new HashMap<>();

    // Map: studentId -> Set of roleIds applied to
    private final Map<Integer, Set<Integer>> applications = new HashMap<>();

    private int nextCompanyId = 1;
    private int nextRoleId = 1;

    public InMemoryPlacementDao() {
        seedInitialPlacementData();
    }

    private void seedInitialPlacementData() {
        // Sample Company 1
        Company google = new Company(nextCompanyId++, "Google", "Bangalore", "Technology");
        companyMap.put(google.getCompanyId(), google);

        JobRole sdeGoogle = new JobRole(nextRoleId++, "Software Development Engineer", 8.0, google);
        sdeGoogle.addRequiredSkill(new Skill(1, "Java", "Programming"));
        sdeGoogle.addRequiredSkill(new Skill(6, "Data Structures", "Core CS"));
        google.addJobRole(sdeGoogle);
        jobRoleMap.put(sdeGoogle.getRoleId(), sdeGoogle);

        // Sample Company 2
        Company microsoft = new Company(nextCompanyId++, "Microsoft", "Hyderabad", "Cloud & Enterprise");
        companyMap.put(microsoft.getCompanyId(), microsoft);

        JobRole backendDev = new JobRole(nextRoleId++, "Backend Developer", 7.5, microsoft);
        backendDev.addRequiredSkill(new Skill(1, "Java", "Programming"));
        backendDev.addRequiredSkill(new Skill(3, "SQL", "Database"));
        microsoft.addJobRole(backendDev);
        jobRoleMap.put(backendDev.getRoleId(), backendDev);

        // Sample Company 3
        Company infosys = new Company(nextCompanyId++, "Infosys", "Pune", "IT Services");
        companyMap.put(infosys.getCompanyId(), infosys);

        JobRole systemEngineer = new JobRole(nextRoleId++, "Systems Engineer", 6.5, infosys);
        systemEngineer.addRequiredSkill(new Skill(1, "Java", "Programming"));
        infosys.addJobRole(systemEngineer);
        jobRoleMap.put(systemEngineer.getRoleId(), systemEngineer);
    }

    @Override
    public List<Company> findAllCompanies() throws DataAccessException {
        return new ArrayList<>(companyMap.values());
    }

    @Override
    public Company findCompanyById(int companyId) throws DataAccessException {
        return companyMap.get(companyId);
    }

    @Override
    public void saveCompany(Company company) throws DataAccessException {
        if (company == null) {
            throw new DataAccessException("Cannot save null company.");
        }
        if (company.getCompanyId() <= 0) {
            company.setCompanyId(nextCompanyId++);
        }
        companyMap.put(company.getCompanyId(), company);
    }

    @Override
    public List<JobRole> findAllJobRoles() throws DataAccessException {
        return new ArrayList<>(jobRoleMap.values());
    }

    @Override
    public JobRole findJobRoleById(int roleId) throws DataAccessException {
        return jobRoleMap.get(roleId);
    }

    @Override
    public void saveJobRole(JobRole role) throws DataAccessException {
        if (role == null) {
            throw new DataAccessException("Cannot save null job role.");
        }
        if (role.getRoleId() <= 0) {
            role.setRoleId(nextRoleId++);
        }
        jobRoleMap.put(role.getRoleId(), role);

        // Also associate with company list
        if (role.getCompany() != null) {
            Company comp = companyMap.get(role.getCompany().getCompanyId());
            if (comp != null && !comp.getJobRoles().contains(role)) {
                comp.addJobRole(role);
            }
        }
    }

    @Override
    public void saveApplication(int studentId, int roleId) throws DataAccessException {
        applications.computeIfAbsent(studentId, k -> new HashSet<>()).add(roleId);
    }

    @Override
    public boolean hasApplied(int studentId, int roleId) throws DataAccessException {
        Set<Integer> roles = applications.get(studentId);
        return roles != null && roles.contains(roleId);
    }

    @Override
    public List<Integer> findAppliedRoleIds(int studentId) throws DataAccessException {
        Set<Integer> roles = applications.get(studentId);
        return roles != null ? new ArrayList<>(roles) : new ArrayList<>();
    }
}
