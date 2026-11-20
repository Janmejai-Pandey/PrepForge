-- PREPFORGE: VALIDATE MARKS WHEN AN EXISTING ANSWER IS UPDATED
-- Run after the original PrepForge_DBMS_Complete.sql.
-- The original script already creates an INSERT validation trigger.
-- This adds matching validation for UPDATE. The table CHECK constraint
-- separately prevents negative marks. MySQL 8.0.16+.

USE prepforge;
DROP TRIGGER IF EXISTS trg_validate_answer_marks_update;

DELIMITER $$

CREATE TRIGGER trg_validate_answer_marks_update
BEFORE UPDATE ON answers
FOR EACH ROW
BEGIN
    DECLARE v_max_marks DECIMAL(5,2);

    SELECT max_marks INTO v_max_marks
    FROM questions
    WHERE question_id = NEW.question_id;

    IF NEW.marks IS NOT NULL AND NEW.marks > v_max_marks THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Updated marks cannot exceed question maximum marks';
    END IF;
END$$

DELIMITER ;

-- Expected to fail: question 1 has maximum marks 10.
-- UPDATE answers SET marks = 11 WHERE answer_id = 1;
-- Expected to succeed:
-- UPDATE answers SET marks = 9 WHERE answer_id = 1;
