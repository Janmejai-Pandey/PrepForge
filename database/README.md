# PrepForge – Database Documentation

## 1. Introduction

PrepForge is an intelligent placement preparation and mock interview database system developed using MySQL.

The database manages the information required for placement preparation, including student profiles, technical skills, company requirements, job applications, mock interviews, answers, performance results and personalized recommendations.

The database is designed to maintain data consistency, reduce redundancy and establish meaningful relationships between different entities.

**Database Name:** `prepforge`  
**DBMS:** MySQL 8.0.16 or later  
**Database Language:** SQL

---

## 2. Database Architecture

The database follows a relational model consisting of 15 main tables.

The tables are divided into different functional groups:

| Category | Tables |
|---|---|
| Student Management | `students`, `skills`, `student_skills` |
| Company and Job Management | `companies`, `job_roles`, `role_skills` |
| Mock Interview Management | `questions`, `interviews`, `interview_questions`, `answers` |
| Performance Evaluation | `results`, `skill_scores` |
| Recommendations | `recommendations` |
| Placement Management | `applications`, `placements` |

An additional audit table is used to maintain application activity records.

---

## 3. Understanding the Tables

This section explains the purpose of each table and the information stored in it.

### A. Student Management

**1. students**

Stores the basic information of registered students.

Important attributes:
- `student_id` – Unique identifier for every student.
- `name` – Student's name.
- `email` – Unique email address.
- `branch` – Academic branch.
- `cgpa` – Current CGPA.
- `grad_year` – Expected graduation year.

**2. skills**

Maintains the list of skills available in the system.

Important attributes:
- `skill_id` – Unique skill identifier.
- `skill_name` – Name of the skill.
- `category` – Skill category.

**3. student_skills**

Connects students with their acquired skills.

It resolves the many-to-many relationship between `students` and `skills`.

Important attributes:
- `student_id` – References the student.
- `skill_id` – References the skill.
- `proficiency` – Student's proficiency level.

### B. Company and Job Management

**4. companies**

Stores information about recruiting companies.

Important attributes:
- `company_id` – Unique company identifier.
- `name` – Company name.
- `sector` – Industry sector.
- `min_cgpa` – Minimum CGPA requirement.

**5. job_roles**

Stores the job positions offered by companies.

Important attributes:
- `role_id` – Unique job role identifier.
- `company_id` – References the recruiting company.
- `title` – Job title.
- `package_lpa` – Package offered in LPA.

**6. role_skills**

Connects job roles with their required skills.

Important attributes:
- `role_id` – References a job role.
- `skill_id` – References a required skill.
- `min_level` – Minimum proficiency level required.

### C. Mock Interview Management

**7. questions**

Stores the questions used in mock interviews.

Important attributes:
- `question_id` – Unique question identifier.
- `skill_id` – Skill associated with the question.
- `question_text` – Actual question.
- `difficulty` – Question difficulty.
- `max_marks` – Maximum marks available.

**8. interviews**

Maintains individual interview attempts made by students.

Important attributes:
- `interview_id` – Unique interview identifier.
- `student_id` – Student appearing for the interview.
- `role_id` – Job role targeted.
- `attempt_date` – Date of interview.
- `status` – Current interview status.

**9. interview_questions**

Connects interviews with the questions assigned to them.

It resolves the many-to-many relationship between `interviews` and `questions`.

Important attributes:
- `interview_id` – References an interview.
- `question_id` – References an assigned question.
- `seq_no` – Position of the question in the interview.

**10. answers**

Stores the responses submitted by students.

Important attributes:
- `answer_id` – Unique answer identifier.
- `interview_id` – References the interview.
- `question_id` – References the question.
- `answer_text` – Submitted response.
- `marks` – Marks awarded.

### D. Performance Evaluation

**11. results**

Stores the overall performance of an interview.

Important attributes:
- `result_id` – Unique result identifier.
- `interview_id` – References the interview.
- `total_score` – Marks obtained.
- `max_score` – Maximum possible marks.
- `percentage` – Overall percentage.
- `evaluated_at` – Evaluation timestamp.

**12. skill_scores**

