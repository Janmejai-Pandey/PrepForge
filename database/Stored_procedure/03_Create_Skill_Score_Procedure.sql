-- PREPFORGE: AUTOMATIC SKILL-WISE SCORE CALCULATION
-- Run after the original PrepForge_DBMS_Complete.sql.
-- MySQL 8.0.16+

USE prepforge;
DROP PROCEDURE IF EXISTS sp_calculate_skill_scores;

DELIMITER $$

CREATE PROCEDURE sp_calculate_skill_scores(IN p_interview_id INT)
BEGIN
    DECLARE v_interview_count INT DEFAULT 0;
    DECLARE v_question_count INT DEFAULT 0;
    DECLARE v_incomplete_count INT DEFAULT 0;

    SELECT COUNT(*) INTO v_interview_count
    FROM interviews WHERE interview_id = p_interview_id;

    IF v_interview_count = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Skill scoring failed: interview does not exist';
    END IF;

    SELECT COUNT(*),
           COALESCE(SUM(CASE
               WHEN a.answer_id IS NULL OR a.marks IS NULL THEN 1 ELSE 0
           END), 0)
    INTO v_question_count, v_incomplete_count
    FROM interview_questions iq
    LEFT JOIN answers a
      ON a.interview_id = iq.interview_id
     AND a.question_id = iq.question_id
    WHERE iq.interview_id = p_interview_id;

    IF v_question_count = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Skill scoring failed: no questions are assigned';
    END IF;

    IF v_incomplete_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Skill scoring failed: every assigned question needs an answer with marks';
    END IF;

    INSERT INTO skill_scores (interview_id, skill_id, score, max_score)
    SELECT iq.interview_id, q.skill_id,
           SUM(a.marks), SUM(q.max_marks)
    FROM interview_questions iq
    JOIN questions q ON q.question_id = iq.question_id
    JOIN answers a
      ON a.interview_id = iq.interview_id
     AND a.question_id = iq.question_id
    WHERE iq.interview_id = p_interview_id
    GROUP BY iq.interview_id, q.skill_id
    ON DUPLICATE KEY UPDATE
        score = VALUES(score),
        max_score = VALUES(max_score);

    DELETE ss
    FROM skill_scores ss
    WHERE ss.interview_id = p_interview_id
      AND NOT EXISTS (
          SELECT 1
          FROM interview_questions iq
          JOIN questions q ON q.question_id = iq.question_id
          WHERE iq.interview_id = ss.interview_id
            AND q.skill_id = ss.skill_id
      );

    SELECT ss.interview_id, sk.skill_id, sk.skill_name,
           ss.score, ss.max_score,
           ROUND(100 * ss.score / NULLIF(ss.max_score, 0), 2) AS percentage
    FROM skill_scores ss
    JOIN skills sk ON sk.skill_id = ss.skill_id
    WHERE ss.interview_id = p_interview_id
    ORDER BY sk.skill_name;
END$$

DELIMITER ;

-- Example: CALL sp_calculate_skill_scores(1);
-- Sample interview 1:
-- Java 8/10, SQL 9/10, Data Structures 7/10, DBMS 7/10.
