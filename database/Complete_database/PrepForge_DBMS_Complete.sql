/*
PREPFORGE — DBMS LAB DATABASE IMPLEMENTATION
Target: MySQL 8.0.16+
Run sections in order. This script creates the 15 tables from the synopsis,
sample data, demonstration queries, views, procedures, function, triggers,
and transaction examples.

IMPORTANT:
- Run this on a fresh schema (the script drops and recreates prepforge).
- Demo data is fictional.
- Password/authentication tables are not included because they are absent
  from the approved 15-table synopsis. Coordinate any authentication schema
  addition with Member 1 before changing the shared design.
*/

-- =========================================================
-- 1. DATABASE
-- =========================================================
DROP DATABASE IF EXISTS prepforge;
CREATE DATABASE prepforge;
USE prepforge;

-- =========================================================
-- 2. TABLES
-- Create parent tables before child tables.
-- =========================================================

CREATE TABLE students (
    student_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    branch VARCHAR(50) NOT NULL,
    cgpa DECIMAL(3,2) NOT NULL,
    grad_year YEAR NOT NULL,
    CONSTRAINT chk_student_cgpa CHECK (cgpa BETWEEN 0 AND 10)
) ENGINE=InnoDB;

CREATE TABLE skills (
    skill_id INT PRIMARY KEY AUTO_INCREMENT,
    skill_name VARCHAR(80) NOT NULL UNIQUE,
    category VARCHAR(50) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE companies (
    company_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL UNIQUE,
    sector VARCHAR(80),
    min_cgpa DECIMAL(3,2) NOT NULL DEFAULT 0,
    CONSTRAINT chk_company_min_cgpa CHECK (min_cgpa BETWEEN 0 AND 10)
) ENGINE=InnoDB;

CREATE TABLE student_skills (
    student_id INT NOT NULL,
    skill_id INT NOT NULL,
    proficiency TINYINT NOT NULL,
    PRIMARY KEY (student_id, skill_id),
    CONSTRAINT chk_student_proficiency CHECK (proficiency BETWEEN 1 AND 5),
    CONSTRAINT fk_student_skills_student FOREIGN KEY (student_id)
        REFERENCES students(student_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_student_skills_skill FOREIGN KEY (skill_id)
        REFERENCES skills(skill_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE job_roles (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    company_id INT NOT NULL,
    title VARCHAR(120) NOT NULL,
    package_lpa DECIMAL(6,2),
    CONSTRAINT chk_role_package CHECK (package_lpa IS NULL OR package_lpa >= 0),
    CONSTRAINT fk_job_roles_company FOREIGN KEY (company_id)
        REFERENCES companies(company_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE role_skills (
    role_id INT NOT NULL,
    skill_id INT NOT NULL,
    min_level TINYINT NOT NULL,
    PRIMARY KEY (role_id, skill_id),
    CONSTRAINT chk_role_min_level CHECK (min_level BETWEEN 1 AND 5),
    CONSTRAINT fk_role_skills_role FOREIGN KEY (role_id)
        REFERENCES job_roles(role_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_role_skills_skill FOREIGN KEY (skill_id)
        REFERENCES skills(skill_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE questions (
    question_id INT PRIMARY KEY AUTO_INCREMENT,
    skill_id INT NOT NULL,
    question_text TEXT NOT NULL,
    difficulty ENUM('EASY','MEDIUM','HARD') NOT NULL,
    max_marks DECIMAL(5,2) NOT NULL DEFAULT 10,
    question_type ENUM('HR','TECHNICAL','CODING') NOT NULL DEFAULT 'TECHNICAL',
    CONSTRAINT chk_question_max_marks CHECK (max_marks > 0),
    CONSTRAINT fk_questions_skill FOREIGN KEY (skill_id)
        REFERENCES skills(skill_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE interviews (
    interview_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    role_id INT NOT NULL,
    attempt_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status ENUM('IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'IN_PROGRESS',
    CONSTRAINT fk_interviews_student FOREIGN KEY (student_id)
        REFERENCES students(student_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_interviews_role FOREIGN KEY (role_id)
        REFERENCES job_roles(role_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE interview_questions (
    interview_id INT NOT NULL,
    question_id INT NOT NULL,
    seq_no INT NOT NULL,
    PRIMARY KEY (interview_id, question_id),
    CONSTRAINT uq_interview_sequence UNIQUE (interview_id, seq_no),
    CONSTRAINT chk_interview_seq CHECK (seq_no > 0),
    CONSTRAINT fk_iq_interview FOREIGN KEY (interview_id)
        REFERENCES interviews(interview_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_iq_question FOREIGN KEY (question_id)
        REFERENCES questions(question_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE answers (
    answer_id INT PRIMARY KEY AUTO_INCREMENT,
    interview_id INT NOT NULL,
    question_id INT NOT NULL,
    answer_text TEXT,
    marks DECIMAL(5,2),
    submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_answer_marks CHECK (marks IS NULL OR marks >= 0),
    CONSTRAINT uq_answer_per_interview_question UNIQUE (interview_id, question_id),
    CONSTRAINT fk_answers_assigned_question
        FOREIGN KEY (interview_id, question_id)
        REFERENCES interview_questions(interview_id, question_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE results (
    result_id INT PRIMARY KEY AUTO_INCREMENT,
    interview_id INT NOT NULL UNIQUE,
    total_score DECIMAL(7,2) NOT NULL DEFAULT 0,
    max_score DECIMAL(7,2) NOT NULL DEFAULT 0,
    percentage DECIMAL(5,2) NOT NULL DEFAULT 0,
    evaluated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_result_scores CHECK (
        total_score >= 0 AND max_score >= 0
        AND total_score <= max_score
        AND percentage BETWEEN 0 AND 100
    ),
    CONSTRAINT fk_results_interview FOREIGN KEY (interview_id)
        REFERENCES interviews(interview_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE skill_scores (
    interview_id INT NOT NULL,
    skill_id INT NOT NULL,
    score DECIMAL(7,2) NOT NULL DEFAULT 0,
    max_score DECIMAL(7,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (interview_id, skill_id),
    CONSTRAINT chk_skill_score CHECK (
        score >= 0 AND max_score >= 0 AND score <= max_score
    ),
    CONSTRAINT fk_skill_scores_interview FOREIGN KEY (interview_id)
        REFERENCES interviews(interview_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_skill_scores_skill FOREIGN KEY (skill_id)
        REFERENCES skills(skill_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE recommendations (
    rec_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    skill_id INT NOT NULL,
    message VARCHAR(500) NOT NULL,
    created_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recommendations_student FOREIGN KEY (student_id)
        REFERENCES students(student_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_recommendations_skill FOREIGN KEY (skill_id)
        REFERENCES skills(skill_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE applications (
    application_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    role_id INT NOT NULL,
    applied_on DATE NOT NULL,
    status ENUM('APPLIED','UNDER_REVIEW','SHORTLISTED','REJECTED','SELECTED','WITHDRAWN')
        NOT NULL DEFAULT 'APPLIED',
    CONSTRAINT uq_student_role_application UNIQUE (student_id, role_id),
    CONSTRAINT fk_applications_student FOREIGN KEY (student_id)
        REFERENCES students(student_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_applications_role FOREIGN KEY (role_id)
        REFERENCES job_roles(role_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE placements (
    placement_id INT PRIMARY KEY AUTO_INCREMENT,
    application_id INT NOT NULL UNIQUE,
    offer_date DATE NOT NULL,
    joining_date DATE,
    CONSTRAINT chk_joining_date CHECK (
        joining_date IS NULL OR joining_date >= offer_date
    ),
    CONSTRAINT fk_placements_application FOREIGN KEY (application_id)
        REFERENCES applications(application_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =========================================================
-- 3. SAMPLE DATA
-- IDs are explicit to make the demonstration queries predictable.
-- =========================================================

INSERT INTO students (student_id, name, email, branch, cgpa, grad_year) VALUES
(1,'Aarav Sharma','aarav@example.com','CSE',8.70,2027),
(2,'Diya Verma','diya@example.com','CSE',9.10,2027),
(3,'Kabir Singh','kabir@example.com','IT',7.80,2027),
(4,'Ananya Gupta','ananya@example.com','CSE',8.20,2027),
(5,'Rohan Mehta','rohan@example.com','ECE',7.40,2027),
(6,'Meera Joshi','meera@example.com','CSE',9.40,2027);

INSERT INTO skills (skill_id, skill_name, category) VALUES
(1,'Java','Programming'),
(2,'Python','Programming'),
(3,'SQL','Database'),
(4,'Data Structures','Computer Science'),
(5,'DBMS','Database'),
(6,'Operating Systems','Computer Science'),
(7,'Communication','Soft Skills'),
(8,'Computer Networks','Computer Science');

INSERT INTO companies (company_id, name, sector, min_cgpa) VALUES
(1,'TechNova','IT Services',7.00),
(2,'CloudSphere','Cloud Computing',7.50),
(3,'DataBridge','Analytics',8.00),
(4,'FinEdge','FinTech',7.80);

INSERT INTO job_roles (role_id, company_id, title, package_lpa) VALUES
(1,1,'Graduate Software Engineer',8.50),
(2,1,'Java Developer',10.00),
(3,2,'Cloud Engineer',12.00),
(4,3,'Data Analyst',9.00),
(5,4,'Backend Developer',14.00);

INSERT INTO student_skills (student_id, skill_id, proficiency) VALUES
(1,1,4),(1,3,4),(1,4,3),(1,5,3),(1,7,3),
(2,1,5),(2,2,4),(2,3,5),(2,4,5),(2,5,5),(2,7,4),
(3,2,4),(3,3,3),(3,4,2),(3,6,3),(3,7,3),
(4,1,3),(4,3,4),(4,5,4),(4,6,3),(4,8,3),
(5,2,3),(5,4,3),(5,6,4),(5,8,4),
(6,1,5),(6,2,5),(6,3,4),(6,4,5),(6,5,4),(6,7,5);

INSERT INTO role_skills (role_id, skill_id, min_level) VALUES
(1,1,3),(1,3,3),(1,4,3),(1,5,3),
(2,1,4),(2,3,3),(2,4,3),(2,5,3),
(3,2,3),(3,6,3),(3,8,3),
(4,2,4),(4,3,4),(4,5,3),
(5,1,4),(5,3,4),(5,4,4),(5,5,4);

INSERT INTO questions (question_id, skill_id, question_text, difficulty, max_marks, question_type) VALUES
(1,1,'Explain the four pillars of object-oriented programming.','EASY',10,'TECHNICAL'),
(2,1,'What is the difference between an interface and an abstract class?','MEDIUM',10,'TECHNICAL'),
(3,3,'Explain the difference between WHERE and HAVING.','EASY',10,'TECHNICAL'),
(4,3,'Write a query to find the second-highest salary.','MEDIUM',10,'CODING'),
(5,4,'Explain the time complexity of binary search.','EASY',10,'TECHNICAL'),
(6,4,'Explain how a balanced binary search tree works.','HARD',15,'TECHNICAL'),
(7,5,'Explain normalization and the purpose of 3NF.','MEDIUM',10,'TECHNICAL'),
(8,2,'What is the difference between a list and a tuple in Python?','EASY',10,'TECHNICAL'),
(9,6,'What is the difference between a process and a thread?','MEDIUM',10,'TECHNICAL'),
(10,7,'Tell me about a technical challenge you solved.','EASY',10,'HR'),
(11,8,'Explain the TCP three-way handshake.','MEDIUM',10,'TECHNICAL'),
(12,2,'Explain overfitting and two ways to reduce it.','HARD',15,'TECHNICAL');

INSERT INTO interviews (interview_id, student_id, role_id, attempt_date, status) VALUES
(1,1,1,'2026-10-10 10:00:00','COMPLETED'),
(2,1,2,'2026-10-20 11:00:00','COMPLETED'),
(3,2,2,'2026-10-21 12:00:00','COMPLETED'),
(4,3,1,'2026-10-22 09:30:00','COMPLETED'),
(5,4,4,'2026-10-23 14:00:00','IN_PROGRESS'),
(6,6,5,'2026-10-24 15:00:00','COMPLETED');

INSERT INTO interview_questions (interview_id, question_id, seq_no) VALUES
(1,1,1),(1,3,2),(1,5,3),(1,7,4),
(2,1,1),(2,2,2),(2,4,3),(2,7,4),
(3,1,1),(3,2,2),(3,3,3),(3,5,4),
(4,3,1),(4,5,2),(4,7,3),(4,10,4),
(5,8,1),(5,3,2),(5,12,3),
(6,1,1),(6,3,2),(6,5,3),(6,7,4);

INSERT INTO answers (answer_id, interview_id, question_id, answer_text, marks) VALUES
(1,1,1,'Encapsulation, inheritance, polymorphism and abstraction.',8),
(2,1,3,'WHERE filters rows; HAVING filters groups.',9),
(3,1,5,'Binary search takes O(log n) on sorted data.',7),
(4,1,7,'3NF removes transitive dependencies.',7),
(5,2,1,'Explained all four OOP pillars with examples.',9),
(6,2,2,'Compared interfaces and abstract classes.',8),
(7,2,4,'Used a nested query with MAX.',7),
(8,2,7,'Explained 1NF, 2NF and 3NF.',8),
(9,3,1,'Gave clear examples of OOP principles.',10),
(10,3,2,'Explained both concepts correctly.',9),
(11,3,3,'Explained filtering before and after grouping.',9),
(12,3,5,'Correct complexity and sorted-array condition.',8),
(13,4,3,'Partially correct explanation.',6),
(14,4,5,'Correct complexity but missed a condition.',5),
(15,4,7,'Basic explanation of normalization.',6),
(16,4,10,'Described a college project challenge.',8),
(17,6,1,'Detailed explanation with examples.',10),
(18,6,3,'Correct explanation.',9),
(19,6,5,'Correct explanation.',10),
(20,6,7,'Explained normalization clearly.',9);

INSERT INTO results (result_id, interview_id, total_score, max_score, percentage) VALUES
(1,1,31,40,77.50),
(2,2,32,40,80.00),
(3,3,36,40,90.00),
(4,4,25,40,62.50),
(5,6,38,40,95.00);

INSERT INTO skill_scores (interview_id, skill_id, score, max_score) VALUES
(1,1,8,10),(1,3,9,10),(1,4,7,10),(1,5,7,10),
(2,1,17,20),(2,3,7,10),(2,5,8,10),
(3,1,19,20),(3,3,9,10),(3,4,8,10),
(4,3,6,10),(4,4,5,10),(4,5,6,10),(4,7,8,10),
(6,1,10,10),(6,3,9,10),(6,4,10,10),(6,5,9,10);

INSERT INTO recommendations (rec_id, student_id, skill_id, message) VALUES
(1,1,4,'Practice tree traversals, heaps and graph algorithms.'),
(2,3,4,'Revise data structures and solve timed coding problems.'),
(3,3,5,'Study normalization, joins, indexing and transactions.'),
(4,4,1,'Practice Java OOP concepts and collections.'),
(5,4,4,'Strengthen problem solving and complexity analysis.'),
(6,5,3,'Practice SQL joins, grouping and nested queries.');

INSERT INTO applications (application_id, student_id, role_id, applied_on, status) VALUES
(1,1,1,'2026-10-01','SHORTLISTED'),
(2,1,2,'2026-10-05','UNDER_REVIEW'),
(3,2,2,'2026-10-06','SELECTED'),
(4,3,1,'2026-10-07','REJECTED'),
(5,4,4,'2026-10-08','APPLIED'),
(6,6,5,'2026-10-09','SELECTED'),
(7,5,3,'2026-10-11','UNDER_REVIEW');

INSERT INTO placements (placement_id, application_id, offer_date, joining_date) VALUES
(1,3,'2026-10-25','2027-07-01'),
(2,6,'2026-10-26','2027-06-15');

-- =========================================================
-- 4. BASIC SELECT / CRUD DEMONSTRATIONS
-- =========================================================

-- 4.1 Display all students.
SELECT * FROM students;

-- 4.2 Students with CGPA greater than 8.
SELECT student_id, name, cgpa
FROM students
WHERE cgpa > 8.00
ORDER BY cgpa DESC;

-- 4.3 Students from CSE.
SELECT * FROM students WHERE branch = 'CSE';

-- 4.4 List all skills.
SELECT * FROM skills ORDER BY category, skill_name;

-- 4.5 Companies whose minimum CGPA is at least 7.5.
SELECT * FROM companies WHERE min_cgpa >= 7.5;

-- 4.6 Roles offering more than 10 LPA.
SELECT title, package_lpa
FROM job_roles
WHERE package_lpa > 10;

-- 4.7 Update a student's proficiency (example; run when needed).
-- UPDATE student_skills
-- SET proficiency = 4
-- WHERE student_id = 1 AND skill_id = 4;

-- 4.8 Delete a recommendation (example; run when needed).
-- DELETE FROM recommendations WHERE rec_id = 6;

-- =========================================================
-- 5. JOIN QUERIES
-- =========================================================

-- 5.1 Show all skills possessed by each student.
SELECT st.student_id, st.name, sk.skill_name, ss.proficiency
FROM students st
JOIN student_skills ss ON st.student_id = ss.student_id
JOIN skills sk ON ss.skill_id = sk.skill_id
ORDER BY st.student_id, sk.skill_name;

-- 5.2 Skills possessed by one student (change ID as required).
SELECT st.name, sk.skill_name, ss.proficiency
FROM students st
JOIN student_skills ss ON st.student_id = ss.student_id
JOIN skills sk ON ss.skill_id = sk.skill_id
WHERE st.student_id = 1;

-- 5.3 Skills required for each job role.
SELECT c.name AS company, jr.title, sk.skill_name, rs.min_level
FROM job_roles jr
JOIN companies c ON jr.company_id = c.company_id
JOIN role_skills rs ON jr.role_id = rs.role_id
JOIN skills sk ON rs.skill_id = sk.skill_id
ORDER BY jr.role_id, sk.skill_name;

-- 5.4 Interview results with student and role details.
SELECT i.interview_id, st.name, jr.title, i.attempt_date,
       r.total_score, r.max_score, r.percentage
FROM interviews i
JOIN students st ON i.student_id = st.student_id
JOIN job_roles jr ON i.role_id = jr.role_id
LEFT JOIN results r ON i.interview_id = r.interview_id
ORDER BY i.interview_id;

-- 5.5 Questions assigned to each interview.
SELECT i.interview_id, st.name, iq.seq_no, q.question_text,
       q.difficulty, q.max_marks
FROM interviews i
JOIN students st ON i.student_id = st.student_id
JOIN interview_questions iq ON i.interview_id = iq.interview_id
JOIN questions q ON iq.question_id = q.question_id
ORDER BY i.interview_id, iq.seq_no;

-- 5.6 Answers with question details.
SELECT a.answer_id, a.interview_id, q.question_text,
       a.answer_text, a.marks, q.max_marks
FROM answers a
JOIN questions q ON a.question_id = q.question_id
ORDER BY a.interview_id, a.answer_id;

-- 5.7 Applications with student, company and role.
SELECT a.application_id, st.name, c.name AS company,
       jr.title, a.applied_on, a.status
FROM applications a
JOIN students st ON a.student_id = st.student_id
JOIN job_roles jr ON a.role_id = jr.role_id
JOIN companies c ON jr.company_id = c.company_id
ORDER BY a.application_id;

-- 5.8 Placement offers.
SELECT st.name, c.name AS company, jr.title,
       p.offer_date, p.joining_date
FROM placements p
JOIN applications a ON p.application_id = a.application_id
JOIN students st ON a.student_id = st.student_id
JOIN job_roles jr ON a.role_id = jr.role_id
JOIN companies c ON jr.company_id = c.company_id;

-- 5.9 Students and their recommendations.
SELECT st.name, sk.skill_name, rec.message, rec.created_on
FROM recommendations rec
JOIN students st ON rec.student_id = st.student_id
JOIN skills sk ON rec.skill_id = sk.skill_id
ORDER BY st.name;

-- =========================================================
-- 6. AGGREGATE FUNCTIONS, GROUP BY, HAVING
-- =========================================================

-- 6.1 Number of students.
SELECT COUNT(*) AS total_students FROM students;

-- 6.2 Average, highest and lowest CGPA.
SELECT AVG(cgpa) AS average_cgpa,
       MAX(cgpa) AS highest_cgpa,
       MIN(cgpa) AS lowest_cgpa
FROM students;

-- 6.3 Number of students in each branch.
SELECT branch, COUNT(*) AS student_count
FROM students
GROUP BY branch;

-- 6.4 Average interview percentage for each student.
SELECT st.student_id, st.name, AVG(r.percentage) AS average_percentage
FROM students st
JOIN interviews i ON st.student_id = i.student_id
JOIN results r ON i.interview_id = r.interview_id
GROUP BY st.student_id, st.name;

-- 6.5 Students with at least two applications.
SELECT st.student_id, st.name, COUNT(*) AS applications_count
FROM students st
JOIN applications a ON st.student_id = a.student_id
GROUP BY st.student_id, st.name
HAVING COUNT(*) >= 2;

-- 6.6 Number of roles offered by each company.
SELECT c.name, COUNT(jr.role_id) AS roles_offered
FROM companies c
LEFT JOIN job_roles jr ON c.company_id = jr.company_id
GROUP BY c.company_id, c.name;

-- 6.7 Total marks scored in each interview.
SELECT interview_id, SUM(marks) AS marks_obtained,
       COUNT(*) AS answers_evaluated
FROM answers
WHERE marks IS NOT NULL
GROUP BY interview_id;

-- =========================================================
-- 7. SUBQUERIES / NESTED QUERIES / EXISTS
-- =========================================================

-- 7.1 Students whose CGPA is above the overall average.
SELECT name, cgpa
FROM students
WHERE cgpa > (SELECT AVG(cgpa) FROM students);

-- 7.2 Students who have never attempted an interview.
SELECT st.student_id, st.name
FROM students st
WHERE NOT EXISTS (
    SELECT 1 FROM interviews i
    WHERE i.student_id = st.student_id
);

-- 7.3 Students who have at least one completed interview.
SELECT st.student_id, st.name
FROM students st
WHERE EXISTS (
    SELECT 1 FROM interviews i
    WHERE i.student_id = st.student_id
      AND i.status = 'COMPLETED'
);

-- 7.4 Students scoring above the average interview percentage.
SELECT DISTINCT st.student_id, st.name, r.percentage
FROM students st
JOIN interviews i ON st.student_id = i.student_id
JOIN results r ON i.interview_id = r.interview_id
WHERE r.percentage > (SELECT AVG(percentage) FROM results);

-- 7.5 Highest package role(s).
SELECT title, package_lpa
FROM job_roles
WHERE package_lpa = (SELECT MAX(package_lpa) FROM job_roles);

-- 7.6 Roles requiring SQL.
SELECT jr.role_id, jr.title
FROM job_roles jr
WHERE jr.role_id IN (
    SELECT rs.role_id
    FROM role_skills rs
    JOIN skills sk ON rs.skill_id = sk.skill_id
    WHERE sk.skill_name = 'SQL'
);

-- 7.7 Students who meet or exceed the required skill level for a role.
-- Example role_id = 1.
SELECT st.student_id, st.name, sk.skill_name,
       ss.proficiency, rs.min_level
FROM students st
JOIN student_skills ss ON st.student_id = ss.student_id
JOIN role_skills rs ON ss.skill_id = rs.skill_id
JOIN skills sk ON sk.skill_id = rs.skill_id
WHERE rs.role_id = 1
  AND ss.proficiency >= rs.min_level;

-- 7.8 Students who have applied to every role offered by company 1.
SELECT st.student_id, st.name
FROM students st
WHERE NOT EXISTS (
    SELECT 1
    FROM job_roles jr
    WHERE jr.company_id = 1
      AND NOT EXISTS (
          SELECT 1
          FROM applications a
          WHERE a.student_id = st.student_id
            AND a.role_id = jr.role_id
      )
);

-- =========================================================
-- 8. VIEWS
-- =========================================================

CREATE OR REPLACE VIEW v_student_performance AS
SELECT st.student_id, st.name, i.interview_id, jr.title AS target_role,
       i.attempt_date, i.status, r.total_score, r.max_score, r.percentage
FROM students st
JOIN interviews i ON st.student_id = i.student_id
JOIN job_roles jr ON i.role_id = jr.role_id
LEFT JOIN results r ON i.interview_id = r.interview_id;

CREATE OR REPLACE VIEW v_student_skill_profile AS
SELECT st.student_id, st.name, sk.skill_name, sk.category, ss.proficiency
FROM students st
JOIN student_skills ss ON st.student_id = ss.student_id
JOIN skills sk ON ss.skill_id = sk.skill_id;

CREATE OR REPLACE VIEW v_application_status AS
SELECT a.application_id, st.student_id, st.name,
       c.name AS company, jr.title AS role_title,
       a.applied_on, a.status
FROM applications a
JOIN students st ON a.student_id = st.student_id
JOIN job_roles jr ON a.role_id = jr.role_id
JOIN companies c ON jr.company_id = c.company_id;

-- View demonstration:
SELECT * FROM v_student_performance;
SELECT * FROM v_student_skill_profile WHERE student_id = 1;
SELECT * FROM v_application_status;

-- =========================================================
-- 9. STORED PROCEDURES
-- =========================================================

DELIMITER $$

-- 9.1 Student interview report.
CREATE PROCEDURE sp_student_interview_report(IN p_student_id INT)
BEGIN
    SELECT st.student_id, st.name,
           COUNT(r.result_id) AS completed_interviews,
           ROUND(AVG(r.percentage), 2) AS average_percentage,
           MAX(r.percentage) AS highest_percentage
    FROM students st
    LEFT JOIN interviews i ON st.student_id = i.student_id
    LEFT JOIN results r ON i.interview_id = r.interview_id
    WHERE st.student_id = p_student_id
    GROUP BY st.student_id, st.name;

    SELECT i.interview_id, i.attempt_date, jr.title,
           r.total_score, r.max_score, r.percentage
    FROM interviews i
    JOIN job_roles jr ON i.role_id = jr.role_id
    LEFT JOIN results r ON i.interview_id = r.interview_id
    WHERE i.student_id = p_student_id
    ORDER BY i.attempt_date DESC
    LIMIT 1;
END$$

-- 9.2 Apply for a role with validation.
CREATE PROCEDURE sp_apply_for_role(
    IN p_student_id INT,
    IN p_role_id INT
)
BEGIN
    DECLARE v_student_count INT DEFAULT 0;
    DECLARE v_role_count INT DEFAULT 0;
    DECLARE v_existing_count INT DEFAULT 0;
    DECLARE v_student_cgpa DECIMAL(3,2);
    DECLARE v_min_cgpa DECIMAL(3,2);

    SELECT COUNT(*), MAX(cgpa)
    INTO v_student_count, v_student_cgpa
    FROM students WHERE student_id = p_student_id;

    SELECT COUNT(*), MAX(c.min_cgpa)
    INTO v_role_count, v_min_cgpa
    FROM job_roles jr
    JOIN companies c ON jr.company_id = c.company_id
    WHERE jr.role_id = p_role_id;

    IF v_student_count = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Student does not exist';
    ELSEIF v_role_count = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Job role does not exist';
    ELSEIF v_student_cgpa < v_min_cgpa THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Student does not meet company CGPA requirement';
    ELSE
        SELECT COUNT(*) INTO v_existing_count
        FROM applications
        WHERE student_id = p_student_id AND role_id = p_role_id;

        IF v_existing_count > 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Student has already applied for this role';
        ELSE
            INSERT INTO applications(student_id, role_id, applied_on, status)
            VALUES(p_student_id, p_role_id, CURDATE(), 'APPLIED');
            SELECT 'Application submitted successfully' AS message,
                   LAST_INSERT_ID() AS application_id;
        END IF;
    END IF;
END$$

-- 9.3 Generate recommendations from role skill gaps.
-- Inserts recommendations where student proficiency is missing or below
-- the role's minimum level. Existing identical recommendations are skipped.
CREATE PROCEDURE sp_generate_recommendations(
    IN p_student_id INT,
    IN p_role_id INT
)
BEGIN
    INSERT INTO recommendations(student_id, skill_id, message)
    SELECT p_student_id, rs.skill_id,
           CONCAT('Improve ', sk.skill_name,
                  '. Required proficiency: ', rs.min_level,
                  ', current proficiency: ',
                  COALESCE(ss.proficiency, 0), '.')
    FROM role_skills rs
    JOIN skills sk ON sk.skill_id = rs.skill_id
    LEFT JOIN student_skills ss
      ON ss.student_id = p_student_id
     AND ss.skill_id = rs.skill_id
    WHERE rs.role_id = p_role_id
      AND COALESCE(ss.proficiency, 0) < rs.min_level
      AND NOT EXISTS (
          SELECT 1
          FROM recommendations rec
          WHERE rec.student_id = p_student_id
            AND rec.skill_id = rs.skill_id
            AND rec.message = CONCAT('Improve ', sk.skill_name,
                  '. Required proficiency: ', rs.min_level,
                  ', current proficiency: ',
                  COALESCE(ss.proficiency, 0), '.')
      );

    SELECT ROW_COUNT() AS recommendations_created;
END$$

DELIMITER ;

-- Procedure calls:
CALL sp_student_interview_report(1);
-- Use a student/role pair not already present when testing application:
-- CALL sp_apply_for_role(2, 1);
CALL sp_generate_recommendations(3, 1);

-- =========================================================
-- 10. USER-DEFINED FUNCTION
-- =========================================================

DELIMITER $$

CREATE FUNCTION fn_interview_percentage(p_interview_id INT)
RETURNS DECIMAL(5,2)
READS SQL DATA
BEGIN
    DECLARE v_obtained DECIMAL(7,2);
    DECLARE v_maximum DECIMAL(7,2);

    SELECT COALESCE(SUM(a.marks), 0),
           COALESCE(SUM(q.max_marks), 0)
    INTO v_obtained, v_maximum
    FROM interview_questions iq
    JOIN questions q ON iq.question_id = q.question_id
    LEFT JOIN answers a
      ON a.interview_id = iq.interview_id
     AND a.question_id = iq.question_id
    WHERE iq.interview_id = p_interview_id;

    IF v_maximum = 0 THEN
        RETURN 0.00;
    END IF;

    RETURN ROUND((v_obtained / v_maximum) * 100, 2);
END$$

DELIMITER ;

-- Function demonstration:
SELECT fn_interview_percentage(1) AS calculated_percentage;

-- =========================================================
-- 11. TRIGGERS AND AUDIT TABLE
-- =========================================================

CREATE TABLE application_audit (
    audit_id INT PRIMARY KEY AUTO_INCREMENT,
    application_id INT NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    action_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details VARCHAR(255),
    CONSTRAINT fk_audit_application FOREIGN KEY (application_id)
        REFERENCES applications(application_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

DELIMITER $$

-- 11.1 Validate marks do not exceed question maximum.
CREATE TRIGGER trg_validate_answer_marks
BEFORE INSERT ON answers
FOR EACH ROW
BEGIN
    DECLARE v_max_marks DECIMAL(5,2);

    SELECT max_marks INTO v_max_marks
    FROM questions
    WHERE question_id = NEW.question_id;

    IF NEW.marks IS NOT NULL AND NEW.marks > v_max_marks THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Awarded marks cannot exceed question maximum marks';
    END IF;
END$$

-- 11.2 Audit every newly inserted application.
CREATE TRIGGER trg_application_insert_audit
AFTER INSERT ON applications
FOR EACH ROW
BEGIN
    INSERT INTO application_audit(application_id, action_type, details)
    VALUES(NEW.application_id, 'INSERT', 'New job application created');
END$$

-- 11.3 Audit application status changes.
CREATE TRIGGER trg_application_update_audit
AFTER UPDATE ON applications
FOR EACH ROW
BEGIN
    IF OLD.status <> NEW.status THEN
        INSERT INTO application_audit(application_id, action_type, details)
        VALUES(NEW.application_id, 'STATUS_UPDATE',
               CONCAT('Status changed from ', OLD.status, ' to ', NEW.status));
    END IF;
END$$

DELIMITER ;

-- Trigger demonstrations (use a fresh student-role pair):
-- CALL sp_apply_for_role(5, 1);
-- SELECT * FROM application_audit ORDER BY audit_id DESC;
-- UPDATE applications SET status='UNDER_REVIEW' WHERE application_id=7;
-- SELECT * FROM application_audit ORDER BY audit_id DESC;

-- =========================================================
-- 12. TRANSACTION DEMONSTRATIONS
-- Run each transaction example separately and inspect the result.
-- =========================================================

-- 12.1 COMMIT example: create an application and keep it.
-- Choose a valid student-role pair that has not already been applied for.
-- START TRANSACTION;
-- INSERT INTO applications(student_id, role_id, applied_on, status)
-- VALUES(5, 1, CURDATE(), 'APPLIED');
-- COMMIT;

-- 12.2 ROLLBACK example: this inserted row will not remain.
-- START TRANSACTION;
-- INSERT INTO recommendations(student_id, skill_id, message)
-- VALUES(5, 1, 'Temporary recommendation for rollback demonstration');
-- ROLLBACK;
-- SELECT * FROM recommendations WHERE message LIKE 'Temporary recommendation%';

-- 12.3 Savepoint example.
-- START TRANSACTION;
-- UPDATE students SET cgpa = 7.50 WHERE student_id = 5;
-- SAVEPOINT after_cgpa_update;
-- UPDATE students SET cgpa = 6.50 WHERE student_id = 5;
-- ROLLBACK TO after_cgpa_update;
-- COMMIT;

-- =========================================================
-- 13. INDEXING AND QUERY OPTIMIZATION
-- PK and UNIQUE columns are already indexed automatically.
-- Add indexes for frequent filters/joins not already covered.
-- =========================================================

CREATE INDEX idx_interviews_student_date
    ON interviews(student_id, attempt_date);

CREATE INDEX idx_interviews_role
    ON interviews(role_id);

CREATE INDEX idx_answers_question
    ON answers(question_id);

CREATE INDEX idx_applications_status
    ON applications(status);

CREATE INDEX idx_recommendations_student
    ON recommendations(student_id);

-- EXPLAIN demonstration:
EXPLAIN
SELECT interview_id, attempt_date, status
FROM interviews
WHERE student_id = 1
ORDER BY attempt_date DESC;

-- =========================================================
-- 14. DATA INTEGRITY / CONSTRAINT DEMONSTRATIONS
-- Run these individually; expected to fail where noted.
-- =========================================================

-- Duplicate email: expected UNIQUE constraint error.
-- INSERT INTO students(name,email,branch,cgpa,grad_year)
-- VALUES('Test Student','aarav@example.com','CSE',8.0,2027);

-- Invalid CGPA: expected CHECK constraint error.
-- INSERT INTO students(name,email,branch,cgpa,grad_year)
-- VALUES('Invalid Student','invalid@example.com','CSE',12.0,2027);

-- Invalid student FK: expected FOREIGN KEY constraint error.
-- INSERT INTO student_skills(student_id,skill_id,proficiency)
-- VALUES(999,1,3);

-- Answer to a question not assigned to that interview:
-- expected composite FOREIGN KEY constraint error.
-- INSERT INTO answers(interview_id,question_id,answer_text,marks)
-- VALUES(1,12,'This question was not assigned to interview 1',5);

-- Duplicate application for same student-role:
-- expected UNIQUE constraint error.
-- INSERT INTO applications(student_id,role_id,applied_on,status)
-- VALUES(1,1,CURDATE(),'APPLIED');

-- =========================================================
-- 15. FINAL VERIFICATION QUERIES
-- =========================================================

SHOW TABLES;

SELECT 'students' AS table_name, COUNT(*) AS records FROM students
UNION ALL SELECT 'skills', COUNT(*) FROM skills
UNION ALL SELECT 'student_skills', COUNT(*) FROM student_skills
UNION ALL SELECT 'companies', COUNT(*) FROM companies
UNION ALL SELECT 'job_roles', COUNT(*) FROM job_roles
UNION ALL SELECT 'role_skills', COUNT(*) FROM role_skills
UNION ALL SELECT 'questions', COUNT(*) FROM questions
UNION ALL SELECT 'interviews', COUNT(*) FROM interviews
UNION ALL SELECT 'interview_questions', COUNT(*) FROM interview_questions
UNION ALL SELECT 'answers', COUNT(*) FROM answers
UNION ALL SELECT 'results', COUNT(*) FROM results
UNION ALL SELECT 'skill_scores', COUNT(*) FROM skill_scores
UNION ALL SELECT 'recommendations', COUNT(*) FROM recommendations
UNION ALL SELECT 'applications', COUNT(*) FROM applications
UNION ALL SELECT 'placements', COUNT(*) FROM placements;

-- End of PrepForge DBMS implementation script.
