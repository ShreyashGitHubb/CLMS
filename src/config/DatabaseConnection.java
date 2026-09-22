package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/clms?serverTimezone=UTC";
    private static final String DEFAULT_USER = "clms_app";
    private static final String DEFAULT_PASSWORD = "your_password";

    private DatabaseConnection() {
    }

    public static Connection open() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("MySQL Connector/J is missing from the runtime classpath.", exception);
        }
        String url = environmentOrDefault("CLMS_DB_URL", DEFAULT_URL);
        String user = environmentOrDefault("CLMS_DB_USER", DEFAULT_USER);
        String password = environmentOrDefault("CLMS_DB_PASSWORD", DEFAULT_PASSWORD);
        return DriverManager.getConnection(url, user, password);
    }

    private static String environmentOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
