package prepforge.dao;

import java.util.ArrayList;
import java.util.List;
import prepforge.exception.DataAccessException;
import prepforge.model.CodingQuestion;
import prepforge.model.HRQuestion;
import prepforge.model.MCQQuestion;
import prepforge.model.Question;
import prepforge.model.TechnicalQuestion;
import prepforge.util.StorageManager;

public class InMemoryQuestionDao implements QuestionDao {

    private final StorageManager.PersistentState state;

    public InMemoryQuestionDao() {
        this.state = StorageManager.getState();
        if (state.questionStore.isEmpty()) {
            seedInitialQuestions();
        }
    }

    private void seedInitialQuestions() {
        saveQuestionInternal(new MCQQuestion(
                "Which keyword in Java prevents a class from being subclassed?",
                "Java", "EASY",
                List.of("static", "final", "abstract", "private"), 1
        ));

        saveQuestionInternal(new MCQQuestion(
                "Which SQL clause is used to filter records after aggregate grouping?",
                "SQL", "EASY",
                List.of("WHERE", "HAVING", "GROUP BY", "ORDER BY"), 1
        ));

        saveQuestionInternal(new MCQQuestion(
                "What is the average time complexity of searching in a HashMap?",
                "Data Structures", "MEDIUM",
                List.of("O(n)", "O(log n)", "O(1)", "O(n log n)"), 2
        ));

        saveQuestionInternal(new MCQQuestion(
                "Which normal form eliminates transitive dependencies?",
                "DBMS", "MEDIUM",
                List.of("1NF", "2NF", "3NF", "BCNF"), 2
        ));

        saveQuestionInternal(new MCQQuestion(
                "In Operating Systems, what does Banker's algorithm primarily avoid?",
                "Operating Systems", "HARD",
                List.of("Starvation", "Deadlock", "Page Faults", "Thrashing"), 1
        ));

        saveQuestionInternal(new TechnicalQuestion(
                "Explain the four fundamental pillars of object-oriented programming.",
                "Java", "EASY",
                List.of("encapsulation", "inheritance", "polymorphism", "abstraction")
        ));

        saveQuestionInternal(new TechnicalQuestion(
                "What is the difference between an interface and an abstract class in Java?",
                "Java", "MEDIUM",
                List.of("multiple", "inheritance", "default", "implementation")
        ));

        saveQuestionInternal(new TechnicalQuestion(
                "Explain the difference between clustered and non-clustered indexes in SQL databases.",
                "SQL", "MEDIUM",
                List.of("order", "leaf", "table", "pointer")
        ));

        saveQuestionInternal(new TechnicalQuestion(
                "Explain time and space complexity trade-offs between Merge Sort and Quick Sort.",
                "Data Structures", "EASY",
                List.of("log", "worst", "memory", "recursion")
        ));

        saveQuestionInternal(new TechnicalQuestion(
                "Explain ACID properties and transaction isolation levels in relational DBMS.",
                "DBMS", "HARD",
                List.of("atomicity", "consistency", "isolation", "durability")
        ));

        saveQuestionInternal(new CodingQuestion(
                "Detect a cycle in a singly linked list.",
                "Data Structures", "MEDIUM",
                "Given the head of a linked list, determine if it has a cycle using Floyd's algorithm.",
                List.of("slow", "fast", "next", "null")
        ));

        saveQuestionInternal(new CodingQuestion(
                "Find the second highest salary from an Employee table.",
                "SQL", "MEDIUM",
                "Write a query using nested subquery or window function without using LIMIT.",
                List.of("select", "max", "salary", "where")
        ));

        saveQuestionInternal(new HRQuestion(
                "Tell me about a time you handled a difficult conflict in a team project.",
                "HR", "EASY",
                List.of("communication", "compromise", "resolution", "listen")
        ));

        saveQuestionInternal(new HRQuestion(
                "Why do you want to join our company and what are your long-term career goals?",
                "HR", "MEDIUM",
                List.of("growth", "values", "contribute", "learning")
        ));
        StorageManager.saveState();
    }

    private void saveQuestionInternal(Question q) {
        int id = state.nextQuestionId++;
        q.setId(id);
        state.questionStore.put(id, q);
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
