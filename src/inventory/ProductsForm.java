package inventory;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Product records: details form on the left, searchable product list on the right.
 * Admins can add, update and archive; staff can only view.
 */
public class ProductsForm extends javax.swing.JFrame {

    private static final String ALL_CATEGORIES = "All categories";

    private final DefaultTableModel model = Ui.tableModel("Code", "Name", "Category", "Qty", "Reorder", "Unit Cost", "Value", "Status");
    private final TableRowSorter<DefaultTableModel> sorter;
    private List<Product> products = new ArrayList<>();
    private Product selected;

    public ProductsForm() {
        initComponents();
        Nav.setupSidebar(userLabel, usersNav, auditNav);
        sorter = Ui.setupTable(productTable, model, 45, 125, 85, 35, 55, 70, 80, 95);
        Ui.moneyColumns(productTable, 5, 6);
        productTable.getColumnModel().getColumn(7).setCellRenderer(new Ui.StatusRenderer());
        searchField.setToolTipText("Search by code, name or category");

        boolean admin = Session.isAdmin();
        for (javax.swing.JComponent c : new javax.swing.JComponent[]{addButton, updateButton, archiveButton, addCategoryButton}) {
            c.setVisible(admin);
        }
        roleNoteLabel.setText(admin
                ? "<html>Quantities change only through <b>Stock In / Out</b>. Opening stock is saved as the first stock-in.</html>"
                : "<html><b>View only.</b> Staff accounts cannot add or edit products.</html>");

        loadCombos();
        loadProducts();
        clearForm();
    }

    private void loadCombos() {
        try {
            List<String> categories = ProductDao.findCategories();
            categoryCombo.setModel(new DefaultComboBoxModel<>(categories.toArray(new String[0])));
            DefaultComboBoxModel<String> filter = new DefaultComboBoxModel<>();
            filter.addElement(ALL_CATEGORIES);
            categories.forEach(filter::addElement);
            categoryFilter.setModel(filter);

            DefaultComboBoxModel<Supplier> suppliers = new DefaultComboBoxModel<>();
            suppliers.addElement(Supplier.NONE);
            SupplierDao.findAll().forEach(suppliers::addElement);
            supplierCombo.setModel(suppliers);
        } catch (SQLException ex) {
            Ui.error(this, ex);
        }
    }

    private void loadProducts() {
        try {
            products = ProductDao.findAll();
        } catch (SQLException ex) {
            Ui.error(this, ex);
        }
        model.setRowCount(0);
        for (Product p : products) {
            model.addRow(new Object[]{p.getCode(), p.getName(), p.getCategory(), p.getQuantity(),
                p.getReorderLevel(), p.getUnitCost(), p.getValue(), p.getStatus()});
        }
        applyFilter();
    }

    private void applyFilter() {
        if (sorter == null) {
            return;
        }
        String category = (String) categoryFilter.getSelectedItem();
        Ui.filter(sorter, searchField.getText(), 2, ALL_CATEGORIES.equals(category) ? null : category);
    }

    /** Reads and validates the form. Returns null (after showing a message) if invalid. */
    private Product readForm() {
        String code = codeField.getText().trim().toUpperCase();
        String name = nameField.getText().trim();
        String category = (String) categoryCombo.getSelectedItem();
        Supplier supplier = (Supplier) supplierCombo.getSelectedItem();
        if (code.isEmpty() || name.isEmpty() || category == null) {
            Ui.warn(this, "Product code, name and category are required.");
            return null;
        }
        double unitCost;
        double sellingPrice;
        int reorder;
        try {
            unitCost = Double.parseDouble(unitCostField.getText().trim());
            sellingPrice = Double.parseDouble(sellingPriceField.getText().trim());
            reorder = Integer.parseInt(reorderField.getText().trim());
        } catch (NumberFormatException ex) {
            Ui.warn(this, "Unit cost and selling price must be numbers, and reorder level a whole number.");
            return null;
        }
        if (unitCost < 0 || sellingPrice < 0 || reorder < 0) {
            Ui.warn(this, "Cost, price and reorder level cannot be negative.");
            return null;
        }
        return new Product(selected == null ? 0 : selected.getId(), code, name, category,
                supplier == null ? 0 : supplier.getId(), null, unitCost, sellingPrice,
                selected == null ? 0 : selected.getQuantity(), reorder);
    }

