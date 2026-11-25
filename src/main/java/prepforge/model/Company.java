package prepforge.model;

import java.util.ArrayList;
import java.util.List;

public class Company implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private int companyId;
    private String name;
    private String description;
    private String location;
    private String industry;
    private List<JobRole> jobRoles;

    public Company() {
        this.jobRoles = new ArrayList<>();
    }

    public Company(String name, String description) {
        this.name = name;
        this.description = description;
        this.jobRoles = new ArrayList<>();
    }

    public Company(String name, String location, String industry) {
        this.name = name;
        this.location = location;
        this.industry = industry;
        this.description = industry + " company based in " + location;
        this.jobRoles = new ArrayList<>();
    }

    public Company(String name, String description, String location, String industry) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.industry = industry;
        this.jobRoles = new ArrayList<>();
    }

    public Company(int companyId, String name, String location, String industry) {
        this.companyId = companyId;
        this.name = name;
        this.location = location;
        this.industry = industry;
        this.description = industry + " company based in " + location;
        this.jobRoles = new ArrayList<>();
    }

    public Company(int companyId, String name, String description, String location, String industry) {
        this.companyId = companyId;
        this.name = name;
        this.description = description;
        this.location = location;
        this.industry = industry;
        this.jobRoles = new ArrayList<>();
    }

    public void addJobRole(JobRole role) {
        if (role != null) {
            jobRoles.add(role);
            if (role.getCompany() != this) {
                role.setCompany(this);
            }
        }
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getId() {
        return companyId;
    }

    public void setId(int id) {
        this.companyId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public List<JobRole> getJobRoles() {
        return jobRoles;
    }

    public void setJobRoles(List<JobRole> jobRoles) {
        this.jobRoles = jobRoles != null ? jobRoles : new ArrayList<>();
    }

    @Override
    public String toString() {
        return name + " (" + industry + ", " + location + ")";
    }
}
