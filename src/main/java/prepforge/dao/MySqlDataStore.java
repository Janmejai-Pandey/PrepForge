package prepforge.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import prepforge.exception.DataAccessException;
import prepforge.model.Admin;
import prepforge.model.ApplicationRecord;
import prepforge.model.CodingQuestion;
import prepforge.model.Company;
import prepforge.model.HRQuestion;
import prepforge.model.Interview;
import prepforge.model.JobRole;
import prepforge.model.KeywordBased;
import prepforge.model.MCQQuestion;
import prepforge.model.MockInterview;
import prepforge.model.Question;
import prepforge.model.Result;
import prepforge.model.Skill;
import prepforge.model.Student;
import prepforge.model.TechnicalInterview;
import prepforge.model.TechnicalQuestion;
import prepforge.model.User;
import prepforge.util.DBConnection;

public class MySqlDataStore implements UserDao, CompanyDao, QuestionDao, InterviewDao {

    @FunctionalInterface
    private interface SqlWork<T> {
        T run(Connection connection) throws SQLException;
    }

    private <T> T withConnection(SqlWork<T> work) throws DataAccessException {
        try (Connection connection = DBConnection.getConnection()) {
            return work.run(connection);
        } catch (SQLException e) {
            throw new DataAccessException("MySQL operation failed: " + e.getMessage(), e);
        }
    }

    private static void requireUpdated(PreparedStatement statement, String entity, int id) throws SQLException {
        if (statement.executeUpdate() == 0) {
            throw new SQLException(entity + " with ID " + id + " was not found.");
        }
    }

