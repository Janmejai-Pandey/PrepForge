package prepforge.evaluator;

import prepforge.model.Question;

/**
 * Interface: Evaluator
 * 
 * Contract Owner: Member 3
 * Strategy interface for scoring answers against question criteria.
 */
public interface Evaluator {

    /**
     * Evaluates a candidate's answer for a question and awards marks.
     * 
     * @param question The question prompt
     * @param answer The candidate's submitted answer string
     * @return Marks awarded (0.0 up to question.getMaxMarks())
     */
    double evaluate(Question question, String answer);
}