    private void fillForm(Product p) {
        selected = p;
        codeField.setText(p.getCode());
        codeField.setEditable(false);
        nameField.setText(p.getName());
        categoryCombo.setSelectedItem(p.getCategory());
        supplierCombo.setSelectedItem(Supplier.NONE);
        for (int i = 0; i < supplierCombo.getItemCount(); i++) {
            if (supplierCombo.getItemAt(i).getId() == p.getSupplierId()) {
                supplierCombo.setSelectedIndex(i);
            }
        }
        unitCostField.setText(String.format(Locale.US, "%.2f", p.getUnitCost()));
        sellingPriceField.setText(String.format(Locale.US, "%.2f", p.getSellingPrice()));
        reorderField.setText(String.valueOf(p.getReorderLevel()));
        openingStockField.setText("");
        openingStockField.setEnabled(false);
        stockInfoLabel.setText("In stock: " + p.getQuantity() + "   Value: " + Ui.peso(p.getValue()));
    }

    private void clearForm() {
        selected = null;
        codeField.setText("");
        codeField.setEditable(true);
        nameField.setText("");
        if (categoryCombo.getItemCount() > 0) {
            categoryCombo.setSelectedIndex(0);
        }
        supplierCombo.setSelectedItem(Supplier.NONE);
        unitCostField.setText("");
        sellingPriceField.setText("");
        reorderField.setText("10");
        openingStockField.setText("0");
        openingStockField.setEnabled(true);
        stockInfoLabel.setText("New product");
        productTable.clearSelection();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        sidebarPanel = new javax.swing.JPanel();
        brandLabel = new javax.swing.JLabel();
        brandSubLabel = new javax.swing.JLabel();
        menuLabel = new javax.swing.JLabel();
        dashboardNav = new javax.swing.JLabel();
        productsNav = new javax.swing.JLabel();
        stockNav = new javax.swing.JLabel();
        suppliersNav = new javax.swing.JLabel();
        reportsNav = new javax.swing.JLabel();
        usersNav = new javax.swing.JLabel();
        auditNav = new javax.swing.JLabel();
        userLabel = new javax.swing.JLabel();
        logoutNav = new javax.swing.JLabel();
        headerLabel = new javax.swing.JLabel();
        headerSubLabel = new javax.swing.JLabel();
        formPanel = new javax.swing.JPanel();
        formTitleLabel = new javax.swing.JLabel();
        codeLabel = new javax.swing.JLabel();
        codeField = new javax.swing.JTextField();
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        categoryLabel = new javax.swing.JLabel();
        categoryCombo = new javax.swing.JComboBox<>();
        addCategoryButton = new javax.swing.JButton();
        supplierLabel = new javax.swing.JLabel();
        supplierCombo = new javax.swing.JComboBox<>();
        unitCostLabel = new javax.swing.JLabel();
        unitCostField = new javax.swing.JTextField();
        sellingPriceLabel = new javax.swing.JLabel();
        sellingPriceField = new javax.swing.JTextField();
        reorderLabel = new javax.swing.JLabel();
        reorderField = new javax.swing.JTextField();
        openingStockLabel = new javax.swing.JLabel();
        openingStockField = new javax.swing.JTextField();
        stockInfoLabel = new javax.swing.JLabel();
        addButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();
        archiveButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        roleNoteLabel = new javax.swing.JLabel();
        tablePanel = new javax.swing.JPanel();
        tableTitleLabel = new javax.swing.JLabel();
        categoryFilter = new javax.swing.JComboBox<>();
        searchLabel = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        tableScroll = new javax.swing.JScrollPane();
        productTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("StockWise - Products");
        setResizable(false);

        mainPanel.setBackground(new java.awt.Color(241, 245, 249));
        mainPanel.setPreferredSize(new java.awt.Dimension(1160, 720));

        mainPanel.setLayout(null);

        sidebarPanel.setBackground(new java.awt.Color(15, 23, 42));

        sidebarPanel.setLayout(null);

        brandLabel.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        brandLabel.setForeground(new java.awt.Color(255, 255, 255));
        brandLabel.setText("StockWise");
        sidebarPanel.add(brandLabel);
        brandLabel.setBounds(24, 28, 180, 32);

        brandSubLabel.setFont(new java.awt.Font("Segoe UI", 0, 11)); // NOI18N
        brandSubLabel.setForeground(new java.awt.Color(148, 163, 184));
        brandSubLabel.setText("Inventory Management System");
        sidebarPanel.add(brandSubLabel);
        brandSubLabel.setBounds(24, 60, 190, 18);

        menuLabel.setFont(new java.awt.Font("Segoe UI", 1, 11)); // NOI18N
        menuLabel.setForeground(new java.awt.Color(100, 116, 139));
        menuLabel.setText("MENU");
        sidebarPanel.add(menuLabel);
        menuLabel.setBounds(24, 108, 180, 18);

        dashboardNav.setBackground(new java.awt.Color(15, 23, 42));
        dashboardNav.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        dashboardNav.setForeground(new java.awt.Color(148, 163, 184));
        dashboardNav.setText("Dashboard");
        dashboardNav.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 0, 0));
        dashboardNav.setOpaque(true);
        dashboardNav.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        dashboardNav.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dashboardNavMouseClicked(evt);
            }
        });
        sidebarPanel.add(dashboardNav);
        dashboardNav.setBounds(14, 132, 192, 40);

        productsNav.setBackground(new java.awt.Color(30, 41, 59));
        productsNav.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        productsNav.setForeground(new java.awt.Color(255, 255, 255));
        productsNav.setText("Products");
        productsNav.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 0, 0));
        productsNav.setOpaque(true);
        productsNav.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        productsNav.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                productsNavMouseClicked(evt);
            }
        });
        sidebarPanel.add(productsNav);
        productsNav.setBounds(14, 178, 192, 40);

        stockNav.setBackground(new java.awt.Color(15, 23, 42));
        stockNav.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        stockNav.setForeground(new java.awt.Color(148, 163, 184));
        stockNav.setText("Stock In / Out");
        stockNav.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 0, 0));
        stockNav.setOpaque(true);
        stockNav.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        stockNav.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                stockNavMouseClicked(evt);
            }
        });
        sidebarPanel.add(stockNav);
        stockNav.setBounds(14, 224, 192, 40);

        suppliersNav.setBackground(new java.awt.Color(15, 23, 42));
        suppliersNav.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        suppliersNav.setForeground(new java.awt.Color(148, 163, 184));
        suppliersNav.setText("Suppliers");
        suppliersNav.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 0, 0));
        suppliersNav.setOpaque(true);
        suppliersNav.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        suppliersNav.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                suppliersNavMouseClicked(evt);
            }
        });
        sidebarPanel.add(suppliersNav);
        suppliersNav.setBounds(14, 270, 192, 40);

        reportsNav.setBackground(new java.awt.Color(15, 23, 42));
        reportsNav.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        reportsNav.setForeground(new java.awt.Color(148, 163, 184));
        reportsNav.setText("Reports");
        reportsNav.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 0, 0));
        reportsNav.setOpaque(true);
        reportsNav.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        reportsNav.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                reportsNavMouseClicked(evt);
            }
        });
        sidebarPanel.add(reportsNav);
        reportsNav.setBounds(14, 316, 192, 40);

        usersNav.setBackground(new java.awt.Color(15, 23, 42));
        usersNav.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        usersNav.setForeground(new java.awt.Color(148, 163, 184));
        usersNav.setText("User Accounts");
        usersNav.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 0, 0));
        usersNav.setOpaque(true);
        usersNav.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        usersNav.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                usersNavMouseClicked(evt);
            }
        });
        sidebarPanel.add(usersNav);
        usersNav.setBounds(14, 362, 192, 40);

        auditNav.setBackground(new java.awt.Color(15, 23, 42));
        auditNav.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        auditNav.setForeground(new java.awt.Color(148, 163, 184));
        auditNav.setText("Audit Log");
        auditNav.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 0, 0));
        auditNav.setOpaque(true);
        auditNav.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        auditNav.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                auditNavMouseClicked(evt);
            }
        });
        sidebarPanel.add(auditNav);
        auditNav.setBounds(14, 408, 192, 40);

        userLabel.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        userLabel.setForeground(new java.awt.Color(148, 163, 184));
        userLabel.setText("User");
        sidebarPanel.add(userLabel);
        userLabel.setBounds(24, 612, 190, 20);

        logoutNav.setBackground(new java.awt.Color(15, 23, 42));
        logoutNav.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        logoutNav.setForeground(new java.awt.Color(148, 163, 184));
        logoutNav.setText("Log out");
        logoutNav.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 0, 0));
        logoutNav.setOpaque(true);
        logoutNav.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        logoutNav.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                logoutNavMouseClicked(evt);
            }
        });
        sidebarPanel.add(logoutNav);
        logoutNav.setBounds(14, 640, 192, 40);

        mainPanel.add(sidebarPanel);
        sidebarPanel.setBounds(0, 0, 220, 720);

        headerLabel.setFont(new java.awt.Font("Segoe UI", 1, 26)); // NOI18N
        headerLabel.setForeground(new java.awt.Color(15, 23, 42));
        headerLabel.setText("Products");
        mainPanel.add(headerLabel);
        headerLabel.setBounds(250, 22, 860, 36);

        headerSubLabel.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        headerSubLabel.setForeground(new java.awt.Color(100, 116, 139));
        headerSubLabel.setText("Add and look up products. Quantities change only through Stock In / Out.");
        mainPanel.add(headerSubLabel);
        headerSubLabel.setBounds(250, 58, 860, 20);

        formPanel.setBackground(new java.awt.Color(255, 255, 255));

        formPanel.setLayout(null);

        formTitleLabel.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        formTitleLabel.setForeground(new java.awt.Color(15, 23, 42));
        formTitleLabel.setText("Product Details");
        formPanel.add(formTitleLabel);
        formTitleLabel.setBounds(20, 16, 300, 24);

        codeLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        codeLabel.setForeground(new java.awt.Color(100, 116, 139));
        codeLabel.setText("Product Code");
        formPanel.add(codeLabel);
        codeLabel.setBounds(20, 52, 260, 18);

        codeField.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        formPanel.add(codeField);
        codeField.setBounds(20, 70, 260, 30);

        nameLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        nameLabel.setForeground(new java.awt.Color(100, 116, 139));
        nameLabel.setText("Product Name");
        formPanel.add(nameLabel);
        nameLabel.setBounds(20, 106, 260, 18);

        nameField.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        formPanel.add(nameField);
        nameField.setBounds(20, 124, 260, 30);

        categoryLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        categoryLabel.setForeground(new java.awt.Color(100, 116, 139));
        categoryLabel.setText("Category");
        formPanel.add(categoryLabel);
        categoryLabel.setBounds(20, 160, 260, 18);

        categoryCombo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        formPanel.add(categoryCombo);
        categoryCombo.setBounds(20, 178, 214, 30);

        addCategoryButton.setBackground(new java.awt.Color(100, 116, 139));
        addCategoryButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        addCategoryButton.setForeground(new java.awt.Color(255, 255, 255));
        addCategoryButton.setText("+");
        addCategoryButton.setBorderPainted(false);
        addCategoryButton.setContentAreaFilled(false);
        addCategoryButton.setFocusPainted(false);
        addCategoryButton.setOpaque(true);
        addCategoryButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addCategoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCategoryButtonActionPerformed(evt);
            }
        });
        formPanel.add(addCategoryButton);
        addCategoryButton.setBounds(240, 178, 40, 30);

        supplierLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        supplierLabel.setForeground(new java.awt.Color(100, 116, 139));
        supplierLabel.setText("Supplier");
        formPanel.add(supplierLabel);
        supplierLabel.setBounds(20, 214, 260, 18);

        supplierCombo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        formPanel.add(supplierCombo);
        supplierCombo.setBounds(20, 232, 260, 30);

        unitCostLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        unitCostLabel.setForeground(new java.awt.Color(100, 116, 139));
        unitCostLabel.setText("Unit Cost");
        formPanel.add(unitCostLabel);
        unitCostLabel.setBounds(20, 268, 125, 18);

        unitCostField.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        formPanel.add(unitCostField);
        unitCostField.setBounds(20, 286, 125, 30);

        sellingPriceLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        sellingPriceLabel.setForeground(new java.awt.Color(100, 116, 139));
        sellingPriceLabel.setText("Selling Price");
        formPanel.add(sellingPriceLabel);
        sellingPriceLabel.setBounds(155, 268, 125, 18);

        sellingPriceField.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        formPanel.add(sellingPriceField);
        sellingPriceField.setBounds(155, 286, 125, 30);

        reorderLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        reorderLabel.setForeground(new java.awt.Color(100, 116, 139));
        reorderLabel.setText("Reorder Level");
        formPanel.add(reorderLabel);
        reorderLabel.setBounds(20, 322, 125, 18);

        reorderField.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        formPanel.add(reorderField);
        reorderField.setBounds(20, 340, 125, 30);

        openingStockLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        openingStockLabel.setForeground(new java.awt.Color(100, 116, 139));
        openingStockLabel.setText("Opening Stock");
        formPanel.add(openingStockLabel);
        openingStockLabel.setBounds(155, 322, 125, 18);

        openingStockField.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        formPanel.add(openingStockField);
        openingStockField.setBounds(155, 340, 125, 30);

        stockInfoLabel.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        stockInfoLabel.setForeground(new java.awt.Color(100, 116, 139));
        stockInfoLabel.setText("New product");
        formPanel.add(stockInfoLabel);
        stockInfoLabel.setBounds(20, 378, 260, 18);

        addButton.setBackground(new java.awt.Color(22, 163, 74));
        addButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        addButton.setForeground(new java.awt.Color(255, 255, 255));
        addButton.setText("Add");
        addButton.setBorderPainted(false);
        addButton.setContentAreaFilled(false);
        addButton.setFocusPainted(false);
        addButton.setOpaque(true);
        addButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        formPanel.add(addButton);
        addButton.setBounds(20, 406, 125, 38);

        updateButton.setBackground(new java.awt.Color(37, 99, 235));
        updateButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        updateButton.setForeground(new java.awt.Color(255, 255, 255));
        updateButton.setText("Update");
        updateButton.setBorderPainted(false);
        updateButton.setContentAreaFilled(false);
        updateButton.setFocusPainted(false);
        updateButton.setOpaque(true);
        updateButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });
        formPanel.add(updateButton);
        updateButton.setBounds(155, 406, 125, 38);

        archiveButton.setBackground(new java.awt.Color(220, 38, 38));
        archiveButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        archiveButton.setForeground(new java.awt.Color(255, 255, 255));
        archiveButton.setText("Archive");
        archiveButton.setBorderPainted(false);
        archiveButton.setContentAreaFilled(false);
        archiveButton.setFocusPainted(false);
        archiveButton.setOpaque(true);
        archiveButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        archiveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                archiveButtonActionPerformed(evt);
            }
        });
        formPanel.add(archiveButton);
        archiveButton.setBounds(20, 452, 125, 38);

        clearButton.setBackground(new java.awt.Color(100, 116, 139));
        clearButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        clearButton.setForeground(new java.awt.Color(255, 255, 255));
        clearButton.setText("Clear");
        clearButton.setBorderPainted(false);
        clearButton.setContentAreaFilled(false);
        clearButton.setFocusPainted(false);
        clearButton.setOpaque(true);
        clearButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });
        formPanel.add(clearButton);
        clearButton.setBounds(155, 452, 125, 38);

        roleNoteLabel.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        roleNoteLabel.setForeground(new java.awt.Color(100, 116, 139));
        roleNoteLabel.setText(" ");
        formPanel.add(roleNoteLabel);
        roleNoteLabel.setBounds(20, 502, 260, 70);

        mainPanel.add(formPanel);
        formPanel.setBounds(250, 96, 300, 600);

        tablePanel.setBackground(new java.awt.Color(255, 255, 255));

        tablePanel.setLayout(null);

        tableTitleLabel.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        tableTitleLabel.setForeground(new java.awt.Color(15, 23, 42));
        tableTitleLabel.setText("Product List");
        tablePanel.add(tableTitleLabel);
        tableTitleLabel.setBounds(20, 16, 160, 24);

        categoryFilter.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        categoryFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                categoryFilterActionPerformed(evt);
            }
        });
        tablePanel.add(categoryFilter);
        categoryFilter.setBounds(170, 12, 145, 32);

        searchLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        searchLabel.setForeground(new java.awt.Color(100, 116, 139));
        searchLabel.setText("Search");
        tablePanel.add(searchLabel);
        searchLabel.setBounds(325, 19, 45, 18);

        searchField.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchFieldKeyReleased(evt);
            }
        });
        tablePanel.add(searchField);
        searchField.setBounds(372, 12, 178, 32);

        productTable.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        productTable.setRowHeight(26);
        productTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                productTableMouseClicked(evt);
            }
        });
        tableScroll.setViewportView(productTable);
        tablePanel.add(tableScroll);
        tableScroll.setBounds(20, 58, 530, 522);

        mainPanel.add(tablePanel);
        tablePanel.setBounds(565, 96, 570, 600);

        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void dashboardNavMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dashboardNavMouseClicked
        Nav.open(this, Nav.Page.DASHBOARD);
    }//GEN-LAST:event_dashboardNavMouseClicked

    private void productsNavMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_productsNavMouseClicked
        Nav.open(this, Nav.Page.PRODUCTS);
    }//GEN-LAST:event_productsNavMouseClicked

    private void stockNavMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_stockNavMouseClicked
        Nav.open(this, Nav.Page.STOCK);
    }//GEN-LAST:event_stockNavMouseClicked

    private void suppliersNavMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_suppliersNavMouseClicked
        Nav.open(this, Nav.Page.SUPPLIERS);
    }//GEN-LAST:event_suppliersNavMouseClicked

    private void reportsNavMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_reportsNavMouseClicked
        Nav.open(this, Nav.Page.REPORTS);
    }//GEN-LAST:event_reportsNavMouseClicked

    private void usersNavMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_usersNavMouseClicked
        Nav.open(this, Nav.Page.USERS);
    }//GEN-LAST:event_usersNavMouseClicked

    private void auditNavMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_auditNavMouseClicked
        Nav.open(this, Nav.Page.AUDIT);
    }//GEN-LAST:event_auditNavMouseClicked

    private void logoutNavMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logoutNavMouseClicked
        Nav.logout(this);
    }//GEN-LAST:event_logoutNavMouseClicked

    private void addCategoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCategoryButtonActionPerformed
        String name = JOptionPane.showInputDialog(this, "New category name:", "Add Category", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        try {
            ProductDao.addCategory(name.trim());
            AuditDao.log("CATEGORY_ADD", name.trim());
            loadCombos();
            categoryCombo.setSelectedItem(name.trim());
        } catch (SQLIntegrityConstraintViolationException ex) {
            Ui.warn(this, "That category already exists.");
        } catch (SQLException ex) {
            Ui.error(this, ex);
        }
    }//GEN-LAST:event_addCategoryButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        if (selected != null) {
            Ui.warn(this, "Click Clear first to enter a new product.");
            return;
        }
        Product p = readForm();
        if (p == null) {
            return;
        }
        int opening;
        try {
            opening = Integer.parseInt(openingStockField.getText().trim());
        } catch (NumberFormatException ex) {
            Ui.warn(this, "Opening stock must be a whole number.");
            return;
        }
        if (opening < 0) {
            Ui.warn(this, "Opening stock cannot be negative.");
            return;
        }
        try {
            if (ProductDao.codeExists(p.getCode())) {
                Ui.warn(this, "Product code " + p.getCode() + " is already used.");
                return;
            }
            ProductDao.add(p, opening);
            AuditDao.log("PRODUCT_ADD", p + " (opening stock: " + opening + ")");
            loadProducts();
            clearForm();
        } catch (SQLException ex) {
            Ui.error(this, ex);
        }
    }//GEN-LAST:event_addButtonActionPerformed

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        if (selected == null) {
            Ui.warn(this, "Select a product from the list to update.");
            return;
        }
        Product p = readForm();
        if (p == null) {
            return;
        }
        try {
            ProductDao.update(p);
            AuditDao.log("PRODUCT_UPDATE", p.toString());
            loadProducts();
            clearForm();
        } catch (SQLException ex) {
            Ui.error(this, ex);
        }
    }//GEN-LAST:event_updateButtonActionPerformed

    private void archiveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_archiveButtonActionPerformed
        if (selected == null) {
            Ui.warn(this, "Select a product from the list to archive.");
            return;
        }
        if (!Ui.confirm(this, "Archive " + selected + "?\nIt will be hidden from lists, but its transaction history is kept.")) {
            return;
        }
        try {
            ProductDao.archive(selected.getId());
            AuditDao.log("PRODUCT_ARCHIVE", selected.toString());
            loadProducts();
            clearForm();
        } catch (SQLException ex) {
            Ui.error(this, ex);
        }
    }//GEN-LAST:event_archiveButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        clearForm();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void categoryFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_categoryFilterActionPerformed
        applyFilter();
    }//GEN-LAST:event_categoryFilterActionPerformed

    private void searchFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchFieldKeyReleased
        applyFilter();
    }//GEN-LAST:event_searchFieldKeyReleased

    private void productTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_productTableMouseClicked
        int row = productTable.getSelectedRow();
        if (row < 0) {
            return;
        }
        String code = (String) model.getValueAt(productTable.convertRowIndexToModel(row), 0);
        for (Product p : products) {
            if (p.getCode().equals(code)) {
                fillForm(p);
            }
        }
    }//GEN-LAST:event_productTableMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton addCategoryButton;
    private javax.swing.JButton archiveButton;
    private javax.swing.JLabel auditNav;
    private javax.swing.JLabel brandLabel;
    private javax.swing.JLabel brandSubLabel;
    private javax.swing.JComboBox<String> categoryCombo;
    private javax.swing.JComboBox<String> categoryFilter;
    private javax.swing.JLabel categoryLabel;
    private javax.swing.JButton clearButton;
    private javax.swing.JTextField codeField;
    private javax.swing.JLabel codeLabel;
    private javax.swing.JLabel dashboardNav;
    private javax.swing.JPanel formPanel;
    private javax.swing.JLabel formTitleLabel;
    private javax.swing.JLabel headerLabel;
    private javax.swing.JLabel headerSubLabel;
    private javax.swing.JLabel logoutNav;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JLabel menuLabel;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField openingStockField;
    private javax.swing.JLabel openingStockLabel;
    private javax.swing.JTable productTable;
    private javax.swing.JLabel productsNav;
    private javax.swing.JTextField reorderField;
    private javax.swing.JLabel reorderLabel;
    private javax.swing.JLabel reportsNav;
    private javax.swing.JLabel roleNoteLabel;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JTextField sellingPriceField;
    private javax.swing.JLabel sellingPriceLabel;
    private javax.swing.JPanel sidebarPanel;
    private javax.swing.JLabel stockInfoLabel;
    private javax.swing.JLabel stockNav;
    private javax.swing.JComboBox<Supplier> supplierCombo;
    private javax.swing.JLabel supplierLabel;
    private javax.swing.JLabel suppliersNav;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JScrollPane tableScroll;
    private javax.swing.JLabel tableTitleLabel;
    private javax.swing.JTextField unitCostField;
    private javax.swing.JLabel unitCostLabel;
    private javax.swing.JButton updateButton;
    private javax.swing.JLabel userLabel;
    private javax.swing.JLabel usersNav;
    // End of variables declaration//GEN-END:variables
}
