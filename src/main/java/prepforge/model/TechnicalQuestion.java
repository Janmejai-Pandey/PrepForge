package prepforge.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Short-answer technical question, marked by keyword coverage. Worth 5 marks. */
public class TechnicalQuestion extends Question implements KeywordBased {

    private final List<String> keywords;   // stored in lower case

    public TechnicalQuestion(int id, String text, String topic, String difficulty,
                             List<String> keywords) {
        super(id, text, topic, difficulty);
        if (keywords == null || keywords.isEmpty()) {
            throw new IllegalArgumentException("A technical question needs expected keywords");
        }
        this.keywords = new ArrayList<>();
        for (String k : keywords) {
            this.keywords.add(k.trim().toLowerCase());
        }
    }

    public TechnicalQuestion(String text, String topic, String difficulty, List<String> keywords) {
        this(0, text, topic, difficulty, keywords);
    }

    @Override
    public List<String> getKeywords() { return Collections.unmodifiableList(keywords); }

    @Override public String getType() { return "TECHNICAL"; }

    @Override public int getMaxMarks() { return 5; }

    @Override
    public String display() {
        return "[Technical | " + getTopic() + " | " + getDifficulty() + "] " + getText()
             + "\n  (Write your answer in a few sentences)";
    }
}