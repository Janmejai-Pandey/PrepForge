# Contract: Member 4 - UI and Integration

Project: PrepForge (Java, JavaFX, MySQL) | Package: `prepforge` | Version 1.0

## 1. Your role

You own the JavaFX screens, the build setup, the integration of everyone's work, concurrency testing and the final presentation. You are never blocked, because you build against the Phase 0 stubs.

## 2. Files you must create

Paths are relative to `src/main/java/prepforge/` unless they start with `src/`, `database/` or are marked project root. Every folder has a `FILES.md` listing the files that belong in it.

| File | Purpose |
|---|---|
| `README.md` (project root) | Setup steps, architecture overview, normalization note from M2, test results |
| `pom.xml` (project root) | Maven build with JavaFX and the MySQL connector; runs with one command |
| `.gitignore` (project root) | Ignore `db.properties`, build output, IDE folders |
| `Main.java` | JavaFX entry point. Keeps the logged-in user and switches between screens |
| `ui/LoginScreen.java` | Login and registration |
| `ui/StudentScreen.java` | Tabs: profile and skills, start interview, history, recommendations, companies with eligibility |
| `ui/InterviewScreen.java` | One question at a time, submit, show the score |
| `ui/AdminScreen.java` | Tabs: students, questions, companies and job roles, statistics |
| `src/test/java/prepforge/ConcurrencyTest.java` | Several threads submitting interviews at once |

Screens are written in Java code. There is no FXML.

## 3. Inputs (what you receive)

| What | From | When |
|---|---|---|
| `UserService` and `PlacementService` stubs | M1 | End of Phase 0 |
| `InterviewService` and `AnalyticsService` stubs | M3 | End of Phase 0 |
| Frozen `database/schema.sql` | M2 | End of Phase 0 |
| Working services | M1, M3 | Middle of Phase 1 |
| Sample data, views and triggers | M2 | Middle of Phase 1 |
| Normalization note and ER diagram | M2 | End of Phase 1 |

## 4. Deliverables (what you hand over)

| What | To | When |
|---|---|---|
| `pom.xml` and `.gitignore` | Everyone | Day 1 of Phase 0 |
| Login screen working against stubs | Team | Middle of Phase 1 |
| Weekly integration build from `develop` | Team | Every week from Phase 1 |
| Complete application, `ConcurrencyTest` passing, README, test results | Team | End of Phase 2 |
| Final presentation, rehearsed with all four members | Examiner | Phase 3 |

## 5. Methods you call (never change them yourself)

```java
// UserService (M1)
User login(String email, String password)
void registerStudent(Student s, String password)
void registerAdmin(Admin a, String password)
Student getProfile(int studentId)
void updateProfile(Student s)
void addSkill(int studentId, int skillId, String level)
void removeSkill(int studentId, int skillId)
List<Skill> getAllSkills()
List<Student> getAllStudents()
void deleteStudent(int studentId)

// PlacementService (M1)
List<Company> getAllCompanies()
List<JobRole> getEligibleRoles(int studentId)
void applyToRole(int studentId, int roleId)
void saveCompany(Company c)
void saveJobRole(JobRole r)

// InterviewService (M3)
Interview startInterview(int studentId, String type, String difficulty, int numQuestions)
void submitAnswer(Interview interview, int questionId, String answer)
Result submitInterview(Interview interview)
List<Result> getHistory(int studentId)
void addQuestion(Question q)
void updateQuestion(Question q)
void deleteQuestion(int questionId)
List<Question> searchQuestions(String topic, String difficulty, String type)

// AnalyticsService (M3)
Map<String, Double> getSkillScores(int studentId)
List<String> getWeakSkills(int studentId)
List<String> getRecommendations(int studentId)
Map<String, Integer> getPlatformStats()
```

If you need a different method, ask the owner in writing. Do not add SQL or business logic to the UI.

## 6. Requirements checklist

- [ ] Working JavaFX desktop application started with one Maven command
- [ ] Login and registration route to the student or admin screen by role
- [ ] Student screen with all five tabs
- [ ] Interview screen: question by question, then score
- [ ] Admin screen with all four tabs
- [ ] Every screen validates input and shows an alert for each `PrepForgeException`
- [ ] Database calls run in a JavaFX `Task`, so the window never freezes
- [ ] `ConcurrencyTest`: several threads submitting at once, showing InnoDB row locking and the isolation level chosen with M2
- [ ] README with setup steps, architecture and test results
- [ ] Presentation prepared

## 7. Done when

From a clean clone the full flow works: register, take an interview, see the score, weak skills and recommendations; an admin adds a question and sees statistics. The concurrency test passes. All four members have rehearsed the demo.

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
