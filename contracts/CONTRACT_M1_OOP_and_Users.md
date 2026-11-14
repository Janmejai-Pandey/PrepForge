# Contract: Member 1 - OOP and User Management

Project: PrepForge (Java, JavaFX, MySQL) | Package: `prepforge` | Version 1.0

## 1. Your role

You own the core domain classes for users and placements, registration and login, student profiles and skills, input validation, and the exception classes. You are the main owner of the Java OOP marks.

## 2. Files you must create

Paths are relative to `src/main/java/prepforge/` unless they start with `src/`, `database/` or are marked project root. Every folder has a `FILES.md` listing the files that belong in it.

| File | Purpose |
|---|---|
| `exception/PrepForgeException.java` | Checked base exception for the whole project |
| `exception/ValidationException.java` | Bad input, wrong login, duplicate email |
| `exception/DataAccessException.java` | Database failures (thrown by DAOs) |
| `model/User.java` | Abstract base: id, name, email, passwordHash, abstract `getRole()` and `showMenu()` |
| `model/Student.java` | Extends User: cgpa, branch, `Map<Skill, String>` of skill levels, overloaded `updateProfile(...)` |
| `model/Admin.java` | Extends User: overrides `getRole()` and `showMenu()` |
| `model/Skill.java` | id, name, category |
| `model/Company.java` | id, name, description, `List<JobRole>` (aggregation) |
| `model/JobRole.java` | id, title, minCgpa, `List<Skill>` required skills |
| `service/UserService.java` | Registration, login, profile, skills, student management |
| `service/PlacementService.java` | Companies, job roles, eligibility, applications |
| `util/Validator.java` | Email format, password length, CGPA 0 to 10, non-empty names |
| `util/PasswordHasher.java` | Hash and check passwords (never store plain text) |
| `src/test/java/prepforge/ValidatorTest.java` | Unit tests for every validation rule |
| `src/test/java/prepforge/UserServiceTest.java` | Register and login tests |
| `model/class-diagram.png` | Java class diagram for the whole project |

## 3. Inputs (what you receive)

| What | From | When |
|---|---|---|
| Frozen `database/schema.sql` (column names for students, skills, companies, job roles) | M2 | End of Phase 0 |
| `UserDao` and `CompanyDao` classes | M2 | Start of Phase 1 |
| Sample data loaded in the database for testing | M2 | Middle of Phase 1 |
| Question, interview and result class names (for the class diagram) | M3 | End of Phase 0 |

## 4. Deliverables (what you hand over)

| What | To | When |
|---|---|---|
| The three exception classes (real, not stubs) | Everyone | Day 1 of Phase 0 |
| Model classes with fields and getters (`Student`, `Skill`, `Company`, `JobRole`) | M2, M3, M4 | End of Phase 0 |
| `UserService` and `PlacementService` stubs with final method names | M4 | End of Phase 0 |
| Working `UserService` and `PlacementService` | M4 | Middle of Phase 1 |
| Unit tests and class diagram | Whole team | End of Phase 1 |

## 5. Public methods (the contract)

```java
// UserService
User login(String email, String password)
void registerStudent(Student s, String password)
void registerAdmin(Admin a, String password)
Student getProfile(int studentId)
void updateProfile(Student s)
void addSkill(int studentId, int skillId, String level)
void removeSkill(int studentId, int skillId)
List<Skill> getAllSkills()
List<Student> getAllStudents()           // admin
void deleteStudent(int studentId)        // admin

// PlacementService
List<Company> getAllCompanies()
List<JobRole> getEligibleRoles(int studentId)
void applyToRole(int studentId, int roleId)
void saveCompany(Company c)              // admin
void saveJobRole(JobRole r)              // admin
```

Eligibility rule: a student is eligible for a job role if their CGPA is at least the role's `minCgpa` and they have every required skill.

## 6. Requirements checklist

- [ ] Encapsulation: private fields with getters and setters
- [ ] Abstraction and inheritance: `User` -> `Student`, `Admin`
- [ ] Overriding: `getRole()`, `showMenu()`
- [ ] Overloading: constructors and `updateProfile(...)`
- [ ] Polymorphism: a `User` variable holding a `Student` or `Admin`
- [ ] Association, aggregation, composition: `Company` holds `JobRole`s, `JobRole` holds `Skill`s, `Student` holds skill levels
- [ ] Collections: `List`, `Map`, `Set`
- [ ] Exceptions: custom classes thrown and caught
- [ ] Passwords hashed, never plain text
- [ ] Input validation on every public method

## 7. Done when

A student and an admin can register and log in through the services alone. Every validation rule has a unit test. The class diagram is drawn. You can point to each checklist item in the code during the viva.

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
