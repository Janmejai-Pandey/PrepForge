package prepforge.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import prepforge.dao.InterviewDao;
import prepforge.dao.MySqlDataStore;
import prepforge.exception.PrepForgeException;
import prepforge.exception.ValidationException;

public class AnalyticsService {

    public static final double WEAK_THRESHOLD = 50.0;

    private final InterviewDao interviewDao;

    public AnalyticsService() {
        this(new MySqlDataStore());
    }

    public AnalyticsService(InterviewDao interviewDao) {
        this.interviewDao = interviewDao;
    }

    public Map<String, Double> getSkillScores(int studentId) throws PrepForgeException {
        checkStudent(studentId);
        Map<String, Double> raw = interviewDao.findSkillScores(studentId);

        List<Map.Entry<String, Double>> entries = new ArrayList<>(raw.entrySet());
        entries.sort(Map.Entry.comparingByValue());

        Map<String, Double> sorted = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : entries) {
            sorted.put(e.getKey(), e.getValue());
        }
        return sorted;
    }

    public List<String> getWeakSkills(int studentId) throws PrepForgeException {
        List<String> weak = new ArrayList<>();
        for (Map.Entry<String, Double> e : getSkillScores(studentId).entrySet()) {
            if (e.getValue() < WEAK_THRESHOLD) {
                weak.add(e.getKey());
            }
        }
        return weak;
    }

    public List<String> getRecommendations(int studentId) throws PrepForgeException {
        checkStudent(studentId);
        List<String> stored = interviewDao.findRecommendations(studentId);
        if (!stored.isEmpty()) {
            return stored;
        }

        List<String> built = new ArrayList<>();
        for (String skill : getWeakSkills(studentId)) {
            built.add("Practice more " + skill + " questions to improve your score.");
        }
        return built;
    }

    public Map<String, Integer> getPlatformStats() throws PrepForgeException {
        return interviewDao.getPlatformStats();
    }

    private void checkStudent(int studentId) throws ValidationException {
        if (studentId <= 0) {
            throw new ValidationException("Invalid student ID.");
        }
    }
}
