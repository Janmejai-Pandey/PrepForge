package prepforge.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JobRole implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private int roleId;
    private String title;
    private double minCgpa;
    private Company company;
    private Set<Skill> requiredSkills;

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

    public void addRequiredSkill(Skill skill) {
        if (skill != null) {
            requiredSkills.add(skill);
        }
    }

    public boolean isCgpaEligible(double studentCgpa) {
        return studentCgpa >= this.minCgpa;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

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
