package tp.gestion_cleints;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:gestion_clients.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS clients ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "email TEXT, "
                + "phone TEXT, "
                + "address TEXT, "
                + "notes TEXT, "
                + "revenue REAL DEFAULT 0.0"
                + ");";

        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Database initialized and table 'clients' checked.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
