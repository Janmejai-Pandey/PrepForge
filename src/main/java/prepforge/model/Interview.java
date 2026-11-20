package prepforge.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract Base Class: Interview
 * 
 * Contract Owner: Member 3
 * Represents an interview session taken by a student.
 * 
 * Demonstrates:
 * 1. ABSTRACTION & INHERITANCE: Specific interview types (Technical, HR, Mock)
 *    inherit common session state and implement buildQuestionSet().
 * 2. COLLECTIONS: List<Question> ordered session questions, Map<Integer, String> answers.
 */
public abstract class Interview {

    private int id;
    private int studentId;
    private String difficulty; // "EASY", "MEDIUM", "HARD"
    private String status;     // "STARTED", "SUBMITTED", "EVALUATED"
    private List<Question> questions;
    private Map<Integer, String> answers; // questionId -> answer text

    public Interview() {
        this.questions = new ArrayList<>();
        this.answers = new HashMap<>();
        this.status = "STARTED";
    }

    public Interview(int studentId, String difficulty) {
        this.studentId = studentId;
        this.difficulty = difficulty;
        this.questions = new ArrayList<>();
        this.answers = new HashMap<>();
        this.status = "STARTED";
    }

    public Interview(int id, int studentId, String difficulty) {
        this.id = id;
        this.studentId = studentId;
        this.difficulty = difficulty;
        this.questions = new ArrayList<>();
        this.answers = new HashMap<>();
        this.status = "STARTED";
    }

    // ---------------- Abstract Methods (Contract M3) ----------------
    public abstract String getInterviewType();
    public abstract void buildQuestionSet(List<Question> availableQuestions, int numQuestions);

    // ---------------- Helper Methods ----------------
    public void recordAnswer(int questionId, String answer) {
        answers.put(questionId, answer);
    }

    public String getAnswer(int questionId) {
        return answers.get(questionId);
    }

    public void addQuestion(Question q) {
        if (q != null && !questions.contains(q)) {
            questions.add(q);
        }
    }

    // ---------------- Getters and Setters ----------------
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInterviewId() {
        return id;
    }

    public void setInterviewId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions != null ? questions : new ArrayList<>();
    }

    public Map<Integer, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Integer, String> answers) {
        this.answers = answers != null ? answers : new HashMap<>();
    }
}
