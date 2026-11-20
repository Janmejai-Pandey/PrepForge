package prepforge.dao;

import java.util.List;
import java.util.Map;
import prepforge.exception.DataAccessException;
import prepforge.model.Interview;
import prepforge.model.Result;

/**
 * TEMPORARY placeholder. M2 owns the real class and fills in the SQL.
 * Do not commit this file. It must keep a public no-argument constructor
 * and non-final methods so that the services can be tested with fakes.
 */
public class InterviewDao {

    /** Insert the interview and its questions; set the generated id on the interview. */
    public void save(Interview interview) throws DataAccessException { throw notReady(); }

    /** One transaction: answers (with marks), the result, and the interview status. Commit or rollback. */
    public void saveResultInTransaction(Interview interview, Result result) throws DataAccessException {
        throw notReady();
    }

    public List<Result> findResultsByStudent(int studentId) throws DataAccessException { throw notReady(); }

    /** Skill name -> average percentage (from v_skill_performance). */
    public Map<String, Double> findSkillScores(int studentId) throws DataAccessException { throw notReady(); }

    /** Recommendation messages (filled by the database trigger). */
    public List<String> findRecommendations(int studentId) throws DataAccessException { throw notReady(); }

    /** Keys: totalStudents, totalInterviews, totalQuestions, totalCompanies (from v_platform_stats). */
    public Map<String, Integer> getPlatformStats() throws DataAccessException { throw notReady(); }

    private DataAccessException notReady() {
        return new DataAccessException("InterviewDao is not implemented yet (M2)");
    }
}
