package prepforge.model;

import java.util.List;
import java.util.Set;

/** Technical interview: MCQ, technical and coding questions only. */
public class TechnicalInterview extends Interview {

    private static final Set<String> ALLOWED = Set.of("MCQ", "TECHNICAL", "CODING");

    public TechnicalInterview(int studentId, String difficulty) {
        super(studentId, difficulty);
    }

    public TechnicalInterview(int id, int studentId, String difficulty) {
        super(id, studentId, difficulty);
    }

    @Override public String getType() { return "TECHNICAL"; }

    @Override
    public List<Question> buildQuestionSet(List<Question> pool, int count) {
        List<Question> list = candidates(pool, ALLOWED);
        return list.subList(0, Math.min(count, list.size()));
    }
}
