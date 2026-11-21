package prepforge.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import prepforge.exception.DataAccessException;
import prepforge.model.CodingQuestion;
import prepforge.model.HRQuestion;
import prepforge.model.MCQQuestion;
import prepforge.model.Question;
import prepforge.model.TechnicalQuestion;

public class InMemoryQuestionDao implements QuestionDao {

    private final Map<Integer, Question> store = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    public InMemoryQuestionDao() {
        seedInitialQuestions();
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

        saveQuestionInternal(new CodingQuestion(
                "Implement an LRU Cache data structure.",
                "Data Structures", "HARD",
                "Support get and put in O(1) time complexity using HashMap and Doubly Linked List.",
                List.of("hashmap", "head", "tail", "node")
        ));

        saveQuestionInternal(new HRQuestion(
                "Tell me about a challenging technical conflict in a team project and how you resolved it.",
                "Soft Skills", "EASY",
                List.of("team", "listen", "compromise", "result")
        ));

        saveQuestionInternal(new HRQuestion(
                "Where do you see yourself professionally in five years?",
                "Soft Skills", "EASY",
                List.of("growth", "leadership", "skills", "contribute")
        ));

        saveQuestionInternal(new HRQuestion(
                "Describe a project deadline that changed unexpectedly and how you adapted to it.",
                "Soft Skills", "MEDIUM",
                List.of("prioritize", "communicate", "plan", "deliver")
        ));
    }

    private void saveQuestionInternal(Question q) {
        int id = nextId.getAndIncrement();
        q.setId(id);
        store.put(id, q);
    }

    @Override
    public void save(Question q) throws DataAccessException {
        if (q == null) {
            throw new DataAccessException("Cannot save null question.");
        }
        if (q.getId() <= 0) {
            q.setId(nextId.getAndIncrement());
        }
        store.put(q.getId(), q);
    }

    @Override
    public Question findById(int id) throws DataAccessException {
        return store.get(id);
    }

    @Override
    public List<Question> findAll() throws DataAccessException {
        return new ArrayList<>(store.values());
    }

    @Override
    public void update(Question q) throws DataAccessException {
        if (q == null) {
            throw new DataAccessException("Cannot update null question.");
        }
        if (!store.containsKey(q.getId())) {
            throw new DataAccessException("Question not found with ID: " + q.getId());
        }
        store.put(q.getId(), q);
    }

    @Override
    public void delete(int id) throws DataAccessException {
        if (store.remove(id) == null) {
            throw new DataAccessException("Question not found with ID: " + id);
        }
    }

    @Override
    public List<Question> search(String topic, String difficulty, String type) throws DataAccessException {
        List<Question> result = new ArrayList<>();
        for (Question q : store.values()) {
            boolean matchesTopic = (topic == null || topic.isBlank() || q.getTopic().equalsIgnoreCase(topic.trim()));
            boolean matchesDifficulty = (difficulty == null || difficulty.isBlank() || q.getDifficulty().equalsIgnoreCase(difficulty.trim()));
            boolean matchesType = (type == null || type.isBlank() || q.getType().equalsIgnoreCase(type.trim()));

            if (matchesTopic && matchesDifficulty && matchesType) {
                result.add(q);
            }
        }
        return result;
    }
}
