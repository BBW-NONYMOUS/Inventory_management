package inventory;

/**
 * A product in the inventory. Quantity is only changed through stock transactions.
 */
public class Product {

    public static final String IN_STOCK = "In Stock";
    public static final String REORDER = "Reorder";
    public static final String OUT_OF_STOCK = "Out of Stock";

    private final int id;
    private final String code;
    private final String name;
    private final String category;
    private final int supplierId;
    private final String supplierName;
    private final double unitCost;
    private final double sellingPrice;
    private final int quantity;
    private final int reorderLevel;

    public Product(int id, String code, String name, String category, int supplierId, String supplierName,
            double unitCost, double sellingPrice, int quantity, int reorderLevel) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.category = category;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.unitCost = unitCost;
        this.sellingPrice = sellingPrice;
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    /** 0 when the product has no supplier. */
    public int getSupplierId() {
        return supplierId;
    }

    public String getSupplierName() {
        return supplierName == null ? "" : supplierName;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    /** Stock value at cost. */
    public double getValue() {
        return quantity * unitCost;
    }

    public boolean needsReorder() {
        return quantity <= reorderLevel;
    }

    public String getStatus() {
        if (quantity == 0) {
            return OUT_OF_STOCK;
        }
        return needsReorder() ? REORDER : IN_STOCK;
    }

    @Override
    public String toString() {
        return code + " - " + name;
    }
}
