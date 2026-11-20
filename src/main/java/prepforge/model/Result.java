package prepforge.model;

/** Final result of one interview: score, percentage and grade. */
public class Result {

    private int id;                    // 0 until saved in the database
    private final int interviewId;
    private final int studentId;
    private final double totalScore;   // marks obtained
    private final double maxScore;     // maximum possible marks

    // Constructor for a new result
    public Result(int interviewId, int studentId, double totalScore, double maxScore) {
        this(0, interviewId, studentId, totalScore, maxScore);
    }

    // Overloaded constructor for loading from the database
    public Result(int id, int interviewId, int studentId, double totalScore, double maxScore) {
        if (maxScore < 0 || totalScore < 0 || totalScore > maxScore) {
            throw new IllegalArgumentException("Invalid score values");
        }
        this.id = id;
        this.interviewId = interviewId;
        this.studentId = studentId;
        this.totalScore = totalScore;
        this.maxScore = maxScore;
    }

    public double getPercentage() {
        return maxScore == 0 ? 0 : (totalScore / maxScore) * 100.0;
    }

    public String getGrade() {
        return gradeFor(getPercentage());
    }

    /** Grade bands. M2's SQL function fn_grade must use the same bands. */
    public static String gradeFor(double percentage) {
        if (percentage >= 90) return "A+";
        if (percentage >= 75) return "A";
        if (percentage >= 60) return "B";
        if (percentage >= 40) return "C";
        return "F";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getInterviewId() { return interviewId; }
    public int getStudentId() { return studentId; }
    public double getTotalScore() { return totalScore; }
    public double getMaxScore() { return maxScore; }

    @Override
    public String toString() {
        return String.format("Result: %.1f/%.1f (%.1f%%, grade %s)",
                totalScore, maxScore, getPercentage(), getGrade());
    }
}
