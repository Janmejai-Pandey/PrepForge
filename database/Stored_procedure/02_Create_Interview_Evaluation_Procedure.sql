-- PREPFORGE: AUTOMATIC OVERALL INTERVIEW EVALUATION
-- Run after the original PrepForge_DBMS_Complete.sql.
-- MySQL 8.0.16+
-- Marks must be assigned by an evaluator / Java application first.
-- This procedure does not AI-grade free-text answers.

USE prepforge;
DROP PROCEDURE IF EXISTS sp_evaluate_interview;

DELIMITER $$

CREATE PROCEDURE sp_evaluate_interview(IN p_interview_id INT)
BEGIN
    DECLARE v_interview_count INT DEFAULT 0;
    DECLARE v_question_count INT DEFAULT 0;
    DECLARE v_incomplete_count INT DEFAULT 0;
    DECLARE v_total_score DECIMAL(7,2) DEFAULT 0;
    DECLARE v_max_score DECIMAL(7,2) DEFAULT 0;
    DECLARE v_percentage DECIMAL(5,2) DEFAULT 0;

    SELECT COUNT(*) INTO v_interview_count
    FROM interviews WHERE interview_id = p_interview_id;

    IF v_interview_count = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Evaluation failed: interview does not exist';
    END IF;

    SELECT COUNT(*),
           COALESCE(SUM(CASE
               WHEN a.answer_id IS NULL OR a.marks IS NULL THEN 1 ELSE 0
           END), 0),
           COALESCE(SUM(q.max_marks), 0),
           COALESCE(SUM(COALESCE(a.marks, 0)), 0)
    INTO v_question_count, v_incomplete_count, v_max_score, v_total_score
    FROM interview_questions iq
    JOIN questions q ON q.question_id = iq.question_id
    LEFT JOIN answers a
      ON a.interview_id = iq.interview_id
     AND a.question_id = iq.question_id
    WHERE iq.interview_id = p_interview_id;

    IF v_question_count = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Evaluation failed: no questions are assigned';
    END IF;

    IF v_incomplete_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Evaluation failed: every assigned question needs an answer with marks';
    END IF;

    IF v_max_score <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Evaluation failed: maximum marks must be greater than zero';
    END IF;

    SET v_percentage = ROUND((v_total_score / v_max_score) * 100, 2);

    INSERT INTO results
        (interview_id, total_score, max_score, percentage, evaluated_at)
    VALUES
        (p_interview_id, v_total_score, v_max_score,
         v_percentage, CURRENT_TIMESTAMP)
    ON DUPLICATE KEY UPDATE
        total_score = VALUES(total_score),
        max_score = VALUES(max_score),
        percentage = VALUES(percentage),
        evaluated_at = CURRENT_TIMESTAMP;

    SELECT p_interview_id AS interview_id,
           v_total_score AS total_score,
           v_max_score AS max_score,
           v_percentage AS percentage,
           'Evaluation saved successfully' AS message;
END$$

DELIMITER ;

-- Example: CALL sp_evaluate_interview(1);
-- Verify: SELECT * FROM results WHERE interview_id = 1;
-- Sample interview 1: 31 / 40 = 77.50%.
