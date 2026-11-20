package prepforge.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain Model: Company
 * 
 * Represents an employer or recruiting organization (e.g. "Google", "Infosys", "Amazon").
 * 
 * Demonstrates:
 * 1. ENCAPSULATION: Fields are private and accessed via getters and setters.
 * 2. AGGREGATION: A Company HAS-A list of JobRoles (jobRoles).
 *    Even if a company entity is processed, job roles can conceptually exist on their own.
 */
public class Company {

    // ---------------- Private Fields ----------------
    private int companyId;
    private String name;
    private String description;
    private String location;
    private String industry;

    // Aggregation: Company HAS-A list of JobRoles
    private List<JobRole> jobRoles;

    // ---------------- Constructors (Overloading) ----------------
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

    // ---------------- Aggregation Helper ----------------
    public void addJobRole(JobRole role) {
        if (role != null) {
            jobRoles.add(role);
            if (role.getCompany() != this) {
                role.setCompany(this);
            }
        }
    }

    // ---------------- Getters and Setters ----------------
    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    // Contract alias: id
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
        return description != null ? description : (industry != null ? industry : "");
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
        return name + (location != null ? " (" + location + ")" : "");
    }
}
