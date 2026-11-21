package prepforge.model;

import java.util.List;
import java.util.Set;

public class HRInterview extends Interview {

    private static final Set<String> ALLOWED_TYPES = Set.of("HR");

    public HRInterview(int studentId, String difficulty) {
        super(studentId, difficulty);
    }

    public HRInterview(int id, int studentId, String difficulty) {
        super(id, studentId, difficulty);
    }

    @Override
    public String getType() {
        return "HR";
    }

    @Override
    public List<Question> buildQuestionSet(List<Question> pool, int count) {
        List<Question> available = candidates(pool, ALLOWED_TYPES);
        int take = Math.min(count, available.size());
        return available.subList(0, take);
    }
}
