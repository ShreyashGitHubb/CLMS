package service;

import config.DatabaseConnection;
import dao.EquipmentDao;
import dao.TransactionDao;
import exception.EquipmentNotAvailableException;
import exception.InvalidTransactionException;
import model.BorrowTransaction;
import model.Equipment;

import java.sql.SQLException;
import java.time.LocalDate;

public class BorrowService {
    private static final double DAILY_FINE = 10.0;
    private final EquipmentDao equipmentDao;
    private final TransactionDao transactionDao;

    public BorrowService(EquipmentDao equipmentDao, TransactionDao transactionDao) {
        this.equipmentDao = equipmentDao;
        this.transactionDao = transactionDao;
    }

    public int requestEquipment(int userId, int equipmentId, LocalDate dueDate)
            throws SQLException, InvalidTransactionException, EquipmentNotAvailableException {
        validateDueDate(dueDate);
        try (var connection = DatabaseConnection.open()) {
            connection.setAutoCommit(false);
            try {
                Equipment equipment = equipmentDao.findByIdForUpdate(connection, equipmentId);
                if (equipment == null || !equipment.isAvailable()) {
                    throw new EquipmentNotAvailableException("Equipment is unavailable for borrowing.");
                }
                int transactionId = transactionDao.createRequest(connection, userId, equipmentId, dueDate);
                connection.commit();
                return transactionId;
            } catch (SQLException | EquipmentNotAvailableException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public void approveRequest(int transactionId, int approverId)
            throws SQLException, InvalidTransactionException, EquipmentNotAvailableException {
        try (var connection = DatabaseConnection.open()) {
            connection.setAutoCommit(false);
            try {
                BorrowTransaction transaction = transactionDao.findByIdForUpdate(connection, transactionId);
                if (transaction == null || !"PENDING".equals(transaction.status())) {
                    throw new InvalidTransactionException("Only pending requests can be approved.");
                }
                if (!equipmentDao.decreaseAvailable(connection, transaction.equipmentId())) {
                    throw new EquipmentNotAvailableException("Equipment is no longer available.");
                }
                if (!transactionDao.approve(connection, transactionId, approverId, LocalDate.now())) {
                    throw new InvalidTransactionException("The request could not be approved.");
                }
                connection.commit();
            } catch (SQLException | InvalidTransactionException | EquipmentNotAvailableException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public void returnEquipment(int transactionId)
            throws SQLException, InvalidTransactionException {
        try (var connection = DatabaseConnection.open()) {
            connection.setAutoCommit(false);
            try {
                BorrowTransaction transaction = transactionDao.findByIdForUpdate(connection, transactionId);
                if (transaction == null || !"APPROVED".equals(transaction.status())) {
                    throw new InvalidTransactionException("Only approved requests can be returned.");
                }
                LocalDate returnDate = LocalDate.now();
                long overdueDays = Math.max(0, returnDate.toEpochDay() - transaction.dueDate().toEpochDay());
                double fine = overdueDays * DAILY_FINE;
                if (!equipmentDao.increaseAvailable(connection, transaction.equipmentId())
                        || !transactionDao.markReturned(connection, transactionId, returnDate, fine)) {
                    throw new InvalidTransactionException("The equipment could not be marked as returned.");
                }
                connection.commit();
            } catch (SQLException | InvalidTransactionException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private void validateDueDate(LocalDate dueDate) throws InvalidTransactionException {
        if (dueDate == null || !dueDate.isAfter(LocalDate.now())) {
            throw new InvalidTransactionException("Due date must be after today.");
        }
    }
}
