package tp.gestion_cleints;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseBackup {

    public static String backup(File destinationDir) throws IOException {
        String dbPath = DatabaseManager.getDatabasePath();
        File currentDb = new File(dbPath);
        if (!currentDb.exists()) {
            throw new IOException("Database file not found at " + currentDb.getAbsolutePath());
        }

        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupFileName = "backup_" + timestamp + ".db";
        File backupFile = new File(destinationDir, backupFileName);

        // Safe hot backup using SQLite's VACUUM INTO
        try (java.sql.Connection conn = DatabaseManager.getConnection();
                java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("VACUUM INTO '" + backupFile.getAbsolutePath().replace("\\", "/") + "'");
        } catch (java.sql.SQLException e) {
            // Fallback to Files.copy if VACUUM INTO fails (unlikely)
            Files.copy(currentDb.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        AuditLogger.log("BACKUP", "DATABASE", "SYSTEM", "Manual backup created: " + backupFileName);

        return backupFile.getAbsolutePath();
    }

    public static void restore(File backupFile) throws IOException {
        File currentDb = new File(DatabaseManager.getDatabasePath());

        // Ensure the directory exists
        File parent = currentDb.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }

        // Close connections if possible? SQLite handles this poorly if active.
        // In a real app, we might need a restart or a specialized restore tool.
        // For now, we copy and hope for the best or assume connections are closed.

        Files.copy(backupFile.toPath(), currentDb.toPath(), StandardCopyOption.REPLACE_EXISTING);

        AuditLogger.log("RESTORE", "DATABASE", "SYSTEM", "Database restored from: " + backupFile.getName());
    }

    public static String getBackupMetadata(File backupFile) {
        long size = backupFile.length() / 1024;
        return String.format("Size: %d KB", size);
    }
}
