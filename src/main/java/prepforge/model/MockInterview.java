package prepforge.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Mixed interview: takes questions turn by turn from every type (MCQ, technical, coding, HR). */
public class MockInterview extends Interview {

    private static final Set<String> ALLOWED = Set.of("MCQ", "TECHNICAL", "CODING", "HR");

    public MockInterview(int studentId, String difficulty) {
        super(studentId, difficulty);
    }

    public MockInterview(int id, int studentId, String difficulty) {
        super(id, studentId, difficulty);
    }

    @Override public String getType() { return "MIXED"; }

    @Override
    public List<Question> buildQuestionSet(List<Question> pool, int count) {
        // group the shuffled candidates by question type
        Map<String, Deque<Question>> byType = new LinkedHashMap<>();
        for (Question q : candidates(pool, ALLOWED)) {
            byType.computeIfAbsent(q.getType(), k -> new ArrayDeque<>()).add(q);
        }
        // take one question from each type in turn until we have enough
        List<Question> picked = new ArrayList<>();
        while (picked.size() < count && !byType.isEmpty()) {
            Iterator<Map.Entry<String, Deque<Question>>> it = byType.entrySet().iterator();
            while (it.hasNext() && picked.size() < count) {
                Deque<Question> queue = it.next().getValue();
                picked.add(queue.poll());
                if (queue.isEmpty()) {
                    it.remove();
                }
            }
        }
        return picked;
    }
}
