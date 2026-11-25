package prepforge.dao;

import java.util.ArrayList;
import java.util.List;
import prepforge.exception.DataAccessException;
import prepforge.model.Question;
import prepforge.util.StorageManager;

public class InMemoryQuestionDao implements QuestionDao {

    private final StorageManager.PersistentState state;

    public InMemoryQuestionDao() {
        this.state = StorageManager.getState();
    }

    @Override
    public synchronized void save(Question q) throws DataAccessException {
        if (q == null) {
            throw new DataAccessException("Cannot save null question.");
        }
        if (q.getId() <= 0) {
            q.setId(state.nextQuestionId++);
        }
        state.questionStore.put(q.getId(), q);
        StorageManager.saveState();
    }

    @Override
    public synchronized Question findById(int id) throws DataAccessException {
        return state.questionStore.get(id);
    }

    @Override
    public synchronized List<Question> findAll() throws DataAccessException {
        return new ArrayList<>(state.questionStore.values());
    }

    @Override
    public synchronized void update(Question q) throws DataAccessException {
        if (q == null || !state.questionStore.containsKey(q.getId())) {
            throw new DataAccessException("Question not found for update: " + (q != null ? q.getId() : "null"));
        }
        state.questionStore.put(q.getId(), q);
        StorageManager.saveState();
    }

    @Override
    public synchronized void delete(int id) throws DataAccessException {
        if (state.questionStore.remove(id) == null) {
            throw new DataAccessException("Question with ID " + id + " does not exist.");
        }
        StorageManager.saveState();
    }

    @Override
    public synchronized List<Question> search(String topic, String difficulty, String type) throws DataAccessException {
        List<Question> result = new ArrayList<>();
        for (Question q : state.questionStore.values()) {
            if (topic != null && !q.getTopic().equalsIgnoreCase(topic)) continue;
            if (difficulty != null && !q.getDifficulty().equalsIgnoreCase(difficulty)) continue;
            if (type != null && !q.getType().equalsIgnoreCase(type)) continue;
            result.add(q);
        }
        return result;
    }
}
