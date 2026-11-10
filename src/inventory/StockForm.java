package inventory;

import java.sql.SQLException;
import java.util.Locale;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Stock-in (deliveries) and stock-out (releases). Every transaction updates the product
 * quantity immediately and is kept in the transaction log.
 */
public class StockForm extends javax.swing.JFrame {

    private static final String STOCK_IN = "Stock In";
    private static final String STOCK_OUT = "Stock Out";
    private static final String ALL = "All types";

    private final DefaultTableModel model = Ui.tableModel(
            "Date/Time", "Type", "Product", "Qty", "Unit Cost", "Total", "Supplier", "Ref No.", "By");
    private final TableRowSorter<DefaultTableModel> sorter;

    public StockForm() {
        initComponents();
        Nav.setupSidebar(userLabel, usersNav, auditNav);
        typeCombo.setModel(new DefaultComboBoxModel<>(new String[]{STOCK_IN, STOCK_OUT}));
        typeFilter.setModel(new DefaultComboBoxModel<>(new String[]{ALL, STOCK_IN, STOCK_OUT}));
        sorter = Ui.setupTable(transactionTable, model, 110, 45, 170, 45, 85, 95, 150, 100, 70);
        transactionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        Ui.moneyColumns(transactionTable, 4, 5);
        transactionTable.getColumnModel().getColumn(1).setCellRenderer(new Ui.TypeRenderer());
        searchField.setToolTipText("Search by date, product, supplier, reference or user");

        loadCombos(0);
        loadTransactions();
        clearForm();
    }

    private boolean isStockIn() {
        return STOCK_IN.equals(typeCombo.getSelectedItem());
    }

    /** Reloads products (with current stock) and suppliers, then reselects the given product id. */
    private void loadCombos(int selectProductId) {
        try {
            DefaultComboBoxModel<Product> products = new DefaultComboBoxModel<>();
            for (Product p : ProductDao.findAll()) {
                products.addElement(p);
                if (p.getId() == selectProductId) {
                    products.setSelectedItem(p);
                }
            }
            productCombo.setModel(products);

            DefaultComboBoxModel<Supplier> suppliers = new DefaultComboBoxModel<>();
            suppliers.addElement(Supplier.NONE);
            SupplierDao.findAll().forEach(suppliers::addElement);
            supplierCombo.setModel(suppliers);
        } catch (SQLException ex) {
            Ui.error(this, ex);
        }
        onProductChanged();
    }

    private void loadTransactions() {
        model.setRowCount(0);
        try {
            for (Object[] row : TransactionDao.findAll(null, 2000)) {
                model.addRow(row);
            }
        } catch (SQLException ex) {
            Ui.error(this, ex);
        }
        applyFilter();
    }

    private void applyFilter() {
        if (sorter == null) {
            return;
        }
        Object type = typeFilter.getSelectedItem();
        String exact = STOCK_IN.equals(type) ? "IN" : STOCK_OUT.equals(type) ? "OUT" : null;
        Ui.filter(sorter, searchField.getText(), 1, exact);
    }

    private void onProductChanged() {
        Product p = (Product) productCombo.getSelectedItem();
        if (p == null) {
            currentStockLabel.setText("No products yet - add one in Products.");
            return;
        }
        currentStockLabel.setText("In stock: " + p.getQuantity() + "     Reorder level: " + p.getReorderLevel());
        currentStockLabel.setForeground(p.needsReorder() ? Ui.AMBER : new java.awt.Color(37, 99, 235));
        unitCostField.setText(String.format(Locale.US, "%.2f", p.getUnitCost()));
        if (isStockIn() && p.getSupplierId() > 0) {
            for (int i = 0; i < supplierCombo.getItemCount(); i++) {
                if (supplierCombo.getItemAt(i).getId() == p.getSupplierId()) {
                    supplierCombo.setSelectedIndex(i);
                }
            }
        }
    }

