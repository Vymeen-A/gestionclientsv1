package tp.gestion_cleints;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class DatabaseManager {
    private static final String DB_PATH;
    private static final String DB_URL;

    static {
        String userHome = System.getProperty("user.home");
        String appDataPath = userHome + "/.gestion_clients";
        java.io.File directory = new java.io.File(appDataPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        DB_PATH = appDataPath + "/gestion_clients.db";
        DB_URL = "jdbc:sqlite:" + DB_PATH;
        System.out.println("Database path: " + DB_PATH);
    }

    public static String getDatabasePath() {
        return DB_PATH;
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found");
            e.printStackTrace();
        }
        Connection conn = DriverManager.getConnection(DB_URL);
        // Enable foreign keys for SQLite
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {

            // Create years table
            String createYearsTable = "CREATE TABLE IF NOT EXISTS years ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT UNIQUE, "
                    + "soft_deleted BOOLEAN DEFAULT 0, "
                    + "deleted_at TEXT"
                    + ");";
            stmt.execute(createYearsTable);

            // Ensure a default year exists (e.g. current year or 2025) if table is empty
            ResultSet rsYears = stmt.executeQuery("SELECT COUNT(*) FROM years");
            if (rsYears.next() && rsYears.getInt(1) == 0) {
                stmt.execute("INSERT INTO years (name) VALUES ('2025')");
            }
            rsYears.close();

            // Create payment_types table
            String createPaymentTypesTable = "CREATE TABLE IF NOT EXISTS payment_types ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT UNIQUE"
                    + ");";
            stmt.execute(createPaymentTypesTable);

            // Seed default payment types if empty
            ResultSet rsPT = stmt.executeQuery("SELECT COUNT(*) FROM payment_types");
            if (rsPT.next() && rsPT.getInt(1) == 0) {
                stmt.execute("INSERT INTO payment_types (name) VALUES ('Cash')");
                stmt.execute("INSERT INTO payment_types (name) VALUES ('Check')");
                stmt.execute("INSERT INTO payment_types (name) VALUES ('Bank Transfer')");
            }
            rsPT.close();

            // Create clients table with all fields if it doesn't exist
            String createClientsTable = "CREATE TABLE IF NOT EXISTS clients ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "raison_sociale TEXT, "
                    + "nom_prenom TEXT, "
                    + "adresse TEXT, "
                    + "ville TEXT, "
                    + "ice TEXT, "
                    + "rc TEXT, "
                    + "tp TEXT, "
                    + "taxe_habit TEXT, "
                    + "tva TEXT, "
                    + "regime_tva TEXT, "
                    + "fax TEXT, "
                    + "email TEXT, "
                    + "rib TEXT, "
                    + "username TEXT, "
                    + "password TEXT, "
                    + "secteur TEXT, "
                    + "debut_act TEXT, "
                    + "fixed_total_amount REAL DEFAULT 0.0, "
                    + "ttc REAL DEFAULT 0.0, "
                    + "is_hidden BOOLEAN DEFAULT 0, "
                    + "year_id INTEGER, "
                    + "FOREIGN KEY(year_id) REFERENCES years(id)"
                    + ");";
            stmt.execute(createClientsTable);

            // Create transactions table
            String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "client_id INTEGER, "
                    + "amount REAL, "
                    + "date TEXT, "
                    + "notes TEXT, "
                    + "type TEXT, "
                    + "payment_type_id INTEGER, "
                    + "year_id INTEGER, "
                    + "FOREIGN KEY(client_id) REFERENCES clients(id), "
                    + "FOREIGN KEY(payment_type_id) REFERENCES payment_types(id), "
                    + "FOREIGN KEY(year_id) REFERENCES years(id)"
                    + ");";
            stmt.execute(createTransactionsTable);

            // Create expenses table
            String createExpensesTable = "CREATE TABLE IF NOT EXISTS expenses ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "description TEXT, "
                    + "amount REAL, "
                    + "date TEXT, "
                    + "year_id INTEGER, "
                    + "FOREIGN KEY(year_id) REFERENCES years(id)"
                    + ");";
            stmt.execute(createExpensesTable);

            // Create users table for administrator
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                    + "username TEXT PRIMARY KEY, "
                    + "password_hash TEXT, "
                    + "role TEXT DEFAULT 'READ_ONLY', "
                    + "full_name TEXT, "
                    + "email TEXT, "
                    + "phone TEXT, "
                    + "profile_photo TEXT, "
                    + "is_active BOOLEAN DEFAULT 1, "
                    + "last_login TEXT, "
                    + "failed_attempts INTEGER DEFAULT 0, "
                    + "lockout_until TEXT"
                    + ");";
            stmt.execute(createUsersTable);

            // Create client_documents table
            String createDocumentsTable = "CREATE TABLE IF NOT EXISTS client_documents ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "client_id INTEGER, "
                    + "file_name TEXT, "
                    + "file_path TEXT, "
                    + "upload_date TEXT, "
                    + "FOREIGN KEY(client_id) REFERENCES clients(id) ON DELETE CASCADE"
                    + ");";
            stmt.execute(createDocumentsTable);

            // Perform schema updates (migrations)
            updateSchema(stmt);

            // Cleanup old audit logs (Retention Policy: 1 Year)
            AuditLogger.cleanupOldLogs();

            // Create password_history table
            String createPasswordHistoryTable = "CREATE TABLE IF NOT EXISTS password_history ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "username TEXT, "
                    + "password_hash TEXT, "
                    + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY(username) REFERENCES users(username) ON DELETE CASCADE"
                    + ");";
            stmt.execute(createPasswordHistoryTable);

            // Create admin_info table
            String createAdminInfoTable = "CREATE TABLE IF NOT EXISTS admin_info ("
                    + "id INTEGER PRIMARY KEY DEFAULT 1, "
                    + "raison_sociale TEXT, "
                    + "nom_prenom TEXT, "
                    + "adresse TEXT, "
                    + "ville TEXT, "
                    + "ice TEXT, "
                    + "rc TEXT, "
                    + "tp TEXT, "
                    + "identifiant_tva TEXT, "
                    + "regime_tva TEXT, "
                    + "email TEXT, "
                    + "phone TEXT, "
                    + "logo_path TEXT"
                    + ");";
            stmt.execute(createAdminInfoTable);

            // Create client_year_data table for year-specific financial data
            String createClientYearDataTable = "CREATE TABLE IF NOT EXISTS client_year_data ("
                    + "client_id INTEGER, "
                    + "year_id INTEGER, "
                    + "fixed_total_amount REAL DEFAULT 0.0, "
                    + "ttc REAL DEFAULT 0.0, "
                    + "PRIMARY KEY(client_id, year_id), "
                    + "FOREIGN KEY(client_id) REFERENCES clients(id) ON DELETE CASCADE, "
                    + "FOREIGN KEY(year_id) REFERENCES years(id) ON DELETE CASCADE"
                    + ");";
            stmt.execute(createClientYearDataTable);

            // Migrate data: if clients has amount and ttc, and they aren't in
            // client_year_data yet
            stmt.execute("INSERT OR IGNORE INTO client_year_data (client_id, year_id, fixed_total_amount, ttc) "
                    + "SELECT id, year_id, fixed_total_amount, ttc FROM clients WHERE year_id IS NOT NULL");

            // Ensure one row exists
            stmt.execute("INSERT OR IGNORE INTO admin_info (id) VALUES (1)");

            // Create invoices table
            String createInvoicesTable = "CREATE TABLE IF NOT EXISTS invoices ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "client_id INTEGER, "
                    + "number TEXT UNIQUE, "
                    + "date TEXT, "
                    + "due_date TEXT, "
                    + "total_ht REAL, "
                    + "total_tva REAL, "
                    + "total_ttc REAL, "
                    + "status TEXT, "
                    + "year_id INTEGER, "
                    + "FOREIGN KEY(client_id) REFERENCES clients(id), "
                    + "FOREIGN KEY(year_id) REFERENCES years(id)"
                    + ");";
            stmt.execute(createInvoicesTable);

            // Create audit_logs table
            String createAuditLogsTable = "CREATE TABLE IF NOT EXISTS audit_logs ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "username TEXT, "
                    + "action TEXT, "
                    + "entity_type TEXT, "
                    + "entity_id TEXT, "
                    + "details TEXT, "
                    + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ");";
            stmt.execute(createAuditLogsTable);

            // Create tasks table
            String createTasksTable = "CREATE TABLE IF NOT EXISTS tasks ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "client_id INTEGER, "
                    + "title TEXT, "
                    + "description TEXT, "
                    + "due_date TEXT, "
                    + "status TEXT DEFAULT 'PENDING', "
                    + "recurring_type TEXT, "
                    + "FOREIGN KEY(client_id) REFERENCES clients(id)"
                    + ");";
            stmt.execute(createTasksTable);

            // Add Indexes for Performance & Scalability
            String[] indexes = {
                    "CREATE INDEX IF NOT EXISTS idx_transactions_client ON transactions(client_id)",
                    "CREATE INDEX IF NOT EXISTS idx_transactions_year ON transactions(year_id)",
                    "CREATE INDEX IF NOT EXISTS idx_clients_hidden ON clients(is_hidden)",
                    "CREATE INDEX IF NOT EXISTS idx_clients_deleted ON clients(is_deleted)",
                    "CREATE INDEX IF NOT EXISTS idx_clients_year ON clients(year_id)",
                    "CREATE INDEX IF NOT EXISTS idx_invoices_client ON invoices(client_id)",
                    "CREATE INDEX IF NOT EXISTS idx_year_data_client ON client_year_data(client_id)",
                    "CREATE INDEX IF NOT EXISTS idx_year_data_year ON client_year_data(year_id)"
            };
            for (String idx : indexes) {
                stmt.execute(idx);
            }

            // Check admin existence (simplified)
            String checkAdmin = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
            ResultSet rs = stmt.executeQuery(checkAdmin);
            if (rs.next() && rs.getInt(1) == 0) {
                // Initial admin creation script can be added in UserDAO
            }
            rs.close();

            // Migration: Add role column if missing
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN role TEXT DEFAULT 'READ_ONLY'");
            } catch (SQLException e) {
            }
            // Migrate old role names if any
            stmt.execute("UPDATE users SET role = 'READ_ONLY' WHERE role = 'ASSISTANT'");
            // Ensure admin has ADMIN role
            stmt.execute("UPDATE users SET role = 'ADMIN' WHERE username = 'admin'");

            // Migration: Add profile columns if missing
            String[] userCols = {
                    "full_name TEXT", "email TEXT", "phone TEXT", "profile_photo TEXT",
                    "is_active BOOLEAN DEFAULT 1", "last_login TEXT",
                    "failed_attempts INTEGER DEFAULT 0", "lockout_until TEXT"
            };
            for (String col : userCols) {
                try {
                    stmt.execute("ALTER TABLE users ADD COLUMN " + col);
                } catch (SQLException e) {
                }
            }

            // Migrating existing data
            String[] newCols = {
                    "raison_sociale TEXT", "nom_prenom TEXT", "adresse TEXT", "ville TEXT", "ice TEXT", "rc TEXT",
                    "tp TEXT", "taxe_habit TEXT", "tva TEXT", "regime_tva TEXT", "fax TEXT",
                    "email TEXT", "rib TEXT", "username TEXT", "password TEXT", "secteur TEXT", "debut_act TEXT",
                    "fixed_total_amount REAL DEFAULT 0.0", "ttc REAL DEFAULT 0.0",
                    "is_hidden BOOLEAN DEFAULT 0", "year_id INTEGER"
            };

            for (String col : newCols) {
                try {
                    stmt.execute("ALTER TABLE clients ADD COLUMN " + col);
                } catch (SQLException e) {
                    // Column might already exist, ignore
                }
            }

            // Migrate transactions: add payment_type_id and year_id
            try {
                stmt.execute("ALTER TABLE transactions ADD COLUMN payment_type_id INTEGER");
            } catch (SQLException e) {
            }
            try {
                stmt.execute("ALTER TABLE transactions ADD COLUMN year_id INTEGER");
            } catch (SQLException e) {
            }

            // Migrate expenses: add year_id
            try {
                stmt.execute("ALTER TABLE expenses ADD COLUMN year_id INTEGER");
            } catch (SQLException e) {
            }

            // Migrate transactions table (add type)
            try {
                stmt.execute("ALTER TABLE transactions ADD COLUMN type TEXT");
                stmt.execute("UPDATE transactions SET type = 'PAYMENT' WHERE type IS NULL");
            } catch (SQLException e) {
            }

            // Migrate transactions table (add receipt_number)
            try {
                stmt.execute("ALTER TABLE transactions ADD COLUMN receipt_number TEXT");
            } catch (SQLException e) {
            }

            // Data Migration for old clients
            try {
                stmt.execute(
                        "UPDATE clients SET raison_sociale = name WHERE (raison_sociale IS NULL OR raison_sociale = '') AND name IS NOT NULL");
            } catch (SQLException e) {
            }

            try {
                stmt.execute(
                        "UPDATE clients SET adresse = address WHERE (adresse IS NULL OR adresse = '') AND address IS NOT NULL");
            } catch (SQLException e) {
            }

            try {
                stmt.execute(
                        "UPDATE clients SET fixed_total_amount = revenue WHERE (fixed_total_amount = 0.0) AND revenue IS NOT NULL");
            } catch (SQLException e) {
            }

            // Assign default year to existing data
            try {
                stmt.execute("UPDATE clients SET year_id = 1 WHERE year_id IS NULL");
                stmt.execute("UPDATE transactions SET year_id = 1 WHERE year_id IS NULL");
                stmt.execute("UPDATE expenses SET year_id = 1 WHERE year_id IS NULL");
            } catch (SQLException e) {
            }

            System.out.println("Database initialized and data migrated.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void updateSchema(Statement stmt) {
        try {
            // Add category column
            try {
                stmt.execute("ALTER TABLE clients ADD COLUMN category TEXT");
            } catch (SQLException e) {
                // Column likely exists
            }

            // Add tags column
            try {
                stmt.execute("ALTER TABLE clients ADD COLUMN tags TEXT");
            } catch (SQLException e) {
                // Column likely exists
            }

            // Add is_deleted column
            try {
                stmt.execute("ALTER TABLE clients ADD COLUMN is_deleted BOOLEAN DEFAULT 0");
            } catch (SQLException e) {
                // Column likely exists
            }

            // Add deleted_at column
            try {
                stmt.execute("ALTER TABLE clients ADD COLUMN deleted_at TEXT");
            } catch (SQLException e) {
                // Column likely exists
            }

            // Ensure admin_info has all required columns (for migrations)
            String[] adminCols = {
                    "raison_sociale TEXT", "nom_prenom TEXT", "adresse TEXT", "ville TEXT",
                    "ice TEXT", "rc TEXT", "tp TEXT", "identifiant_tva TEXT",
                    "regime_tva TEXT", "email TEXT", "phone TEXT", "logo_path TEXT"
            };
            for (String col : adminCols) {
                try {
                    stmt.execute("ALTER TABLE admin_info ADD COLUMN " + col);
                } catch (SQLException e) {
                    // Column already exists
                }
            }
        } catch (Exception e) {
            System.err.println("Schema update warning: " + e.getMessage());
        }
    }
}
