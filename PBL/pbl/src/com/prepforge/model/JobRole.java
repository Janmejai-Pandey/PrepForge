package com.prepforge.model;

import java.util.ArrayList;

// DOMAIN CLASS: JobRole
// Represents an open placement job (e.g. "Software Engineer", "Data Analyst").
public class JobRole {

    // ENCAPSULATION: private variables
    private String roleTitle;
    private double minCgpa;

    // ASSOCIATION:
    // A JobRole is linked to a Company (e.g. "Software Engineer at Google").
    private Company company;

    // AGGREGATION:
    // A JobRole HAS-A list of required Skills.
    private ArrayList<Skill> requiredSkills;

    // Constructor
    public JobRole(String roleTitle, double minCgpa, Company company) {
        setRoleTitle(roleTitle);
        setMinCgpa(minCgpa);
        this.company = company;
        this.requiredSkills = new ArrayList<>();
    }

    // Helper method to add a skill requirement
    public void addSkill(Skill skill) {
        if (skill != null) {
            requiredSkills.add(skill);
        }
    }

    // Check if a student's CGPA is high enough for this job
    public boolean isEligible(double studentCgpa) {
        return studentCgpa >= this.minCgpa;
    }

    // Getters and Setters with basic validation
    public String getRoleTitle() {
        return roleTitle;
    }

    public void setRoleTitle(String roleTitle) {
        if (roleTitle == null || roleTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("Role title cannot be empty!");
        }
        this.roleTitle = roleTitle;
    }

    public double getMinCgpa() {
        return minCgpa;
    }

    public void setMinCgpa(double minCgpa) {
        // Validation: CGPA in Indian universities is typically between 0.0 and 10.0
        if (minCgpa < 0.0 || minCgpa > 10.0) {
            throw new IllegalArgumentException("CGPA must be between 0.0 and 10.0!");
        }
        this.minCgpa = minCgpa;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public ArrayList<Skill> getRequiredSkills() {
        return requiredSkills;
    }

    @Override
    public String toString() {
        String compName = (company != null) ? company.getCompanyName() : "Unknown Company";
        return roleTitle + " at " + compName + " (Min CGPA: " + minCgpa + ")";
    }
}
