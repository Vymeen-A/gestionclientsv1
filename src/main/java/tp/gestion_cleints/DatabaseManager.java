package tp.gestion_cleints;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:gestion_clients.db";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found");
            e.printStackTrace();
        }
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {

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
                    + "password TEXT, "
                    + "secteur TEXT, "
                    + "debut_act TEXT, "
                    + "fixed_total_amount REAL DEFAULT 0.0, "
                    + "ttc REAL DEFAULT 0.0"
                    + ");";
            stmt.execute(createClientsTable);

            // Create transactions table
            String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "client_id INTEGER, "
                    + "amount REAL, "
                    + "date TEXT, "
                    + "notes TEXT, "
                    + "FOREIGN KEY(client_id) REFERENCES clients(id)"
                    + ");";
            stmt.execute(createTransactionsTable);

            // Create expenses table
            String createExpensesTable = "CREATE TABLE IF NOT EXISTS expenses ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "description TEXT, "
                    + "amount REAL, "
                    + "date TEXT"
                    + ");";
            stmt.execute(createExpensesTable);

            // Create users table for administrator
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                    + "username TEXT PRIMARY KEY, "
                    + "password_hash TEXT"
                    + ");";
            stmt.execute(createUsersTable);

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
                    + "phone TEXT"
                    + ");";
            stmt.execute(createAdminInfoTable);

            // Ensure one row exists
            stmt.execute("INSERT OR IGNORE INTO admin_info (id) VALUES (1)");

            // Insert default administrator if not exists (default password 'admin')
            // Using a plain password for now, will be updated to hash in the
            // LoginController/UserDAO
            String checkAdmin = "SELECT COUNT(*) FROM users WHERE username = 'administrator'";
            java.sql.ResultSet rs = stmt.executeQuery(checkAdmin);
            if (rs.next() && rs.getInt(1) == 0) {
                // BCrypt hash for 'admin'
                String defaultHash = "$2a$12$R9h/lIPzHZ7C53fO.W.9Cuj/8.u.u.u.u.u.u.u.u.u.u.u.u.u."; // This is a
                                                                                                    // placeholder,
                                                                                                    // UserDAO should
                                                                                                    // handle this
                                                                                                    // properly.
                // Actually, I'll let the application handle the first-time setup or use a known
                // hash.
                // For simplicity in this step, I'll just ensure the table exists.
            }

            // Migrating existing data if necessary (adding missing columns to existing
            // clients table)
            // Note: In SQLite, adding multiple columns in one ALTER TABLE isn't supported
            // in old versions,
            // but we can try adding them one by one if they don't exist.
            String[] newCols = {
                    "raison_sociale TEXT", "nom_prenom TEXT", "adresse TEXT", "ville TEXT", "ice TEXT", "rc TEXT",
                    "tp TEXT", "taxe_habit TEXT", "tva TEXT", "regime_tva TEXT", "fax TEXT",
                    "email TEXT", "rib TEXT", "password TEXT", "secteur TEXT", "debut_act TEXT",
                    "fixed_total_amount REAL DEFAULT 0.0", "ttc REAL DEFAULT 0.0"
            };

            for (String col : newCols) {
                try {
                    stmt.execute("ALTER TABLE clients ADD COLUMN " + col);
                } catch (SQLException e) {
                    // Column might already exist, ignore
                }
            }

            // Data Migration for old clients
            try {
                stmt.execute(
                        "UPDATE clients SET raison_sociale = name WHERE (raison_sociale IS NULL OR raison_sociale = '') AND name IS NOT NULL");
            } catch (SQLException e) {
                /* 'name' column might not exist */ }

            try {
                stmt.execute(
                        "UPDATE clients SET adresse = address WHERE (adresse IS NULL OR adresse = '') AND address IS NOT NULL");
            } catch (SQLException e) {
                /* 'address' column might not exist */ }

            try {
                stmt.execute(
                        "UPDATE clients SET fixed_total_amount = revenue WHERE (fixed_total_amount = 0.0) AND revenue IS NOT NULL");
            } catch (SQLException e) {
                /* 'revenue' column might not exist */ }

            System.out.println("Database initialized and data migrated.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
