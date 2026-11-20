package prepforge.model;

import java.time.LocalDateTime;

/**
 * Domain Model: Result
 * 
 * Contract Owner: Member 3
 * Stores the evaluation score, percentage, grade, and reference for an interview.
 */
public class Result {

    private int resultId;
    private int interviewId;
    private int studentId;
    private double totalScore;
    private double maxScore;
    private double percentage;
    private String grade;
    private LocalDateTime evaluatedAt;

    public Result() {
        this.evaluatedAt = LocalDateTime.now();
    }

    public Result(int interviewId, int studentId, double totalScore, double maxScore, double percentage, String grade) {
        this.interviewId = interviewId;
        this.studentId = studentId;
        this.totalScore = totalScore;
        this.maxScore = maxScore;
        this.percentage = percentage;
        this.grade = grade;
        this.evaluatedAt = LocalDateTime.now();
    }

    public Result(int resultId, int interviewId, int studentId, double totalScore, double maxScore, double percentage, String grade) {
        this.resultId = resultId;
        this.interviewId = interviewId;
        this.studentId = studentId;
        this.totalScore = totalScore;
        this.maxScore = maxScore;
        this.percentage = percentage;
        this.grade = grade;
        this.evaluatedAt = LocalDateTime.now();
    }

    // ---------------- Getters and Setters ----------------
    public int getResultId() {
        return resultId;
    }

    public void setResultId(int resultId) {
        this.resultId = resultId;
    }

    public int getInterviewId() {
        return interviewId;
    }

    public void setInterviewId(int interviewId) {
        this.interviewId = interviewId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(double maxScore) {
        this.maxScore = maxScore;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(LocalDateTime evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }

    @Override
    public String toString() {
        return "Result [Score=" + totalScore + "/" + maxScore + " (" + String.format("%.1f", percentage) + "%), Grade=" + grade + "]";
    }
}
