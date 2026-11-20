# PrepForge — Intelligent Placement Preparation & Mock Interview System

PrepForge is a comprehensive enterprise campus placement readiness and simulated assessment platform built with **Java**, **JavaFX**, and **MySQL**. It provides adaptive mock interviews, cryptographic user authentication, real-time company eligibility matching, and automated weak-skill analytics.

---

## 1. Architectural Overview: 4-Tier Enterprise Architecture

PrepForge strictly adheres to a clean 4-tier layered architecture:

$$\text{Presentation Layer (JavaFX UI)} \longrightarrow \text{Service / Business Layer} \longrightarrow \text{Data Access Layer (DAO)} \longrightarrow \text{Persistence Layer (MySQL)}$$

### Ground Architectural Rules
1. **Separation of Concerns:**
   - **UI Layer (`prepforge.ui`):** Written purely in Java (no FXML). Contains zero SQL and zero direct database calls. All background operations run inside JavaFX `Task<T>` workers to ensure the application thread never freezes.
   - **Service Layer (`prepforge.service`):** Contains all domain business rules, password hashing, and placement eligibility algorithms. Interacts exclusively with DAO interfaces.
   - **DAO Layer (`prepforge.dao`):** Encapsulates persistence logic. Catches low-level SQL exceptions and re-throws checked `DataAccessException` to isolate higher layers.
   - **Database Layer (`database/`):** 15-table relational MySQL schema in Third Normal Form (3NF), backed by stored procedures, functions, triggers, and analytical views.

2. **Polymorphism & OOP marks:**
   - Abstract `User` base class with concrete polymorphic subclasses: `Student` and `Admin`.
   - Abstract `Question` base class with subclasses: `MCQQuestion`, `TechnicalQuestion`, `CodingQuestion`, and `HRQuestion`.
   - Abstract `Interview` base class with subclasses: `TechnicalInterview`, `HRInterview`, and `MockInterview`.
   - Strategy pattern via `Evaluator` interface implemented by `MCQEvaluator` and `KeywordEvaluator`.

3. **Cryptographic Security:**
   - Passwords are never stored in plain text.
   - Secure hashing implemented via `PBKDF2WithHmacSHA256` with 10,000 iterations and 16-byte cryptographically secure random salts.

---

## 2. Work Distribution & Contracts

| Contract | Owner / Role | Primary Responsibilities | Status |
|---|---|---|---|
| **Member 1** | OOP & User Management | `User`, `Student`, `Admin`, `Skill`, `Company`, `JobRole`, `UserService`, `PlacementService`, `Validator`, `PasswordHasher`, and Exception hierarchy | **100% Completed** |
| **Member 2** | Database & Backend (DBMS) | Relational schema (`schema.sql`), views, stored procedures, triggers, 3NF normalization doc, JDBC integration | **Completed & Scripted** |
| **Member 3** | Mock Interviews & Analytics | `Question` hierarchy, `Interview` sessions, `Evaluator` strategies, `InterviewService`, and `AnalyticsService` | **Contract Stubs & Logic Ready** |
| **Member 4** | UI & Integration (Lead) | Project build (`pom.xml`, `.gitignore`), JavaFX desktop application (`Main`, `LoginScreen`, `StudentScreen`, `InterviewScreen`, `AdminScreen`), `ConcurrencyTest`, integration builds | **100% Completed** |

---

## 3. Database Normalization Note (1NF, 2NF, 3NF)

The relational schema is normalized to **Third Normal Form (3NF)** to eliminate insert, update, and delete anomalies:

* **First Normal Form (1NF):**
  - All attributes contain atomic, scalar values. Repeating groups such as a student's multiple skills or an interview's multiple questions are resolved into separate rows within dedicated junction tables (`student_skills`, `interview_questions`, `role_skills`).
* **Second Normal Form (2NF):**
  - The database is in 1NF and contains no partial functional dependencies. In composite-key tables like `student_skills (student_id, skill_id)`, the non-key attribute `proficiency` depends on the **entire** composite candidate key, not a single constituent.
* **Third Normal Form (3NF):**
  - The database is in 2NF and contains no transitive dependencies ($X \to Y$ and $Y \to Z$). Company details reside in `companies`, referenced by `job_roles` via foreign key `company_id`. Student applications reference `role_id` rather than redundantly copying job titles or company names.
