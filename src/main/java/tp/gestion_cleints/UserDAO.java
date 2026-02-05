package tp.gestion_cleints;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class UserDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }

    public boolean authenticate(String username, String password) {
        String sql = "SELECT username, password_hash, role, full_name, email, phone, profile_photo FROM users WHERE username = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password_hash");
                    if (BCrypt.checkpw(password, hash)) {
                        Role role = Role.fromString(rs.getString("role"));
                        User user = new User(
                                rs.getString("username"),
                                role,
                                rs.getString("full_name"),
                                rs.getString("email"),
                                rs.getString("phone"),
                                rs.getString("profile_photo"));
                        SessionContext.getInstance().setCurrentUser(user);
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Auth error: " + e.getMessage());
        }
        return false;
    }

    public void updatePassword(String username, String newPassword) {
        updateUser(username, newPassword, null);
    }

    public void updateUser(String username, String newPassword, Role role) {
        try (Connection conn = getConnection()) {
            if (newPassword != null) {
                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
                String sql = "INSERT INTO users(username, password_hash, role) VALUES(?, ?, ?) " +
                        "ON CONFLICT(username) DO UPDATE SET password_hash = excluded.password_hash" +
                        (role != null ? ", role = excluded.role" : "");
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, hashedPassword);
                    pstmt.setString(3, role != null ? role.name() : Role.READ_ONLY.name());
                    pstmt.executeUpdate();
                }
            } else if (role != null) {
                String sql = "UPDATE users SET role = ? WHERE username = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, role.name());
                    pstmt.setString(2, username);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
    }

    public void deleteUser(String username) {
        if ("admin".equals(username))
            return; // Protection
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
        }
    }

    public void ensureAdminExists(String defaultPassword) {
        boolean exists = false;
        String sql = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
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
            updateUser("admin", defaultPassword, Role.ADMIN);
            System.out.println("Default administrator created with password: " + defaultPassword);
        } else {
            // Force role to ADMIN for existing admin
            updateUser("admin", null, Role.ADMIN);
        }
    }

    public java.util.List<User> getAllUsers() {
        java.util.List<User> users = new java.util.ArrayList<>();
        String sql = "SELECT username, role FROM users";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(rs.getString("username"), Role.fromString(rs.getString("role"))));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean updateUserProfile(User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, profile_photo = ? WHERE username = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhone());
            pstmt.setString(4, user.getProfilePhoto());
            pstmt.setString(5, user.getUsername());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
