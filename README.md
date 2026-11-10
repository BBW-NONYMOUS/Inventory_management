# StockWise – Inventory Management Database System

Java Swing (NetBeans JFrame forms) + MySQL (XAMPP).

## How to run

1. Open **XAMPP Control Panel** and start **MySQL**.
2. In NetBeans: *File → Open Project* → select this folder → **Run**.
3. On first run the program creates the `inventory_db` database, its tables and sample data automatically.
   (To set it up by hand instead, create `inventory_db` in phpMyAdmin and import `src/inventory/schema.sql`.)

Default accounts:

| Username | Password | Role  |
|----------|----------|-------|
| admin    | admin123 | Admin |
| staff    | staff123 | Staff |

Change these passwords in **User Accounts** after the first login.

## How the system answers the Statement of the Problem

| Specific problem | Feature that solves it |
|---|---|
| **1. Inaccurate stock tracking and manual discrepancies** | **Stock In / Out** screen. Every delivery or release is saved as a transaction, and the product quantity is updated in the same database transaction, so the stock on record always matches the log. Quantities cannot be typed in directly, and a stock-out larger than the available stock is blocked. |
| **2. No automated low-stock / reorder alerts** | Each product has a **reorder level**. Items at or below it are flagged *Reorder* / *Out of Stock*, listed under **Reorder Alerts** on the Dashboard, shown in a popup after login, and announced right after a stock-out drops them to that level. |
| **3. Manual computation errors in valuation** | Unit cost is recomputed automatically on every stock-in as a **weighted average**. Stock value and retail value come from the database view `v_inventory_valuation`. **Reports** shows the daily inventory summary (units and cost in/out per day), valuation by category, and can export to CSV. |
| **4. Inefficient search and record retrieval** | Normalized relational schema (`categories`, `suppliers`, `products`, `stock_transactions`, `users`, `audit_logs`) with foreign keys and indexes. Every list has instant search, products can be filtered by category, and the transaction log can be filtered by type. Supplier records are on the **Suppliers** screen. |
| **5. Data security and unauthorized access** | Login with **salted PBKDF2 password hashing**, and **role-based access**. *Admin* has full access. *Staff* can record stock in/out and view products, suppliers and reports, but cannot edit products or suppliers and does not see User Accounts or the Audit Log. Accounts can be deactivated. The **Audit Log** records sign-ins (including failed ones) and every change. |

## Screens

All screens are NetBeans JFrame forms (`.java` + `.form`) using Null Layout, so they can be edited in the **Design** tab.

| Form | Purpose |
|---|---|
| `LoginForm` | Sign in (program entry point) |
| `DashboardForm` | Summary cards, reorder alerts, recent transactions |
| `ProductsForm` | Product records, categories, opening stock |
| `StockForm` | Stock in / stock out and the transaction log |
| `SuppliersForm` | Supplier directory |
| `ReportsForm` | Valuation, daily summary, valuation by category, CSV export |
| `UsersForm` | User accounts and roles (Admin only) |
| `AuditForm` | Audit log (Admin only) |

Screenshots are in `docs/screenshots`.

## Database settings

Edit `src/inventory/Database.java` if your MySQL is not `root` with no password on `localhost:3306`.
