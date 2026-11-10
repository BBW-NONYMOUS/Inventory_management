package inventory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * System audit log: sign-ins and every change made to the inventory.
 */
public final class AuditDao {

    private AuditDao() {
    }

    /** Logs an action by the signed-in user. Never throws, so logging cannot block the action itself. */
    public static void log(String action, String details) {
        User u = Session.getUser();
        logAs(u == null ? null : u.getId(), u == null ? null : u.getUsername(), action, details);
    }

    public static void logAs(Integer userId, String username, String action, String details) {
        String sql = "INSERT INTO audit_logs (user_id, username, action, details) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            String d = details == null ? null : details.substring(0, Math.min(details.length(), 255));
            Database.bind(ps, userId, username, action, d);
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Audit log failed: " + ex.getMessage());
        }
    }

    /** Newest first. Columns: date/time, username, action, details. */
    public static List<Object[]> findAll(int limit) throws SQLException {
        String sql = "SELECT DATE_FORMAT(created_at, '%Y-%m-%d %H:%i:%s') AS created, COALESCE(username, '') AS username, "
                + "action, COALESCE(details, '') AS details FROM audit_logs ORDER BY id DESC LIMIT ?";
        List<Object[]> rows = new ArrayList<>();
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            Database.bind(ps, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{rs.getString("created"), rs.getString("username"),
                        rs.getString("action"), rs.getString("details")});
                }
            }
        }
        return rows;
    }
}
