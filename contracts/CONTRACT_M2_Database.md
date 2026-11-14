# Contract: Member 2 - Database and Backend

Project: PrepForge (Java, JavaFX, MySQL) | Package: `prepforge` | Version 1.0

## 1. Your role

You own the MySQL schema, every SQL script, the JDBC connection and the DAO classes. The schema is the contract for the whole team, so it is your first and most important deliverable. You own the DBMS marks.

## 2. Files you must create

Paths are relative to `src/main/java/prepforge/` unless they start with `src/`, `database/` or are marked project root. Every folder has a `FILES.md` listing the files that belong in it.

| File | Purpose |
|---|---|
| `database/schema.sql` | Tables, constraints, indexes, sample data |
| `database/advanced.sql` | Views, function, procedures, triggers, demo queries, transaction demo |
| `database/er-diagram.png` | ER diagram |
| `util/DBConnection.java` | Reads `db.properties`, returns a JDBC connection |
| `dao/UserDao.java` | students, admins, skills, student_skills |
| `dao/QuestionDao.java` | questions |
| `dao/InterviewDao.java` | interviews, interview_questions, answers, results, skill_scores, recommendations |
| `dao/CompanyDao.java` | companies, job_roles, role_skills, applications, placements |
| `src/main/resources/db.properties.example` | Template (url, user, password). The real `db.properties` is gitignored |
| `src/test/java/prepforge/DaoTest.java` | A working test for each DAO |

## 3. Inputs (what you receive)

| What | From | When |
|---|---|---|
| Fields of `Student`, `Skill`, `Company`, `JobRole` | M1 | End of Phase 0 |
| Fields of `Question`, `Interview`, `Result` | M3 | End of Phase 0 |
| Weak-skill threshold (50 percent) and score formula (below) | M3 | End of Phase 0 |
| Requests for extra finder methods, in writing | M1, M3 | Any time; you answer within one working day |

Score formula agreed with M3: the score for a skill is the average of (marks obtained / maximum marks x 100) over the answered questions tagged with that skill. A skill is weak when its average is below 50.

## 4. Deliverables (what you hand over)

| What | To | When |
|---|---|---|
| Frozen `schema.sql` | Everyone | End of Phase 0 |
| `DBConnection` and all DAO classes | M1, M3 | Start of Phase 1 |
| Views, triggers, sample data loaded | M3, M4 | Middle of Phase 1 |
| `advanced.sql` complete, ER diagram, normalization note (a short paragraph per table group explaining 1NF, 2NF, 3NF) | M4 for the README | End of Phase 1 |

## 5. Public methods (the contract)

```java
// every DAO, named per entity (shown for QuestionDao)
void save(Question q)
Question findById(int id)
List<Question> findAll()
void update(Question q)
void delete(int id)

// DBConnection
static Connection getConnection()

// InterviewDao: one transaction (answers + result + interview status), commit or rollback
void saveResultInTransaction(Interview interview, Result result)
```

Every DAO uses `PreparedStatement`. DAOs throw `DataAccessException`. Extra finders such as `QuestionDao.findByTypeAndDifficulty` are added on request.

Names the other members rely on:

| Type | Exact names |
|---|---|
| Views | `v_interview_history`, `v_skill_performance`, `v_weak_skills`, `v_platform_stats` |
| Function | `fn_grade(score)` |
| Procedure | `sp_student_report(student_id)` |
| Triggers | after insert on `results` updates `skill_scores`; when a skill average drops below 50, insert into `recommendations` |

## 6. Requirements checklist

- [ ] ER diagram and relational schema
- [ ] Normalization to 3NF, with the written reasoning
- [ ] Primary keys and foreign keys (referential integrity)
- [ ] `UNIQUE`, `NOT NULL`, `CHECK` constraints
- [ ] One-to-one (`interviews` and `results`), one-to-many, many-to-many (`student_skills`, `role_skills`, `interview_questions`)
- [ ] Tables: `students`, `admins`, `skills`, `student_skills`, `companies`, `job_roles`, `role_skills`, `questions`, `interviews`, `interview_questions`, `answers`, `results`, `skill_scores`, `recommendations`, `applications`, `placements`
- [ ] DDL and DML
- [ ] Joins (inner and outer), nested queries, correlated subqueries
- [ ] Aggregates with `GROUP BY` and `HAVING`
- [ ] Views, indexes on join and filter columns
- [ ] Stored function and procedure in MySQL syntax (`DELIMITER`, not Oracle PL/SQL)
- [ ] Triggers
- [ ] Transactions with `COMMIT` and `ROLLBACK`
- [ ] Concurrency: InnoDB row locks, `SELECT ... FOR UPDATE`, isolation levels
- [ ] Sample data: at least 30 questions across topics, 5 companies, 10 job roles, 5 students
- [ ] Hosting: check whether Aiven's free MySQL tier suits the project (it needs SSL). If not, use local MySQL and tell the team the same day

## 7. Done when

Both SQL scripts run cleanly in order on a fresh database. Each DAO has a working test. The app connects using only `db.properties`. All four members have signed off the schema and ER diagram.

## Ground rules (identical in all four contracts)

1. **Layers:** ui -> service -> dao -> database. UI classes contain no SQL. DAO classes contain no JavaFX.
2. **Freeze:** `database/schema.sql` and every method name in the four contracts are fixed at the end of Phase 0. Any change needs all four members to agree.
3. **Stubs first:** in Phase 0 push your classes with the correct method names and dummy return values, so everyone can build against them.
4. **Naming:** Java classes PascalCase, methods camelCase. SQL tables snake_case and plural, keys named `<entity>_id`.
5. **Errors:** `PrepForgeException` is the base, `ValidationException` covers bad input, wrong login and duplicate email, `DataAccessException` covers database failures. Any method may throw `PrepForgeException`. UI classes catch it and show an alert.
6. **Git:** branches `main`, `develop` and one per member (`m1`..`m4`). Merge into `develop` by pull request, reviewed by one other member. `db.properties` is never committed.
7. **Phases:** 0 Foundation (schema, stubs) -> 1 Build your own module -> 2 Integration -> 3 Testing, documentation, presentation.
8. **Blockers:** raise them with the other member within one working day.

## Sign-off

| Name | Signature | Date |
|---|---|---|
| | | |
