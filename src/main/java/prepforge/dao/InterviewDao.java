package prepforge.dao;

import java.util.List;
import java.util.Map;
import prepforge.exception.DataAccessException;
import prepforge.model.Interview;
import prepforge.model.Result;

public interface InterviewDao {

    void save(Interview interview) throws DataAccessException;

    void saveResultInTransaction(Interview interview, Result result) throws DataAccessException;

    List<Result> findResultsByStudent(int studentId) throws DataAccessException;

    Map<String, Double> findSkillScores(int studentId) throws DataAccessException;

    List<String> findRecommendations(int studentId) throws DataAccessException;

    Map<String, Integer> getPlatformStats() throws DataAccessException;
}
