package inventory;

import java.awt.Component;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * Sidebar navigation shared by all screens, plus role-based access checks.
 */
public final class Nav {

    public enum Page {
        DASHBOARD, PRODUCTS, STOCK, SUPPLIERS, REPORTS, USERS, AUDIT
    }

    private Nav() {
    }

    public static void open(JFrame from, Page page) {
        if ((page == Page.USERS || page == Page.AUDIT) && !Session.isAdmin()) {
            Ui.warn(from, "Only administrators can open this page.");
            return;
        }
        JFrame next;
        switch (page) {
            case PRODUCTS:
                next = new ProductsForm();
                break;
            case STOCK:
                next = new StockForm();
                break;
            case SUPPLIERS:
                next = new SuppliersForm();
                break;
            case REPORTS:
                next = new ReportsForm();
                break;
            case USERS:
                next = new UsersForm();
                break;
            case AUDIT:
                next = new AuditForm();
                break;
            default:
                next = new DashboardForm();
        }
        next.setVisible(true);
        from.dispose();
    }

    /** Shows the signed-in user and hides admin-only menu items from staff. */
    public static void setupSidebar(JLabel userLabel, JLabel... adminOnly) {
        User u = Session.getUser();
        userLabel.setText(u == null ? "" : u.getFullName() + " (" + u.getRoleLabel() + ")");
        for (JLabel item : adminOnly) {
            item.setVisible(Session.isAdmin());
        }
    }

    public static void logout(JFrame from) {
        if (!Ui.confirm(from, "Log out of the system?")) {
            return;
        }
        AuditDao.log("LOGOUT", "Signed out");
        Session.clear();
        new LoginForm().setVisible(true);
        from.dispose();
    }

    /** Pops up a warning listing products at or below their reorder level (nothing if none). */
    public static void showReorderAlert(Component parent) {
        try {
            List<Product> low = ProductDao.findLowStock();
            if (low.isEmpty()) {
                return;
            }
            StringBuilder sb = new StringBuilder("<html><b>")
                    .append(low.size()).append(" item(s) are at or below their reorder level:</b><br><br>");
            for (int i = 0; i < low.size(); i++) {
                if (i == 10) {
                    sb.append("...and ").append(low.size() - 10).append(" more (see Dashboard)<br>");
                    break;
                }
                Product p = low.get(i);
                sb.append(Ui.html(p.toString())).append(": <b>").append(p.getQuantity())
                        .append("</b> left (reorder at ").append(p.getReorderLevel()).append(")<br>");
            }
            JOptionPane.showMessageDialog(parent, sb.append("</html>").toString(), "Reorder Alert", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            Ui.error(parent, ex);
        }
    }
}
