package prepforge.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HRQuestion extends Question implements KeywordBased {

    private final List<String> keywords;

    public HRQuestion(int id, String text, String topic, String difficulty, List<String> keywords) {
        super(id, text, topic, difficulty);
        if (keywords == null || keywords.isEmpty()) {
            throw new IllegalArgumentException("An HR question needs expected keywords");
        }
        this.keywords = new ArrayList<>();
        for (String k : keywords) {
            this.keywords.add(k.trim().toLowerCase());
        }
    }

    public HRQuestion(String text, String topic, String difficulty, List<String> keywords) {
        this(0, text, topic, difficulty, keywords);
    }

    @Override
    public List<String> getKeywords() {
        return Collections.unmodifiableList(keywords);
    }

    @Override
    public String getType() {
        return "HR";
    }

    @Override
    public int getMaxMarks() {
        return 5;
    }

    @Override
    public String display() {
        return "[HR | " + getTopic() + " | " + getDifficulty() + "] " + getText()
                + "\n  (Answer as you would in a real interview)";
    }
}
