package prepforge.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Subclass: CodingQuestion
 * 
 * Contract Owner: Member 3
 * Coding / algorithmic problem statement evaluated via keyword or logic coverage.
 */
public class CodingQuestion extends Question {

    private String problemStatement;
    private List<String> expectedKeywords;

    public CodingQuestion() {
        super();
        this.expectedKeywords = new ArrayList<>();
    }

    public CodingQuestion(int id, String text, String topic, String difficulty, double marks,
                          String problemStatement, List<String> expectedKeywords) {
        super(id, text, topic, difficulty, marks);
        this.problemStatement = problemStatement;
        this.expectedKeywords = expectedKeywords != null ? expectedKeywords : new ArrayList<>();
    }

    public CodingQuestion(String text, String topic, String difficulty, double marks,
                          String problemStatement, List<String> expectedKeywords) {
        super(text, topic, difficulty, marks);
        this.problemStatement = problemStatement;
        this.expectedKeywords = expectedKeywords != null ? expectedKeywords : new ArrayList<>();
    }

    @Override
    public double getMaxMarks() {
        return getMarks();
    }

    @Override
    public String display() {
        return "Coding Challenge: " + getText() + "\nSpecification:\n" + problemStatement;
    }

    @Override
    public String getQuestionType() {
        return "CODING";
    }

    public String getProblemStatement() {
        return problemStatement;
    }

    public void setProblemStatement(String problemStatement) {
        this.problemStatement = problemStatement;
    }

    public List<String> getExpectedKeywords() {
        return expectedKeywords;
    }

    public void setExpectedKeywords(List<String> expectedKeywords) {
        this.expectedKeywords = expectedKeywords != null ? expectedKeywords : new ArrayList<>();
    }
}