* **Controlled Denormalization:**
  - `results.total_score` and `results.percentage` are precomputed evaluation summaries to accelerate historical lookups. MySQL triggers and stored procedures (`02_Create_Interview_Evaluation_Procedure.sql`) maintain consistency upon answer updates.

---

## 4. Setup, Build & Execution Instructions

### Prerequisites
* **Java Development Kit (JDK):** Version 17 or higher (tested on Java 17, 21, and 25).
* **Apache Maven:** Version 3.8+ or 3.9+.
* **MySQL Community Server:** Version 8.0.16+ (Optional for in-memory mode; required for persistent DB).

### One-Command Maven Build
To compile the complete application and run the automated test suite:
```bash
mvn clean test
```

### Launch the JavaFX Application
To launch the graphical desktop application:
```bash
mvn javafx:run
```

### Standalone Execution (Zero Dependencies / Offline Mode)
If Maven is unavailable, the application and tests can be compiled and run with standard Java:
```powershell
# Compile all source files
javac -d bin (Get-ChildItem -Recurse src/main/java -Filter *.java | ForEach-Object { $_.FullName }) src/test/java/prepforge/TestRunner.java src/test/java/prepforge/ConcurrencyTest.java

# Run the Automated Test Suite (23 Tests)
java -cp bin prepforge.TestRunner

# Run the Multi-Threaded Concurrency Test (10 Concurrent Threads)
java -cp bin prepforge.ConcurrencyTest
```

---

## 5. Automated Test Suite Results

### Unit Test Execution (`TestRunner.java`)
```
==================================================
       PREPFORGE AUTOMATED TEST SUITE             
==================================================

--- [Suite 1: Validator Tests] ---
  [PASS] Valid email accepted
  [PASS] Invalid email without @ rejected
  [PASS] Invalid email without domain rejected
  [PASS] Null email rejected
  [PASS] Valid password accepted
  [PASS] Short password rejected
  [PASS] Valid CGPA accepted
  [PASS] Negative CGPA rejected
  [PASS] CGPA > 10.0 rejected

--- [Suite 2: PasswordHasher (PBKDF2) Tests] ---
  [PASS] Hash string format contains salt separator ':'
  [PASS] Unique salts produce different hashes for identical passwords
  [PASS] Password verification succeeds for correct password
  [PASS] Password verification fails for incorrect password

--- [Suite 3: UserService Tests] ---
  [PASS] Student registered and assigned ID
  [PASS] Polymorphic login returned non-null user
  [PASS] User role is STUDENT
  [PASS] Wrong password throws ValidationException
  [PASS] Skill 'Java' added to student
  [PASS] Admin logged in with role ADMIN

--- [Suite 4: PlacementService Tests] ---
  [PASS] Qualified student matched to eligible job roles
  [PASS] Student successfully applied to role
  [PASS] Duplicate application blocked by ValidationException
  [PASS] Ineligible student application blocked

==================================================
TEST SUMMARY
  Passed: 23 | Failed: 0 | Total: 23
  Status: ALL TESTS PASSED SUCCESSFULLY! [OK]
==================================================
```

### Multi-Threaded Concurrency Test Execution (`ConcurrencyTest.java`)
```
ConcurrencyTest: Successfully executed 10 concurrent interview submissions in 118 ms with 100% data integrity.
[PASS] ConcurrencyTest completed successfully! (Zero race conditions, atomic evaluations).
```

---

## 6. Viva & Defense Checklist

* [x] **Polymorphic UI Routing:** `LoginScreen` uses `user.getRole()` to dynamically switch between `StudentScreen` and `AdminScreen`.
* [x] **Non-Blocking UI:** Long-running database operations and PBKDF2 hashing execute inside `javafx.concurrent.Task` with progress indicators.
* [x] **Student Dashboard Tabs:** Profile & Skills, Start Mock Interview, History, Weak-Skill Recommendations, and Recruiting Companies with real-time eligibility matching.
* [x] **Admin Console Tabs:** Student Directory Management, Question Bank CRUD & Filtering, Company Registration & Job Posting, and Institutional Platform Analytics.
* [x] **Concurrency & Isolation:** Multi-threaded stress testing confirms thread safety and transaction atomicity under simultaneous candidate submissions.
