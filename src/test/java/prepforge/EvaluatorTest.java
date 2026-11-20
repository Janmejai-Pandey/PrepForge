package prepforge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import prepforge.evaluator.Evaluator;
import prepforge.evaluator.KeywordEvaluator;
import prepforge.evaluator.MCQEvaluator;
import prepforge.model.CodingQuestion;
import prepforge.model.HRQuestion;
import prepforge.model.MCQQuestion;
import prepforge.model.Question;
import prepforge.model.TechnicalQuestion;

class EvaluatorTest {

    private final MCQQuestion mcq = new MCQQuestion(1, "Which keyword makes a class abstract?",
            "Java", "EASY", List.of("final", "abstract", "static"), 1);   // correct = B

    private final TechnicalQuestion tech = new TechnicalQuestion(2, "Explain OOP pillars", "OOP", "EASY",
            List.of("encapsulation", "inheritance", "polymorphism", "abstraction"));

    private final CodingQuestion coding = new CodingQuestion(3, "Sum an array", "DSA", "EASY",
            "Return the sum of an int array", List.of("for", "return"));

    private final HRQuestion hr = new HRQuestion(4, "Tell me about yourself", "Communication", "EASY",
            List.of("team", "goal"));

    // ---------- MCQEvaluator ----------

    @Test
    void mcqCorrectLetterGetsFullMarks() {
        assertEquals(1, new MCQEvaluator().evaluate(mcq, "B"));
    }

    @Test
    void mcqAcceptsLowerCaseAndBracket() {
        assertEquals(1, new MCQEvaluator().evaluate(mcq, "b"));
        assertEquals(1, new MCQEvaluator().evaluate(mcq, "B)"));
    }

    @Test
    void mcqAcceptsOptionText() {
        assertEquals(1, new MCQEvaluator().evaluate(mcq, "Abstract"));
    }

    @Test
    void mcqWrongOrBlankGetsZero() {
        assertEquals(0, new MCQEvaluator().evaluate(mcq, "A"));
        assertEquals(0, new MCQEvaluator().evaluate(mcq, ""));
        assertEquals(0, new MCQEvaluator().evaluate(mcq, null));
    }

    @Test
    void mcqEvaluatorRejectsOtherQuestionTypes() {
        assertThrows(IllegalArgumentException.class, () -> new MCQEvaluator().evaluate(tech, "B"));
    }

    // ---------- KeywordEvaluator ----------

    @Test
    void allKeywordsGetFullMarks() {
        String answer = "Encapsulation, inheritance, polymorphism and abstraction.";
        assertEquals(5.0, new KeywordEvaluator().evaluate(tech, answer));
    }

    @Test
    void halfTheKeywordsGetHalfMarks() {
        assertEquals(2.5, new KeywordEvaluator().evaluate(tech, "inheritance and polymorphism"));
    }

    @Test
    void noKeywordsGetZero() {
        assertEquals(0.0, new KeywordEvaluator().evaluate(tech, "I do not know"));
    }

    @Test
    void blankOrNullAnswerGetsZero() {
        assertEquals(0.0, new KeywordEvaluator().evaluate(tech, "   "));
        assertEquals(0.0, new KeywordEvaluator().evaluate(tech, null));
    }

    @Test
    void matchingIgnoresCase() {
        assertEquals(5.0, new KeywordEvaluator().evaluate(hr, "TEAM work and my GOAL"));
    }

    @Test
    void keywordMustBeAWholeWord() {
        // "for" must not match inside "before"; only "return" is found
        assertEquals(5.0, new KeywordEvaluator().evaluate(coding, "return sum; // before"));
    }

    @Test
    void codingAnswerUsesKeywordsAndTenMarks() {
        String code = "int sum = 0; for (int x : arr) { sum += x; } return sum;";
        assertEquals(10.0, new KeywordEvaluator().evaluate(coding, code));
    }

    @Test
    void keywordEvaluatorRejectsMcq() {
        assertThrows(IllegalArgumentException.class, () -> new KeywordEvaluator().evaluate(mcq, "B"));
    }

    // ---------- choosing the evaluator at runtime (polymorphism) ----------

    @Test
    void factoryPicksTheRightEvaluator() {
        assertTrue(Evaluator.forQuestion(mcq) instanceof MCQEvaluator);
        assertTrue(Evaluator.forQuestion(tech) instanceof KeywordEvaluator);
        assertTrue(Evaluator.forQuestion(coding) instanceof KeywordEvaluator);
        assertTrue(Evaluator.forQuestion(hr) instanceof KeywordEvaluator);
    }

    @Test
    void oneLoopMarksEveryQuestionType() {
        List<Question> questions = List.of(mcq, tech, coding, hr);
        List<String> answers = List.of("B", "inheritance and polymorphism", "for return", "team goal");
        double total = 0;
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            total += Evaluator.forQuestion(q).evaluate(q, answers.get(i));
        }
        assertEquals(1 + 2.5 + 10 + 5, total);
    }
}
