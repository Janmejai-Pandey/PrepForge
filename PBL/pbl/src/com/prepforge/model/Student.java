package com.prepforge.model;

import java.util.ArrayList;

// SUBCLASS: Student
//
// 1. INHERITANCE:
//    - "extends User" means Student gets all properties (userId, name, email) from User.
//    - We use "super(...)" in the constructor to pass basic details to User.
//
// 2. POLYMORPHISM:
//    - Overrides getUserRole() and displayDetails() with Student-specific behavior.
//
// 3. ASSOCIATION:
//    - Student has a "targetRole" (linking a student to their dream placement job).
//
// 4. AGGREGATION:
//    - Student has a list of Skills and a list of InterviewRecords.
public class Student extends User {

    // Student-specific fields (Encapsulation)
    private String enrollmentNumber;
    private String branch;
    private double cgpa;

    // ASSOCIATION: Student is linked to a target JobRole
    private JobRole targetRole;

    // AGGREGATION: Student HAS-A list of skills & interview records
    private ArrayList<Skill> skills;
    private ArrayList<InterviewRecord> interviewHistory;

    // Constructor
    public Student(String userId, String name, String email, 
                   String enrollmentNumber, String branch, double cgpa) {
        // INHERITANCE: "super" calls the constructor of the parent class (User)
        super(userId, name, email);
        
        setEnrollmentNumber(enrollmentNumber);
        setBranch(branch);
        setCgpa(cgpa);
        this.skills = new ArrayList<>();
        this.interviewHistory = new ArrayList<>();
    }

    // POLYMORPHISM: Subclass provides its own implementation of the abstract method
    @Override
    public String getUserRole() {
        return "Student";
    }

    // POLYMORPHISM: Displaying student-specific profile
    @Override
    public void displayDetails() {
        System.out.println("----------------------------------------------");
        System.out.println("STUDENT PROFILE");
        System.out.println("Name           : " + getName());
        System.out.println("Email          : " + getEmail());
        System.out.println("Enrollment No  : " + enrollmentNumber);
        System.out.println("Branch         : " + branch);
        System.out.println("CGPA           : " + cgpa + " / 10.0");
        
        if (targetRole != null) {
            System.out.println("Target Job     : " + targetRole.getRoleTitle() + " at " + targetRole.getCompany().getCompanyName());
            System.out.println("Eligible?      : " + (isEligibleForTargetRole() ? "YES" : "NO (Needs CGPA " + targetRole.getMinCgpa() + ")"));
        } else {
            System.out.println("Target Job     : None Selected");
        }

        System.out.println("Skills Acquired: " + skills);
        System.out.println("Mock Interviews: " + interviewHistory.size() + " completed");
        for (InterviewRecord record : interviewHistory) {
            System.out.println("  * " + record);
        }
        System.out.println("----------------------------------------------");
    }

    // Helper method to check if student meets target job's CGPA cutoff
    public boolean isEligibleForTargetRole() {
        if (targetRole == null) {
            return false;
        }
        return this.cgpa >= targetRole.getMinCgpa();
    }

    // Helper methods for Aggregation lists
    public void addSkill(Skill skill) {
        if (skill != null) {
            skills.add(skill);
        }
    }

    public void addInterviewRecord(InterviewRecord record) {
        if (record != null) {
            interviewHistory.add(record);
        }
    }

    // Getters and Setters (Encapsulation)
    public String getEnrollmentNumber() {
        return enrollmentNumber;
    }

    public void setEnrollmentNumber(String enrollmentNumber) {
        if (enrollmentNumber == null || enrollmentNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Enrollment number cannot be empty!");
        }
        this.enrollmentNumber = enrollmentNumber;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        if (branch == null || branch.trim().isEmpty()) {
            throw new IllegalArgumentException("Branch cannot be empty!");
        }
        this.branch = branch;
    }

    public double getCgpa() {
        return cgpa;
    }

    public void setCgpa(double cgpa) {
        if (cgpa < 0.0 || cgpa > 10.0) {
            throw new IllegalArgumentException("CGPA must be between 0.0 and 10.0!");
        }
        this.cgpa = cgpa;
    }

    public JobRole getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(JobRole targetRole) {
        this.targetRole = targetRole;
    }

    public ArrayList<Skill> getSkills() {
        return skills;
    }

    public ArrayList<InterviewRecord> getInterviewHistory() {
        return interviewHistory;
    }
}
