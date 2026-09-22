package model;

public record User(int id, String fullName, String email, Role role) {
    public enum Role {
        ADMIN,
        LAB_ASSISTANT,
        STUDENT
    }
}
