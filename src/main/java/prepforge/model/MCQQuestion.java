package prepforge.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Subclass: MCQQuestion
 * 
 * Contract Owner: Member 3
 * Multiple Choice Question with options and a defined correct option.
 */
public class MCQQuestion extends Question {

    private List<String> options;
    private String correctOption; // e.g. "A", "B", "C", "D" or the exact option text

    public MCQQuestion() {
        super();
        this.options = new ArrayList<>();
    }

    public MCQQuestion(int id, String text, String topic, String difficulty, double marks,
                       List<String> options, String correctOption) {
        super(id, text, topic, difficulty, marks);
        this.options = options != null ? options : new ArrayList<>();
        this.correctOption = correctOption;
    }

    public MCQQuestion(String text, String topic, String difficulty, double marks,
                       List<String> options, String correctOption) {
        super(text, topic, difficulty, marks);
        this.options = options != null ? options : new ArrayList<>();
        this.correctOption = correctOption;
    }

    @Override
    public double getMaxMarks() {
        return getMarks();
    }

    @Override
    public String display() {
        StringBuilder sb = new StringBuilder();
        sb.append(getText()).append("\n");
        char optChar = 'A';
        for (String opt : options) {
            sb.append("  (").append(optChar++).append(") ").append(opt).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String getQuestionType() {
        return "MCQ";
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options != null ? options : new ArrayList<>();
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(String correctOption) {
        this.correctOption = correctOption;
    }
}
