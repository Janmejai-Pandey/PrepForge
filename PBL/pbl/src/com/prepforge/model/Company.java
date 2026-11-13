package com.prepforge.model;

import java.util.ArrayList;

// DOMAIN CLASS: Company
// Represents a hiring company (e.g. "Google", "TCS").
public class Company {

    // ENCAPSULATION: private variables
    private String companyName;
    private String location;

    // AGGREGATION (HAS-A Relationship):
    // A Company HAS-A list of JobRoles.
    // Notice: JobRoles can exist independently even if a company closes down.
    private ArrayList<JobRole> jobRoles;

    // Constructor
    public Company(String companyName, String location) {
        setCompanyName(companyName);
        setLocation(location);
        this.jobRoles = new ArrayList<>(); // Initialize the list
    }

    // Method to add a job role to this company
    public void addJobRole(JobRole role) {
        if (role != null) {
            jobRoles.add(role);
        }
    }

    // Getters and Setters
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be empty!");
        }
        this.companyName = companyName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ArrayList<JobRole> getJobRoles() {
        return jobRoles;
    }

    @Override
    public String toString() {
        return companyName + " (" + location + ")";
    }
}
