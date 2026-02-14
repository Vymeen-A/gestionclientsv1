package tp.gestion_cleints;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class AuditLogger {

    public static void log(String action, String entityType, String entityId, String details) {
        String username = "System";
        User current = SessionContext.getInstance().getCurrentUser();
        if (current != null) {
            username = current.getUsername();
        }

        String sql = "INSERT INTO audit_logs (username, action, entity_type, entity_id, details) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, action);
            pstmt.setString(3, entityType);
            pstmt.setString(4, entityId);
            pstmt.setString(5, details);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Audit log error: " + e.getMessage());
        }
    }

    public static void exportLogs() {
        // Logic for PDF export would go here
        // Standardizing system actions for compliance
    }

    public static void cleanupOldLogs() {
        String sql = "DELETE FROM audit_logs WHERE timestamp < datetime('now', '-1 year')";
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement()) {
            int deletedCount = stmt.executeUpdate(sql);
            if (deletedCount > 0) {
                System.out.println("Cleaned up " + deletedCount + " old audit logs.");
            }
        } catch (SQLException e) {
            System.err.println("Audit log cleanup error: " + e.getMessage());
        }
    }
}
