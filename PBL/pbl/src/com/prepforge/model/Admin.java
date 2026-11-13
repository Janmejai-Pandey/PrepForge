package com.prepforge.model;

import java.util.ArrayList;

// SUBCLASS: Admin
//
// 1. INHERITANCE:
//    - "extends User" so Admin inherits basic user details.
//
// 2. POLYMORPHISM:
//    - Overrides getUserRole() and displayDetails() with Admin-specific behavior.
//
// 3. ADMIN RESPONSIBILITIES:
//    - Contains methods to manage Companies, Job Roles, and Skills.
public class Admin extends User {

    // Admin-specific field (Encapsulation)
    private String department;

    // Collections of managed resources
    private ArrayList<Company> managedCompanies;
    private ArrayList<Skill> managedSkills;

    // Constructor
    public Admin(String userId, String name, String email, String department) {
        // INHERITANCE: Call parent constructor
        super(userId, name, email);
        
        setDepartment(department);
        this.managedCompanies = new ArrayList<>();
        this.managedSkills = new ArrayList<>();
    }

    // POLYMORPHISM: Admin role implementation
    @Override
    public String getUserRole() {
        return "Admin";
    }

    // POLYMORPHISM: Admin details display
    @Override
    public void displayDetails() {
        System.out.println("----------------------------------------------");
        System.out.println("ADMIN PROFILE");
        System.out.println("Name           : " + getName());
        System.out.println("Email          : " + getEmail());
        System.out.println("Role           : " + getUserRole());
        System.out.println("Department     : " + department);
        System.out.println("Companies Added: " + managedCompanies.size());
        System.out.println("Skills Added   : " + managedSkills.size());
        System.out.println("----------------------------------------------");
    }

    // Admin method: register a new company
    public void addCompany(Company company) {
        if (company != null) {
            managedCompanies.add(company);
            System.out.println("[Admin Action] Added company: " + company.getCompanyName());
        }
    }

    // Admin method: post a new job role for a company
    public void addJobRoleToCompany(Company company, JobRole role) {
        if (company != null && role != null) {
            company.addJobRole(role);
            System.out.println("[Admin Action] Added job '" + role.getRoleTitle() + "' to " + company.getCompanyName());
        }
    }

    // Admin method: create a new skill in the system
    public void addSkill(Skill skill) {
        if (skill != null) {
            managedSkills.add(skill);
            System.out.println("[Admin Action] Added skill: " + skill.getSkillName());
        }
    }

    // Getter and Setter
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department cannot be empty!");
        }
        this.department = department;
    }

    public ArrayList<Company> getManagedCompanies() {
        return managedCompanies;
    }

    public ArrayList<Skill> getManagedSkills() {
        return managedSkills;
    }
}
