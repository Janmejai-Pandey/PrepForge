package prepforge.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import prepforge.exception.DataAccessException;
import prepforge.model.Interview;
import prepforge.model.Question;
import prepforge.model.Result;
import prepforge.util.StorageManager;

public class InMemoryInterviewDao implements InterviewDao {

    private final StorageManager.PersistentState state;
    public boolean failOnSaveResult = false;

    public InMemoryInterviewDao() {
        this.state = StorageManager.getState();
    }

    @Override
    public synchronized void save(Interview interview) throws DataAccessException {
        if (interview == null) {
            throw new DataAccessException("Cannot save null interview.");
        }
        if (interview.getId() <= 0) {
            interview.setId(state.nextInterviewId++);
        }
        state.interviewStore.put(interview.getId(), interview);
        StorageManager.saveState();
    }

    @Override
    public synchronized void saveResultInTransaction(Interview interview, Result result) throws DataAccessException {
        if (interview == null || result == null) {
            throw new DataAccessException("Transaction aborted: Interview and Result must not be null.");
        }
        if (failOnSaveResult) {
            throw new DataAccessException("Simulated transactional rollback: Database error saving result.");
        }

        if (result.getId() <= 0) {
            result.setId(state.nextResultId++);
        }

        interview.setStatus(Interview.COMPLETED);
        state.interviewStore.put(interview.getId(), interview);
        state.resultStore.put(result.getId(), result);

        int studentId = interview.getStudentId();
        state.studentResults.computeIfAbsent(studentId, k -> new ArrayList<>()).add(result);

        Map<String, double[]> topicScores = new HashMap<>();
        for (Question q : interview.getQuestions()) {
            String topic = q.getTopic();
            double marks = interview.getMarks(q.getId());
            double maxMarks = q.getMaxMarks();

            double[] current = topicScores.computeIfAbsent(topic, k -> new double[2]);
            current[0] += marks;
            current[1] += maxMarks;
        }

        Map<String, Double> skillAverages = state.studentSkillScores.computeIfAbsent(studentId, k -> new HashMap<>());
        for (Map.Entry<String, double[]> entry : topicScores.entrySet()) {
            double totalObtained = entry.getValue()[0];
            double totalMax = entry.getValue()[1];
            double pct = totalMax > 0 ? (totalObtained / totalMax) * 100.0 : 0.0;
            skillAverages.put(entry.getKey(), Math.round(pct * 10.0) / 10.0);

            if (pct < 50.0) {
                List<String> recs = state.studentRecommendations.computeIfAbsent(studentId, k -> new ArrayList<>());
                String msg = "Diagnostic alert: Score of " + String.format("%.1f", pct) + "% in " + entry.getKey() + " requires focused study and practice.";
                if (!recs.contains(msg)) {
                    recs.add(msg);
                }
            }
        }
        StorageManager.saveState();
    }

    @Override
    public synchronized List<Result> findResultsByStudent(int studentId) throws DataAccessException {
        List<Result> res = state.studentResults.get(studentId);
        if (res == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(res);
    }

    @Override
    public synchronized Map<String, Double> findSkillScores(int studentId) throws DataAccessException {
        Map<String, Double> scores = state.studentSkillScores.get(studentId);
        if (scores == null) {
            return Collections.emptyMap();
        }
        return new LinkedHashMap<>(scores);
    }

    @Override
    public synchronized List<String> findRecommendations(int studentId) throws DataAccessException {
        List<String> recs = state.studentRecommendations.get(studentId);
        if (recs == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(recs);
    }

    @Override
    public synchronized Map<String, Integer> getPlatformStats() throws DataAccessException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("totalInterviews", state.interviewStore.size());
        stats.put("totalResults", state.resultStore.size());
        stats.put("totalStudentsEvaluated", state.studentResults.size());
        return stats;
    }

    @Override
    public synchronized Interview findInterviewById(int interviewId) throws DataAccessException {
        return state.interviewStore.get(interviewId);
    }
}
