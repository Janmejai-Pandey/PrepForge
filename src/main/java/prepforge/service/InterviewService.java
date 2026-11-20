package prepforge.service;

import java.util.List;
import prepforge.dao.InterviewDao;
import prepforge.dao.QuestionDao;
import prepforge.evaluator.Evaluator;
import prepforge.exception.PrepForgeException;
import prepforge.exception.ValidationException;
import prepforge.model.HRInterview;
import prepforge.model.Interview;
import prepforge.model.MockInterview;
import prepforge.model.Question;
import prepforge.model.Result;
import prepforge.model.TechnicalInterview;

/**
 * Business logic for interviews and the question bank:
 * start an interview, collect answers, mark them, save the result, show history.
 */
public class InterviewService {

    public static final int MAX_QUESTIONS = 50;

    private final QuestionDao questionDao;
    private final InterviewDao interviewDao;

    /** Normal use: real DAOs. */
    public InterviewService() {
        this(new QuestionDao(), new InterviewDao());
    }

    /** Overloaded constructor: lets tests pass in fake DAOs. */
    public InterviewService(QuestionDao questionDao, InterviewDao interviewDao) {
        this.questionDao = questionDao;
        this.interviewDao = interviewDao;
    }

    // ---------------------------------------------------------------- interviews

    /** Create an interview of the given type ("HR", "TECHNICAL" or "MIXED"), pick its questions, save it. */
    public Interview startInterview(int studentId, String type, String difficulty, int numQuestions)
            throws PrepForgeException {
        if (studentId <= 0) {
            throw new ValidationException("Invalid student");
        }
        if (numQuestions < 1 || numQuestions > MAX_QUESTIONS) {
            throw new ValidationException("Number of questions must be between 1 and " + MAX_QUESTIONS);
        }
        String level = normalizeDifficulty(difficulty);

        Interview interview;
        try {
            interview = createInterview(studentId, type, level);
            List<Question> pool = questionDao.search(null, level, null);
            interview.selectQuestions(pool, numQuestions);   // each interview type picks in its own way
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ValidationException(e.getMessage());
        }
        interviewDao.save(interview);
        return interview;
    }

    /** Keep the student's answer in the interview. Nothing is written to the database yet. */
    public void submitAnswer(Interview interview, int questionId, String answer) throws PrepForgeException {
        requireInterview(interview);
        try {
            interview.addAnswer(questionId, answer);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    /** Mark every answer, build the result and save everything in one database transaction. */
    public Result submitInterview(Interview interview) throws PrepForgeException {
        requireInterview(interview);
        if (Interview.COMPLETED.equals(interview.getStatus())) {
            throw new ValidationException("This interview has already been submitted");
        }
        if (interview.getId() <= 0) {
            throw new ValidationException("This interview was not saved when it started");
        }

        double total = 0;
        for (Question q : interview.getQuestions()) {
            Evaluator evaluator = Evaluator.forQuestion(q);               // runtime polymorphism
            double marks = evaluator.evaluate(q, interview.getAnswer(q.getId()));
            interview.recordMarks(q.getId(), marks);
            total += marks;
        }

        Result result = new Result(interview.getId(), interview.getStudentId(),
                total, interview.getTotalMarks());

        // all-or-nothing: if this throws, nothing is saved and the interview stays open
        interviewDao.saveResultInTransaction(interview, result);
        interview.markCompleted();
        return result;
    }

    /** All past results of one student, newest first (order decided by the DAO). */
    public List<Result> getHistory(int studentId) throws PrepForgeException {
        if (studentId <= 0) {
            throw new ValidationException("Invalid student");
        }
        return interviewDao.findResultsByStudent(studentId);
    }

    // ---------------------------------------------------------------- question bank (admin)

    public void addQuestion(Question q) throws PrepForgeException {
        if (q == null) {
            throw new ValidationException("Question cannot be empty");
        }
        questionDao.save(q);
    }

    public void updateQuestion(Question q) throws PrepForgeException {
        if (q == null || q.getId() <= 0) {
            throw new ValidationException("Only a saved question can be updated");
        }
        questionDao.update(q);
    }

    public void deleteQuestion(int questionId) throws PrepForgeException {
        if (questionId <= 0) {
            throw new ValidationException("Invalid question");
        }
        questionDao.delete(questionId);
    }

    /** Blank arguments mean "any". The type here is a QUESTION type: MCQ, TECHNICAL, CODING or HR. */
    public List<Question> searchQuestions(String topic, String difficulty, String type)
            throws PrepForgeException {
        String t = blankToNull(topic);
        String d = blankToNull(difficulty);
        String ty = blankToNull(type);
        if (d != null) {
            d = normalizeDifficulty(d);
        }
        if (ty != null) {
            ty = ty.toUpperCase();
            if (!ty.equals("MCQ") && !ty.equals("TECHNICAL") && !ty.equals("CODING") && !ty.equals("HR")) {
                throw new ValidationException("Question type must be MCQ, TECHNICAL, CODING or HR");
            }
        }
        return questionDao.search(t, d, ty);
    }

    // ---------------------------------------------------------------- helpers

    private Interview createInterview(int studentId, String type, String level) {
        if (type == null) {
            throw new IllegalArgumentException("Interview type is required");
        }
        switch (type.trim().toUpperCase()) {
            case "HR":
                return new HRInterview(studentId, level);
            case "TECHNICAL":
                return new TechnicalInterview(studentId, level);
            case "MIXED":
                return new MockInterview(studentId, level);
            default:
                throw new IllegalArgumentException("Interview type must be HR, TECHNICAL or MIXED");
        }
    }

    private String normalizeDifficulty(String difficulty) throws ValidationException {
        if (difficulty == null) {
            throw new ValidationException("Difficulty is required");
        }
        String d = difficulty.trim().toUpperCase();
        if (!d.equals(Question.EASY) && !d.equals(Question.MEDIUM) && !d.equals(Question.HARD)) {
            throw new ValidationException("Difficulty must be EASY, MEDIUM or HARD");
        }
        return d;
    }

    private void requireInterview(Interview interview) throws ValidationException {
        if (interview == null) {
            throw new ValidationException("No interview in progress");
        }
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
