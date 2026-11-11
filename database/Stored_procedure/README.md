# PrepForge — Member 2 DBMS Additions

## Folder tree
```text
PrepForge_Member2_Additions/
├── README.md
├── 01_Normalization/
│   └── PrepForge_Normalization_Documentation.md
├── 02_Automatic_Interview_Evaluation/
│   └── 02_Create_Interview_Evaluation_Procedure.sql
├── 03_Skill_Wise_Score_Calculation/
│   └── 03_Create_Skill_Score_Procedure.sql
├── 04_Interview_Completion_Validation/
│   └── 04_Create_Interview_Completion_Validation.sql
└── 05_Answer_Update_Validation/
    └── 05_Create_Answer_Update_Validation_Trigger.sql
```

These are add-ons to `PrepForge_DBMS_Complete.sql`; they do not recreate the database or alter the original 15-table schema. Use MySQL 8.0.16+.

## Prerequisites and execution order
1. Run the original `PrepForge_DBMS_Complete.sql`.
2. Run `05_Answer_Update_Validation/05_Create_Answer_Update_Validation_Trigger.sql`.
3. Run `03_Skill_Wise_Score_Calculation/03_Create_Skill_Score_Procedure.sql`.
4. Run `02_Automatic_Interview_Evaluation/02_Create_Interview_Evaluation_Procedure.sql`.
5. Run `04_Interview_Completion_Validation/04_Create_Interview_Completion_Validation.sql`.

The normalization file is documentation, not executable SQL. The order matters because the final completion procedure calls the two calculation procedures.

## Main workflow
```sql
USE prepforge;

-- Check whether every assigned question has an answer and marks.
SELECT iq.interview_id, iq.question_id, q.max_marks, a.marks
FROM interview_questions iq
JOIN questions q ON q.question_id = iq.question_id
LEFT JOIN answers a
  ON a.interview_id = iq.interview_id
 AND a.question_id = iq.question_id
WHERE iq.interview_id = 1
ORDER BY iq.seq_no;

-- Calculate and save result + skill scores, then mark completed.
CALL sp_complete_interview(1);

SELECT * FROM results WHERE interview_id = 1;

SELECT ss.interview_id, sk.skill_name, ss.score, ss.max_score,
       ROUND(100 * ss.score / NULLIF(ss.max_score, 0), 2) AS percentage
FROM skill_scores ss
JOIN skills sk ON sk.skill_id = ss.skill_id
WHERE ss.interview_id = 1;
```

## Rules implemented
- Every assigned question must have an `answers` row with non-NULL `marks`.
- A blank `answer_text` is not independently rejected; empty text may be valid for some application workflows. The database uses the presence of the answer row and marks as the completion criterion.
- `sp_complete_interview` validates, calculates the overall result, calculates skill scores, and only then changes status to `COMPLETED` in one transaction.
- A `BEFORE UPDATE` trigger also rejects direct status changes to `COMPLETED` when questions are missing answers or marks.
- The denominator is the sum of `questions.max_marks` for questions assigned to that interview; it does not assume all questions have equal marks.
- `results` is inserted or updated using its UNIQUE `interview_id`.
- Skill scores are grouped by `questions.skill_id`. Existing values are refreshed and obsolete skill rows for that interview are deleted.
- The procedures calculate from `answers.marks`; they do not grade free-text answers using AI. Java or a human evaluator must assign marks first.
- If answers are edited after evaluation, run the evaluation and skill-score procedures again (preferably inside a transaction) to refresh the stored summaries.

## Test with supplied sample data
Interview 1 is fully answered in the original sample data:
```sql
CALL sp_complete_interview(1);
```
Expected overall result: 31 / 40 = 77.50%. Expected skill scores: Java 8/10, SQL 9/10, Data Structures 7/10, DBMS 7/10.

Interview 5 is incomplete and should fail:
```sql
CALL sp_complete_interview(5);
```
It should remain `IN_PROGRESS`. A direct update should also fail:
```sql
UPDATE interviews SET status = 'COMPLETED'
WHERE interview_id = 5;
```

## Re-running files
Each SQL file drops and recreates only the specific trigger/procedure it owns. None of these files drops tables or erases sample records.
