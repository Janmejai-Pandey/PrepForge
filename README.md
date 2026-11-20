# PrepForge — Placement Preparation Platform

PrepForge is a campus placement readiness platform built with Java, JavaFX, and MySQL. It provides student profile and skill management, corporate placement drive tracking, automated eligibility matching, and placement cell administration.

## 1. Architectural Overview

PrepForge strictly adheres to a clean layered architecture:

Presentation Layer (JavaFX UI) -> Service / Business Layer -> Data Access Layer (DAO) -> Persistence Layer (MySQL)

### Architectural Rules
1. Separation of Concerns:
   - UI Layer (`prepforge.ui`): Written purely in Java without FXML. Contains no SQL queries. Database and service operations execute within JavaFX `Task<T>` workers to keep the UI thread responsive.
   - Service Layer (`prepforge.service`): Contains business logic, input validation, password hashing, and placement eligibility checks. Interacts with persistence exclusively through DAO interfaces.
   - DAO Layer (`prepforge.dao`): Encapsulates data persistence. Converts low-level SQL exceptions into checked `DataAccessException` to isolate domain and UI layers.
   - Database Layer (`database/`): Relational MySQL schema in Third Normal Form (3NF), backed by views, stored procedures, functions, triggers, and transactional locking.

2. Object-Oriented Principles:
   - Encapsulation: Private fields with public accessors and mutators across all domain entities.
   - Inheritance and Abstraction: Abstract base class `User` extended by concrete subclasses `Student` and `Admin`.
   - Method Overriding: Concrete implementations of `getRole()` and `showMenu()`.
   - Method Overloading: Multiple constructors across domain entities and overloaded profile update routines.
   - Polymorphism: Base `User` references dynamically handling `Student` and `Admin` instances during authentication and session routing.
   - Aggregation and Association: `Company` aggregates `JobRole` instances; `JobRole` associates with hiring `Company` and aggregates required `Skill` sets; `Student` maps acquired `Skill` instances to proficiency levels.

3. Cryptographic Security:
   - Passwords are never stored in plaintext.
   - Cryptographic hashing implemented using `PBKDF2WithHmacSHA256` with 10,000 iterations and 16-byte cryptographically secure random salts.

## 2. Work Distribution & Contracts

| Contract | Owner / Role | Primary Responsibilities | Status |
|---|---|---|---|
| Member 1 | OOP & User Management | `User`, `Student`, `Admin`, `Skill`, `Company`, `JobRole`, `UserService`, `PlacementService`, `Validator`, `PasswordHasher`, and Exception hierarchy | Complete |
| Member 2 | Database & Backend | Relational schema (`schema.sql`), views, stored procedure, function, triggers (`advanced.sql`), ER diagram (`er-diagram.png`), `DBConnection`, `UserDao`, `CompanyDao` | Complete |
| Member 4 | UI & Integration | Maven configuration (`pom.xml`, `.gitignore`), JavaFX desktop application (`Main`, `LoginScreen`, `StudentScreen`, `AdminScreen`), unit test runner (`TestRunner`), project documentation | Complete |

## 3. Database Normalization (1NF, 2NF, 3NF)

The database schema is normalized to Third Normal Form (3NF) to eliminate data redundancy and prevent insertion, update, and deletion anomalies:

1. First Normal Form (1NF):
   - Every column contains atomic scalar values.
   - Repeating groups are eliminated; student skill associations and role requirements reside in dedicated junction tables (`student_skills`, `role_skills`).

2. Second Normal Form (2NF):
   - The relations meet 1NF criteria and have no partial functional dependencies on composite keys.
   - In composite-key tables such as `student_skills(student_id, skill_id)`, the non-prime attribute `proficiency` depends on the complete composite key `(student_id, skill_id)`.

3. Third Normal Form (3NF):
   - The relations meet 2NF criteria and contain no transitive dependencies ($X \to Y$ and $Y \to Z$).
   - Corporate details reside in `companies` and are referenced by `job_roles` via foreign key `company_id`. Student applications reference `role_id` and `student_id` without duplicating entity attributes.

## 4. Build & Execution Instructions

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- Apache Maven 3.8+ or higher
- MySQL Community Server 8.0+ (Optional; in-memory DAOs provide zero-dependency standalone execution)

### Compile and Run Tests via Maven
```bash
mvn clean test
```

### Launch JavaFX Desktop Application
```bash
mvn javafx:run
```

### Standalone Compilation and Execution
```powershell
javac -d bin (Get-ChildItem -Recurse src/main/java -Filter *.java | ForEach-Object { $_.FullName }) src/test/java/prepforge/TestRunner.java
java -cp bin prepforge.TestRunner
```
