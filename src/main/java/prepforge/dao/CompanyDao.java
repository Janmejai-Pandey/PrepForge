package prepforge.dao;

import java.util.List;
import prepforge.exception.DataAccessException;
import prepforge.model.Company;
import prepforge.model.JobRole;

public interface CompanyDao {

    List<Company> findAllCompanies() throws DataAccessException;

    Company findCompanyById(int companyId) throws DataAccessException;

    void saveCompany(Company company) throws DataAccessException;

    List<JobRole> findAllJobRoles() throws DataAccessException;

    JobRole findJobRoleById(int roleId) throws DataAccessException;

    void saveJobRole(JobRole role) throws DataAccessException;

    void saveApplication(int studentId, int roleId) throws DataAccessException;

    boolean hasApplied(int studentId, int roleId) throws DataAccessException;

    List<Integer> findAppliedRoleIds(int studentId) throws DataAccessException;
}
