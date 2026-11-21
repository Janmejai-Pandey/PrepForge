package prepforge.evaluator;

import prepforge.model.MCQQuestion;
import prepforge.model.Question;

public class MCQEvaluator implements Evaluator {

    @Override
    public double evaluate(Question question, String answer) {
        if (!(question instanceof MCQQuestion)) {
            throw new IllegalArgumentException("MCQEvaluator can only mark MCQ questions");
        }
        MCQQuestion mcq = (MCQQuestion) question;
        if (answer == null || answer.isBlank()) {
            return 0;
        }
        String given = answer.trim();

        boolean looksLikeLetter = given.length() == 1
                || (given.length() == 2 && (given.charAt(1) == ')' || given.charAt(1) == '.'));
        if (looksLikeLetter) {
            return given.substring(0, 1).equalsIgnoreCase(mcq.getCorrectLetter()) ? mcq.getMaxMarks() : 0;
        }

        String correctText = mcq.getOptions().get(mcq.getCorrectIndex());
        return given.equalsIgnoreCase(correctText) ? mcq.getMaxMarks() : 0;
    }
}
