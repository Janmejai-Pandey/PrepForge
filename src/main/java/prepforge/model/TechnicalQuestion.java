package prepforge.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Subclass: TechnicalQuestion
 * 
 * Contract Owner: Member 3
 * Technical domain question evaluated via keyword coverage.
 */
public class TechnicalQuestion extends Question {

    private List<String> expectedKeywords;

    public TechnicalQuestion() {
        super();
        this.expectedKeywords = new ArrayList<>();
    }

    public TechnicalQuestion(int id, String text, String topic, String difficulty, double marks,
                             List<String> expectedKeywords) {
        super(id, text, topic, difficulty, marks);
        this.expectedKeywords = expectedKeywords != null ? expectedKeywords : new ArrayList<>();
    }

    public TechnicalQuestion(String text, String topic, String difficulty, double marks,
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
        return "Technical Question (" + getTopic() + "):\n" + getText();
    }

    @Override
    public String getQuestionType() {
        return "TECHNICAL";
    }

    public List<String> getExpectedKeywords() {
        return expectedKeywords;
    }

    public void setExpectedKeywords(List<String> expectedKeywords) {
        this.expectedKeywords = expectedKeywords != null ? expectedKeywords : new ArrayList<>();
    }
}
