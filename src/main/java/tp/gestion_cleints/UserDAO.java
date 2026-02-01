package tp.gestion_cleints;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class UserDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }

    public boolean authenticate(String username, String password) {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password_hash");
                    return BCrypt.checkpw(password, hash);
                }
            }
        } catch (SQLException e) {
            System.err.println("Auth error: " + e.getMessage());
        }
        return false;
    }

    public void updatePassword(String username, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        String sql = "INSERT INTO users(username, password_hash) VALUES(?, ?) " +
                "ON CONFLICT(username) DO UPDATE SET password_hash = excluded.password_hash";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
        }
    }

    public void ensureAdminExists(String defaultPassword) {
        boolean exists = false;
        String sql = "SELECT COUNT(*) FROM users WHERE username = 'administrator'";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next() && rs.getInt(1) > 0) {
                exists = true;
            }
        } catch (SQLException e) {
            System.err.println("Error checking admin: " + e.getMessage());
        }

        if (!exists) {
            updatePassword("administrator", defaultPassword);
            System.out.println("Default administrator created with password: " + defaultPassword);
        }
    }
}
