package prepforge.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import prepforge.evaluator.Evaluator;
import prepforge.evaluator.KeywordEvaluator;
import prepforge.evaluator.MCQEvaluator;
import prepforge.exception.PrepForgeException;
import prepforge.exception.ValidationException;
import prepforge.model.CodingQuestion;
import prepforge.model.HRInterview;
import prepforge.model.HRQuestion;
import prepforge.model.Interview;
import prepforge.model.MCQQuestion;
import prepforge.model.MockInterview;
import prepforge.model.Question;
import prepforge.model.Result;
import prepforge.model.TechnicalInterview;
import prepforge.model.TechnicalQuestion;
import prepforge.util.Validator;

/**
 * Service Class: InterviewService
 * 
 * Contract Owner: Member 3
 * Manages question bank, interview generation, answer recording, evaluation,
 * and historical performance tracking.
 */
public class InterviewService {

    private final Map<Integer, Question> questionBank = new HashMap<>();
    private final List<Interview> interviewHistory = new ArrayList<>();
    private final List<Result> resultHistory = new ArrayList<>();

    private final Evaluator mcqEvaluator = new MCQEvaluator();
    private final Evaluator keywordEvaluator = new KeywordEvaluator();

    private int nextQuestionId = 1;
    private int nextInterviewId = 1;
    private int nextResultId = 1;

    public InterviewService() {
        seedDefaultQuestions();
    }

    private void seedDefaultQuestions() {
        // Technical MCQs
        addQuestionInternal(new MCQQuestion(
                "What is the time complexity of searching an element in a balanced Binary Search Tree (BST)?",
                "Data Structures", "MEDIUM", 10.0,
                List.of("O(1)", "O(log n)", "O(n)", "O(n log n)"), "O(log n)"));

        addQuestionInternal(new MCQQuestion(
                "Which Java feature allows a subclass to provide a specific implementation of a parent method?",
                "Java", "EASY", 10.0,
                List.of("Overloading", "Overriding", "Encapsulation", "Abstraction"), "Overriding"));

        addQuestionInternal(new MCQQuestion(
                "Which SQL clause is used to filter groups created by GROUP BY?",
                "SQL", "EASY", 10.0,
                List.of("WHERE", "HAVING", "ORDER BY", "FILTER"), "HAVING"));

        addQuestionInternal(new MCQQuestion(
                "In DBMS, which isolation level completely eliminates phantom reads?",
                "Database", "HARD", 10.0,
                List.of("Read Uncommitted", "Read Committed", "Repeatable Read", "Serializable"), "Serializable"));

        // Open-ended Technical Questions
        addQuestionInternal(new TechnicalQuestion(
                "Explain the difference between HashMap and ConcurrentHashMap in Java.",
                "Java", "MEDIUM", 10.0,
                List.of("thread-safe", "synchronization", "lock", "segment", "bucket", "null")));

        addQuestionInternal(new TechnicalQuestion(
                "What is database indexing and how do B-Trees optimize query lookups?",
                "Database", "HARD", 10.0,
                List.of("index", "b-tree", "lookup", "disk", "binary", "pointer", "performance")));

        addQuestionInternal(new TechnicalQuestion(
                "Explain the four ACID properties in relational database transactions.",
                "SQL", "MEDIUM", 10.0,
                List.of("atomicity", "consistency", "isolation", "durability", "transaction")));

        // Coding Questions
        addQuestionInternal(new CodingQuestion(
                "Two Sum problem: Find two indices that sum up to target.",
                "Data Structures", "EASY", 15.0,
                "Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.",
                List.of("hashmap", "complement", "target", "index", "O(n)", "loop")));

        addQuestionInternal(new CodingQuestion(
                "Detect cycle in a directed graph.",
                "Algorithms", "HARD", 20.0,
                "Implement cycle detection using DFS or Kahn's topological sort algorithm.",
                List.of("dfs", "visited", "recursion", "stack", "topological", "indegree")));

        // HR Questions
        addQuestionInternal(new HRQuestion(
                "Tell me about a challenging technical bug you encountered and how you resolved it.",
                "HR", "MEDIUM", 10.0,
                List.of("problem", "debug", "solution", "team", "learned", "result", "root cause")));

        addQuestionInternal(new HRQuestion(
                "Where do you see yourself in three to five years in software engineering?",
                "HR", "EASY", 10.0,
                List.of("growth", "leadership", "technical", "skills", "impact", "learning", "mentor")));

        addQuestionInternal(new HRQuestion(
                "Describe a situation where you had a disagreement with a team member and how you handled it.",
                "HR", "MEDIUM", 10.0,
                List.of("listen", "perspective", "communication", "consensus", "compromise", "respect", "outcome")));
    }

    private void addQuestionInternal(Question q) {
        q.setId(nextQuestionId++);
        questionBank.put(q.getId(), q);
    }

