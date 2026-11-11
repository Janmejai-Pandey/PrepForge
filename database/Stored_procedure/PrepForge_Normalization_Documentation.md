# PrepForge Database Normalization — 1NF, 2NF and 3NF

## 1. Purpose
Normalization organizes relational data to reduce duplication and prevent insertion, update and deletion anomalies. PrepForge separates students, skills, companies, roles, questions, interviews, answers, results, applications and placements into related tables.

Notation: `X → Y` means attribute set X functionally determines attribute set Y.

## 2. Candidate keys and functional dependencies

| Relation | Primary key / additional candidate key | Main functional dependencies |
|---|---|---|
| students | `student_id`; UNIQUE `email` | `student_id → name, email, branch, cgpa, grad_year`; `email → student_id, name, branch, cgpa, grad_year` |
| skills | `skill_id`; UNIQUE `skill_name` | `skill_id → skill_name, category`; `skill_name → skill_id, category` |
| student_skills | `(student_id, skill_id)` | `(student_id, skill_id) → proficiency` |
| companies | `company_id`; UNIQUE `name` | `company_id → name, sector, min_cgpa`; `name → company_id, sector, min_cgpa` |
| job_roles | `role_id` | `role_id → company_id, title, package_lpa` |
| role_skills | `(role_id, skill_id)` | `(role_id, skill_id) → min_level` |
| questions | `question_id` | `question_id → skill_id, question_text, difficulty, max_marks, question_type` |
| interviews | `interview_id` | `interview_id → student_id, role_id, attempt_date, status` |
| interview_questions | `(interview_id, question_id)`; UNIQUE `(interview_id, seq_no)` | `(interview_id, question_id) → seq_no`; `(interview_id, seq_no) → question_id` |
| answers | `answer_id`; UNIQUE `(interview_id, question_id)` | `answer_id → interview_id, question_id, answer_text, marks, submitted_at`; `(interview_id, question_id) → answer_id, answer_text, marks, submitted_at` |
| results | `result_id`; UNIQUE `interview_id` | `result_id → interview_id, total_score, max_score, percentage, evaluated_at`; `interview_id → result_id, total_score, max_score, percentage, evaluated_at` |
| skill_scores | `(interview_id, skill_id)` | `(interview_id, skill_id) → score, max_score` |
| recommendations | `rec_id` | `rec_id → student_id, skill_id, message, created_on` |
| applications | `application_id`; UNIQUE `(student_id, role_id)` | `application_id → student_id, role_id, applied_on, status`; `(student_id, role_id) → application_id, applied_on, status` |
| placements | `placement_id`; UNIQUE `application_id` | `placement_id → application_id, offer_date, joining_date`; `application_id → placement_id, offer_date, joining_date` |

These dependencies reflect the implemented business rules: one student-skill row per pair, one role-skill row per pair, one occurrence of a question per interview, one answer per assigned question, one result per interview, one application per student-role pair, and one placement per application.

## 3. First Normal Form (1NF)
A relation is in 1NF when each cell contains one value from its attribute domain, repeating groups are not stored as lists in a column, and rows can be uniquely identified.

**Unnormalized example:** storing `Java, SQL, DBMS` together in one `students.skills` column.

**PrepForge design:** student details are in `students`, skill definitions are in `skills`, and each student-skill mapping is a separate row in `student_skills`.

| student_id | skill_id | proficiency |
|---:|---:|---:|
| 1 | 1 | 4 |
| 1 | 3 | 4 |
| 1 | 5 | 3 |

Interview questions and answers are similarly represented by separate rows, not lists inside `interviews`.

**Conclusion:** the tables use scalar-valued columns and separate rows for repeating relationships, satisfying the intended 1NF design.

## 4. Second Normal Form (2NF)
A relation is in 2NF when it is in 1NF and every non-prime attribute is fully dependent on every candidate key; no non-key attribute depends on only part of a composite key.

Examples:
- `student_skills`: `(student_id, skill_id) → proficiency`. Proficiency belongs to the particular student-skill pair. Student name belongs in `students`; skill name belongs in `skills`.
- `role_skills`: `(role_id, skill_id) → min_level`. The required level belongs to the role-skill pair.
- `skill_scores`: `(interview_id, skill_id) → score, max_score`.
- `interview_questions`: `(interview_id, question_id) → seq_no`.

The non-key attributes depend on the complete composite key. Entity details are not copied into these junction tables.

**Conclusion:** the composite-key relations avoid partial dependencies; a single-column primary key cannot have a partial dependency on a proper subset of that key.

## 5. Third Normal Form (3NF)
A relation is in 3NF when it is in 2NF and non-key attributes do not depend transitively on a candidate key through another non-key attribute. Formally, for each non-trivial FD `X → A`, X is a superkey or A is prime.

Examples:
- **Companies and job roles:** `role_id → company_id`; `company_id → company name, sector, min_cgpa`. Company details are stored once in `companies`, not copied into every role.
- **Skills and questions:** `question_id → skill_id`; `skill_id → skill_name, category`. Skill details remain in `skills`, not repeated in `questions`.
- **Applications:** an application stores `role_id`, not duplicated job title and company details. Those are retrieved by joining `job_roles` and `companies`.
- **Placements:** a placement stores `application_id` and offer/joining dates. Student and role details are obtained through the application relationship.

**Conclusion:** entity-specific facts are kept in their own relations, avoiding the main transitive duplication that would result from storing company, skill or role descriptions repeatedly in child tables.

## 6. Why junction tables are necessary

| M:N relationship | Junction table | Relationship-specific attribute |
|---|---|---|
| Student ↔ Skill | `student_skills` | `proficiency` |
| Job role ↔ Skill | `role_skills` | `min_level` |
| Interview ↔ Question | `interview_questions` | `seq_no` |

These attributes describe the relationship itself, not either entity independently.

## 7. Controlled denormalization caveats

### `results`
`total_score`, `max_score` and `percentage` are summaries calculated from assigned questions and answers. `percentage` is mathematically derived from total and maximum marks. Storing these values is a deliberate reporting/cache decision; they must be recalculated when answers change. The supplied evaluation procedure performs this recalculation.

### `skill_scores`
`score` and `max_score` are derived by grouping answers by the skill attached to each question. They are stored for convenient analytics and must be refreshed after relevant answer or question changes. The supplied skill-score procedure refreshes them.

These are controlled denormalized summaries. For strict normalization, they could instead be calculated at query time using views and not stored.

### `answers`
The table uses surrogate primary key `answer_id` and UNIQUE `(interview_id, question_id)`. This enforces at most one answer per assigned question in an interview.

## 8. Viva summary
- **1NF:** no repeating groups or lists in a cell; each student-skill and interview-question mapping is a separate row.
- **2NF:** in composite-key tables, non-key attributes depend on the complete key. Example: proficiency depends on both student and skill.
- **3NF:** entity-specific facts remain with their entity. Example: company details are in `companies`, not repeated in each job role.
- **Junction tables:** resolve M:N relationships and store relationship-specific attributes.
- **Controlled denormalization:** `results` and `skill_scores` store calculated summaries for reporting; procedures refresh them.
