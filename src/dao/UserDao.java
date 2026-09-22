package dao;

import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDao {
    private static final String FIND_BY_CREDENTIALS = "SELECT user_id, full_name, email, role FROM users WHERE email = ? AND password_hash = ?";

    public Optional<User> findByCredentials(Connection connection, String email, String password) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_CREDENTIALS)) {
            statement.setString(1, email);
            statement.setString(2, password);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return Optional.empty();
                }
                return Optional.of(new User(
                        results.getInt("user_id"),
                        results.getString("full_name"),
                        results.getString("email"),
                        User.Role.valueOf(results.getString("role"))
                ));
            }
        }
    }
}