    // ------------------------------------------------------------------------
    // 1. START INTERVIEW
    // ------------------------------------------------------------------------
    public synchronized Interview startInterview(int studentId, String type, String difficulty, int numQuestions)
            throws PrepForgeException {
        Validator.validatePositiveId(studentId, "Student ID");
        if (numQuestions <= 0) {
            numQuestions = 5;
        }

        Interview interview;
        if ("TECHNICAL".equalsIgnoreCase(type)) {
            interview = new TechnicalInterview(nextInterviewId++, studentId, difficulty);
        } else if ("HR".equalsIgnoreCase(type)) {
            interview = new HRInterview(nextInterviewId++, studentId, difficulty);
        } else {
            interview = new MockInterview(nextInterviewId++, studentId, difficulty);
        }

        List<Question> candidatePool = new ArrayList<>(questionBank.values());
        Collections.shuffle(candidatePool);
        interview.buildQuestionSet(candidatePool, numQuestions);

        if (interview.getQuestions().isEmpty()) {
            // Fallback: load any available questions
            for (Question q : questionBank.values()) {
                interview.addQuestion(q);
                if (interview.getQuestions().size() >= numQuestions) break;
            }
        }

        interviewHistory.add(interview);
        return interview;
    }

    // ------------------------------------------------------------------------
    // 2. SUBMIT ANSWER
    // ------------------------------------------------------------------------
    public void submitAnswer(Interview interview, int questionId, String answer) throws PrepForgeException {
        Validator.validateNotNull(interview, "Interview");
        Validator.validatePositiveId(questionId, "Question ID");

        interview.recordAnswer(questionId, answer != null ? answer : "");
    }

    // ------------------------------------------------------------------------
    // 3. SUBMIT INTERVIEW (EVALUATION TRANSACTION)
    // ------------------------------------------------------------------------
    public synchronized Result submitInterview(Interview interview) throws PrepForgeException {
        Validator.validateNotNull(interview, "Interview");

        double totalScore = 0.0;
        double maxScore = 0.0;

        for (Question q : interview.getQuestions()) {
            maxScore += q.getMaxMarks();
            String ans = interview.getAnswer(q.getId());

            double awarded = 0.0;
            if (q instanceof MCQQuestion) {
                awarded = mcqEvaluator.evaluate(q, ans);
            } else {
                awarded = keywordEvaluator.evaluate(q, ans);
            }
            totalScore += awarded;
        }

        if (maxScore == 0.0) maxScore = 1.0;
        double percentage = Math.round(((totalScore / maxScore) * 100.0) * 10.0) / 10.0;

        String grade;
        if (percentage >= 90.0) grade = "A+";
        else if (percentage >= 80.0) grade = "A";
        else if (percentage >= 70.0) grade = "B";
        else if (percentage >= 60.0) grade = "C";
        else if (percentage >= 50.0) grade = "D";
        else grade = "Needs Improvement";

        Result result = new Result(nextResultId++, interview.getId(), interview.getStudentId(),
                totalScore, maxScore, percentage, grade);

        interview.setStatus("EVALUATED");
        resultHistory.add(result);
        return result;
    }

    // ------------------------------------------------------------------------
    // 4. GET HISTORY
    // ------------------------------------------------------------------------
    public synchronized List<Result> getHistory(int studentId) throws PrepForgeException {
        Validator.validatePositiveId(studentId, "Student ID");
        List<Result> studentResults = new ArrayList<>();
        for (Result r : resultHistory) {
            if (r.getStudentId() == studentId) {
                studentResults.add(r);
            }
        }
        return studentResults;
    }

    // ------------------------------------------------------------------------
    // 5. QUESTION MANAGEMENT (ADMIN)
    // ------------------------------------------------------------------------
    public synchronized void addQuestion(Question q) throws PrepForgeException {
        Validator.validateNotNull(q, "Question");
        Validator.validateNotNull(q.getText(), "Question Text");
        if (q.getText().trim().isEmpty()) {
            throw new ValidationException("Question text cannot be empty.");
        }
        if (q.getId() <= 0) {
            q.setId(nextQuestionId++);
        }
        questionBank.put(q.getId(), q);
    }

    public synchronized void updateQuestion(Question q) throws PrepForgeException {
        Validator.validateNotNull(q, "Question");
        Validator.validatePositiveId(q.getId(), "Question ID");
        if (!questionBank.containsKey(q.getId())) {
            throw new ValidationException("Question not found with ID: " + q.getId());
        }
        questionBank.put(q.getId(), q);
    }

    public synchronized void deleteQuestion(int questionId) throws PrepForgeException {
        Validator.validatePositiveId(questionId, "Question ID");
        if (!questionBank.containsKey(questionId)) {
            throw new ValidationException("Question not found with ID: " + questionId);
        }
        questionBank.remove(questionId);
    }

    public synchronized List<Question> searchQuestions(String topic, String difficulty, String type) {
        List<Question> results = new ArrayList<>();
        for (Question q : questionBank.values()) {
            boolean matchesTopic = (topic == null || topic.trim().isEmpty() || "ALL".equalsIgnoreCase(topic)
                    || q.getTopic().equalsIgnoreCase(topic.trim()));
            boolean matchesDiff = (difficulty == null || difficulty.trim().isEmpty() || "ALL".equalsIgnoreCase(difficulty)
                    || q.getDifficulty().equalsIgnoreCase(difficulty.trim()));
            boolean matchesType = (type == null || type.trim().isEmpty() || "ALL".equalsIgnoreCase(type)
                    || q.getQuestionType().equalsIgnoreCase(type.trim()));

            if (matchesTopic && matchesDiff && matchesType) {
                results.add(q);
            }
        }
        return results;
    }

    public synchronized List<Question> getAllQuestions() {
        return new ArrayList<>(questionBank.values());
    }

    public synchronized int getTotalInterviewsCount() {
        return interviewHistory.size();
    }
}
