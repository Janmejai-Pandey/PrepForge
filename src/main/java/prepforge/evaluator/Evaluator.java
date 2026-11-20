package prepforge.evaluator;

import prepforge.model.Question;

/**
 * Marks one answer. Different question types use different implementations
 * (polymorphism): the right evaluator is chosen at runtime from the question type.
 */
public interface Evaluator {

    /**
     * @return marks awarded, from 0 up to question.getMaxMarks()
     */
    double evaluate(Question question, String answer);

    /** Pick the right evaluator for a question: MCQ gets exact match, everything else keywords. */
    static Evaluator forQuestion(Question question) {
        if (question == null) {
            throw new IllegalArgumentException("Question cannot be null");
        }
        if ("MCQ".equals(question.getType())) {
            return new MCQEvaluator();
        }
        return new KeywordEvaluator();
    }
}
