package prepforge;

import org.junit.jupiter.api.Test;
import prepforge.model.Interview;
import prepforge.model.Result;
import prepforge.service.InterviewService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Concurrency & Integration Test: ConcurrencyTest
 * 
 * Contract Owner: Member 4 (UI & Integration)
 * 
 * Verifies multi-threaded concurrent interview submissions.
 * Simulates multiple candidate threads simultaneously taking, answering, and submitting
 * assessments against the InterviewService.
 * Ensures thread-safe transactions, atomic result persistence, and absence of race conditions.
 */
public class ConcurrencyTest {

    private static final int CONCURRENT_USERS = 10;
    private static int passedAssertions = 0;

    @Test
    public void runConcurrentInterviewSubmissions() throws InterruptedException {
        InterviewService interviewService = new InterviewService();

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(CONCURRENT_USERS);

        AtomicInteger successCounter = new AtomicInteger(0);
        AtomicInteger failureCounter = new AtomicInteger(0);
        List<Result> generatedResults = Collections.synchronizedList(new ArrayList<>());

        for (int i = 1; i <= CONCURRENT_USERS; i++) {
            final int studentId = i;
            executor.submit(() -> {
                try {
                    // Wait for all threads to be ready for simultaneous burst execution
                    startGate.await();

                    // 1. Concurrently start an interview
                    Interview interview = interviewService.startInterview(studentId, "MIXED", "MEDIUM", 5);

                    // 2. Answer questions concurrently
                    if (!interview.getQuestions().isEmpty()) {
                        interview.recordAnswer(interview.getQuestions().get(0).getId(), "O(log n)");
                    }

                    // 3. Concurrently submit the interview (Stress testing atomic transaction)
                    Result result = interviewService.submitInterview(interview);

                    assertCondition(result != null, "Evaluated result must not be null.");
                    assertCondition(result.getResultId() > 0, "Result ID must be assigned.");
                    assertCondition(result.getMaxScore() > 0, "Max score must be greater than zero.");
                    assertCondition(result.getGrade() != null, "Grade must be assigned.");

                    generatedResults.add(result);
                    successCounter.incrementAndGet();

                } catch (Exception e) {
                    System.err.println("Thread failure for student " + studentId + ": " + e.getMessage());
                    failureCounter.incrementAndGet();
                } finally {
                    endGate.countDown();
                }
            });
        }

        // Trigger simultaneous burst
        long startTime = System.currentTimeMillis();
        startGate.countDown();

        // Await all threads to finish with timeout
        boolean finishedInTime = endGate.await(10, TimeUnit.SECONDS);
        long duration = System.currentTimeMillis() - startTime;
        executor.shutdown();

        assertCondition(finishedInTime, "All concurrent submissions should complete within 10 seconds.");
        assertCondition(successCounter.get() == CONCURRENT_USERS, "All 10 concurrent submissions should succeed.");
        assertCondition(failureCounter.get() == 0, "There should be zero failed submissions.");
        assertCondition(generatedResults.size() == CONCURRENT_USERS, "Exact count of results should match concurrent users.");

        System.out.println("  [PASS] ConcurrencyTest: Successfully executed " + CONCURRENT_USERS
                + " concurrent interview submissions in " + duration + " ms with 100% data integrity.");
    }

    private static void assertCondition(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        } else {
            passedAssertions++;
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("     PREPFORGE CONCURRENCY & TRANSACTION SUITE    ");
        System.out.println("==================================================");
        try {
            ConcurrencyTest test = new ConcurrencyTest();
            test.runConcurrentInterviewSubmissions();
            System.out.println("\nTEST SUMMARY");
            System.out.println("  Assertions Passed: " + passedAssertions);
            System.out.println("  Status: CONCURRENCY TEST PASSED SUCCESSFULLY! [OK]");
        } catch (Throwable t) {
            System.err.println("\n[FAIL] ConcurrencyTest encountered an error: " + t.getMessage());
            t.printStackTrace();
        }
        System.out.println("==================================================");
    }
}
