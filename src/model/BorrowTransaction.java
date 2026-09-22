package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BorrowTransaction(
        int id,
        int userId,
        int equipmentId,
        LocalDate issueDate,
        LocalDate dueDate,
        LocalDate returnDate,
        String status,
        BigDecimal fineAmount
) {
}
