package prepforge.model;

import java.util.List;

/**
 * Subclass: MockInterview
 * 
 * Contract Owner: Member 3
 * Comprehensive mixed mock interview combining Technical, Coding, and HR questions.
 */
public class MockInterview extends Interview {

    public MockInterview() {
        super();
    }

    public MockInterview(int studentId, String difficulty) {
        super(studentId, difficulty);
    }

    public MockInterview(int id, int studentId, String difficulty) {
        super(id, studentId, difficulty);
    }

    @Override
    public String getInterviewType() {
        return "MIXED";
    }

    @Override
    public void buildQuestionSet(List<Question> availableQuestions, int numQuestions) {
        getQuestions().clear();
        int count = 0;
        for (Question q : availableQuestions) {
            addQuestion(q);
            count++;
            if (count >= numQuestions) break;
        }
    }
}
