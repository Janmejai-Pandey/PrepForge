package prepforge.evaluator;

import prepforge.model.Question;

public interface Evaluator {

    double evaluate(Question question, String answer);

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
