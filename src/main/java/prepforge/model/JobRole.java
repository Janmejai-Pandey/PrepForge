package prepforge.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Domain Model: JobRole
 * 
 * Represents an open job profile offered by a company (e.g. "Software Engineer", "Backend Developer").
 * 
 * Demonstrates:
 * 1. ENCAPSULATION: All attributes are private.
 * 2. ASSOCIATION: JobRole references its hiring Company (association relationship).
 * 3. AGGREGATION: JobRole HAS-A Set of required Skills. A Set is used to guarantee uniqueness.
 * 4. DOMAIN LOGIC: isCgpaEligible(studentCgpa) validates academic qualification cutoff.
 */
public class JobRole {

    // ---------------- Private Fields ----------------
    private int roleId;
    private String title;
    private double minCgpa;

    // Association: A JobRole belongs to a hiring Company
    private Company company;

    // Aggregation: A JobRole requires a Set of unique Skills
    private Set<Skill> requiredSkills;

    // ---------------- Constructors (Overloading) ----------------
    public JobRole() {
        this.requiredSkills = new HashSet<>();
    }

    public JobRole(String title, double minCgpa, Company company) {
        this.title = title;
        this.minCgpa = minCgpa;
        this.company = company;
        this.requiredSkills = new HashSet<>();
    }

    public JobRole(int roleId, String title, double minCgpa, Company company) {
        this.roleId = roleId;
        this.title = title;
        this.minCgpa = minCgpa;
        this.company = company;
        this.requiredSkills = new HashSet<>();
    }

    // ---------------- Helper Methods ----------------
    public void addRequiredSkill(Skill skill) {
        if (skill != null) {
            requiredSkills.add(skill);
        }
    }

    public boolean isCgpaEligible(double studentCgpa) {
        return studentCgpa >= this.minCgpa;
    }

    // ---------------- Getters and Setters ----------------
    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    // Contract alias: id
    public int getId() {
        return roleId;
    }

    public void setId(int id) {
        this.roleId = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getMinCgpa() {
        return minCgpa;
    }

    public void setMinCgpa(double minCgpa) {
        this.minCgpa = minCgpa;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Set<Skill> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(Set<Skill> requiredSkills) {
        this.requiredSkills = requiredSkills != null ? requiredSkills : new HashSet<>();
    }

    /**
     * List-based accessor to directly satisfy CONTRACT_M1: List<Skill> required skills
     */
    public List<Skill> getRequiredSkillsList() {
        return new ArrayList<>(requiredSkills);
    }

    public void setRequiredSkillsList(List<Skill> skills) {
        this.requiredSkills = skills != null ? new HashSet<>(skills) : new HashSet<>();
    }

    @Override
    public String toString() {
        String companyName = (company != null) ? company.getName() : "Unknown Company";
        return title + " at " + companyName + " [Min CGPA: " + minCgpa + ", Skills Required: " + requiredSkills.size() + "]";
    }
}
