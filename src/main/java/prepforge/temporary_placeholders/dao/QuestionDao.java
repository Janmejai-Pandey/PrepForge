package prepforge.dao;

import java.util.List;
import prepforge.exception.DataAccessException;
import prepforge.model.Question;

/**
 * TEMPORARY placeholder. M2 owns the real class and fills in the SQL.
 * Do not commit this file. It must keep a public no-argument constructor
 * and non-final methods so that the services can be tested with fakes.
 */
public class QuestionDao {

    public void save(Question q) throws DataAccessException { throw notReady(); }

    public Question findById(int id) throws DataAccessException { throw notReady(); }

    public List<Question> findAll() throws DataAccessException { throw notReady(); }

    public void update(Question q) throws DataAccessException { throw notReady(); }

    public void delete(int id) throws DataAccessException { throw notReady(); }

    /** Any argument may be null, meaning no filter on that field. */
    public List<Question> search(String topic, String difficulty, String type) throws DataAccessException {
        throw notReady();
    }

    private DataAccessException notReady() {
        return new DataAccessException("QuestionDao is not implemented yet (M2)");
    }
}
