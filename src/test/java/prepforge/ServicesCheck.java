package prepforge;

import java.util.List;
import prepforge.FakeDaos.FakeInterviewDao;
import prepforge.FakeDaos.FakeQuestionDao;
import prepforge.exception.DataAccessException;
import prepforge.exception.ValidationException;
import prepforge.model.HRQuestion;
import prepforge.model.Interview;
import prepforge.model.MCQQuestion;
import prepforge.model.MockInterview;
import prepforge.model.Question;
import prepforge.model.Result;
import prepforge.service.AnalyticsService;
import prepforge.service.InterviewService;

/**
 * Temporary test without JUnit: run this class (Run button above main in VS Code).
 * Uses in-memory fake DAOs, so no database is needed. Delete once JUnit is set up.
 */
public class ServicesCheck {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        FakeQuestionDao questions = new FakeQuestionDao();
        FakeInterviewDao interviews = new FakeInterviewDao();
        InterviewService service = new InterviewService(questions, interviews);

        questions.save(new MCQQuestion("Q one", "Java", "EASY", List.of("a", "b", "c"), 1));
        questions.save(new MCQQuestion("Q two", "OOP", "EASY", List.of("a", "b", "c"), 1));

        System.out.println("--- InterviewService ---");
        Interview i = service.startInterview(1, "TECHNICAL", "EASY", 2);
        check("interview saved with an id", i.getId() > 0);
        check("two questions chosen", i.getQuestions().size() == 2);

        List<Question> qs = i.getQuestions();
        service.submitAnswer(i, qs.get(0).getId(), "B");   // correct
        service.submitAnswer(i, qs.get(1).getId(), "A");   // wrong

        interviews.failOnSaveResult = true;
        try {
            service.submitInterview(i);
            check("failed save throws DataAccessException", false);
        } catch (DataAccessException e) {
            check("failed save throws DataAccessException", true);
        }
        check("failed save: interview still in progress", Interview.IN_PROGRESS.equals(i.getStatus()));
        check("failed save: nothing stored", interviews.savedResult == null);

        interviews.failOnSaveResult = false;
        Result r = service.submitInterview(i);
        check("score is 1 of 2", r.getTotalScore() == 1.0 && r.getMaxScore() == 2.0);
        check("percentage is 50", r.getPercentage() == 50.0);
        check("grade is C", "C".equals(r.getGrade()));
        check("interview completed", Interview.COMPLETED.equals(i.getStatus()));

        try {
            service.submitInterview(i);
            check("second submit is rejected", false);
        } catch (ValidationException e) {
            check("second submit is rejected", true);
        }

        check("history has the result", service.getHistory(1).size() == 1);
        check("history of another student is empty", service.getHistory(2).isEmpty());

        try {
            service.startInterview(1, "XYZ", "EASY", 2);
            check("bad interview type is rejected", false);
        } catch (ValidationException e) {
            check("bad interview type is rejected", true);
        }

        questions.save(new HRQuestion("About yourself", "Communication", "EASY", List.of("team")));
        check("mixed interview type", service.startInterview(1, "MIXED", "EASY", 3) instanceof MockInterview);
        check("search by type finds 2 MCQs", service.searchQuestions(null, "easy", "mcq").size() == 2);

        System.out.println("--- AnalyticsService ---");
        FakeInterviewDao dao = new FakeInterviewDao();
        AnalyticsService analytics = new AnalyticsService(dao);
        dao.skillScores.put("Java", 80.0);
        dao.skillScores.put("OOP", 30.0);
        dao.skillScores.put("DSA", 50.0);
        check("scores sorted weakest first", analytics.getSkillScores(1).keySet().iterator().next().equals("OOP"));
        check("only OOP is weak (50 is not weak)", analytics.getWeakSkills(1).equals(List.of("OOP")));
        check("recommendation built for weak skill", analytics.getRecommendations(1).get(0).contains("OOP"));
        dao.recommendations.add("Revise inheritance");
        check("stored recommendations are used", analytics.getRecommendations(1).equals(List.of("Revise inheritance")));

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }
}
