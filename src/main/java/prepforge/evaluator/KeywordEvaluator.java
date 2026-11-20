package prepforge.evaluator;

import java.util.List;
import prepforge.model.CodingQuestion;
import prepforge.model.HRQuestion;
import prepforge.model.Question;
import prepforge.model.TechnicalQuestion;

/**
 * Evaluator Implementation: KeywordEvaluator
 * 
 * Contract Owner: Member 3
 * Scores open-ended answers (Technical, HR, Coding) based on the presence of
 * domain keywords in the candidate's response.
 */
public class KeywordEvaluator implements Evaluator {

    @Override
    public double evaluate(Question question, String answer) {
        if (question == null || answer == null || answer.trim().isEmpty()) {
            return 0.0;
        }

        List<String> keywords = null;
        if (question instanceof TechnicalQuestion) {
            keywords = ((TechnicalQuestion) question).getExpectedKeywords();
        } else if (question instanceof HRQuestion) {
            keywords = ((HRQuestion) question).getExpectedKeywords();
        } else if (question instanceof CodingQuestion) {
            keywords = ((CodingQuestion) question).getExpectedKeywords();
        }

        if (keywords == null || keywords.isEmpty()) {
            // Default credit if no specific keywords mandated and substantial answer provided
            return answer.trim().length() > 15 ? question.getMaxMarks() : question.getMaxMarks() * 0.5;
        }

        String lowerAnswer = answer.toLowerCase();
        int matched = 0;
        for (String kw : keywords) {
            if (kw != null && lowerAnswer.contains(kw.toLowerCase().trim())) {
                matched++;
            }
        }

        double ratio = (double) matched / keywords.size();
        return Math.round((ratio * question.getMaxMarks()) * 100.0) / 100.0;
    }
}
