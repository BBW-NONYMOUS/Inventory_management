package inventory;

/**
 * A system account. Role is either ADMIN (full access) or STAFF (stock transactions and viewing).
 */
public class User {

    public static final String ADMIN = "ADMIN";
    public static final String STAFF = "STAFF";

    private final int id;
    private final String username;
    private final String fullName;
    private final String role;
    private final boolean active;
    private final String createdAt;

    public User(int id, String username, String fullName, String role, boolean active, String createdAt) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public String getRoleLabel() {
        return ADMIN.equals(role) ? "Admin" : "Staff";
    }

    public boolean isAdmin() {
        return ADMIN.equals(role);
    }

    public boolean isActive() {
        return active;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
