package prepforge.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class for every interview type.
 * Each subclass decides HOW its questions are chosen (polymorphism).
 */
public abstract class Interview {

    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String COMPLETED = "COMPLETED";

    private int id;                          // 0 until saved in the database
    private final int studentId;             // plain id, so no dependency on M1's Student class
    private final String difficulty;
    private final LocalDateTime startedAt;
    private String status = IN_PROGRESS;

    private final List<Question> questions = new ArrayList<>();
    private final Map<Integer, String> answers = new LinkedHashMap<>();   // questionId -> answer
    private final Map<Integer, Double> marks = new LinkedHashMap<>();     // questionId -> marks awarded

    // Constructor for a new interview
    protected Interview(int studentId, String difficulty) {
        this(0, studentId, difficulty);
    }

    // Overloaded constructor for loading from the database
    protected Interview(int id, int studentId, String difficulty) {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Invalid student id");
        }
        if (difficulty == null) {
            throw new IllegalArgumentException("Difficulty cannot be null");
        }
        String d = difficulty.trim().toUpperCase();
        if (!d.equals(Question.EASY) && !d.equals(Question.MEDIUM) && !d.equals(Question.HARD)) {
            throw new IllegalArgumentException("Difficulty must be EASY, MEDIUM or HARD");
        }
        this.id = id;
        this.studentId = studentId;
        this.difficulty = d;
        this.startedAt = LocalDateTime.now();
    }

    // ----- abstract methods: each interview type overrides these -----

    /** "HR", "TECHNICAL" or "MIXED". */
    public abstract String getType();

    /** Choose up to 'count' questions from the pool, using this interview type's own rule. */
    public abstract List<Question> buildQuestionSet(List<Question> pool, int count);

    // ----- shared behaviour -----

    /** Template method: build the set, then attach it to this interview. */
    public final void selectQuestions(List<Question> pool, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Number of questions must be positive");
        }
        List<Question> chosen = buildQuestionSet(pool, count);
        if (chosen.isEmpty()) {
            throw new IllegalStateException("No questions available for this interview");
        }
        questions.clear();
        questions.addAll(chosen);
    }

    /** Helper for subclasses: pool questions of the allowed types and this interview's difficulty. */
    protected List<Question> candidates(List<Question> pool, Set<String> allowedTypes) {
        List<Question> result = new ArrayList<>();
        for (Question q : pool) {
            if (allowedTypes.contains(q.getType()) && q.getDifficulty().equals(difficulty)) {
                result.add(q);
            }
        }
        Collections.shuffle(result);
        return result;
    }

    /** Store an answer by question id. */
    public void addAnswer(int questionId, String answer) {
        if (COMPLETED.equals(status)) {
            throw new IllegalStateException("Interview already completed");
        }
        boolean belongs = false;
        for (Question q : questions) {
            if (q.getId() == questionId) {
                belongs = true;
                break;
            }
        }
        if (!belongs) {
            throw new IllegalArgumentException("Question " + questionId + " is not part of this interview");
        }
        answers.put(questionId, answer == null ? "" : answer.trim());
    }

    /** Overloaded version: store an answer by Question object. */
    public void addAnswer(Question question, String answer) {
        addAnswer(question.getId(), answer);
    }

    public String getAnswer(int questionId) {
        return answers.getOrDefault(questionId, "");
    }

    /** Record the marks awarded for one question (set by InterviewService when the interview is marked). */
    public void recordMarks(int questionId, double marksAwarded) {
        boolean belongs = false;
        for (Question q : questions) {
            if (q.getId() == questionId) {
                belongs = true;
                break;
            }
        }
        if (!belongs) {
            throw new IllegalArgumentException("Question " + questionId + " is not part of this interview");
        }
        marks.put(questionId, marksAwarded);
    }

    /** Marks awarded for one question (0 if not marked yet). */
    public double getMarks(int questionId) {
        return marks.getOrDefault(questionId, 0.0);
    }

    /** Sum of the maximum marks of all questions (each question type reports its own max). */
    public int getTotalMarks() {
        int total = 0;
        for (Question q : questions) {
            total += q.getMaxMarks();
        }
        return total;
    }

    public void markCompleted() { this.status = COMPLETED; }

    // ----- getters -----

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public String getDifficulty() { return difficulty; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Question> getQuestions() { return Collections.unmodifiableList(questions); }
    public Map<Integer, String> getAnswers() { return Collections.unmodifiableMap(answers); }
    public Map<Integer, Double> getAllMarks() { return Collections.unmodifiableMap(marks); }

    @Override
    public String toString() {
        return getType() + " interview #" + id + " (" + difficulty + ", " + questions.size() + " questions)";
    }
}
