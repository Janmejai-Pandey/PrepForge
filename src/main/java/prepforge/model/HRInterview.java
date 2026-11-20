package prepforge.model;

import java.util.List;

/**
 * Subclass: HRInterview
 * 
 * Contract Owner: Member 3
 * Assembles and conducts behavioral and HR-only interviews.
 */
public class HRInterview extends Interview {

    public HRInterview() {
        super();
    }

    public HRInterview(int studentId, String difficulty) {
        super(studentId, difficulty);
    }

    public HRInterview(int id, int studentId, String difficulty) {
        super(id, studentId, difficulty);
    }

    @Override
    public String getInterviewType() {
        return "HR";
    }

    @Override
    public void buildQuestionSet(List<Question> availableQuestions, int numQuestions) {
        getQuestions().clear();
        int count = 0;
        for (Question q : availableQuestions) {
            if ("HR".equalsIgnoreCase(q.getQuestionType())) {
                addQuestion(q);
                count++;
                if (count >= numQuestions) break;
            }
        }
    }
}
