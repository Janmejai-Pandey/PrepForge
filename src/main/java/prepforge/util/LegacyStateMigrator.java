package prepforge.util;

import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import prepforge.dao.MySqlDataStore;
import prepforge.exception.DataAccessException;
import prepforge.model.Admin;
import prepforge.model.ApplicationRecord;
import prepforge.model.Company;
import prepforge.model.Interview;
import prepforge.model.JobRole;
import prepforge.model.Question;
import prepforge.model.Result;
import prepforge.model.Skill;
import prepforge.model.Student;
import prepforge.model.User;

public final class LegacyStateMigrator {

    private static final Path LEGACY_FILE = Path.of("prepforge_state.dat");
    private static final String MIGRATION_KEY = "legacy_state_migrated";

    private LegacyStateMigrator() {
    }

    public static void migrateIfPresent() throws DataAccessException {
        if (!Files.isRegularFile(LEGACY_FILE)) {
            return;
        }
        try (Connection connection = DBConnection.getConnection()) {
            if (isMigrated(connection)) {
                deleteLegacyFile();
                return;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not check the local-state migration status: " + e.getMessage(), e);
        }

        StorageManager.PersistentState state = readState();
        MySqlDataStore store = new MySqlDataStore();
        try {
            migrateSkills(state);
            migrateUsers(state, store);
            migrateCompanies(state, store);
            migrateQuestions(state, store);
            migrateApplications(state, store);
            migrateInterviews(state, store);
            migrateAnalytics(state);
            markMigrated();
            deleteLegacyFile();
        } catch (DataAccessException e) {
            throw new DataAccessException("Could not migrate prepforge_state.dat into MySQL. The local file was "
                    + "left untouched; resolve the database error and restart to retry: " + e.getMessage(), e);
        }
    }

    private static void deleteLegacyFile() throws DataAccessException {
        try {
            Files.deleteIfExists(LEGACY_FILE);
        } catch (IOException e) {
            throw new DataAccessException("MySQL migration completed, but prepforge_state.dat could not be "
                    + "removed: " + e.getMessage(), e);
        }
    }

    private static boolean isMigrated(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM app_metadata WHERE metadata_key = ?")) {
            statement.setString(1, MIGRATION_KEY);
            try (var results = statement.executeQuery()) {
                return results.next();
            }
        }
    }