Maintains the performance of students in individual skills.

Important attributes:
- `interview_id` – References an interview.
- `skill_id` – References a skill.
- `score` – Marks obtained for that skill.
- `max_score` – Maximum possible marks for that skill.

### E. Recommendations and Placement Management

**13. recommendations**

Stores personalized recommendations generated from student skill gaps.

Important attributes:
- `rec_id` – Unique recommendation identifier.
- `student_id` – Student receiving the recommendation.
- `skill_id` – Skill requiring improvement.
- `message` – Recommendation description.
- `created_on` – Creation timestamp.

**14. applications**

Maintains student applications for different job roles.

Important attributes:
- `application_id` – Unique application identifier.
- `student_id` – References the applicant.
- `role_id` – References the applied job role.
- `applied_on` – Application date.
- `status` – Application status.

**15. placements**

Stores placement offers received by students.

Important attributes:
- `placement_id` – Unique placement identifier.
- `application_id` – References the associated application.
- `offer_date` – Date of offer.
- `joining_date` – Expected joining date.

---

## 4. Database Relationships

The database uses primary keys and foreign keys to connect related tables.

### One-to-Many Relationships

- One company can offer multiple job roles.
- One student can participate in multiple interviews.
- One student can submit multiple applications.
- One interview can have multiple answers.
- One student can receive multiple recommendations.

### Many-to-Many Relationships

The following relationships are implemented through junction tables:

| Relationship | Junction Table |
|---|---|
| Students and Skills | `student_skills` |
| Job Roles and Skills | `role_skills` |
| Interviews and Questions | `interview_questions` |

### One-to-One Relationships

- One interview has at most one result.
- One application has at most one placement.

These relationships are enforced using UNIQUE foreign keys.

---

## 5. Database Constraints and Integrity

The database uses constraints to maintain valid and consistent records.

- **PRIMARY KEY:** Uniquely identifies each record.
- **FOREIGN KEY:** Establishes relationships between tables.
- **UNIQUE:** Prevents duplicate values in specified columns or combinations.
- **NOT NULL:** Ensures required fields cannot remain empty.
- **CHECK:** Restricts values to valid ranges.
- **DEFAULT:** Assigns default values where specified.
- **ENUM:** Restricts columns to predefined values where used.

Examples include:
- Student email addresses must be unique.
- CGPA must remain between 0 and 10.
- A skill name cannot be duplicated.
- A student cannot apply for the same job role more than once.
- An interview cannot have multiple results.

---

## 6. SQL Features Implemented

The database demonstrates the following SQL concepts.

### Basic SQL Operations
- CREATE
- INSERT
- SELECT
- UPDATE
- DELETE
- ALTER
- DROP

### Joins
- INNER JOIN
- LEFT JOIN
- Multiple-table JOIN operations

### Aggregate Functions
- COUNT()
- SUM()
- AVG()
- MIN()
- MAX()

### Other SQL Concepts
- GROUP BY
- HAVING
- ORDER BY
- DISTINCT
- Nested queries
- Subqueries
- IN and EXISTS
- NOT EXISTS

### Indexing
Indexes are used on selected columns to improve the performance of frequently executed queries.

The `EXPLAIN` statement can be used to inspect query execution plans.

---

## 7. Views

Views provide simplified representations of information obtained from multiple related tables.

The database includes:

**1. `v_student_performance`**

Provides an overview of student interview performance.

**2. `v_student_skill_profile`**

Displays students' skills and their proficiency levels.

**3. `v_application_status`**

Displays job application information along with relevant student and job role details.

Views can be accessed using:

```sql
SELECT * FROM v_student_performance;
SELECT * FROM v_student_skill_profile;
SELECT * FROM v_application_status;
```

---

## 8. Stored Procedures

Stored procedures encapsulate frequently used database operations.

### 1. `sp_student_interview_report`

Generates an interview performance report for a student, including interview statistics and performance information.

### 2. `sp_apply_for_role`

Processes a student's application for a job role after checking eligibility and duplicate applications.

### 3. `sp_generate_recommendations`

Identifies skill gaps by comparing student proficiency with job role requirements and generates recommendations.

