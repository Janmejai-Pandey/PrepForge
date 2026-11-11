-- PREPFORGE: COMPLETION VALIDATION + ATOMIC INTERVIEW FINALIZATION
-- Prerequisites: original SQL file, then skill-score procedure, then
-- interview-evaluation procedure. MySQL 8.0.16+.

USE prepforge;

DROP TRIGGER IF EXISTS trg_validate_interview_completion;
DELIMITER $$

CREATE TRIGGER trg_validate_interview_completion
BEFORE UPDATE ON interviews
FOR EACH ROW
BEGIN
    DECLARE v_question_count INT DEFAULT 0;
    DECLARE v_incomplete_count INT DEFAULT 0;

    IF NEW.status = 'COMPLETED' AND OLD.status <> 'COMPLETED' THEN
        SELECT COUNT(*),
               COALESCE(SUM(CASE
                   WHEN a.answer_id IS NULL OR a.marks IS NULL THEN 1 ELSE 0
               END), 0)
        INTO v_question_count, v_incomplete_count
        FROM interview_questions iq
        LEFT JOIN answers a
          ON a.interview_id = iq.interview_id
         AND a.question_id = iq.question_id
        WHERE iq.interview_id = OLD.interview_id;

        IF v_question_count = 0 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Cannot complete: no questions are assigned';
        END IF;

        IF v_incomplete_count > 0 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Cannot complete: assigned questions are unanswered or unmarked';
        END IF;
    END IF;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_complete_interview;
DELIMITER $$

CREATE PROCEDURE sp_complete_interview(IN p_interview_id INT)
BEGIN
    DECLARE v_interview_count INT DEFAULT 0;
    DECLARE v_status VARCHAR(20);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT COUNT(*), MAX(status)
    INTO v_interview_count, v_status
    FROM interviews
    WHERE interview_id = p_interview_id
    FOR UPDATE;

    IF v_interview_count = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Cannot complete: interview does not exist';
    END IF;

    IF v_status = 'CANCELLED' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Cannot complete: interview is cancelled';
    END IF;

    CALL sp_evaluate_interview(p_interview_id);
    CALL sp_calculate_skill_scores(p_interview_id);

    UPDATE interviews
    SET status = 'COMPLETED'
    WHERE interview_id = p_interview_id;

    COMMIT;

    SELECT p_interview_id AS interview_id,
           'COMPLETED' AS final_status,
           'Interview evaluated and completed successfully' AS message;
END$$
DELIMITER ;

-- Success test using original sample data:
-- CALL sp_complete_interview(1);
-- Expected result: 31/40 = 77.50%.
--
-- Failure test: interview 5 is intentionally incomplete:
-- CALL sp_complete_interview(5);
-- Direct status update must also fail:
-- UPDATE interviews SET status='COMPLETED' WHERE interview_id=5;
--
-- Verify:
-- SELECT interview_id, status FROM interviews ORDER BY interview_id;
-- SELECT * FROM results ORDER BY interview_id;
-- SELECT * FROM skill_scores ORDER BY interview_id, skill_id;
