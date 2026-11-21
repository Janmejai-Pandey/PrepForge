package prepforge.dao;

import java.util.List;
import prepforge.exception.DataAccessException;
import prepforge.model.Question;

public interface QuestionDao {

    void save(Question q) throws DataAccessException;

    Question findById(int id) throws DataAccessException;

    List<Question> findAll() throws DataAccessException;

    void update(Question q) throws DataAccessException;

    void delete(int id) throws DataAccessException;

    List<Question> search(String topic, String difficulty, String type) throws DataAccessException;
}
