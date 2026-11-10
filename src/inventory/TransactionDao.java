package inventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Stock-in / stock-out transactions. Every movement updates the product quantity in the same
 * database transaction, so the stock on record always matches the transaction log.
 */
public final class TransactionDao {

    private TransactionDao() {
    }

    /**
     * Records a stock movement and returns the product's new quantity.
     * Stock-in recomputes the unit cost as a weighted average of the old and new stock.
     *
     * @throws IllegalArgumentException if a stock-out is larger than the available stock
     */
    public static int record(int productId, String type, int quantity, double unitCost, int supplierId,
            String referenceNo, String remarks) throws SQLException {
        Connection c = Database.get();
        c.setAutoCommit(false);
        try {
            int current;
            double currentCost;
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT quantity, unit_cost FROM products WHERE id = ? AND active = 1 FOR UPDATE")) {
                Database.bind(ps, productId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Product not found.");
                    }
                    current = rs.getInt(1);
                    currentCost = rs.getDouble(2);
                }
            }

            boolean in = "IN".equals(type);
            if (!in && quantity > current) {
                throw new IllegalArgumentException("Not enough stock. Available: " + current);
            }
            int newQty = in ? current + quantity : current - quantity;
            double newCost = in
                    ? Math.round((current * currentCost + quantity * unitCost) / newQty * 100.0) / 100.0
                    : currentCost;

            try (PreparedStatement ps = c.prepareStatement("UPDATE products SET quantity = ?, unit_cost = ? WHERE id = ?")) {
                Database.bind(ps, newQty, newCost, productId);
                ps.executeUpdate();
            }
            insert(c, productId, type, quantity, in ? unitCost : currentCost, in ? supplierId : 0,
                    referenceNo, remarks, Session.userId());
            c.commit();
            return newQty;
        } catch (SQLException | RuntimeException ex) {
            c.rollback();
            throw ex;
        } finally {
            c.setAutoCommit(true);
        }
    }

    /** Inserts a log row only; the caller is responsible for updating the product quantity. */
    static void insert(Connection c, int productId, String type, int quantity, double unitCost, int supplierId,
            String referenceNo, String remarks, int userId) throws SQLException {
        String sql = "INSERT INTO stock_transactions (product_id, type, quantity, unit_cost, supplier_id, reference_no, remarks, user_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            Database.bind(ps, productId, type, quantity, unitCost, supplierId > 0 ? supplierId : null,
                    emptyToNull(referenceNo), emptyToNull(remarks), userId);
            ps.executeUpdate();
        }
    }

    /**
     * Transaction log, newest first. Columns: date/time, type, product, qty, unit cost, total,
     * supplier, reference no., user.
     *
     * @param type "IN", "OUT" or null for all
     */
    public static List<Object[]> findAll(String type, int limit) throws SQLException {
        String sql = "SELECT DATE_FORMAT(t.created_at, '%Y-%m-%d %H:%i') AS created, t.type, "
                + "CONCAT(p.code, ' - ', p.name) AS product, t.quantity, t.unit_cost, t.quantity * t.unit_cost AS total, "
                + "COALESCE(s.name, '') AS supplier, COALESCE(t.reference_no, '') AS ref, u.username "
                + "FROM stock_transactions t JOIN products p ON p.id = t.product_id "
                + "LEFT JOIN suppliers s ON s.id = t.supplier_id JOIN users u ON u.id = t.user_id "
                + (type == null ? "" : "WHERE t.type = ? ")
                + "ORDER BY t.id DESC LIMIT ?";
        List<Object[]> rows = new ArrayList<>();
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            if (type == null) {
                Database.bind(ps, limit);
            } else {
                Database.bind(ps, type, limit);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getString("created"), rs.getString("type"), rs.getString("product"), rs.getInt("quantity"),
                        rs.getDouble("unit_cost"), rs.getDouble("total"), rs.getString("supplier"),
                        rs.getString("ref"), rs.getString("username")
                    });
                }
            }
        }
        return rows;
    }

    private static String emptyToNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }
}
