package prepforge.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ApplicationRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private int applicationId;
    private int studentId;
    private String studentName;
    private String studentEmail;
    private String branch;
    private double cgpa;
    private int roleId;
    private String roleTitle;
    private String companyName;
    private String status;
    private LocalDateTime appliedAt;

    public ApplicationRecord() {
        this.appliedAt = LocalDateTime.now();
        this.status = "APPLIED";
    }

    public ApplicationRecord(int applicationId, int studentId, String studentName, String studentEmail,
                             String branch, double cgpa, int roleId, String roleTitle, String companyName, String status) {
        this.applicationId = applicationId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.branch = branch;
        this.cgpa = cgpa;
        this.roleId = roleId;
        this.roleTitle = roleTitle;
        this.companyName = companyName;
        this.status = status != null ? status : "APPLIED";
        this.appliedAt = LocalDateTime.now();
    }

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public double getCgpa() { return cgpa; }
    public void setCgpa(double cgpa) { this.cgpa = cgpa; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public String getRoleTitle() { return roleTitle; }
    public void setRoleTitle(String roleTitle) { this.roleTitle = roleTitle; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }
}
