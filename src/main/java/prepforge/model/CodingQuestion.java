package prepforge.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Coding question with a problem statement, marked by keyword/pattern coverage. Worth 10 marks. */
public class CodingQuestion extends Question implements KeywordBased {

    private final String problemStatement;
    private final List<String> keywords;   // e.g. "for", "return", "stack" (lower case)

    public CodingQuestion(int id, String text, String topic, String difficulty,
                          String problemStatement, List<String> keywords) {
        super(id, text, topic, difficulty);
        if (problemStatement == null || problemStatement.isBlank()) {
            throw new IllegalArgumentException("A coding question needs a problem statement");
        }
        if (keywords == null || keywords.isEmpty()) {
            throw new IllegalArgumentException("A coding question needs expected keywords");
        }
        this.problemStatement = problemStatement.trim();
        this.keywords = new ArrayList<>();
        for (String k : keywords) {
            this.keywords.add(k.trim().toLowerCase());
        }
    }

    public CodingQuestion(String text, String topic, String difficulty,
                          String problemStatement, List<String> keywords) {
        this(0, text, topic, difficulty, problemStatement, keywords);
    }

    public String getProblemStatement() { return problemStatement; }
    @Override
    public List<String> getKeywords() { return Collections.unmodifiableList(keywords); }

    @Override public String getType() { return "CODING"; }

    @Override public int getMaxMarks() { return 10; }

    @Override
    public String display() {
        return "[Coding | " + getTopic() + " | " + getDifficulty() + "] " + getText()
             + "\n  " + problemStatement
             + "\n  (Write your code below)";
    }
}