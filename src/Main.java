import dao.EquipmentDao;
import model.Equipment;
import service.EquipmentService;

public class Main {
    public static void main(String[] args) {
        try {
            EquipmentService equipmentService = new EquipmentService(new EquipmentDao());
            System.out.println("Available equipment:");
            for (Equipment equipment : equipmentService.listAvailable()) {
                System.out.printf("- %s (%d/%d available)%n",
                        equipment.name(), equipment.availableQuantity(), equipment.totalQuantity());
            }
        } catch (Exception exception) {
            System.err.println("CLMS could not connect to the database: " + exception.getMessage());
            System.err.println("Check the JDBC driver and CLMS_DB_* environment variables.");
            System.exit(1);9:19 AM
39s
•
GPT-5.6 Luna • 0.9 credits
Restore Checkpoint
can you make the reade me perfectly formated and also every this is proper explained about th
        }
    }
}
