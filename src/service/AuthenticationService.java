package service;

import config.DatabaseConnection;
import dao.UserDao;
import model.User;

import java.sql.SQLException;

public class AuthenticationService {
    private final UserDao userDao;

    public AuthenticationService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User authenticate(String email, String password) throws SQLException {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Email and password are required.");
        }
        try (var connection = DatabaseConnection.open()) {
            return userDao.findByCredentials(connection, email.trim().toLowerCase(), password)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        }
    }
}
