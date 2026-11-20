package prepforge;

import java.util.List;
import prepforge.evaluator.Evaluator;
import prepforge.evaluator.KeywordEvaluator;
import prepforge.evaluator.MCQEvaluator;
import prepforge.model.CodingQuestion;
import prepforge.model.HRQuestion;
import prepforge.model.MCQQuestion;
import prepforge.model.Question;
import prepforge.model.TechnicalQuestion;

/**
 * Temporary test without JUnit: just run this class (the Run button above main in VS Code).
 * Every line prints PASS or FAIL. Replace with EvaluatorTest once JUnit is set up.
 */
public class EvaluatorCheck {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        MCQQuestion mcq = new MCQQuestion(1, "Which keyword makes a class abstract?",
                "Java", "EASY", List.of("final", "abstract", "static"), 1);   // correct = B
        TechnicalQuestion tech = new TechnicalQuestion(2, "Explain OOP pillars", "OOP", "EASY",
                List.of("encapsulation", "inheritance", "polymorphism", "abstraction"));
        CodingQuestion coding = new CodingQuestion(3, "Sum an array", "DSA", "EASY",
                "Return the sum of an int array", List.of("for", "return"));
        HRQuestion hr = new HRQuestion(4, "Tell me about yourself", "Communication", "EASY",
                List.of("team", "goal"));

        Evaluator mcqEval = new MCQEvaluator();
        Evaluator keyEval = new KeywordEvaluator();

        System.out.println("--- MCQEvaluator ---");
        check("correct letter B", 1, mcqEval.evaluate(mcq, "B"));
        check("lower case b", 1, mcqEval.evaluate(mcq, "b"));
        check("letter with bracket B)", 1, mcqEval.evaluate(mcq, "B)"));
        check("full option text", 1, mcqEval.evaluate(mcq, "Abstract"));
        check("wrong answer A", 0, mcqEval.evaluate(mcq, "A"));
        check("blank answer", 0, mcqEval.evaluate(mcq, ""));
        check("null answer", 0, mcqEval.evaluate(mcq, null));
        checkThrows("rejects a technical question", () -> mcqEval.evaluate(tech, "B"));

        System.out.println("--- KeywordEvaluator ---");
        check("all 4 keywords = 5 marks", 5,
                keyEval.evaluate(tech, "Encapsulation, inheritance, polymorphism and abstraction."));
        check("2 of 4 keywords = 2.5 marks", 2.5, keyEval.evaluate(tech, "inheritance and polymorphism"));
        check("no keywords = 0", 0, keyEval.evaluate(tech, "I do not know"));
        check("blank answer", 0, keyEval.evaluate(tech, "   "));
        check("null answer", 0, keyEval.evaluate(tech, null));
        check("ignores upper case", 5, keyEval.evaluate(hr, "TEAM work and my GOAL"));
        check("whole word only ('for' not in 'before')", 5, keyEval.evaluate(coding, "return sum; // before"));
        check("coding answer = 10 marks", 10,
                keyEval.evaluate(coding, "int sum = 0; for (int x : arr) { sum += x; } return sum;"));
        checkThrows("rejects an MCQ question", () -> keyEval.evaluate(mcq, "B"));

        System.out.println("--- Choosing the evaluator at runtime ---");
        check("MCQ gets MCQEvaluator", 1, Evaluator.forQuestion(mcq) instanceof MCQEvaluator ? 1 : 0);
        check("technical gets KeywordEvaluator", 1, Evaluator.forQuestion(tech) instanceof KeywordEvaluator ? 1 : 0);
        check("coding gets KeywordEvaluator", 1, Evaluator.forQuestion(coding) instanceof KeywordEvaluator ? 1 : 0);
        check("HR gets KeywordEvaluator", 1, Evaluator.forQuestion(hr) instanceof KeywordEvaluator ? 1 : 0);

        List<Question> questions = List.of(mcq, tech, coding, hr);
        List<String> answers = List.of("B", "inheritance and polymorphism", "for return", "team goal");
        double total = 0;
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            total += Evaluator.forQuestion(q).evaluate(q, answers.get(i));
        }
        check("one loop marks every type (1 + 2.5 + 10 + 5)", 18.5, total);

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void check(String name, double expected, double actual) {
        if (Math.abs(expected - actual) < 0.0001) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name + "  (expected " + expected + ", got " + actual + ")");
        }
    }

    private static void checkThrows(String name, Runnable action) {
        try {
            action.run();
            failed++;
            System.out.println("FAIL  " + name + "  (no exception thrown)");
        } catch (IllegalArgumentException e) {
            passed++;
            System.out.println("PASS  " + name);
        }
    }
}
