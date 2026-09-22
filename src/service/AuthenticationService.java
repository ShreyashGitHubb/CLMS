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

    public User findUser(String email) throws SQLException {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        try (var connection = DatabaseConnection.open()) {
            return userDao.findByEmail(connection, email.trim().toLowerCase())
                    .orElseThrow(() -> new IllegalArgumentException("No user was found for that email."));
        }
    }
}
