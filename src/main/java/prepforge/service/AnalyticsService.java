package prepforge.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import prepforge.dao.InterviewDao;
import prepforge.exception.PrepForgeException;
import prepforge.exception.ValidationException;

/** Skill scores, weak skills, recommendations and platform statistics. */
public class AnalyticsService {

    /** A skill whose average is below this percentage is weak. 50 itself is not weak. */
    public static final double WEAK_THRESHOLD = 50.0;

    private final InterviewDao interviewDao;

    public AnalyticsService() {
        this(new InterviewDao());
    }

    /** Overloaded constructor: lets tests pass in a fake DAO. */
    public AnalyticsService(InterviewDao interviewDao) {
        this.interviewDao = interviewDao;
    }

    /** Skill name -> average percentage, weakest skill first. */
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

    /** Skills below the weak threshold, weakest first. */
    public List<String> getWeakSkills(int studentId) throws PrepForgeException {
        List<String> weak = new ArrayList<>();
        for (Map.Entry<String, Double> e : getSkillScores(studentId).entrySet()) {
            if (e.getValue() < WEAK_THRESHOLD) {
                weak.add(e.getKey());
            }
        }
        return weak;
    }

    /**
     * Recommendations written by the database trigger. If there are none yet but the student
     * has weak skills, build a simple message for each weak skill.
     */
    public List<String> getRecommendations(int studentId) throws PrepForgeException {
        checkStudent(studentId);
        List<String> stored = interviewDao.findRecommendations(studentId);
        if (!stored.isEmpty()) {
            return stored;
        }
        List<String> generated = new ArrayList<>();
        Map<String, Double> scores = getSkillScores(studentId);
        for (String skill : getWeakSkills(studentId)) {
            generated.add(String.format("Practice more %s questions (your average is %.0f%%).",
                    skill, scores.get(skill)));
        }
        return generated;
    }

    /** Admin statistics: totalStudents, totalInterviews, totalQuestions, totalCompanies. */
    public Map<String, Integer> getPlatformStats() throws PrepForgeException {
        return interviewDao.getPlatformStats();
    }

    private void checkStudent(int studentId) throws ValidationException {
        if (studentId <= 0) {
            throw new ValidationException("Invalid student");
        }
    }
}