    private void onTypeChanged() {
        boolean in = isStockIn();
        unitCostField.setEnabled(in);
        supplierCombo.setEnabled(in);
        if (!in) {
            supplierCombo.setSelectedItem(Supplier.NONE);
        }
        recordButton.setText(in ? "Record Stock In" : "Record Stock Out");
        recordButton.setBackground(in ? Ui.GREEN : Ui.RED);
        onProductChanged();
    }

    private void clearForm() {
        quantityField.setText("");
        referenceField.setText("");
        remarksField.setText("");
        onTypeChanged();
    }

    private void recordTransaction() {
        Product p = (Product) productCombo.getSelectedItem();
        if (p == null) {
            Ui.warn(this, "Select a product.");
            return;
        }
        boolean in = isStockIn();
        int quantity;
        double unitCost;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
            unitCost = in ? Double.parseDouble(unitCostField.getText().trim()) : p.getUnitCost();
        } catch (NumberFormatException ex) {
            Ui.warn(this, "Quantity must be a whole number and unit cost a number.");
            return;
        }
        if (quantity <= 0 || unitCost < 0) {
            Ui.warn(this, "Quantity must be greater than zero and unit cost cannot be negative.");
            return;
        }
        Supplier supplier = in ? (Supplier) supplierCombo.getSelectedItem() : null;
        try {
            int newQty = TransactionDao.record(p.getId(), in ? "IN" : "OUT", quantity, unitCost,
                    supplier == null ? 0 : supplier.getId(), referenceField.getText(), remarksField.getText());
            AuditDao.log(in ? "STOCK_IN" : "STOCK_OUT", quantity + " x " + p + " (new stock: " + newQty + ")");
            loadCombos(p.getId());
            loadTransactions();
            clearForm();
            if (newQty <= p.getReorderLevel()) {
                Ui.warn(this, "Reorder alert: " + p + " now has " + newQty + " left (reorder level " + p.getReorderLevel() + ").");
            } else {
                Ui.info(this, (in ? "Stock in" : "Stock out") + " recorded. " + p.getName() + " now has " + newQty + " in stock.");
            }
        } catch (IllegalArgumentException ex) {
            Ui.warn(this, ex.getMessage());
        } catch (SQLException ex) {
            Ui.error(this, ex);
        }
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
        typeLabel = new javax.swing.JLabel();
        typeCombo = new javax.swing.JComboBox<>();
        productLabel = new javax.swing.JLabel();
        productCombo = new javax.swing.JComboBox<>();
        currentStockLabel = new javax.swing.JLabel();
        quantityLabel = new javax.swing.JLabel();
        quantityField = new javax.swing.JTextField();
        unitCostLabel = new javax.swing.JLabel();
        unitCostField = new javax.swing.JTextField();
        supplierLabel = new javax.swing.JLabel();
        supplierCombo = new javax.swing.JComboBox<>();
        referenceLabel = new javax.swing.JLabel();
        referenceField = new javax.swing.JTextField();
        remarksLabel = new javax.swing.JLabel();
        remarksField = new javax.swing.JTextField();
        recordButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        logPanel = new javax.swing.JPanel();
        logTitleLabel = new javax.swing.JLabel();
        typeFilter = new javax.swing.JComboBox<>();
        searchLabel = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        logScroll = new javax.swing.JScrollPane();
        transactionTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("StockWise - Stock In / Out");
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

        productsNav.setBackground(new java.awt.Color(15, 23, 42));
        productsNav.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        productsNav.setForeground(new java.awt.Color(148, 163, 184));
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

        stockNav.setBackground(new java.awt.Color(30, 41, 59));
        stockNav.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        stockNav.setForeground(new java.awt.Color(255, 255, 255));
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
        headerLabel.setText("Stock In / Out");
        mainPanel.add(headerLabel);
        headerLabel.setBounds(250, 22, 860, 36);

