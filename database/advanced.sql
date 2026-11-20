USE prepforge;

CREATE OR REPLACE VIEW v_interview_history AS
SELECT 
    i.interview_id,
    s.student_id,
    s.name AS student_name,
    s.email,
    jr.title AS role_title,
    c.name AS company_name,
    r.total_score,
    r.max_score,
    r.percentage,
    i.status,
    i.attempt_date
FROM interviews i
JOIN students s ON i.student_id = s.student_id
LEFT JOIN job_roles jr ON i.role_id = jr.role_id
LEFT JOIN companies c ON jr.company_id = c.company_id
LEFT JOIN results r ON i.interview_id = r.interview_id;

CREATE OR REPLACE VIEW v_skill_performance AS
SELECT 
    sk.skill_id,
    sk.skill_name,
    sk.category,
    COUNT(ss.interview_id) AS total_assessments,
    ROUND(AVG((ss.score / ss.max_score) * 100), 2) AS average_score_pct
FROM skills sk
LEFT JOIN skill_scores ss ON sk.skill_id = ss.skill_id
GROUP BY sk.skill_id, sk.skill_name, sk.category;

CREATE OR REPLACE VIEW v_weak_skills AS
SELECT 
    sk.skill_id,
    sk.skill_name,
    sk.category,
    ROUND(AVG((ss.score / ss.max_score) * 100), 2) AS average_score_pct
FROM skills sk
JOIN skill_scores ss ON sk.skill_id = ss.skill_id
GROUP BY sk.skill_id, sk.skill_name, sk.category
HAVING average_score_pct < 50.0;

CREATE OR REPLACE VIEW v_platform_stats AS
SELECT 
    (SELECT COUNT(*) FROM students) AS total_students,
    (SELECT COUNT(*) FROM companies) AS total_companies,
    (SELECT COUNT(*) FROM job_roles) AS total_job_roles,
    (SELECT COUNT(*) FROM questions) AS total_questions,
    (SELECT COUNT(*) FROM interviews) AS total_interviews,
    (SELECT COUNT(*) FROM placements) AS total_placed;

DELIMITER $$
DROP FUNCTION IF EXISTS fn_grade$$
CREATE FUNCTION fn_grade(score_pct DECIMAL(5,2))
RETURNS VARCHAR(20)
DETERMINISTIC
BEGIN
    DECLARE grade_val VARCHAR(20);
    IF score_pct >= 90.0 THEN
        SET grade_val = 'A+';
    ELSEIF score_pct >= 80.0 THEN
        SET grade_val = 'A';
    ELSEIF score_pct >= 70.0 THEN
        SET grade_val = 'B';
    ELSEIF score_pct >= 60.0 THEN
        SET grade_val = 'C';
    ELSEIF score_pct >= 50.0 THEN
        SET grade_val = 'D';
    ELSE
        SET grade_val = 'Needs Improvement';
    END IF;
    RETURN grade_val;
END$$
DELIMITER ;

DELIMITER $$
DROP PROCEDURE IF EXISTS sp_student_report$$
CREATE PROCEDURE sp_student_report(IN p_student_id INT)
BEGIN
    SELECT student_id, name, email, branch, cgpa, grad_year 
    FROM students 
    WHERE student_id = p_student_id;

    SELECT r.result_id, r.interview_id, r.total_score, r.max_score, r.percentage, fn_grade(r.percentage) AS grade
    FROM results r
    JOIN interviews i ON r.interview_id = i.interview_id
    WHERE i.student_id = p_student_id
    ORDER BY r.evaluated_at DESC;

    SELECT sk.skill_name, ROUND(AVG((ss.score / ss.max_score) * 100), 2) AS skill_avg
    FROM skill_scores ss
    JOIN skills sk ON ss.skill_id = sk.skill_id
    JOIN interviews i ON ss.interview_id = i.interview_id
    WHERE i.student_id = p_student_id
    GROUP BY sk.skill_id, sk.skill_name
    HAVING skill_avg < 50.0;
END$$
DELIMITER ;

DELIMITER $$
DROP TRIGGER IF EXISTS trg_after_result_insert$$
CREATE TRIGGER trg_after_result_insert
AFTER INSERT ON results
FOR EACH ROW
BEGIN
    IF NEW.percentage < 50.0 THEN
        INSERT INTO recommendations (student_id, skill_id, message, created_on)
        SELECT 
            i.student_id,
            q.skill_id,
            CONCAT('Diagnostic alert: Score of ', NEW.percentage, '% on role evaluation requires targeted skill practice.'),
            NOW()
        FROM interviews i
        JOIN interview_questions iq ON i.interview_id = iq.interview_id
        JOIN questions q ON iq.question_id = q.question_id
        WHERE i.interview_id = NEW.interview_id
        LIMIT 1;
    END IF;
END$$
DELIMITER ;

SELECT st.student_id, st.name, sk.skill_name, ss.proficiency
FROM students st
JOIN student_skills ss ON st.student_id = ss.student_id
JOIN skills sk ON ss.skill_id = sk.skill_id
ORDER BY st.student_id, sk.skill_name;

SELECT c.name AS company, jr.title, sk.skill_name, rs.min_level
FROM job_roles jr
JOIN companies c ON jr.company_id = c.company_id
JOIN role_skills rs ON jr.role_id = rs.role_id
JOIN skills sk ON rs.skill_id = sk.skill_id
ORDER BY jr.role_id, sk.skill_name;

SELECT name, cgpa
FROM students
WHERE cgpa > (SELECT AVG(cgpa) FROM students)
ORDER BY cgpa DESC;

SELECT s.student_id, s.name, s.branch, s.cgpa
FROM students s
WHERE s.cgpa = (
    SELECT MAX(s2.cgpa)
    FROM students s2
    WHERE s2.branch = s.branch
);

SELECT c.company_id, c.name, COUNT(jr.role_id) AS open_positions, AVG(jr.package_lpa) AS avg_package
FROM companies c
JOIN job_roles jr ON c.company_id = jr.company_id
GROUP BY c.company_id, c.name
HAVING COUNT(jr.role_id) >= 2;

START TRANSACTION;
SELECT * FROM applications 
WHERE student_id = 1 AND role_id = 1 
FOR UPDATE;

INSERT INTO applications (student_id, role_id, applied_on, status)
VALUES (1, 1, CURDATE(), 'APPLIED')
ON DUPLICATE KEY UPDATE status = 'UNDER_REVIEW';

COMMIT;
