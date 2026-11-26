# PrepForge — Placement Preparation Platform

PrepForge is a campus placement readiness and assessment platform built with Java, JavaFX, and MySQL. It provides comprehensive student profile and skill management, corporate placement drive tracking, automated eligibility matching, curated mock interview assessments with multi-format automated evaluation, skill gap diagnostics, and placement administration.

## 1. Architectural Overview

PrepForge strictly adheres to a clean layered architecture:

Presentation Layer (JavaFX UI) -> Service / Business Layer -> Data Access Layer (DAO) -> Persistence Layer (MySQL)

### Architectural Rules
1. Separation of Concerns:
   - UI Layer (`prepforge.ui`): Written purely in Java without FXML. Contains no SQL queries. Database and service operations execute within JavaFX `Task<T>` workers to keep the UI thread responsive.
   - Service Layer (`prepforge.service`): Contains business logic, input validation, password hashing, placement eligibility checks, interview assessment engines, and diagnostic analytics. Interacts with persistence exclusively through DAO interfaces.
   - DAO Layer (`prepforge.dao`): Encapsulates MySQL persistence and translates low-level SQL exceptions into checked `DataAccessException` to isolate domain and UI layers.
   - Database Layer (`src/main/resources/db/`): MySQL schema, SQL seed data, and transactional persistence for users, placement records, questions, interviews, answers, and results.

2. Object-Oriented Principles:
   - Encapsulation: Private fields with public accessors and mutators across all domain entities.
   - Inheritance and Abstraction: Abstract base classes `User`, `Question`, and `Interview` extended by polymorphic subclasses (`Student`, `Admin`, `MCQQuestion`, `TechnicalQuestion`, `CodingQuestion`, `HRQuestion`, `TechnicalInterview`, `HRInterview`, `MockInterview`).
   - Method Overriding: Concrete implementations of `getRole()`, `getType()`, and `buildQuestionSet()`.
   - Method Overloading: Multiple constructors across domain entities and overloaded search and evaluation routines.
   - Polymorphism: Base interfaces and abstract classes dynamically handling candidate sessions, question pools, and automated evaluation strategies.
   - Aggregation and Association: `Company` aggregates `JobRole` instances; `JobRole` associates with hiring `Company` and aggregates required `Skill` sets; `Student` maps acquired `Skill` instances to proficiency levels; `Interview` aggregates selected `Question` instances and persists scored `Result` instances.

3. Cryptographic Security:
   - Passwords are never stored in plaintext.
   - Cryptographic hashing implemented using `PBKDF2WithHmacSHA256` with 10,000 iterations and 16-byte cryptographically secure random salts.

## 2. Team Work Distribution

| Module | Owner / Role | Primary Responsibilities | Status |
|---|---|---|---|
| Member 1 | OOP & User Management | `User`, `Student`, `Admin`, `Skill`, `Company`, `JobRole`, `ApplicationRecord`, `UserService`, `PlacementService`, `Validator`, `PasswordHasher`, and Exception hierarchy | Complete |
| Member 2 | Database & DAOs | Relational schema, MySQL connection setup, SQL seed data, and JDBC implementations of `UserDao`, `CompanyDao`, `QuestionDao`, and `InterviewDao` | Complete |
| Member 3 | Interviews & Analytics | `Interview`, `TechnicalInterview`, `HRInterview`, `MockInterview`, `Question`, `MCQQuestion`, `TechnicalQuestion`, `CodingQuestion`, `HRQuestion`, `Result`, `Evaluator`, `MCQEvaluator`, `KeywordEvaluator`, `InterviewService`, `AnalyticsService` | Complete |
| Member 4 | UI & Integration | Maven build (`pom.xml`, `.gitignore`), JavaFX desktop application (`Main`, `LoginScreen`, `StudentScreen` [5 tabs], `AdminScreen` [5 tabs], `InterviewScreen`), system integration | Complete |

