package inventory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Database access for suppliers.
 */
public final class SupplierDao {

    private SupplierDao() {
    }

    public static List<Supplier> findAll() throws SQLException {
        String sql = "SELECT s.*, (SELECT COUNT(*) FROM products p WHERE p.supplier_id = s.id AND p.active = 1) AS product_count "
                + "FROM suppliers s ORDER BY s.name";
        List<Supplier> list = new ArrayList<>();
        try (Statement st = Database.get().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Supplier(rs.getInt("id"), rs.getString("name"), nz(rs.getString("contact_person")),
                        nz(rs.getString("phone")), nz(rs.getString("email")), nz(rs.getString("address")),
                        rs.getInt("product_count")));
            }
        }
        return list;
    }

    public static void add(Supplier s) throws SQLException {
        String sql = "INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            Database.bind(ps, s.getName(), s.getContactPerson(), s.getPhone(), s.getEmail(), s.getAddress());
            ps.executeUpdate();
        }
    }

    public static void update(Supplier s) throws SQLException {
        String sql = "UPDATE suppliers SET name = ?, contact_person = ?, phone = ?, email = ?, address = ? WHERE id = ?";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            Database.bind(ps, s.getName(), s.getContactPerson(), s.getPhone(), s.getEmail(), s.getAddress(), s.getId());
            ps.executeUpdate();
        }
    }

    /** Products and transactions that referenced the supplier keep their data with no supplier. */
    public static void delete(int id) throws SQLException {
        try (PreparedStatement ps = Database.get().prepareStatement("DELETE FROM suppliers WHERE id = ?")) {
            Database.bind(ps, id);
            ps.executeUpdate();
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
