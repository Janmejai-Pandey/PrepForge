package prepforge.model;

import java.util.List;

/**
 * Subclass: TechnicalInterview
 * 
 * Contract Owner: Member 3
 * Assembles and conducts technical-only mock interviews.
 */
public class TechnicalInterview extends Interview {

    public TechnicalInterview() {
        super();
    }

    public TechnicalInterview(int studentId, String difficulty) {
        super(studentId, difficulty);
    }

    public TechnicalInterview(int id, int studentId, String difficulty) {
        super(id, studentId, difficulty);
    }

    @Override
    public String getInterviewType() {
        return "TECHNICAL";
    }

    @Override
    public void buildQuestionSet(List<Question> availableQuestions, int numQuestions) {
        getQuestions().clear();
        int count = 0;
        for (Question q : availableQuestions) {
            if ("TECHNICAL".equalsIgnoreCase(q.getQuestionType()) || "CODING".equalsIgnoreCase(q.getQuestionType())
                    || "MCQ".equalsIgnoreCase(q.getQuestionType())) {
                if (getDifficulty() == null || getDifficulty().equalsIgnoreCase(q.getDifficulty())) {
                    addQuestion(q);
                    count++;
                    if (count >= numQuestions) break;
                }
            }
        }
    }
}
