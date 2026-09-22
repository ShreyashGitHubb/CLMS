package model;

public record Equipment(
        int id,
        String name,
        String category,
        String description,
        int totalQuantity,
        int availableQuantity,
        String location,
        String condition,
        String status
) {
    public boolean isAvailable() {
        return availableQuantity > 0 && !"MAINTENANCE".equals(status) && !"RETIRED".equals(status);
    }
}