    @Override
    public User findByEmail(String email) throws DataAccessException {
        if (email == null) {
            return null;
        }
        return withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM users WHERE LOWER(email) = LOWER(?)")) {
                statement.setString(1, email.trim());
                try (ResultSet results = statement.executeQuery()) {
                    return results.next() ? readUser(connection, results) : null;
                }
            }
        });
    }

    @Override
    public void saveStudent(Student student, String passwordHash) throws DataAccessException {
        if (student == null) {
            throw new DataAccessException("Cannot save null student.");
        }
        withConnection(connection -> {
            String sql = "INSERT INTO users (user_role, name, email, password_hash, enrollment_number, branch, cgpa) "
                    + "VALUES ('STUDENT', ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, student.getName());
                statement.setString(2, student.getEmail());
                statement.setString(3, passwordHash);
                statement.setString(4, student.getEnrollmentNumber());
                statement.setString(5, student.getBranch());
                statement.setDouble(6, student.getCgpa());
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        student.setId(keys.getInt(1));
                        student.setUserId(keys.getInt(1));
                        student.setPasswordHash(passwordHash);
                    }
                }
            }
            return null;
        });
    }

    @Override
    public void saveAdmin(Admin admin, String passwordHash) throws DataAccessException {
        if (admin == null) {
            throw new DataAccessException("Cannot save null admin.");
        }
        withConnection(connection -> {
            String sql = "INSERT INTO users (user_role, name, email, password_hash, department, employee_code) "
                    + "VALUES ('ADMIN', ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, admin.getName());
                statement.setString(2, admin.getEmail());
                statement.setString(3, passwordHash);
                statement.setString(4, admin.getDepartment());
                statement.setString(5, admin.getEmployeeCode());
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        admin.setId(keys.getInt(1));
                        admin.setUserId(keys.getInt(1));
                        admin.setPasswordHash(passwordHash);
                    }
                }
            }
            return null;
        });
    }

    @Override
    public Student findStudentById(int studentId) throws DataAccessException {
        return withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM users WHERE user_id = ? AND user_role = 'STUDENT'")) {
                statement.setInt(1, studentId);
                try (ResultSet results = statement.executeQuery()) {
                    if (!results.next()) {
                        return null;
                    }
                    return (Student) readUser(connection, results);
                }
            }
        });
    }

    @Override
    public void updateStudent(Student student) throws DataAccessException {
        if (student == null) {
            throw new DataAccessException("Cannot update null student.");
        }
        withConnection(connection -> {
            try (PreparedStatement exists = connection.prepareStatement(
                    "SELECT 1 FROM users WHERE user_id = ? AND user_role = 'STUDENT'")) {
                exists.setInt(1, student.getId());
                try (ResultSet results = exists.executeQuery()) {
                    if (!results.next()) {
                        throw new SQLException("Student with ID " + student.getId() + " not found.");
                    }
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE users SET name = ?, email = ?, enrollment_number = ?, branch = ?, cgpa = ? "
                            + "WHERE user_id = ? AND user_role = 'STUDENT'")) {
                statement.setString(1, student.getName());
                statement.setString(2, student.getEmail());
                statement.setString(3, student.getEnrollmentNumber());
                statement.setString(4, student.getBranch());
                statement.setDouble(5, student.getCgpa());
                statement.setInt(6, student.getId());
                statement.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public void addStudentSkill(int studentId, int skillId, String level) throws DataAccessException {
        withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO student_skills (student_id, skill_id, proficiency) VALUES (?, ?, ?) "
                            + "ON DUPLICATE KEY UPDATE proficiency = VALUES(proficiency)")) {
                statement.setInt(1, studentId);
                statement.setInt(2, skillId);
                statement.setString(3, level);
                statement.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public void removeStudentSkill(int studentId, int skillId) throws DataAccessException {
        withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM student_skills WHERE student_id = ? AND skill_id = ?")) {
                statement.setInt(1, studentId);
                statement.setInt(2, skillId);
                statement.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public List<Skill> findAllSkills() throws DataAccessException {
        return withConnection(connection -> {
            List<Skill> skills = new ArrayList<>();
            try (Statement statement = connection.createStatement();
                 ResultSet results = statement.executeQuery(
                         "SELECT skill_id, skill_name, category FROM skills ORDER BY skill_name")) {
                while (results.next()) {
                    skills.add(readSkill(results));
                }
            }
            return skills;
        });
    }

    @Override
    public Skill findSkillById(int skillId) throws DataAccessException {
        return withConnection(connection -> findSkill(connection, skillId));
    }

    @Override
    public List<Student> findAllStudents() throws DataAccessException {
        return withConnection(connection -> {
            List<Student> students = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM users WHERE user_role = 'STUDENT' ORDER BY user_id");
                 ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    students.add((Student) readUser(connection, results));
                }
            }
            return students;
        });
    }

    @Override
    public void deleteStudent(int studentId) throws DataAccessException {
        withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM users WHERE user_id = ? AND user_role = 'STUDENT'")) {
                statement.setInt(1, studentId);
                requireUpdated(statement, "Student", studentId);
            }
            return null;
        });
    }

    private User readUser(Connection connection, ResultSet results) throws SQLException {
        int id = results.getInt("user_id");
        String role = results.getString("user_role");
        String name = results.getString("name");
        String email = results.getString("email");
        String passwordHash = results.getString("password_hash");
        if ("ADMIN".equals(role)) {
            Admin admin = new Admin(id, name, email, results.getString("department"),
                    results.getString("employee_code"));
            admin.setPasswordHash(passwordHash);
            return admin;
        }
        Student student = new Student(id, name, email, results.getString("enrollment_number"),
                results.getString("branch"), results.getDouble("cgpa"));
        student.setPasswordHash(passwordHash);
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT s.skill_id, s.skill_name, s.category, ss.proficiency "
                        + "FROM student_skills ss JOIN skills s ON s.skill_id = ss.skill_id "
                        + "WHERE ss.student_id = ? ORDER BY s.skill_name")) {
            statement.setInt(1, id);
            try (ResultSet skills = statement.executeQuery()) {
                while (skills.next()) {
                    student.addSkill(readSkill(skills), skills.getString("proficiency"));
                }
            }
        }
        return student;
    }

    private Skill readSkill(ResultSet results) throws SQLException {
        return new Skill(results.getInt("skill_id"), results.getString("skill_name"),
                results.getString("category"));
    }

    private Skill findSkill(Connection connection, int skillId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT skill_id, skill_name, category FROM skills WHERE skill_id = ?")) {
            statement.setInt(1, skillId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? readSkill(results) : null;
            }
        }
    }

    @Override
    public List<Company> findAllCompanies() throws DataAccessException {
        return withConnection(connection -> {
            List<Company> companies = new ArrayList<>();
            try (Statement statement = connection.createStatement();
                 ResultSet results = statement.executeQuery("SELECT * FROM companies ORDER BY name")) {
                while (results.next()) {
                    Company company = readCompany(results);
                    populateCompanyRoles(connection, company);
                    companies.add(company);
                }
            }
            return companies;
        });
    }

    @Override
    public Company findCompanyById(int companyId) throws DataAccessException {
        return withConnection(connection -> {
            Company company = findCompany(connection, companyId);
            if (company != null) {
                populateCompanyRoles(connection, company);
            }
            return company;
        });
    }

    @Override
    public void saveCompany(Company company) throws DataAccessException {
        if (company == null) {
            throw new DataAccessException("Cannot persist a null company.");
        }
        withConnection(connection -> {
            String sql = company.getCompanyId() > 0
                    ? "INSERT INTO companies (company_id, name, description, location, industry) "
                            + "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), "
                            + "description=VALUES(description), location=VALUES(location), industry=VALUES(industry)"
                    : "INSERT INTO companies (name, description, location, industry) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                int index = 1;
                if (company.getCompanyId() > 0) {
                    statement.setInt(index++, company.getCompanyId());
                }
                statement.setString(index++, company.getName());
                statement.setString(index++, company.getDescription());
                statement.setString(index++, company.getLocation());
                statement.setString(index, company.getIndustry());
                statement.executeUpdate();
                if (company.getCompanyId() <= 0) {
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (keys.next()) {
                            company.setCompanyId(keys.getInt(1));
                        }
                    }
                }
            }
            return null;
        });
    }

    @Override
    public List<JobRole> findAllJobRoles() throws DataAccessException {
        return withConnection(connection -> {
            List<JobRole> roles = new ArrayList<>();
            try (Statement statement = connection.createStatement();
                 ResultSet results = statement.executeQuery("SELECT role_id FROM job_roles ORDER BY role_id")) {
                while (results.next()) {
                    roles.add(findJobRole(connection, results.getInt(1)));
                }
            }
            return roles;
        });
    }

    @Override
    public JobRole findJobRoleById(int roleId) throws DataAccessException {
        return withConnection(connection -> findJobRole(connection, roleId));
    }

    @Override
    public void saveJobRole(JobRole role) throws DataAccessException {
        if (role == null) {
            throw new DataAccessException("Cannot persist a null job role.");
        }
        withConnection(connection -> {
            connection.setAutoCommit(false);
            try {
                String sql = role.getRoleId() > 0
                        ? "INSERT INTO job_roles (role_id, company_id, title, min_cgpa) VALUES (?, ?, ?, ?) "
                                + "ON DUPLICATE KEY UPDATE company_id=VALUES(company_id), title=VALUES(title), "
                                + "min_cgpa=VALUES(min_cgpa)"
                        : "INSERT INTO job_roles (company_id, title, min_cgpa) VALUES (?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    int index = 1;
                    if (role.getRoleId() > 0) {
                        statement.setInt(index++, role.getRoleId());
                    }
                    statement.setInt(index++, role.getCompany().getCompanyId());
                    statement.setString(index++, role.getTitle());
                    statement.setDouble(index, role.getMinCgpa());
                    statement.executeUpdate();
                    if (role.getRoleId() <= 0) {
                        try (ResultSet keys = statement.getGeneratedKeys()) {
                            if (keys.next()) {
                                role.setRoleId(keys.getInt(1));
                            }
                        }
                    }
                }
                try (PreparedStatement delete = connection.prepareStatement(
                        "DELETE FROM role_skills WHERE role_id = ?")) {
                    delete.setInt(1, role.getRoleId());
                    delete.executeUpdate();
                }
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO role_skills (role_id, skill_id) VALUES (?, ?)")) {
                    for (Skill skill : role.getRequiredSkills()) {
                        insert.setInt(1, role.getRoleId());
                        insert.setInt(2, skill.getId());
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
            return null;
        });
    }

    @Override
    public void saveApplication(int studentId, int roleId) throws DataAccessException {
        withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO applications (student_id, role_id) VALUES (?, ?) "
                            + "ON DUPLICATE KEY UPDATE student_id = VALUES(student_id)")) {
                statement.setInt(1, studentId);
                statement.setInt(2, roleId);
                statement.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public boolean hasApplied(int studentId, int roleId) throws DataAccessException {
        return withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT 1 FROM applications WHERE student_id = ? AND role_id = ?")) {
                statement.setInt(1, studentId);
                statement.setInt(2, roleId);
                try (ResultSet results = statement.executeQuery()) {
                    return results.next();
                }
            }
        });
    }

    @Override
    public List<Integer> findAppliedRoleIds(int studentId) throws DataAccessException {
        return withConnection(connection -> {
            List<Integer> roleIds = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT role_id FROM applications WHERE student_id = ? ORDER BY role_id")) {
                statement.setInt(1, studentId);
                try (ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        roleIds.add(results.getInt(1));
                    }
                }
            }
            return roleIds;
        });
    }

    @Override
    public List<ApplicationRecord> findAllApplications() throws DataAccessException {
        return withConnection(connection -> {
            List<ApplicationRecord> applications = new ArrayList<>();
            String sql = "SELECT a.application_id, a.student_id, u.name AS student_name, u.email, u.branch, u.cgpa, "
                    + "r.role_id, r.title, c.name AS company_name, a.status, a.applied_at "
                    + "FROM applications a JOIN users u ON u.user_id = a.student_id "
                    + "JOIN job_roles r ON r.role_id = a.role_id JOIN companies c ON c.company_id = r.company_id "
                    + "ORDER BY a.applied_at DESC";
            try (Statement statement = connection.createStatement();
                 ResultSet results = statement.executeQuery(sql)) {
                while (results.next()) {
                    ApplicationRecord record = new ApplicationRecord(results.getInt("application_id"),
                            results.getInt("student_id"), results.getString("student_name"),
                            results.getString("email"), results.getString("branch"), results.getDouble("cgpa"),
                            results.getInt("role_id"), results.getString("title"),
                            results.getString("company_name"), results.getString("status"));
                    Timestamp appliedAt = results.getTimestamp("applied_at");
                    if (appliedAt != null) {
                        record.setAppliedAt(appliedAt.toLocalDateTime());
                    }
                    applications.add(record);
                }
            }
            return applications;
        });
    }

    @Override
    public void updateApplicationStatus(int studentId, int roleId, String status) throws DataAccessException {
        withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE applications SET status = ? WHERE student_id = ? AND role_id = ?")) {
                statement.setString(1, status);
                statement.setInt(2, studentId);
                statement.setInt(3, roleId);
                statement.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public void saveApplicationRecord(ApplicationRecord record) throws DataAccessException {
        if (record == null) {
            throw new DataAccessException("Cannot persist a null application.");
        }
        withConnection(connection -> {
            String sql = record.getApplicationId() > 0
                    ? "INSERT INTO applications (application_id, student_id, role_id, status) VALUES (?, ?, ?, ?) "
                            + "ON DUPLICATE KEY UPDATE status=VALUES(status)"
                    : "INSERT INTO applications (student_id, role_id, status) VALUES (?, ?, ?) "
                            + "ON DUPLICATE KEY UPDATE status=VALUES(status)";
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                int index = 1;
                if (record.getApplicationId() > 0) {
                    statement.setInt(index++, record.getApplicationId());
                }
                statement.setInt(index++, record.getStudentId());
                statement.setInt(index++, record.getRoleId());
                statement.setString(index, record.getStatus());
                statement.executeUpdate();
                if (record.getApplicationId() <= 0) {
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (keys.next()) {
                            record.setApplicationId(keys.getInt(1));
                        }
                    }
                }
            }
            return null;
        });
    }

    private Company readCompany(ResultSet results) throws SQLException {
        return new Company(results.getInt("company_id"), results.getString("name"),
                results.getString("description"), results.getString("location"), results.getString("industry"));
    }

    private Company findCompany(Connection connection, int companyId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM companies WHERE company_id = ?")) {
            statement.setInt(1, companyId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? readCompany(results) : null;
            }
        }
    }

    private void populateCompanyRoles(Connection connection, Company company) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT role_id FROM job_roles WHERE company_id = ? ORDER BY role_id")) {
            statement.setInt(1, company.getCompanyId());
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    JobRole role = findJobRole(connection, results.getInt(1));
                    if (role != null) {
                        company.addJobRole(role);
                    }
                }
            }
        }
    }

    private JobRole findJobRole(Connection connection, int roleId) throws SQLException {
        String sql = "SELECT role_id, company_id, title, min_cgpa FROM job_roles WHERE role_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roleId);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return null;
                }
                Company company = findCompany(connection, results.getInt("company_id"));
                JobRole role = new JobRole(results.getInt("role_id"), results.getString("title"),
                        results.getDouble("min_cgpa"), company);
                if (company != null) {
                    company.addJobRole(role);
                }
                try (PreparedStatement skills = connection.prepareStatement(
                        "SELECT s.skill_id, s.skill_name, s.category FROM role_skills rs "
                                + "JOIN skills s ON s.skill_id = rs.skill_id WHERE rs.role_id = ?")) {
                    skills.setInt(1, roleId);
                    try (ResultSet skillResults = skills.executeQuery()) {
                        while (skillResults.next()) {
                            role.addRequiredSkill(readSkill(skillResults));
                        }
                    }
                }
                return role;
            }
        }
    }

    @Override
    public void save(Question question) throws DataAccessException {
        if (question == null) {
            throw new DataAccessException("Cannot save null question.");
        }
        withConnection(connection -> {
            connection.setAutoCommit(false);
            try {
                insertQuestion(connection, question);
                writeQuestionDetails(connection, question);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
            return null;
        });
    }

    @Override
    public Question findById(int id) throws DataAccessException {
        return withConnection(connection -> findQuestion(connection, id));
    }

    @Override
    public List<Question> findAll() throws DataAccessException {
        return search(null, null, null);
    }

    @Override
    public void update(Question question) throws DataAccessException {
        if (question == null || question.getId() <= 0) {
            throw new DataAccessException("A saved question is required for update.");
        }
        withConnection(connection -> {
            connection.setAutoCommit(false);
            try {
                if (findQuestion(connection, question.getId()) == null) {
                    throw new SQLException("Question with ID " + question.getId() + " was not found.");
                }
                String sql = "UPDATE questions SET question_type=?, topic=?, difficulty=?, question_text=?, "
                        + "correct_index=?, problem_statement=? WHERE question_id=?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    setQuestionFields(statement, question, false);
                    statement.setInt(7, question.getId());
                    statement.executeUpdate();
                }
                writeQuestionDetails(connection, question);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
            return null;
        });
    }

    @Override
    public void delete(int id) throws DataAccessException {
        withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM questions WHERE question_id = ?")) {
                statement.setInt(1, id);
                requireUpdated(statement, "Question", id);
            }
            return null;
        });
    }

    @Override
    public List<Question> search(String topic, String difficulty, String type) throws DataAccessException {
        return withConnection(connection -> {
            StringBuilder sql = new StringBuilder("SELECT question_id FROM questions WHERE 1=1");
            List<String> values = new ArrayList<>();
            if (topic != null) {
                sql.append(" AND LOWER(topic) = LOWER(?)");
                values.add(topic);
            }
            if (difficulty != null) {
                sql.append(" AND difficulty = ?");
                values.add(difficulty);
            }
            if (type != null) {
                sql.append(" AND question_type = ?");
                values.add(type);
            }
            sql.append(" ORDER BY question_id");
            List<Question> questions = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                for (int i = 0; i < values.size(); i++) {
                    statement.setString(i + 1, values.get(i));
                }
                try (ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        questions.add(findQuestion(connection, results.getInt(1)));
                    }
                }
            }
            return questions;
        });
    }

    private void insertQuestion(Connection connection, Question question) throws SQLException {
        boolean hasId = question.getId() > 0;
        String sql = hasId
                ? "INSERT INTO questions (question_id, question_type, topic, difficulty, question_text, "
                        + "correct_index, problem_statement) VALUES (?, ?, ?, ?, ?, ?, ?)"
                : "INSERT INTO questions (question_type, topic, difficulty, question_text, correct_index, "
                        + "problem_statement) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setQuestionFields(statement, question, hasId);
            statement.executeUpdate();
            if (!hasId) {
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        question.setId(keys.getInt(1));
                    }
                }
            }
        }
    }

    private void setQuestionFields(PreparedStatement statement, Question question, boolean hasId)
            throws SQLException {
        int index = 1;
        if (hasId) {
            statement.setInt(index++, question.getId());
        }
        statement.setString(index++, question.getType());
        statement.setString(index++, question.getTopic());
        statement.setString(index++, question.getDifficulty());
        statement.setString(index++, question.getText());
        if (question instanceof MCQQuestion) {
            statement.setInt(index++, ((MCQQuestion) question).getCorrectIndex());
        } else {
            statement.setNull(index++, java.sql.Types.INTEGER);
        }
        if (question instanceof CodingQuestion) {
            statement.setString(index, ((CodingQuestion) question).getProblemStatement());
        } else {
            statement.setNull(index, java.sql.Types.VARCHAR);
        }
    }

    private void writeQuestionDetails(Connection connection, Question question) throws SQLException {
        try (PreparedStatement deleteOptions = connection.prepareStatement(
                     "DELETE FROM question_options WHERE question_id = ?");
             PreparedStatement deleteKeywords = connection.prepareStatement(
                     "DELETE FROM question_keywords WHERE question_id = ?")) {
            deleteOptions.setInt(1, question.getId());
            deleteOptions.executeUpdate();
            deleteKeywords.setInt(1, question.getId());
            deleteKeywords.executeUpdate();
        }
        if (question instanceof MCQQuestion) {
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO question_options (question_id, option_index, option_text) VALUES (?, ?, ?)")) {
                List<String> options = ((MCQQuestion) question).getOptions();
                for (int i = 0; i < options.size(); i++) {
                    insert.setInt(1, question.getId());
                    insert.setInt(2, i);
                    insert.setString(3, options.get(i));
                    insert.addBatch();
                }
                insert.executeBatch();
            }
        }
        if (question instanceof KeywordBased) {
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO question_keywords (question_id, keyword) VALUES (?, ?)")) {
                for (String keyword : ((KeywordBased) question).getKeywords()) {
                    insert.setInt(1, question.getId());
                    insert.setString(2, keyword);
                    insert.addBatch();
                }
                insert.executeBatch();
            }
        }
    }

    private Question findQuestion(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM questions WHERE question_id = ?")) {
            statement.setInt(1, id);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? readQuestion(connection, results) : null;
            }
        }
    }

    private Question readQuestion(Connection connection, ResultSet results) throws SQLException {
        int id = results.getInt("question_id");
        String type = results.getString("question_type");
        String topic = results.getString("topic");
        String difficulty = results.getString("difficulty");
        String text = results.getString("question_text");
        if ("MCQ".equals(type)) {
            List<String> options = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT option_text FROM question_options WHERE question_id = ? ORDER BY option_index")) {
                statement.setInt(1, id);
                try (ResultSet optionResults = statement.executeQuery()) {
                    while (optionResults.next()) {
                        options.add(optionResults.getString(1));
                    }
                }
            }
            return new MCQQuestion(id, text, topic, difficulty, options, results.getInt("correct_index"));
        }
        List<String> keywords = findKeywords(connection, id);
        if ("TECHNICAL".equals(type)) {
            return new TechnicalQuestion(id, text, topic, difficulty, keywords);
        }
        if ("CODING".equals(type)) {
            return new CodingQuestion(id, text, topic, difficulty,
                    results.getString("problem_statement"), keywords);
        }
        return new HRQuestion(id, text, topic, difficulty, keywords);
    }

    private List<String> findKeywords(Connection connection, int questionId) throws SQLException {
        List<String> keywords = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT keyword FROM question_keywords WHERE question_id = ? ORDER BY keyword")) {
            statement.setInt(1, questionId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    keywords.add(results.getString(1));
                }
            }
        }
        return keywords;
    }

    @Override
    public void save(Interview interview) throws DataAccessException {
        if (interview == null) {
            throw new DataAccessException("Cannot save null interview.");
        }
        withConnection(connection -> {
            connection.setAutoCommit(false);
            try {
                String sql = interview.getId() > 0
                        ? "INSERT INTO interviews (interview_id, student_id, interview_type, difficulty, status, "
                                + "started_at) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
                                + "status=VALUES(status), started_at=VALUES(started_at)"
                        : "INSERT INTO interviews (student_id, interview_type, difficulty, status, started_at) "
                                + "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    int index = 1;
                    if (interview.getId() > 0) {
                        statement.setInt(index++, interview.getId());
                    }
                    statement.setInt(index++, interview.getStudentId());
                    statement.setString(index++, interview.getType());
                    statement.setString(index++, interview.getDifficulty());
                    statement.setString(index++, interview.getStatus());
                    statement.setTimestamp(index, Timestamp.valueOf(interview.getStartedAt()));
                    statement.executeUpdate();
                    if (interview.getId() <= 0) {
                        try (ResultSet keys = statement.getGeneratedKeys()) {
                            if (keys.next()) {
                                interview.setId(keys.getInt(1));
                            }
                            try (PreparedStatement delete = connection.prepareStatement(
                                    "DELETE FROM interview_questions WHERE interview_id = ?")) {
                                delete.setInt(1, interview.getId());
                                delete.executeUpdate();
                            }
                        }
                    }
                }
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO interview_questions (interview_id, question_id, sequence_no) VALUES (?, ?, ?)")) {
                    int order = 0;
                    for (Question question : interview.getQuestions()) {
                        insert.setInt(1, interview.getId());
                        insert.setInt(2, question.getId());
                        insert.setInt(3, order++);
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
            return null;
        });
    }

    @Override
    public void saveResultInTransaction(Interview interview, Result result) throws DataAccessException {
        if (interview == null || result == null) {
            throw new DataAccessException("Transaction aborted: Interview and Result must not be null.");
        }
        withConnection(connection -> {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE interviews SET status='COMPLETED' WHERE interview_id=?")) {
                    update.setInt(1, interview.getId());
                    if (update.executeUpdate() == 0) {
                        try (PreparedStatement exists = connection.prepareStatement(
                                "SELECT 1 FROM interviews WHERE interview_id = ?")) {
                            exists.setInt(1, interview.getId());
                            try (ResultSet rows = exists.executeQuery()) {
                                if (!rows.next()) {
                                    throw new SQLException("Interview with ID " + interview.getId()
                                            + " was not found.");
                                }
                            }
                        }
                    }
                }
                try (PreparedStatement answer = connection.prepareStatement(
                        "INSERT INTO answers (interview_id, question_id, answer_text, marks) VALUES (?, ?, ?, ?) "
                                + "ON DUPLICATE KEY UPDATE answer_text=VALUES(answer_text), marks=VALUES(marks)")) {
                    for (Question question : interview.getQuestions()) {
                        answer.setInt(1, interview.getId());
                        answer.setInt(2, question.getId());
                        answer.setString(3, interview.getAnswer(question.getId()));
                        answer.setDouble(4, interview.getMarks(question.getId()));
                        answer.addBatch();
                    }
                    answer.executeBatch();
                }
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO results (interview_id, student_id, total_score, max_score) VALUES (?, ?, ?, ?) "
                                + "ON DUPLICATE KEY UPDATE total_score=VALUES(total_score), max_score=VALUES(max_score)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    insert.setInt(1, interview.getId());
                    insert.setInt(2, interview.getStudentId());
                    insert.setDouble(3, result.getTotalScore());
                    insert.setDouble(4, result.getMaxScore());
                    insert.executeUpdate();
                    try (ResultSet keys = insert.getGeneratedKeys()) {
                        if (keys.next()) {
                            result.setId(keys.getInt(1));
                        }
                    }
                }

                Map<String, double[]> topicScores = new LinkedHashMap<>();
                for (Question question : interview.getQuestions()) {
                    double[] score = topicScores.computeIfAbsent(question.getTopic(), key -> new double[2]);
                    score[0] += interview.getMarks(question.getId());
                    score[1] += question.getMaxMarks();
                }
                try (PreparedStatement scoreStatement = connection.prepareStatement(
                        "INSERT INTO skill_scores (student_id, topic, score) VALUES (?, ?, ?) "
                                + "ON DUPLICATE KEY UPDATE score=VALUES(score)");
                     PreparedStatement recommendation = connection.prepareStatement(
                             "INSERT IGNORE INTO recommendations (student_id, message) VALUES (?, ?)")) {
                    for (Map.Entry<String, double[]> entry : topicScores.entrySet()) {
                        double percentage = entry.getValue()[1] == 0 ? 0
                                : entry.getValue()[0] * 100.0 / entry.getValue()[1];
                        scoreStatement.setInt(1, interview.getStudentId());
                        scoreStatement.setString(2, entry.getKey());
                        scoreStatement.setDouble(3, Math.round(percentage * 10.0) / 10.0);
                        scoreStatement.addBatch();
                        if (percentage < 50.0) {
                            String message = String.format(
                                    "Diagnostic alert: Score of %.1f%% in %s requires focused study and practice.",
                                    percentage, entry.getKey());
                            recommendation.setInt(1, interview.getStudentId());
                            recommendation.setString(2, message);
                            recommendation.addBatch();
                        }
                    }
                    scoreStatement.executeBatch();
                    recommendation.executeBatch();
                }
                connection.commit();
                interview.markCompleted();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
            return null;
        });
    }

    @Override
    public List<Result> findResultsByStudent(int studentId) throws DataAccessException {
        return withConnection(connection -> {
            List<Result> resultsList = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT result_id, interview_id, student_id, total_score, max_score FROM results "
                            + "WHERE student_id = ? ORDER BY created_at DESC")) {
                statement.setInt(1, studentId);
                try (ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        resultsList.add(new Result(results.getInt("result_id"), results.getInt("interview_id"),
                                results.getInt("student_id"), results.getDouble("total_score"),
                                results.getDouble("max_score")));
                    }
                }
            }
            return resultsList;
        });
    }

    @Override
    public Map<String, Double> findSkillScores(int studentId) throws DataAccessException {
        return withConnection(connection -> {
            Map<String, Double> scores = new LinkedHashMap<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT topic, score FROM skill_scores WHERE student_id = ? ORDER BY topic")) {
                statement.setInt(1, studentId);
                try (ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        scores.put(results.getString("topic"), results.getDouble("score"));
                    }
                }
            }
            return scores;
        });
    }

    @Override
    public List<String> findRecommendations(int studentId) throws DataAccessException {
        return withConnection(connection -> {
            List<String> recommendationsList = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT message FROM recommendations WHERE student_id = ? ORDER BY recommendation_id")) {
                statement.setInt(1, studentId);
                try (ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        recommendationsList.add(results.getString(1));
                    }
                }
            }
            return recommendationsList;
        });
    }

    @Override
    public Map<String, Integer> getPlatformStats() throws DataAccessException {
        return withConnection(connection -> {
            Map<String, Integer> stats = new LinkedHashMap<>();
            try (Statement statement = connection.createStatement();
                 ResultSet results = statement.executeQuery(
                         "SELECT (SELECT COUNT(*) FROM interviews) AS total_interviews, "
                                 + "(SELECT COUNT(*) FROM results) AS total_results, "
                                 + "(SELECT COUNT(DISTINCT student_id) FROM results) AS total_students")) {
                if (results.next()) {
                    stats.put("totalInterviews", results.getInt("total_interviews"));
                    stats.put("totalResults", results.getInt("total_results"));
                    stats.put("totalStudentsEvaluated", results.getInt("total_students"));
                }
            }
            return stats;
        });
    }

    @Override
    public Interview findInterviewById(int interviewId) throws DataAccessException {
        return withConnection(connection -> {
            String sql = "SELECT student_id, interview_type, difficulty, status, started_at "
                    + "FROM interviews WHERE interview_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, interviewId);
                try (ResultSet results = statement.executeQuery()) {
                    if (!results.next()) {
                        return null;
                    }
                    int studentId = results.getInt("student_id");
                    String type = results.getString("interview_type");
                    String difficulty = results.getString("difficulty");
                    Interview interview;
                    if ("HR".equals(type)) {
                        interview = new prepforge.model.HRInterview(interviewId, studentId, difficulty);
                    } else if ("TECHNICAL".equals(type)) {
                        interview = new TechnicalInterview(interviewId, studentId, difficulty);
                    } else {
                        interview = new MockInterview(interviewId, studentId, difficulty);
                    }
                    interview.setStatus(results.getString("status"));
                    Timestamp startedAt = results.getTimestamp("started_at");
                    List<Question> questions = new ArrayList<>();
                    Map<Integer, String> answersMap = new LinkedHashMap<>();
                    Map<Integer, Double> marksMap = new LinkedHashMap<>();
                    try (PreparedStatement questionStatement = connection.prepareStatement(
                            "SELECT question_id FROM interview_questions WHERE interview_id = ? ORDER BY sequence_no")) {
                        questionStatement.setInt(1, interviewId);
                        try (ResultSet questionRows = questionStatement.executeQuery()) {
                            while (questionRows.next()) {
                                int questionId = questionRows.getInt(1);
                                Question question = findQuestion(connection, questionId);
                                if (question != null) {
                                    questions.add(question);
                                    try (PreparedStatement answerStatement = connection.prepareStatement(
                                            "SELECT answer_text, marks FROM answers "
                                                    + "WHERE interview_id = ? AND question_id = ?")) {
                                        answerStatement.setInt(1, interviewId);
                                        answerStatement.setInt(2, questionId);
                                        try (ResultSet answerRows = answerStatement.executeQuery()) {
                                            if (answerRows.next()) {
                                                answersMap.put(questionId, answerRows.getString("answer_text"));
                                                marksMap.put(questionId, answerRows.getDouble("marks"));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    interview.restoreState(questions, answersMap, marksMap,
                            startedAt == null ? null : startedAt.toLocalDateTime());
                    return interview;
                }
            }
        });
    }
}
