package dao;

import model.BorrowTransaction;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class TransactionDao {
    private static final String INSERT_REQUEST = "INSERT INTO borrow_transactions (user_id, equipment_id, due_date, status, fine_amount) VALUES (?, ?, ?, 'PENDING', 0.00)";
    private static final String FIND_BY_ID = "SELECT transaction_id, user_id, equipment_id, issue_date, due_date, return_date, status, fine_amount FROM borrow_transactions WHERE transaction_id = ? FOR UPDATE";
    private static final String APPROVE = "UPDATE borrow_transactions SET issue_date = ?, status = 'APPROVED', approved_by = ? WHERE transaction_id = ? AND status = 'PENDING'";
    private static final String RETURN = "UPDATE borrow_transactions SET return_date = ?, status = 'RETURNED', fine_amount = ? WHERE transaction_id = ? AND status = 'APPROVED'";

    public int createRequest(Connection connection, int userId, int equipmentId, LocalDate dueDate) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_REQUEST, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, userId);
            statement.setInt(2, equipmentId);
            statement.setDate(3, Date.valueOf(dueDate));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("The transaction id was not generated.");
                }
                return keys.getInt(1);
            }
        }
    }

    public BorrowTransaction findByIdForUpdate(Connection connection, int transactionId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setInt(1, transactionId);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return null;
                }
                Date issueDate = results.getDate("issue_date");
                Date returnDate = results.getDate("return_date");
                return new BorrowTransaction(
                        results.getInt("transaction_id"),
                        results.getInt("user_id"),
                        results.getInt("equipment_id"),
                        issueDate == null ? null : issueDate.toLocalDate(),
                        results.getDate("due_date").toLocalDate(),
                        returnDate == null ? null : returnDate.toLocalDate(),
                        results.getString("status"),
                        results.getBigDecimal("fine_amount")
                );
            }
        }
    }

    public boolean approve(Connection connection, int transactionId, int approverId, LocalDate issueDate) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(APPROVE)) {
            statement.setDate(1, Date.valueOf(issueDate));
            statement.setInt(2, approverId);
            statement.setInt(3, transactionId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean markReturned(Connection connection, int transactionId, LocalDate returnDate, double fine) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(RETURN)) {
            statement.setDate(1, Date.valueOf(returnDate));
            statement.setBigDecimal(2, java.math.BigDecimal.valueOf(fine));
            statement.setInt(3, transactionId);
            return statement.executeUpdate() == 1;
        }
    }
}
