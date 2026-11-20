package prepforge.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MCQQuestion extends Question {

    private final List<String> options;
    private final int correctIndex;

    public MCQQuestion(int id, String text, String topic, String difficulty, List<String> options, int correctIndex) {
        super(id, text, topic, difficulty);

        if (options == null || options.size() < 2) {
            throw new IllegalArgumentException("An MCQ needs at least 2 options");
        }

        if (correctIndex < 0 || correctIndex >= options.size()) {
            throw new IllegalArgumentException("Correct option index is out of range");
        }

        this.options = new ArrayList<>(options);
        this.correctIndex = correctIndex;
    }

    public MCQQuestion(String text, String topic, String difficulty, List<String> options, int correctIndex) {
        this(0, text, topic, difficulty, options, correctIndex);
    }

    public List<String> getOptions() { return Collections.unmodifiableList(options); }
    public int getCorrectIndex() { return correctIndex; }

    public String getCorrectLetter() {
        return String.valueOf((char) ('A' + correctIndex));
    }

    @Override public String getType() { return "MCQ"; }

    @Override public int getMaxMarks() { return 1; }

    @Override
    public String display() {
        StringBuilder sb = new StringBuilder();
        sb.append("[MCQ | ").append(getTopic()).append(" | ").append(getDifficulty()).append("] ").append(getText());

        for (int i = 0; i < options.size(); i++) {
            sb.append("\n  ").append((char) ('A' + i)).append(") ").append(options.get(i));
        }
        
        return sb.toString();
    }
}