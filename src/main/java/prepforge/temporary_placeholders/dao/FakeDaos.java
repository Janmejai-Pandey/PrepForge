package prepforge;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import prepforge.dao.InterviewDao;
import prepforge.dao.QuestionDao;
import prepforge.exception.DataAccessException;
import prepforge.model.Interview;
import prepforge.model.Question;
import prepforge.model.Result;

/** In-memory stand-ins for M2's DAOs, so the services can be tested without a database. */
public class FakeDaos {

    public static class FakeQuestionDao extends QuestionDao {
        public final List<Question> store = new ArrayList<>();
        private int nextId = 1;

        @Override
        public void save(Question q) {
            q.setId(nextId++);
            store.add(q);
        }

        @Override
        public void update(Question q) { /* nothing to do in memory */ }

        @Override
        public void delete(int id) {
            store.removeIf(q -> q.getId() == id);
        }

        @Override
        public List<Question> search(String topic, String difficulty, String type) {
            List<Question> found = new ArrayList<>();
            for (Question q : store) {
                boolean ok = (topic == null || q.getTopic().equalsIgnoreCase(topic))
                        && (difficulty == null || q.getDifficulty().equals(difficulty))
                        && (type == null || q.getType().equals(type));
                if (ok) {
                    found.add(q);
                }
            }
            return found;
        }
    }

    public static class FakeInterviewDao extends InterviewDao {
        public boolean failOnSaveResult = false;     // simulate a database failure
        public Interview savedInterview;
        public Result savedResult;
        public final List<Result> results = new ArrayList<>();
        public Map<String, Double> skillScores = new LinkedHashMap<>();
        public List<String> recommendations = new ArrayList<>();
        public Map<String, Integer> stats = new LinkedHashMap<>();
        private int nextId = 100;

        @Override
        public void save(Interview interview) {
            interview.setId(nextId++);
            savedInterview = interview;
        }

        @Override
        public void saveResultInTransaction(Interview interview, Result result) throws DataAccessException {
            if (failOnSaveResult) {
                throw new DataAccessException("Simulated database failure");   // nothing is stored
            }
            result.setId(nextId++);
            savedResult = result;
            results.add(result);
        }

        @Override
        public List<Result> findResultsByStudent(int studentId) {
            List<Result> mine = new ArrayList<>();
            for (Result r : results) {
                if (r.getStudentId() == studentId) {
                    mine.add(r);
                }
            }
            return mine;
        }

        @Override
        public Map<String, Double> findSkillScores(int studentId) { return skillScores; }

        @Override
        public List<String> findRecommendations(int studentId) { return recommendations; }

        @Override
        public Map<String, Integer> getPlatformStats() { return stats; }
    }
}
