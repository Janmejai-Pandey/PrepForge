package prepforge.model;

/**
 * Abstract Base Class: Question
 * 
 * Contract Owner: Member 3 (Mock Interviews & Analytics)
 * 
 * Represents an assessment question in the PrepForge question bank.
 * Demonstrates:
 * 1. ABSTRACTION: Abstract methods getMaxMarks() and display() enforced on all question types.
 * 2. ENCAPSULATION: Private attributes with getters and setters.
 */
public abstract class Question {

    private int id;
    private String text;
    private String topic;
    private String difficulty; // "EASY", "MEDIUM", "HARD"
    private double marks;

    public Question() {
    }

    public Question(int id, String text, String topic, String difficulty, double marks) {
        this.id = id;
        this.text = text;
        this.topic = topic;
        this.difficulty = difficulty;
        this.marks = marks;
    }

    public Question(String text, String topic, String difficulty, double marks) {
        this.text = text;
        this.topic = topic;
        this.difficulty = difficulty;
        this.marks = marks;
    }

    // ---------------- Abstract Methods (Contract M3) ----------------
    public abstract double getMaxMarks();
    public abstract String display();
    public abstract String getQuestionType();

    // ---------------- Getters and Setters ----------------
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Alias for getQuestionId / setQuestionId
    public int getQuestionId() {
        return id;
    }

    public void setQuestionId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public double getMarks() {
        return marks;
    }

    public void setMarks(double marks) {
        this.marks = marks;
    }

    @Override
    public String toString() {
        return "[" + getQuestionType() + " | " + difficulty + " | " + topic + "] " + text;
    }
}
