package prepforge.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import prepforge.exception.DataAccessException;

public class DBConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/prepforge?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "rootpassword";

    private static String url = DEFAULT_URL;
    private static String user = DEFAULT_USER;
    private static String password = DEFAULT_PASSWORD;

    static {
        loadConfiguration();
    }

    private DBConnection() {
    }

    private static void loadConfiguration() {
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                Properties props = new Properties();
                props.load(input);
                if (props.getProperty("db.url") != null) {
                    url = props.getProperty("db.url").trim();
                }
                if (props.getProperty("db.user") != null) {
                    user = props.getProperty("db.user").trim();
                }
                if (props.getProperty("db.password") != null) {
                    password = props.getProperty("db.password").trim();
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static Connection getConnection() throws DataAccessException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            throw new DataAccessException("MySQL JDBC driver not found on classpath: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to establish database connection to " + url + ": " + e.getMessage(), e);
        }
    }
}
