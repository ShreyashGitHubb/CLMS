package dao;

import model.Equipment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EquipmentDao {
    private static final String FIND_AVAILABLE = "SELECT equipment_id, name, category, description, total_quantity, available_quantity, location, equipment_condition, status FROM equipment WHERE available_quantity > 0 AND status NOT IN ('MAINTENANCE', 'RETIRED') ORDER BY name";
    private static final String FIND_BY_ID = "SELECT equipment_id, name, category, description, total_quantity, available_quantity, location, equipment_condition, status FROM equipment WHERE equipment_id = ? FOR UPDATE";
    private static final String DECREASE_AVAILABLE = "UPDATE equipment SET available_quantity = available_quantity - 1, status = CASE WHEN available_quantity - 1 = 0 THEN 'LOW_STOCK' ELSE status END WHERE equipment_id = ? AND available_quantity > 0";
    private static final String INCREASE_AVAILABLE = "UPDATE equipment SET available_quantity = available_quantity + 1, status = CASE WHEN status = 'LOW_STOCK' THEN 'AVAILABLE' ELSE status END WHERE equipment_id = ? AND available_quantity < total_quantity";

    public List<Equipment> findAvailable(Connection connection) throws SQLException {
        List<Equipment> equipment = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(FIND_AVAILABLE);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                equipment.add(map(results));
            }
        }
        return equipment;
    }

    public Equipment findByIdForUpdate(Connection connection, int equipmentId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setInt(1, equipmentId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? map(results) : null;
            }
        }
    }

    public boolean decreaseAvailable(Connection connection, int equipmentId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DECREASE_AVAILABLE)) {
            statement.setInt(1, equipmentId);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean increaseAvailable(Connection connection, int equipmentId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INCREASE_AVAILABLE)) {
            statement.setInt(1, equipmentId);
            return statement.executeUpdate() == 1;
        }
    }

    private Equipment map(ResultSet results) throws SQLException {
        return new Equipment(
                results.getInt("equipment_id"),
                results.getString("name"),
                results.getString("category"),
                results.getString("description"),
                results.getInt("total_quantity"),
                results.getInt("available_quantity"),
                results.getString("location"),
                results.getString("equipment_condition"),
                results.getString("status")
        );
    }
}
