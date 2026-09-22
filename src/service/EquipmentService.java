package service;

import config.DatabaseConnection;
import dao.EquipmentDao;
import model.Equipment;

import java.sql.SQLException;
import java.util.List;

public class EquipmentService {
    private final EquipmentDao equipmentDao;

    public EquipmentService(EquipmentDao equipmentDao) {
        this.equipmentDao = equipmentDao;
    }

    public List<Equipment> listAvailable() throws SQLException {
        try (var connection = DatabaseConnection.open()) {
            return equipmentDao.findAvailable(connection);
        }
    }
}
