package inventory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Database access for user accounts.
 */
public final class UserDao {

    private static final String SELECT = "SELECT id, username, full_name, role, active, "
            + "DATE_FORMAT(created_at, '%Y-%m-%d') AS created, password_hash FROM users ";

    private UserDao() {
    }

    /** Returns the user if the username and password match (active or not), otherwise null. */
    public static User authenticate(String username, String password) throws SQLException {
        try (PreparedStatement ps = Database.get().prepareStatement(SELECT + "WHERE username = ?")) {
            Database.bind(ps, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && PasswordUtil.verify(password, rs.getString("password_hash"))) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public static List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        try (Statement st = Database.get().createStatement();
                ResultSet rs = st.executeQuery(SELECT + "ORDER BY username")) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public static boolean usernameExists(String username) throws SQLException {
        try (PreparedStatement ps = Database.get().prepareStatement("SELECT 1 FROM users WHERE username = ?")) {
            Database.bind(ps, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static void add(String username, String fullName, String role, boolean active, String password) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, full_name, role, active) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            Database.bind(ps, username, PasswordUtil.hash(password), fullName, role, active);
            ps.executeUpdate();
        }
    }

    /** Updates an account. Pass an empty password to keep the current one. */
    public static void update(int id, String fullName, String role, boolean active, String password) throws SQLException {
        boolean newPassword = password != null && !password.isEmpty();
        String sql = "UPDATE users SET full_name = ?, role = ?, active = ?" + (newPassword ? ", password_hash = ?" : "") + " WHERE id = ?";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            if (newPassword) {
                Database.bind(ps, fullName, role, active, PasswordUtil.hash(password), id);
            } else {
                Database.bind(ps, fullName, role, active, id);
            }
            ps.executeUpdate();
        }
    }

    private static User map(ResultSet rs) throws SQLException {
        return new User(rs.getInt("id"), rs.getString("username"), rs.getString("full_name"),
                rs.getString("role"), rs.getBoolean("active"), rs.getString("created"));
    }
}
