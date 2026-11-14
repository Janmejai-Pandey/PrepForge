# Contract: Member 3 - Mock Interviews and Analytics

Project: PrepForge (Java, JavaFX, MySQL) | Package: `prepforge` | Version 1.0

## 1. Your role

You own the question bank, the interview types, answer evaluation, score calculation, history, weak-skill detection and recommendations.

## 2. Files you must create

Paths are relative to `src/main/java/prepforge/` unless they start with `src/`, `database/` or are marked project root. Every folder has a `FILES.md` listing the files that belong in it.

| File | Purpose |
|---|---|
| `model/Question.java` | Abstract: id, text, topic, difficulty, marks, abstract `getMaxMarks()` and `display()` |
| `model/MCQQuestion.java` | Options and the correct option |
| `model/TechnicalQuestion.java` | Expected keywords |
| `model/CodingQuestion.java` | Problem statement and expected keywords |
| `model/HRQuestion.java` | Expected keywords |
| `model/Interview.java` | Abstract: student, difficulty, questions, answers; abstract method that builds the question set |
| `model/TechnicalInterview.java` | Technical questions only |
| `model/HRInterview.java` | HR questions only |
| `model/MockInterview.java` | Mixed (technical, HR, coding) |
| `model/Result.java` | Total score, percentage, grade, interview reference |
| `evaluator/Evaluator.java` | Interface |
| `evaluator/MCQEvaluator.java` | Exact match on the chosen option |
| `evaluator/KeywordEvaluator.java` | Keyword coverage, used for technical, HR and coding answers |
| `service/InterviewService.java` | Start, answer, submit, history, question management |
| `service/AnalyticsService.java` | Skill scores, weak skills, recommendations, platform statistics |
| `src/test/java/prepforge/EvaluatorTest.java` | Tests for both evaluators |
| `src/test/java/prepforge/InterviewServiceTest.java` | Start, submit and failed-submit tests |

Difficulty is a string: `EASY`, `MEDIUM` or `HARD`. Interview type is a string: `HR`, `TECHNICAL` or `MIXED`.

## 3. Inputs (what you receive)

| What | From | When |
|---|---|---|
| `Student` and `Skill` classes | M1 | End of Phase 0 |
| Frozen `database/schema.sql` | M2 | End of Phase 0 |
| `QuestionDao`, `InterviewDao` (including `saveResultInTransaction`) | M2 | Start of Phase 1 |
| Views `v_skill_performance`, `v_weak_skills`, `v_platform_stats` and the trigger-filled tables | M2 | Middle of Phase 1 |
| Sample questions loaded in the database | M2 | Middle of Phase 1 |

## 4. Deliverables (what you hand over)

| What | To | When |
|---|---|---|
| Fields of `Question`, `Interview`, `Result`; weak threshold 50 percent and the score formula (see M2 contract) | M2 | End of Phase 0 |
| `InterviewService` and `AnalyticsService` stubs with final method names | M4 | End of Phase 0 |
| Working `InterviewService` and `AnalyticsService` | M4 | Middle of Phase 1 |
| Both evaluators with tests | M4 | End of Phase 1 |

## 5. Public methods (the contract)

```java
// Evaluator
double evaluate(Question question, String answer)     // marks awarded

// InterviewService
Interview startInterview(int studentId, String type, String difficulty, int numQuestions)
void submitAnswer(Interview interview, int questionId, String answer)
Result submitInterview(Interview interview)           // one DB transaction
List<Result> getHistory(int studentId)
void addQuestion(Question q)                          // admin
void updateQuestion(Question q)                       // admin
void deleteQuestion(int questionId)                   // admin
List<Question> searchQuestions(String topic, String difficulty, String type)

// AnalyticsService
Map<String, Double> getSkillScores(int studentId)
List<String> getWeakSkills(int studentId)
List<String> getRecommendations(int studentId)
Map<String, Integer> getPlatformStats()               // admin
```

Answers stay in the `Interview` object until `submitInterview`, which saves everything through M2's `InterviewDao.saveResultInTransaction`.

## 6. Requirements checklist

- [ ] Inheritance and abstraction: `Question` and `Interview` hierarchies
- [ ] Overriding: `getMaxMarks()`, `display()`, and the question-building method in each interview type
- [ ] Interfaces: `Evaluator`
- [ ] Runtime polymorphism: the evaluator is chosen from the question type
- [ ] Interview types: HR, technical, coding (inside mixed), mixed
- [ ] Difficulty levels
- [ ] Question search by topic, difficulty and type
- [ ] Automatic score, percentage and grade
- [ ] Interview history and skill-wise performance
- [ ] Weak skills (average below 50) and recommendations
- [ ] Collections: `List`, `Map`

## 7. Done when

A student can take each interview type and get a correct score. A deliberately failed submission leaves no partial data in the database. A poor result produces weak skills and recommendations. Both evaluators have unit tests.

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
