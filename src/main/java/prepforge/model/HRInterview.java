package prepforge.model;

import java.util.List;
import java.util.Set;

/** HR interview: HR questions only. */
public class HRInterview extends Interview {

    private static final Set<String> ALLOWED = Set.of("HR");

    public HRInterview(int studentId, String difficulty) {
        super(studentId, difficulty);
    }

    public HRInterview(int id, int studentId, String difficulty) {
        super(id, studentId, difficulty);
    }

    @Override public String getType() { return "HR"; }

    @Override
    public List<Question> buildQuestionSet(List<Question> pool, int count) {
        List<Question> list = candidates(pool, ALLOWED);
        return list.subList(0, Math.min(count, list.size()));
    }
}
