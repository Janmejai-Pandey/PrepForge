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
import prepforge.util.StorageManager;

public class InMemoryCompanyDao implements CompanyDao {

    private final StorageManager.PersistentState state;

    public InMemoryCompanyDao() {
        this.state = StorageManager.getState();
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
