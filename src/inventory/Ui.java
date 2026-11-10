package inventory;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Shared table setup, formatting and message helpers.
 */
public final class Ui {

    public static final Color GREEN = new Color(22, 163, 74);
    public static final Color AMBER = new Color(217, 119, 6);
    public static final Color RED = new Color(220, 38, 38);

    private static final NumberFormat PESO = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));

    private Ui() {
    }

    public static String peso(double amount) {
        return PESO.format(amount);
    }

    /** Read-only table model whose numeric columns sort as numbers. */
    public static DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                for (int r = 0; r < getRowCount(); r++) {
                    Object v = getValueAt(r, column);
                    if (v != null) {
                        return v.getClass();
                    }
                }
                return Object.class;
            }
        };
    }

    public static TableRowSorter<DefaultTableModel> setupTable(JTable table, DefaultTableModel model, int... widths) {
        table.setModel(model);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setReorderingAllowed(false);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        return sorter;
    }

    /** Shows the given columns (holding Double values) as peso amounts. */
    public static void moneyColumns(JTable table, int... columns) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setText(value instanceof Number ? peso(((Number) value).doubleValue()) : String.valueOf(value));
            }
        };
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c : columns) {
            table.getColumnModel().getColumn(c).setCellRenderer(renderer);
        }
    }

    /**
     * Filters rows by free-text search (any column) and, optionally, an exact value in one column.
     *
     * @param exactValue null to skip the column filter
     */
    public static void filter(TableRowSorter<DefaultTableModel> sorter, String search, int exactColumn, String exactValue) {
        List<RowFilter<DefaultTableModel, Integer>> filters = new ArrayList<>();
        if (exactValue != null) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(exactValue) + "$", exactColumn));
        }
        String text = search == null ? "" : search.trim();
        if (!text.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
        }
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    public static void filter(TableRowSorter<DefaultTableModel> sorter, String search) {
        filter(sorter, search, 0, null);
    }

    public static String html(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Inventory", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void warn(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Inventory", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Inventory", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static void error(Component parent, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(parent, "Database error: " + ex.getMessage(), "Inventory", JOptionPane.ERROR_MESSAGE);
    }

    /** Colors product status: green = in stock, amber = reorder, red = out of stock. */
    public static class StatusRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(getFont().deriveFont(Font.BOLD));
            if (!isSelected) {
                setForeground(Product.OUT_OF_STOCK.equals(value) ? RED : Product.REORDER.equals(value) ? AMBER : GREEN);
            }
            return this;
        }
    }

    /** Colors transaction type: green = IN, red = OUT. */
    public static class TypeRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(getFont().deriveFont(Font.BOLD));
            setHorizontalAlignment(SwingConstants.CENTER);
            if (!isSelected) {
                setForeground("IN".equals(value) ? GREEN : RED);
            }
            return this;
        }
    }
}
