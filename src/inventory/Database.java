package inventory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * MySQL connection (XAMPP defaults) and first-run setup of tables and sample data.
 */
public final class Database {

    private static final String URL = "jdbc:mysql://localhost:3306/" + System.getProperty("inventory.db", "inventory_db")
            + "?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection;

    private Database() {
    }

    public static synchronized Connection get() throws SQLException {
        if (connection == null || !connection.isValid(2)) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    /** Creates the tables (if missing) and adds the default accounts and sample data on first run. */
    public static void init() throws SQLException, IOException {
        Connection c = get();
        try (Statement st = c.createStatement()) {
            for (String sql : loadSchema()) {
                st.execute(sql);
            }
        }
        seed(c);
    }

    private static String[] loadSchema() throws IOException {
        try (InputStream in = Database.class.getResourceAsStream("/inventory/schema.sql")) {
            if (in == null) {
                throw new IOException("schema.sql not found on the classpath");
            }
            StringBuilder sb = new StringBuilder();
            for (String line : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\n")) {
                if (!line.trim().startsWith("--")) {
                    sb.append(line).append('\n');
                }
            }
            return java.util.Arrays.stream(sb.toString().split(";"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
        }
    }

    static void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    static int count(String table) throws SQLException {
        try (Statement st = get().createStatement();
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static void seed(Connection c) throws SQLException {
        if (count("users") == 0) {
            String sql = "INSERT INTO users (username, password_hash, full_name, role) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                bind(ps, "admin", PasswordUtil.hash("admin123"), "System Administrator", User.ADMIN);
                ps.executeUpdate();
                bind(ps, "staff", PasswordUtil.hash("staff123"), "Inventory Staff", User.STAFF);
                ps.executeUpdate();
            }
        }
        if (count("categories") == 0) {
            String[] categories = {"Beverages", "Snacks", "Canned Goods", "Personal Care", "Household", "School Supplies", "Others"};
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO categories (name) VALUES (?)")) {
                for (String name : categories) {
                    bind(ps, name);
                    ps.executeUpdate();
                }
            }
        }
        if (count("suppliers") == 0 && count("products") == 0) {
            seedSampleData(c);
        }
    }

    private static void seedSampleData(Connection c) throws SQLException {
        String supplierSql = "INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES (?, ?, ?, ?, ?)";
        Object[][] suppliers = {
            {"Metro Wholesale Trading", "Ana Reyes", "0917 555 1234", "orders@metrowholesale.ph", "Koronadal City"},
            {"Sultan Distributors Inc.", "Mark Santos", "0998 555 6789", "sales@sultandist.ph", "Tacurong City"},
        };
        int[] supplierIds = new int[suppliers.length];
        try (PreparedStatement ps = c.prepareStatement(supplierSql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < suppliers.length; i++) {
                bind(ps, suppliers[i]);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    supplierIds[i] = keys.getInt(1);
                }
            }
        }
        int adminId;
        try (Statement st = c.createStatement();
                ResultSet rs = st.executeQuery("SELECT id FROM users WHERE username = 'admin'")) {
            rs.next();
            adminId = rs.getInt(1);
        }
        int metro = supplierIds[0];
        int sultan = supplierIds[1];
        Object[][] products = {
            {"P001", "Coca-Cola 1.5L", "Beverages", metro, 62.00, 75.00, 48, 12},
            {"P002", "Piattos Cheese 85g", "Snacks", metro, 30.00, 38.50, 8, 10},
            {"P003", "Century Tuna 155g", "Canned Goods", sultan, 34.50, 42.00, 60, 15},
            {"P004", "Safeguard Soap 135g", "Personal Care", sultan, 45.00, 55.00, 0, 10},
            {"P005", "Joy Dishwashing 250ml", "Household", sultan, 55.00, 68.75, 22, 8},
            {"P006", "Ballpen (Black)", "School Supplies", metro, 8.00, 12.00, 5, 20},
        };
        String productSql = "INSERT INTO products (code, name, category_id, supplier_id, unit_cost, selling_price, quantity, reorder_level) "
                + "VALUES (?, ?, (SELECT id FROM categories WHERE name = ?), ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(productSql, Statement.RETURN_GENERATED_KEYS)) {
            for (Object[] p : products) {
                bind(ps, p);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    int qty = (Integer) p[6];
                    if (qty > 0) {
                        TransactionDao.insert(c, keys.getInt(1), "IN", qty, (Double) p[4], (Integer) p[3],
                                "OPENING", "Opening stock", adminId);
                    }
                }
            }
        }
    }
}
