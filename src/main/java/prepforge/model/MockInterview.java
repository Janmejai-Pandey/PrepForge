package prepforge.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockInterview extends Interview {

    private static final List<String> ORDER = List.of("MCQ", "TECHNICAL", "CODING", "HR");
    private static final Set<String> ALLOWED_TYPES = Set.copyOf(ORDER);

    public MockInterview(int studentId, String difficulty) {
        super(studentId, difficulty);
    }

    public MockInterview(int id, int studentId, String difficulty) {
        super(id, studentId, difficulty);
    }

    @Override
    public String getType() {
        return "MIXED";
    }

    @Override
    public List<Question> buildQuestionSet(List<Question> pool, int count) {
        List<Question> available = candidates(pool, ALLOWED_TYPES);

        Map<String, List<Question>> byType = new LinkedHashMap<>();
        for (String t : ORDER) {
            byType.put(t, new ArrayList<>());
        }
        for (Question q : available) {
            byType.get(q.getType()).add(q);
        }

        List<Question> chosen = new ArrayList<>();
        boolean addedAny = true;
        while (chosen.size() < count && addedAny) {
            addedAny = false;
            for (String t : ORDER) {
                List<Question> list = byType.get(t);
                if (!list.isEmpty() && chosen.size() < count) {
                    chosen.add(list.remove(0));
                    addedAny = true;
                }
            }
        }
        return chosen;
    }
}
