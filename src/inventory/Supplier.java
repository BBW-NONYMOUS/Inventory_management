package inventory;

/**
 * A supplier of products.
 */
public class Supplier {

    /** Placeholder shown in combo boxes when no supplier is chosen. */
    public static final Supplier NONE = new Supplier(0, "(none)", "", "", "", "", 0);

    private final int id;
    private final String name;
    private final String contactPerson;
    private final String phone;
    private final String email;
    private final String address;
    private final int productCount;

    public Supplier(int id, String name, String contactPerson, String phone, String email, String address, int productCount) {
        this.id = id;
        this.name = name;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.productCount = productCount;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public int getProductCount() {
        return productCount;
    }

    @Override
    public String toString() {
        return name;
    }
}
