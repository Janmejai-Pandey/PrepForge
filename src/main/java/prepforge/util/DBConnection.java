package prepforge.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Properties;
import prepforge.exception.DataAccessException;

public final class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/prepforge?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DATABASE_NAME = "prepforge";
    private static final String SCHEMA_RESOURCE = "db/schema.sql";
    private static final String SEED_RESOURCE = "db/seed.sql";

    private static final String url;
    private static final String user;
    private static final String password;

    static {
        Properties properties = new Properties();
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Could not read db.properties: " + e.getMessage());
        }
        url = setting("PREPFORGE_DB_URL", "db.url", DEFAULT_URL, properties);
        user = setting("PREPFORGE_DB_USER", "db.user", "root", properties);
        password = setting("PREPFORGE_DB_PASSWORD", "db.password", "", properties);
    }

    private DBConnection() {
    }

    private static String setting(String environmentKey, String propertyKey, String defaultValue,
                                  Properties properties) {
        String environmentValue = System.getenv(environmentKey);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue.trim();
        }
        return properties.getProperty(propertyKey, defaultValue).trim();
    }

    public static void initializeDatabase() throws DataAccessException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection(serverUrl(), user, password);
                 Statement statement = connection.createStatement()) {
                statement.execute("CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME
                        + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            }

            try (Connection connection = getConnection()) {
                validateExistingSchema(connection);
                executeScript(connection, SCHEMA_RESOURCE);
                if (!seeded(connection)) {
                    connection.setAutoCommit(false);
                    try {
                        executeScript(connection, SEED_RESOURCE);
                        try (Statement statement = connection.createStatement()) {
                            statement.executeUpdate(
                                    "INSERT INTO app_metadata (metadata_key, metadata_value) "
                                            + "VALUES ('seed_version', '1')");
                        }

                        connection.commit();
                    } catch (SQLException e) {
                        connection.rollback();
                        throw e;
                    } finally {
                        connection.setAutoCommit(true);
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException | IOException e) {
            throw new DataAccessException(
                    "Could not initialize MySQL database '" + DATABASE_NAME + "'. Check that MySQL is running "
                            + "and PREPFORGE_DB_USER/PREPFORGE_DB_PASSWORD (or db.properties) are correct: "
                            + e.getMessage(), e);
        }
        LegacyStateMigrator.migrateIfPresent();
    }

    private static void validateExistingSchema(Connection connection) throws SQLException {
        String[][] requiredColumns = {
                {"users", "user_id", "user_role", "name", "email", "password_hash", "department",
                        "employee_code", "enrollment_number", "branch", "cgpa"},
                {"skills", "skill_id", "skill_name", "category"},
                {"student_skills", "student_id", "skill_id", "proficiency"},
                {"companies", "company_id", "name", "description", "location", "industry"},
                {"job_roles", "role_id", "company_id", "title", "min_cgpa"},
                {"role_skills", "role_id", "skill_id"},
                {"applications", "application_id", "student_id", "role_id", "status", "applied_at"},
                {"questions", "question_id", "question_type", "topic", "difficulty", "question_text",
                        "correct_index", "problem_statement"},
                {"question_options", "question_id", "option_index", "option_text"},
                {"question_keywords", "question_id", "keyword"},
                {"interviews", "interview_id", "student_id", "interview_type", "difficulty", "status",
                        "started_at"},
                {"interview_questions", "interview_id", "question_id", "sequence_no"},
                {"answers", "interview_id", "question_id", "answer_text", "marks"},
                {"results", "result_id", "interview_id", "student_id", "total_score", "max_score", "created_at"},
                {"skill_scores", "student_id", "topic", "score"},
                {"recommendations", "recommendation_id", "student_id", "message"},
                {"app_metadata", "metadata_key", "metadata_value"}
        };
        List<String> incompatibleTables = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT column_name FROM information_schema.columns "
                        + "WHERE table_schema = DATABASE() AND table_name = ?")) {
            for (String[] contract : requiredColumns) {
                Set<String> existing = new HashSet<>();
                statement.setString(1, contract[0]);
                try (var results = statement.executeQuery()) {
                    while (results.next()) {
                        existing.add(results.getString(1).toLowerCase(Locale.ROOT));
                    }
                }
                if (!existing.isEmpty()) {
                    List<String> missing = new ArrayList<>();
                    for (int i = 1; i < contract.length; i++) {
                        if (!existing.contains(contract[i].toLowerCase(Locale.ROOT))) {
                            missing.add(contract[i]);
                        }
                    }
                    if (!missing.isEmpty()) {
                        incompatibleTables.add(contract[0] + " (missing " + String.join(", ", missing) + ")");
                    }
                }
            }
        }
        if (!incompatibleTables.isEmpty()) {
            throw new SQLException("The existing prepforge database has incompatible tables: "
                    + String.join("; ", incompatibleTables)
                    + ". Back up any data and migrate or recreate the database before launching PrepForge.");
        }
    }

    private static boolean seeded(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             var results = statement.executeQuery(
                     "SELECT metadata_value FROM app_metadata WHERE metadata_key = 'seed_version'")) {
            return results.next();
        }
    }

    private static void executeScript(Connection connection, String resource) throws IOException, SQLException {
        byte[] bytes;
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream(resource)) {
            if (input == null) {
                throw new IOException("Missing SQL resource: " + resource);
            }
            bytes = input.readAllBytes();
        }

        String script = new String(bytes, StandardCharsets.UTF_8);
        for (String sql : script.split(";\\s*(?:\\r?\\n|$)")) {
            String statementText = sql.trim();
            if (!statementText.isEmpty()) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(statementText);
                }
            }
        }
    }

    private static String serverUrl() throws SQLException {
        String prefix = "jdbc:mysql://";
        if (!url.startsWith(prefix)) {
            throw new SQLException("db.url must be a MySQL JDBC URL.");
        }
        int pathStart = url.indexOf('/', prefix.length());
        int queryStart = url.indexOf('?', prefix.length());
        if (pathStart < 0 || (queryStart >= 0 && queryStart < pathStart)) {
            return url + (queryStart < 0 ? "/" : "");
        }
        int pathEnd = queryStart < 0 ? url.length() : queryStart;
        return url.substring(0, pathStart + 1) + url.substring(pathEnd);
    }

    public static Connection getConnection() throws DataAccessException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            throw new DataAccessException("MySQL JDBC driver not found on classpath: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to establish database connection to " + url + ": "
                    + e.getMessage(), e);
        }
    }
}