## 3. Database Normalization (1NF, 2NF, 3NF)

The database schema is normalized to Third Normal Form (3NF) to eliminate data redundancy and prevent insertion, update, and deletion anomalies:

1. First Normal Form (1NF):
   - Every column contains atomic scalar values.
   - Repeating groups are eliminated; student skill associations, interview questions, and role requirements reside in dedicated junction tables (`student_skills`, `role_skills`, `interview_questions`).

2. Second Normal Form (2NF):
   - The relations meet 1NF criteria and have no partial functional dependencies on composite keys.
   - In composite-key tables such as `student_skills(student_id, skill_id)`, the non-prime attribute `proficiency` depends on the complete composite key `(student_id, skill_id)`.

3. Third Normal Form (3NF):
   - The relations meet 2NF criteria and contain no transitive dependencies.
   - Corporate details reside in `companies` and are referenced by `job_roles` via foreign key `company_id`. Student applications reference `role_id` and `student_id` without duplicating entity attributes.

## 4. Data Sources & External Dataset Attribution

PrepForge stores all live application data in the local MySQL database `prepforge`; CSV and serialized files are not used for ongoing storage. On the first launch, the application creates the database tables and imports the sample records from `src/main/resources/db/seed.sql`. If a previous `prepforge_state.dat` exists, the application migrates its records into MySQL and removes that legacy file only after migration succeeds.

The 240 assessment questions in the SQL seed are collected from the following verified open-source datasets and public APIs:

| Question Category | Count | Source Name | Source Link | License |
|---|---|---|---|---|
| `MCQ` (Computer Science) | 45 | Open Trivia Database (OpenTDB) — Science: Computers (Category 18) | [OpenTDB](https://opentdb.com/) / [API Endpoint](https://opentdb.com/api.php?amount=15&category=18&type=multiple) | CC BY-SA 4.0 |
| `TECHNICAL` (Web, JS, React, Node, HTML, CSS) | 103 | 30 Seconds of Interviews (`30-seconds/30-seconds-of-interviews`) | [GitHub Repo](https://github.com/30-seconds/30-seconds-of-interviews) / [Raw JSON](https://raw.githubusercontent.com/30-seconds/30-seconds-of-interviews/master/data/questions.json) | CC0-1.0 |
| `CODING` (Company-Wise DSA & SQL) | 63 | LeetCode Company-Wise Problems (`liquidslr/leetcode-company-wise-problems`) | [GitHub Repo](https://github.com/liquidslr/leetcode-company-wise-problems) | Open Source |
| `HR` (Behavioral Interview Questions) | 29 | Tech Interview Handbook (`yangshun/tech-interview-handbook`) | [GitHub Repo](https://github.com/yangshun/tech-interview-handbook) / [Behavioral Questions](https://raw.githubusercontent.com/yangshun/tech-interview-handbook/main/apps/website/contents/behavioral-interview-questions.md) | MIT License |

## 5. Build & Execution Instructions

### Prerequisites
- Java Development Kit (JDK) 25 or higher
- Apache Maven 3.8+ or higher
- MySQL Community Server 8.0+

### Configure MySQL
The application creates the `prepforge` database and its tables on first launch. The configured MySQL account must be able to create databases. Copy the example configuration and set the password for your local MySQL account:

```powershell
Copy-Item src\main\resources\db.properties.example src\main\resources\db.properties
```

Edit `src/main/resources/db.properties`, or set `PREPFORGE_DB_URL`, `PREPFORGE_DB_USER`, and `PREPFORGE_DB_PASSWORD` environment variables. The defaults connect to `localhost:3306` as `root` with an empty password. Keep local credentials out of version control.

The startup migration does not drop existing tables. If an existing `prepforge` database uses a different schema, back it up and migrate or recreate it before starting the application.

### Compile Project
```bash
mvn compile
```

### Launch JavaFX Desktop Application
```bash
mvn javafx:run
```