### Additional Evaluation Procedures

The extended database implementation includes:

**4. `sp_evaluate_interview`**

Calculates total marks, maximum marks and percentage for an interview and stores the result in `results`.

**5. `sp_calculate_skill_scores`**

Calculates interview performance separately for each skill and updates `skill_scores`.

**6. `sp_complete_interview`**

Validates interview completion, executes evaluation procedures and updates the interview status to `COMPLETED`.

---

## 9. User-Defined Function

### `fn_interview_percentage`

Calculates the percentage obtained by a student in a particular interview.

The function can be used inside SQL queries to retrieve calculated interview percentages.

---

## 10. Triggers

Triggers automatically execute when specified database events occur.

The database implements the following trigger functionality:

### 1. `trg_validate_answer_marks`

Validates marks when a new answer is inserted and prevents marks from exceeding the maximum marks assigned to the question.

### 2. `trg_application_insert_audit`

Maintains an audit record when a new job application is created.

### 3. `trg_application_update_audit`

Records changes made to existing job applications.

### 4. `trg_validate_answer_marks_update`

Performs equivalent marks validation when an existing answer is updated.

### 5. `trg_validate_interview_completion`

Prevents an interview from being marked as completed if assigned questions remain unanswered or unmarked.

---

## 11. Transactions

Transactions help maintain database consistency when multiple operations must succeed together.

The project demonstrates:

- `START TRANSACTION`
- `COMMIT`
- `ROLLBACK`
- `SAVEPOINT`
- `ROLLBACK TO SAVEPOINT`

The interview completion procedure also uses transaction handling so that evaluation, skill-score updates and status changes are treated as one logical operation.

---

## 12. Automatic Interview Evaluation Workflow

The extended evaluation functionality follows this sequence:

1. A student attempts an interview.
2. Questions are assigned through `interview_questions`.
3. Answers are stored in `answers`.
4. Marks are assigned to submitted answers.
5. `sp_evaluate_interview` calculates the overall result.
6. `sp_calculate_skill_scores` calculates skill-wise performance.
7. `sp_complete_interview` validates the interview and changes its status to `COMPLETED`.

For example:

```sql
CALL sp_complete_interview(1);
```

The result can be checked using:

```sql
SELECT *
FROM results
WHERE interview_id = 1;
```

Skill-wise performance can be checked using:

```sql
SELECT *
FROM skill_scores
WHERE interview_id = 1;
```

The database calculates scores from marks already assigned to answers. It does not automatically evaluate the meaning or correctness of free-text responses.

---

## 13. Normalization

The database follows the principles of relational normalization.

**First Normal Form (1NF)**

Each attribute contains atomic values, and repeating groups are represented through separate rows.

**Second Normal Form (2NF)**

Non-key attributes in relations with composite keys depend on the complete candidate key.

**Third Normal Form (3NF)**

Non-key attributes are stored with the entities they describe, avoiding unnecessary transitive dependencies.

Junction tables are used to resolve many-to-many relationships.

The `results` and `skill_scores` tables contain calculated summary values that are deliberately stored for reporting and analytics.

---

## 14. How to Execute the Database

1. Install MySQL Server 8.0.16 or later.
2. Open MySQL Workbench.
3. Open `PrepForge_Complete_DBMS.sql`.
4. Execute the complete SQL script.
5. Select the `prepforge` database.
6. Verify table creation using:

```sql
USE prepforge;
SHOW TABLES;
```

7. Explore the tables and their records:

```sql
SELECT * FROM students;
SELECT * FROM skills;
SELECT * FROM companies;
SELECT * FROM job_roles;
SELECT * FROM interviews;
SELECT * FROM results;
```

8. Test the stored procedures, functions, views and triggers using the relevant SQL statements.

**Note:** The complete setup script recreates the `prepforge` database. Existing data in that database will be lost when the script is executed again. Back up important records before running it.

---

## 15. Important Implementation Note

The database is responsible for data storage, relationships, validation, queries, evaluation calculations and transaction management.

The Java application is responsible for interacting with the database through JDBC and presenting information through its user interface.

The database and Java modules together form the complete PrepForge application.
