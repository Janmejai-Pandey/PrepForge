package prepforge.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Subclass: HRQuestion
 * 
 * Contract Owner: Member 3
 * Behavioral / situational HR interview question evaluated via keyword coverage.
 */
public class HRQuestion extends Question {

    private List<String> expectedKeywords;

    public HRQuestion() {
        super();
        this.expectedKeywords = new ArrayList<>();
    }

    public HRQuestion(int id, String text, String topic, String difficulty, double marks,
                      List<String> expectedKeywords) {
        super(id, text, topic, difficulty, marks);
        this.expectedKeywords = expectedKeywords != null ? expectedKeywords : new ArrayList<>();
    }

    public HRQuestion(String text, String topic, String difficulty, double marks,
                      List<String> expectedKeywords) {
        super(text, topic, difficulty, marks);
        this.expectedKeywords = expectedKeywords != null ? expectedKeywords : new ArrayList<>();
    }

    @Override
    public double getMaxMarks() {
        return getMarks();
    }

    @Override
    public String display() {
        return "HR Behavioral Prompt:\n" + getText();
    }

    @Override
    public String getQuestionType() {
        return "HR";
    }

    public List<String> getExpectedKeywords() {
        return expectedKeywords;
    }

    public void setExpectedKeywords(List<String> expectedKeywords) {
        this.expectedKeywords = expectedKeywords != null ? expectedKeywords : new ArrayList<>();
    }
}
