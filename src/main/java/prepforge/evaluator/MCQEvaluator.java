package prepforge.evaluator;

import prepforge.model.MCQQuestion;
import prepforge.model.Question;

/**
 * Evaluator Implementation: MCQEvaluator
 * 
 * Contract Owner: Member 3
 * Evaluates multiple-choice questions by performing exact/case-insensitive matching
 * against the designated correct option.
 */
public class MCQEvaluator implements Evaluator {

    @Override
    public double evaluate(Question question, String answer) {
        if (!(question instanceof MCQQuestion) || answer == null) {
            return 0.0;
        }

        MCQQuestion mcq = (MCQQuestion) question;
        String correct = mcq.getCorrectOption();
        if (correct == null) {
            return 0.0;
        }

        String trimmedAns = answer.trim();
        if (trimmedAns.equalsIgnoreCase(correct.trim())) {
            return mcq.getMaxMarks();
        }

        // Also check if answer matches option index letter (e.g. "A" for option 0)
        if (trimmedAns.length() == 1) {
            char choiceChar = Character.toUpperCase(trimmedAns.charAt(0));
            int choiceIdx = choiceChar - 'A';
            if (choiceIdx >= 0 && choiceIdx < mcq.getOptions().size()) {
                String optionText = mcq.getOptions().get(choiceIdx);
                if (optionText.equalsIgnoreCase(correct.trim())) {
                    return mcq.getMaxMarks();
                }
            }
        }

        return 0.0;
    }
}
