package com.prepforge.model;

// SUPPORTING CLASS: InterviewRecord
// Stores the result of one mock interview (e.g., Score: 85, Status: "Cleared").
public class InterviewRecord {

    // ENCAPSULATION: private variables
    private String companyName;
    private String roleTitle;
    private double score;     // e.g. 85.0
    private String status;    // "Passed" or "Needs Improvement"

    // Constructor
    public InterviewRecord(String companyName, String roleTitle, double score, String status) {
        this.companyName = companyName;
        this.roleTitle = roleTitle;
        this.score = score;
        this.status = status;
    }

    // Getters
    public String getCompanyName() { return companyName; }
    public String getRoleTitle() { return roleTitle; }
    public double getScore() { return score; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return roleTitle + " at " + companyName + " - Score: " + score + "% (" + status + ")";
    }
}
