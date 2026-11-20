package prepforge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import prepforge.FakeDaos.FakeInterviewDao;
import prepforge.exception.ValidationException;
import prepforge.service.AnalyticsService;

class AnalyticsServiceTest {

    private final FakeInterviewDao dao = new FakeInterviewDao();
    private final AnalyticsService service = new AnalyticsService(dao);

    @Test
    void skillScoresComeBackWeakestFirst() throws Exception {
        dao.skillScores.put("Java", 80.0);
        dao.skillScores.put("OOP", 30.0);
        dao.skillScores.put("DSA", 55.0);
        assertEquals(List.of("OOP", "DSA", "Java"), new ArrayList<>(service.getSkillScores(1).keySet()));
    }

    @Test
    void weakMeansBelowFifty() throws Exception {
        dao.skillScores.put("Java", 80.0);
        dao.skillScores.put("OOP", 30.0);
        dao.skillScores.put("DSA", 50.0);          // exactly 50 is not weak
        assertEquals(List.of("OOP"), service.getWeakSkills(1));
    }

    @Test
    void noWeakSkillsGivesEmptyList() throws Exception {
        dao.skillScores.put("Java", 90.0);
        assertTrue(service.getWeakSkills(1).isEmpty());
    }

    @Test
    void storedRecommendationsAreReturnedAsTheyAre() throws Exception {
        dao.recommendations.add("Revise inheritance");
        assertEquals(List.of("Revise inheritance"), service.getRecommendations(1));
    }

    @Test
    void recommendationsAreBuiltFromWeakSkillsWhenNoneAreStored() throws Exception {
        dao.skillScores.put("OOP", 30.0);
        dao.skillScores.put("Java", 80.0);
        List<String> recs = service.getRecommendations(1);
        assertEquals(1, recs.size());
        assertTrue(recs.get(0).contains("OOP"));
        assertTrue(recs.get(0).contains("30"));
    }

    @Test
    void noRecommendationsWhenEverythingIsStrong() throws Exception {
        dao.skillScores.put("Java", 90.0);
        assertTrue(service.getRecommendations(1).isEmpty());
    }

    @Test
    void platformStatsAreReturnedFromTheDao() throws Exception {
        dao.stats.put("totalStudents", 5);
        assertEquals(5, service.getPlatformStats().get("totalStudents").intValue());
    }

    @Test
    void invalidStudentIdIsRejected() {
        assertThrows(ValidationException.class, () -> service.getSkillScores(0));
        assertThrows(ValidationException.class, () -> service.getWeakSkills(-1));
        assertThrows(ValidationException.class, () -> service.getRecommendations(0));
    }
}
