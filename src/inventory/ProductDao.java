package inventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Database access for products and categories.
 */
public final class ProductDao {

    private static final String SELECT = "SELECT p.id, p.code, p.name, c.name AS category, p.supplier_id, s.name AS supplier, "
            + "p.unit_cost, p.selling_price, p.quantity, p.reorder_level "
            + "FROM products p JOIN categories c ON c.id = p.category_id "
            + "LEFT JOIN suppliers s ON s.id = p.supplier_id WHERE p.active = 1 ";

    private ProductDao() {
    }

    public static List<Product> findAll() throws SQLException {
        return query(SELECT + "ORDER BY p.name");
    }

    /** Products at or below their reorder level. */
    public static List<Product> findLowStock() throws SQLException {
        return query(SELECT + "AND p.quantity <= p.reorder_level ORDER BY p.quantity, p.name");
    }

    /** True if the code is used by any product, including archived ones. */
    public static boolean codeExists(String code) throws SQLException {
        try (PreparedStatement ps = Database.get().prepareStatement("SELECT 1 FROM products WHERE code = ?")) {
            Database.bind(ps, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Adds a product. Opening stock is recorded as its first stock-in transaction. */
    public static void add(Product p, int openingStock) throws SQLException {
        Connection c = Database.get();
        c.setAutoCommit(false);
        try {
            String sql = "INSERT INTO products (code, name, category_id, supplier_id, unit_cost, selling_price, quantity, reorder_level) "
                    + "VALUES (?, ?, (SELECT id FROM categories WHERE name = ?), ?, ?, ?, ?, ?)";
            int id;
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                Database.bind(ps, p.getCode(), p.getName(), p.getCategory(), supplierOrNull(p.getSupplierId()),
                        p.getUnitCost(), p.getSellingPrice(), openingStock, p.getReorderLevel());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    id = keys.getInt(1);
                }
            }
            if (openingStock > 0) {
                TransactionDao.insert(c, id, "IN", openingStock, p.getUnitCost(), p.getSupplierId(),
                        "OPENING", "Opening stock", Session.userId());
            }
            c.commit();
        } catch (SQLException ex) {
            c.rollback();
            throw ex;
        } finally {
            c.setAutoCommit(true);
        }
    }

    /** Updates product details (not the quantity). */
    public static void update(Product p) throws SQLException {
        String sql = "UPDATE products SET name = ?, category_id = (SELECT id FROM categories WHERE name = ?), supplier_id = ?, "
                + "unit_cost = ?, selling_price = ?, reorder_level = ? WHERE id = ?";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            Database.bind(ps, p.getName(), p.getCategory(), supplierOrNull(p.getSupplierId()),
                    p.getUnitCost(), p.getSellingPrice(), p.getReorderLevel(), p.getId());
            ps.executeUpdate();
        }
    }

    /** Hides the product from lists but keeps its transaction history. */
    public static void archive(int id) throws SQLException {
        try (PreparedStatement ps = Database.get().prepareStatement("UPDATE products SET active = 0 WHERE id = ?")) {
            Database.bind(ps, id);
            ps.executeUpdate();
        }
    }

    public static List<String> findCategories() throws SQLException {
        List<String> list = new ArrayList<>();
        try (Statement st = Database.get().createStatement();
                ResultSet rs = st.executeQuery("SELECT name FROM categories ORDER BY name")) {
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        }
        return list;
    }

    public static void addCategory(String name) throws SQLException {
        try (PreparedStatement ps = Database.get().prepareStatement("INSERT INTO categories (name) VALUES (?)")) {
            Database.bind(ps, name);
            ps.executeUpdate();
        }
    }

    private static Integer supplierOrNull(int supplierId) {
        return supplierId > 0 ? supplierId : null;
    }

    private static List<Product> query(String sql) throws SQLException {
        List<Product> list = new ArrayList<>();
        try (Statement st = Database.get().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Product(rs.getInt("id"), rs.getString("code"), rs.getString("name"),
                        rs.getString("category"), rs.getInt("supplier_id"), rs.getString("supplier"),
                        rs.getDouble("unit_cost"), rs.getDouble("selling_price"),
                        rs.getInt("quantity"), rs.getInt("reorder_level")));
            }
        }
        return list;
    }
}