        headerSubLabel.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        headerSubLabel.setForeground(new java.awt.Color(100, 116, 139));
        headerSubLabel.setText("Record deliveries and releases. Stock levels update instantly on every transaction.");
        mainPanel.add(headerSubLabel);
        headerSubLabel.setBounds(250, 58, 860, 20);

        formPanel.setBackground(new java.awt.Color(255, 255, 255));

        formPanel.setLayout(null);

        formTitleLabel.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        formTitleLabel.setForeground(new java.awt.Color(15, 23, 42));
        formTitleLabel.setText("Record Movement");
        formPanel.add(formTitleLabel);
        formTitleLabel.setBounds(20, 16, 300, 24);

        typeLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        typeLabel.setForeground(new java.awt.Color(100, 116, 139));
        typeLabel.setText("Transaction Type");
        formPanel.add(typeLabel);
        typeLabel.setBounds(20, 52, 260, 18);

        typeCombo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        typeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeComboActionPerformed(evt);
            }
        });
        formPanel.add(typeCombo);
        typeCombo.setBounds(20, 70, 260, 30);

        productLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        productLabel.setForeground(new java.awt.Color(100, 116, 139));
        productLabel.setText("Product");
        formPanel.add(productLabel);
        productLabel.setBounds(20, 106, 260, 18);

        productCombo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        productCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                productComboActionPerformed(evt);
            }
        });
        formPanel.add(productCombo);
        productCombo.setBounds(20, 124, 260, 30);

        currentStockLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        currentStockLabel.setForeground(new java.awt.Color(37, 99, 235));
        currentStockLabel.setText(" ");
        formPanel.add(currentStockLabel);
        currentStockLabel.setBounds(20, 158, 260, 18);

        quantityLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        quantityLabel.setForeground(new java.awt.Color(100, 116, 139));
        quantityLabel.setText("Quantity");
        formPanel.add(quantityLabel);
        quantityLabel.setBounds(20, 184, 260, 18);

        quantityField.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        formPanel.add(quantityField);
        quantityField.setBounds(20, 202, 260, 30);

        unitCostLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        unitCostLabel.setForeground(new java.awt.Color(100, 116, 139));
        unitCostLabel.setText("Unit Cost (PHP)");
        formPanel.add(unitCostLabel);
        unitCostLabel.setBounds(20, 238, 260, 18);

        unitCostField.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        formPanel.add(unitCostField);
        unitCostField.setBounds(20, 256, 260, 30);

        supplierLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        supplierLabel.setForeground(new java.awt.Color(100, 116, 139));
        supplierLabel.setText("Supplier");
        formPanel.add(supplierLabel);
        supplierLabel.setBounds(20, 292, 260, 18);

        supplierCombo.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        formPanel.add(supplierCombo);
        supplierCombo.setBounds(20, 310, 260, 30);

        referenceLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        referenceLabel.setForeground(new java.awt.Color(100, 116, 139));
        referenceLabel.setText("Reference No. (DR / OR / Request No.)");
        formPanel.add(referenceLabel);
        referenceLabel.setBounds(20, 346, 260, 18);

        referenceField.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        formPanel.add(referenceField);
        referenceField.setBounds(20, 364, 260, 30);

        remarksLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        remarksLabel.setForeground(new java.awt.Color(100, 116, 139));
        remarksLabel.setText("Remarks");
        formPanel.add(remarksLabel);
        remarksLabel.setBounds(20, 400, 260, 18);

        remarksField.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        formPanel.add(remarksField);
        remarksField.setBounds(20, 418, 260, 30);

        recordButton.setBackground(new java.awt.Color(22, 163, 74));
        recordButton.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        recordButton.setForeground(new java.awt.Color(255, 255, 255));
        recordButton.setText("Record Stock In");
        recordButton.setBorderPainted(false);
        recordButton.setContentAreaFilled(false);
        recordButton.setFocusPainted(false);
        recordButton.setOpaque(true);
        recordButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        recordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recordButtonActionPerformed(evt);
            }
        });
        formPanel.add(recordButton);
        recordButton.setBounds(20, 468, 260, 40);

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
        clearButton.setBounds(20, 516, 260, 36);

        mainPanel.add(formPanel);
        formPanel.setBounds(250, 96, 300, 600);

        logPanel.setBackground(new java.awt.Color(255, 255, 255));

        logPanel.setLayout(null);

        logTitleLabel.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        logTitleLabel.setForeground(new java.awt.Color(15, 23, 42));
        logTitleLabel.setText("Transaction Log");
        logPanel.add(logTitleLabel);
        logTitleLabel.setBounds(20, 16, 160, 24);

        typeFilter.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        typeFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeFilterActionPerformed(evt);
            }
        });
        logPanel.add(typeFilter);
        typeFilter.setBounds(180, 12, 120, 32);

        searchLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        searchLabel.setForeground(new java.awt.Color(100, 116, 139));
        searchLabel.setText("Search");
        logPanel.add(searchLabel);
        searchLabel.setBounds(312, 19, 45, 18);

        searchField.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchFieldKeyReleased(evt);
            }
        });
        logPanel.add(searchField);
        searchField.setBounds(360, 12, 190, 32);

        transactionTable.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        transactionTable.setRowHeight(26);
        logScroll.setViewportView(transactionTable);
        logPanel.add(logScroll);
        logScroll.setBounds(20, 58, 530, 522);

        mainPanel.add(logPanel);
        logPanel.setBounds(565, 96, 570, 600);

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

    private void typeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeComboActionPerformed
        onTypeChanged();
    }//GEN-LAST:event_typeComboActionPerformed

    private void productComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_productComboActionPerformed
        onProductChanged();
    }//GEN-LAST:event_productComboActionPerformed

    private void recordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordButtonActionPerformed
        recordTransaction();
    }//GEN-LAST:event_recordButtonActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        clearForm();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void typeFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeFilterActionPerformed
        applyFilter();
    }//GEN-LAST:event_typeFilterActionPerformed

    private void searchFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchFieldKeyReleased
        applyFilter();
    }//GEN-LAST:event_searchFieldKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel auditNav;
    private javax.swing.JLabel brandLabel;
    private javax.swing.JLabel brandSubLabel;
    private javax.swing.JButton clearButton;
    private javax.swing.JLabel currentStockLabel;
    private javax.swing.JLabel dashboardNav;
    private javax.swing.JPanel formPanel;
    private javax.swing.JLabel formTitleLabel;
    private javax.swing.JLabel headerLabel;
    private javax.swing.JLabel headerSubLabel;
    private javax.swing.JPanel logPanel;
    private javax.swing.JScrollPane logScroll;
    private javax.swing.JLabel logTitleLabel;
    private javax.swing.JLabel logoutNav;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JLabel menuLabel;
    private javax.swing.JComboBox<Product> productCombo;
    private javax.swing.JLabel productLabel;
    private javax.swing.JLabel productsNav;
    private javax.swing.JTextField quantityField;
    private javax.swing.JLabel quantityLabel;
    private javax.swing.JButton recordButton;
    private javax.swing.JTextField referenceField;
    private javax.swing.JLabel referenceLabel;
    private javax.swing.JTextField remarksField;
    private javax.swing.JLabel remarksLabel;
    private javax.swing.JLabel reportsNav;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel sidebarPanel;
    private javax.swing.JLabel stockNav;
    private javax.swing.JComboBox<Supplier> supplierCombo;
    private javax.swing.JLabel supplierLabel;
    private javax.swing.JLabel suppliersNav;
    private javax.swing.JTable transactionTable;
    private javax.swing.JComboBox<String> typeCombo;
    private javax.swing.JComboBox<String> typeFilter;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JTextField unitCostField;
    private javax.swing.JLabel unitCostLabel;
    private javax.swing.JLabel userLabel;
    private javax.swing.JLabel usersNav;
    // End of variables declaration//GEN-END:variables
}
