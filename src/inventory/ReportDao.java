package inventory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Valuation and stock movement summaries, computed by the database.
 */
public final class ReportDao {

    private ReportDao() {
    }

    /** Returns {product count, total units, stock value at cost, retail value}. */
    public static double[] valuation() throws SQLException {
        String sql = "SELECT COUNT(*), COALESCE(SUM(quantity), 0), COALESCE(SUM(stock_value), 0), "
                + "COALESCE(SUM(retail_value), 0) FROM v_inventory_valuation";
        try (Statement st = Database.get().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return new double[]{rs.getDouble(1), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4)};
        }
    }

    /** Returns {units in, units out} between two dates (yyyy-MM-dd, inclusive). */
    public static int[] movement(String from, String to) throws SQLException {
        String sql = "SELECT COALESCE(SUM(CASE WHEN type = 'IN' THEN quantity END), 0), "
                + "COALESCE(SUM(CASE WHEN type = 'OUT' THEN quantity END), 0) "
                + "FROM stock_transactions WHERE DATE(created_at) BETWEEN ? AND ?";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            Database.bind(ps, from, to);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new int[]{rs.getInt(1), rs.getInt(2)};
            }
        }
    }

    /**
     * Daily inventory summary, newest first. Columns: date, transactions, units in, units out,
     * net change, cost in, cost out.
     */
    public static List<Object[]> dailySummary(String from, String to) throws SQLException {
        String sql = "SELECT DATE_FORMAT(DATE(created_at), '%Y-%m-%d') AS day, COUNT(*) AS tx, "
                + "COALESCE(SUM(CASE WHEN type = 'IN' THEN quantity END), 0) AS units_in, "
                + "COALESCE(SUM(CASE WHEN type = 'OUT' THEN quantity END), 0) AS units_out, "
                + "COALESCE(SUM(CASE WHEN type = 'IN' THEN quantity * unit_cost END), 0) AS cost_in, "
                + "COALESCE(SUM(CASE WHEN type = 'OUT' THEN quantity * unit_cost END), 0) AS cost_out "
                + "FROM stock_transactions WHERE DATE(created_at) BETWEEN ? AND ? "
                + "GROUP BY DATE(created_at) ORDER BY DATE(created_at) DESC";
        List<Object[]> rows = new ArrayList<>();
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            Database.bind(ps, from, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int in = rs.getInt("units_in");
                    int out = rs.getInt("units_out");
                    rows.add(new Object[]{rs.getString("day"), rs.getInt("tx"), in, out, in - out,
                        rs.getDouble("cost_in"), rs.getDouble("cost_out")});
                }
            }
        }
        return rows;
    }

    /** Stock value per category. Columns: category, products, units, value. */
    public static List<Object[]> valuationByCategory() throws SQLException {
        String sql = "SELECT category, COUNT(*) AS items, SUM(quantity) AS units, SUM(stock_value) AS value "
                + "FROM v_inventory_valuation GROUP BY category ORDER BY value DESC";
        List<Object[]> rows = new ArrayList<>();
        try (Statement st = Database.get().createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new Object[]{rs.getString("category"), rs.getInt("items"), rs.getInt("units"), rs.getDouble("value")});
            }
        }
        return rows;
    }
}
