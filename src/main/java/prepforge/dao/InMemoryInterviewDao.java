package prepforge.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import prepforge.exception.DataAccessException;
import prepforge.model.Interview;
import prepforge.model.Question;
import prepforge.model.Result;

public class InMemoryInterviewDao implements InterviewDao {

    private final Map<Integer, Interview> interviewStore = new ConcurrentHashMap<>();
    private final Map<Integer, Result> resultStore = new ConcurrentHashMap<>();
    private final Map<Integer, List<Result>> studentResults = new ConcurrentHashMap<>();
    private final Map<Integer, Map<String, Double>> studentSkillScores = new ConcurrentHashMap<>();
    private final Map<Integer, List<String>> studentRecommendations = new ConcurrentHashMap<>();

    private final AtomicInteger nextInterviewId = new AtomicInteger(100);
    private final AtomicInteger nextResultId = new AtomicInteger(500);

    public boolean failOnSaveResult = false;

    @Override
    public synchronized void save(Interview interview) throws DataAccessException {
        if (interview == null) {
            throw new DataAccessException("Cannot save null interview.");
        }
        if (interview.getId() <= 0) {
            interview.setId(nextInterviewId.getAndIncrement());
        }
        interviewStore.put(interview.getId(), interview);
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
            result.setId(nextResultId.getAndIncrement());
        }

        interview.setStatus(Interview.COMPLETED);
        interviewStore.put(interview.getId(), interview);
        resultStore.put(result.getId(), result);

        int studentId = interview.getStudentId();
        studentResults.computeIfAbsent(studentId, k -> Collections.synchronizedList(new ArrayList<>())).add(result);

        Map<String, double[]> topicScores = new HashMap<>();
        Map<Integer, String> answers = interview.getAnswers();

        for (Question q : interview.getQuestions()) {
            String topic = q.getTopic();
            String ans = answers.get(q.getId());
            double marks = interview.getMarks(q.getId());
            double maxMarks = q.getMaxMarks();

            double[] current = topicScores.computeIfAbsent(topic, k -> new double[2]);
            current[0] += marks;
            current[1] += maxMarks;
        }

        Map<String, Double> skillAverages = studentSkillScores.computeIfAbsent(studentId, k -> new ConcurrentHashMap<>());
        for (Map.Entry<String, double[]> entry : topicScores.entrySet()) {
            double totalObtained = entry.getValue()[0];
            double totalMax = entry.getValue()[1];
            double pct = totalMax > 0 ? (totalObtained / totalMax) * 100.0 : 0.0;
            skillAverages.put(entry.getKey(), Math.round(pct * 10.0) / 10.0);

            if (pct < 50.0) {
                List<String> recs = studentRecommendations.computeIfAbsent(studentId, k -> Collections.synchronizedList(new ArrayList<>()));
                String msg = "Diagnostic alert: Score of " + String.format("%.1f", pct) + "% in " + entry.getKey() + " requires focused study and practice.";
                if (!recs.contains(msg)) {
                    recs.add(msg);
                }
            }
        }
    }

    @Override
    public List<Result> findResultsByStudent(int studentId) throws DataAccessException {
        List<Result> res = studentResults.get(studentId);
        if (res == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(res);
    }

    @Override
    public Map<String, Double> findSkillScores(int studentId) throws DataAccessException {
        Map<String, Double> scores = studentSkillScores.get(studentId);
        if (scores == null) {
            return Collections.emptyMap();
        }
        return new LinkedHashMap<>(scores);
    }

    @Override
    public List<String> findRecommendations(int studentId) throws DataAccessException {
        List<String> recs = studentRecommendations.get(studentId);
        if (recs == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(recs);
    }

    @Override
    public Map<String, Integer> getPlatformStats() throws DataAccessException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("totalInterviews", interviewStore.size());
        stats.put("totalResults", resultStore.size());
        stats.put("totalStudentsEvaluated", studentResults.size());
        return stats;
    }
}