    private static StorageManager.PersistentState readState() throws DataAccessException {
        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(LEGACY_FILE))) {
            input.setObjectInputFilter(LegacyStateMigrator::filterSerializedClass);
            Object value = input.readObject();
            if (!(value instanceof StorageManager.PersistentState)) {
                throw new DataAccessException("prepforge_state.dat has an unexpected format.");
            }
            return (StorageManager.PersistentState) value;
        } catch (IOException | ClassNotFoundException e) {
            throw new DataAccessException("Could not read the existing prepforge_state.dat: " + e.getMessage(), e);
        }
    }

    private static ObjectInputFilter.Status filterSerializedClass(ObjectInputFilter.FilterInfo info) {
        if (info.depth() > 64 || info.references() > 1_000_000
                || info.arrayLength() > 1_000_000 || info.streamBytes() > 100_000_000) {
            return ObjectInputFilter.Status.REJECTED;
        }
        Class<?> type = info.serialClass();
        if (type == null) {
            return ObjectInputFilter.Status.UNDECIDED;
        }
        if (type.isArray()) {
            Class<?> component = type.getComponentType();
            return component.isPrimitive() || component == Object.class || isAllowed(component)
                    ? ObjectInputFilter.Status.ALLOWED : ObjectInputFilter.Status.REJECTED;
        }
        return isAllowed(type) ? ObjectInputFilter.Status.ALLOWED : ObjectInputFilter.Status.REJECTED;
    }

    private static boolean isAllowed(Class<?> type) {
        String name = type.getName();
        return type.isPrimitive()
                || type == String.class
                || type == Number.class
                || type == Integer.class
                || type == Double.class
                || type == Long.class
                || type == Boolean.class
                || type == java.time.LocalDateTime.class
                || name.equals("java.time.Ser")
                || type == java.util.HashMap.class
                || type == java.util.LinkedHashMap.class
                || type == java.util.Map.Entry.class
                || type == java.util.HashSet.class
                || type == java.util.ArrayList.class
                || type == StorageManager.PersistentState.class
                || name.startsWith("prepforge.model.");
    }

    private static void migrateSkills(StorageManager.PersistentState state) throws DataAccessException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO skills (skill_id, skill_name, category) VALUES (?, ?, ?) "
                             + "ON DUPLICATE KEY UPDATE skill_name=VALUES(skill_name), category=VALUES(category)")) {
            for (Skill skill : state.skillCatalog.values()) {
                statement.setInt(1, skill.getId());
                statement.setString(2, skill.getName());
                statement.setString(3, skill.getCategory());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new DataAccessException("Could not migrate skills: " + e.getMessage(), e);
        }
    }

    private static void migrateUsers(StorageManager.PersistentState state, MySqlDataStore store)
            throws DataAccessException {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement admin = connection.prepareStatement(
                         "INSERT INTO users (user_id, user_role, name, email, password_hash, department, employee_code) "
                                 + "VALUES (?, 'ADMIN', ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), "
                                 + "email=VALUES(email), password_hash=VALUES(password_hash), "
                                 + "department=VALUES(department), employee_code=VALUES(employee_code)");
                 PreparedStatement student = connection.prepareStatement(
                         "INSERT INTO users (user_id, user_role, name, email, password_hash, enrollment_number, branch, cgpa) "
                                 + "VALUES (?, 'STUDENT', ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), "
                                 + "email=VALUES(email), password_hash=VALUES(password_hash), "
                                 + "enrollment_number=VALUES(enrollment_number), branch=VALUES(branch), cgpa=VALUES(cgpa)");
                 PreparedStatement studentSkill = connection.prepareStatement(
                         "INSERT INTO student_skills (student_id, skill_id, proficiency) VALUES (?, ?, ?) "
                                 + "ON DUPLICATE KEY UPDATE proficiency=VALUES(proficiency)")) {
                for (User user : state.userDatabase.values()) {
                    if (user instanceof Admin) {
                        Admin a = (Admin) user;
                        admin.setInt(1, a.getId());
                        admin.setString(2, a.getName());
                        admin.setString(3, a.getEmail());
                        admin.setString(4, a.getPasswordHash());
                        admin.setString(5, a.getDepartment());
                        admin.setString(6, a.getEmployeeCode());
                        admin.addBatch();
                    } else if (user instanceof Student) {
                        Student s = (Student) user;
                        student.setInt(1, s.getId());
                        student.setString(2, s.getName());
                        student.setString(3, s.getEmail());
                        student.setString(4, s.getPasswordHash());
                        student.setString(5, s.getEnrollmentNumber());
                        student.setString(6, s.getBranch());
                        student.setDouble(7, s.getCgpa());
                        student.addBatch();
                        for (Map.Entry<Integer, String> skill : s.getSkillProficiency().entrySet()) {
                            studentSkill.setInt(1, s.getId());
                            studentSkill.setInt(2, skill.getKey());
                            studentSkill.setString(3, skill.getValue());
                            studentSkill.addBatch();
                        }
                    }
                }
                admin.executeBatch();
                student.executeBatch();
                studentSkill.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not migrate users and student skills: " + e.getMessage(), e);
        }
    }

    private static void migrateCompanies(StorageManager.PersistentState state, MySqlDataStore store)
            throws DataAccessException {
        for (Company company : state.companies.values()) {
            store.saveCompany(company);
        }
        for (JobRole role : state.jobRoles.values()) {
            store.saveJobRole(role);
        }
    }

    private static void migrateQuestions(StorageManager.PersistentState state, MySqlDataStore store)
            throws DataAccessException {
        for (Question question : state.questionStore.values()) {
            if (store.findById(question.getId()) == null) {
                store.save(question);
            } else {
                store.update(question);
            }
        }
    }

    private static void migrateApplications(StorageManager.PersistentState state, MySqlDataStore store)
            throws DataAccessException {
        for (ApplicationRecord record : state.applicationRecords) {
            store.saveApplicationRecord(record);
        }
    }

    private static void migrateInterviews(StorageManager.PersistentState state, MySqlDataStore store)
            throws DataAccessException {
        for (Interview interview : state.interviewStore.values()) {
            store.save(interview);
            saveLegacyAnswers(interview);
        }
        for (Result result : state.resultStore.values()) {
            Interview interview = state.interviewStore.get(result.getInterviewId());
            if (interview != null) {
                store.saveResultInTransaction(interview, result);
            }
        }
    }

    private static void saveLegacyAnswers(Interview interview) throws DataAccessException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement answer = connection.prepareStatement(
                     "INSERT INTO answers (interview_id, question_id, answer_text, marks) VALUES (?, ?, ?, ?) "
                             + "ON DUPLICATE KEY UPDATE answer_text=VALUES(answer_text), marks=VALUES(marks)");
             PreparedStatement startedAt = connection.prepareStatement(
                     "UPDATE interviews SET started_at = ?, status = ? WHERE interview_id = ?")) {
            for (Question question : interview.getQuestions()) {
                if (interview.getAnswers().containsKey(question.getId())
                        || interview.getAllMarks().containsKey(question.getId())) {
                    answer.setInt(1, interview.getId());
                    answer.setInt(2, question.getId());
                    answer.setString(3, interview.getAnswer(question.getId()));
                    answer.setDouble(4, interview.getMarks(question.getId()));
                    answer.addBatch();
                }
            }
            answer.executeBatch();
            startedAt.setTimestamp(1, Timestamp.valueOf(interview.getStartedAt()));
            startedAt.setString(2, interview.getStatus());
            startedAt.setInt(3, interview.getId());
            startedAt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not migrate interview #" + interview.getId() + ": "
                    + e.getMessage(), e);
        }
    }

    private static void migrateAnalytics(StorageManager.PersistentState state) throws DataAccessException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement score = connection.prepareStatement(
                     "INSERT INTO skill_scores (student_id, topic, score) VALUES (?, ?, ?) "
                             + "ON DUPLICATE KEY UPDATE score=VALUES(score)");
             PreparedStatement recommendation = connection.prepareStatement(
                     "INSERT IGNORE INTO recommendations (student_id, message) VALUES (?, ?)")) {
            for (Map.Entry<Integer, Map<String, Double>> student : state.studentSkillScores.entrySet()) {
                for (Map.Entry<String, Double> skill : student.getValue().entrySet()) {
                    score.setInt(1, student.getKey());
                    score.setString(2, skill.getKey());
                    score.setDouble(3, skill.getValue());
                    score.addBatch();
                }
            }
            for (Map.Entry<Integer, java.util.List<String>> student : state.studentRecommendations.entrySet()) {
                for (String message : student.getValue()) {
                    recommendation.setInt(1, student.getKey());
                    recommendation.setString(2, message);
                    recommendation.addBatch();
                }
            }
            score.executeBatch();
            recommendation.executeBatch();
        } catch (SQLException e) {
            throw new DataAccessException("Could not migrate stored analytics: " + e.getMessage(), e);
        }
    }

    private static void markMigrated() throws DataAccessException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO app_metadata (metadata_key, metadata_value) VALUES (?, '1')")) {
            statement.setString(1, MIGRATION_KEY);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not mark the local-state migration complete: "
                    + e.getMessage(), e);
        }
    }
}
