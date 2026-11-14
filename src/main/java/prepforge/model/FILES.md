# Files needed in model/

Create these files here. Delete this note once the folder is complete if you like.

| File | Owner | Purpose |
|---|---|---|
| `User.java` | M1 | Abstract base class for Student and Admin |
| `Student.java` | M1 | Extends User: cgpa, branch, skill levels |
| `Admin.java` | M1 | Extends User |
| `Skill.java` | M1 | id, name, category |
| `Company.java` | M1 | Holds a list of JobRole |
| `JobRole.java` | M1 | Title, minimum CGPA, required skills |
| `class-diagram.png` | M1 | Java class diagram for the whole project |
| `Question.java` | M3 | Abstract base class for all question types |
| `MCQQuestion.java` | M3 | Options and correct option |
| `TechnicalQuestion.java` | M3 | Expected keywords |
| `CodingQuestion.java` | M3 | Problem statement and expected keywords |
| `HRQuestion.java` | M3 | Expected keywords |
| `Interview.java` | M3 | Abstract base class for all interview types |
| `TechnicalInterview.java` | M3 | Technical questions only |
| `HRInterview.java` | M3 | HR questions only |
| `MockInterview.java` | M3 | Mixed interview |
| `Result.java` | M3 | Score, percentage, grade |

Full details for each owner:

- M1: `contracts/CONTRACT_M1_OOP_and_Users.md`
- M3: `contracts/CONTRACT_M3_Interviews_and_Analytics.md`
