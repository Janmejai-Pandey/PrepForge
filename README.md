# PrepForge — Placement Preparation Platform

PrepForge is a campus placement readiness and assessment platform built with Java, JavaFX, and MySQL. It provides comprehensive student profile and skill management, corporate placement drive tracking, automated eligibility matching, curated mock interview assessments with multi-format automated evaluation, skill gap diagnostics, and placement administration.

## 1. Architectural Overview

PrepForge strictly adheres to a clean layered architecture:

Presentation Layer (JavaFX UI) -> Service / Business Layer -> Data Access Layer (DAO) -> Persistence Layer (MySQL)

### Architectural Rules
1. Separation of Concerns:
   - UI Layer (`prepforge.ui`): Written purely in Java without FXML. Contains no SQL queries. Database and service operations execute within JavaFX `Task<T>` workers to keep the UI thread responsive.
   - Service Layer (`prepforge.service`): Contains business logic, input validation, password hashing, placement eligibility checks, interview assessment engines, and diagnostic analytics. Interacts with persistence exclusively through DAO interfaces.
   - DAO Layer (`prepforge.dao`): Encapsulates data persistence. Thread-safe in-memory and database implementations translate low-level SQL exceptions into checked `DataAccessException` to isolate domain and UI layers.
   - Database Layer (`database/`): Relational MySQL schema in Third Normal Form (3NF), backed by views, stored procedures, functions, triggers, and transactional locking.

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

## 2. Work Distribution & Contracts

| Contract | Owner / Role | Primary Responsibilities | Status |
|---|---|---|---|
| Member 1 | OOP & User Management | `User`, `Student`, `Admin`, `Skill`, `Company`, `JobRole`, `UserService`, `PlacementService`, `Validator`, `PasswordHasher`, and Exception hierarchy | Complete |
| Member 2 | Database & DAOs | Relational schema (`schema.sql`), views, stored procedure, function, triggers (`advanced.sql`), ER diagram (`er-diagram.png`), `DBConnection`, `UserDao`, `CompanyDao`, `QuestionDao`, `InterviewDao`, `InMemoryQuestionDao`, `InMemoryInterviewDao` | Complete |
| Member 3 | Interviews & Analytics | `Interview`, `TechnicalInterview`, `HRInterview`, `MockInterview`, `Question`, `MCQQuestion`, `TechnicalQuestion`, `CodingQuestion`, `HRQuestion`, `Result`, `Evaluator`, `MCQEvaluator`, `KeywordEvaluator`, `InterviewService`, `AnalyticsService` | Complete |
| Member 4 | UI & Integration | Maven build (`pom.xml`, `.gitignore`), JavaFX desktop application (`Main`, `LoginScreen`, `StudentScreen` [5 tabs], `AdminScreen` [4 tabs], `InterviewScreen`), system integration | Complete |

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

## 4. Build & Execution Instructions

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- Apache Maven 3.8+ or higher
- MySQL Community Server 8.0+ (Optional; in-memory DAOs provide zero-dependency standalone execution)

### Compile Project
```bash
mvn compile
```

### Launch JavaFX Desktop Application
```bash
mvn javafx:run
```
