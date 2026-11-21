package prepforge.evaluator;

import java.util.List;
import java.util.regex.Pattern;
import prepforge.model.KeywordBased;
import prepforge.model.Question;

public class KeywordEvaluator implements Evaluator {

    @Override
    public double evaluate(Question question, String answer) {
        if (!(question instanceof KeywordBased)) {
            throw new IllegalArgumentException("KeywordEvaluator needs a keyword-based question");
        }
        List<String> keywords = ((KeywordBased) question).getKeywords();
        if (answer == null || answer.isBlank() || keywords.isEmpty()) {
            return 0;
        }
        String text = answer.toLowerCase();
        int found = 0;
        for (String keyword : keywords) {
            if (containsWord(text, keyword)) {
                found++;
            }
        }
        double marks = (double) found / keywords.size() * question.getMaxMarks();
        return Math.round(marks * 100.0) / 100.0;
    }

    private boolean containsWord(String text, String keyword) {
        Pattern p = Pattern.compile("(?<![a-z0-9])" + Pattern.quote(keyword) + "(?![a-z0-9])");
        return p.matcher(text).find();
    }
}
