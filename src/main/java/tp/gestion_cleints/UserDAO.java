package tp.gestion_cleints;

import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class UserDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }

    public boolean authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    boolean active = rs.getBoolean("is_active");
                    String lockoutUntil = rs.getString("lockout_until");
                    int failedAttempts = rs.getInt("failed_attempts");

                    if (!active)
                        return false;

                    if (lockoutUntil != null) {
                        try {
                            java.time.LocalDateTime until = java.time.LocalDateTime.parse(lockoutUntil);
                            if (until.isAfter(java.time.LocalDateTime.now()))
                                return false;
                        } catch (Exception e) {
                        }
                    }

                    String hash = rs.getString("password_hash");
                    if (BCrypt.checkpw(password, hash)) {
                        Role role = Role.fromString(rs.getString("role"));
                        User user = new User(
                                rs.getString("username"),
                                role,
                                rs.getString("full_name"),
                                rs.getString("email"),
                                rs.getString("phone"),
                                rs.getString("profile_photo"),
                                rs.getBoolean("is_active"),
                                rs.getString("last_login"),
                                0,
                                null);
                        SessionContext.getInstance().setCurrentUser(user);

                        // fresh connection for update (avoids locks)
                        updateLoginSuccess(username);
                        return true;
                    } else {
                        handleLoginFailure(username, failedAttempts);
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Auth error: " + e.getMessage());
        }
        return false;
    }

    private void updateLoginSuccess(String username) {
        String sql = "UPDATE users SET last_login = ?, failed_attempts = 0, lockout_until = NULL WHERE username = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, java.time.LocalDateTime.now().toString());
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleLoginFailure(String username, int currentFailedAttempts) {
        int newFailedAttempts = currentFailedAttempts + 1;
        String lockoutUntil = null;

        if (newFailedAttempts >= 5) { // Lockout after 5 attempts
            lockoutUntil = java.time.LocalDateTime.now().plusMinutes(15).toString(); // Lock for 15 mins
        }

        String sql = "UPDATE users SET failed_attempts = ?, lockout_until = ? WHERE username = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newFailedAttempts);
            pstmt.setString(2, lockoutUntil);
            pstmt.setString(3, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePassword(String username, String newPassword) {
        updateUser(username, newPassword, null, false);
    }

    public void resetPassword(String username, String newPassword) {
        updateUser(username, newPassword, null, true);
    }

    public boolean validatePassword(String password) {
        if (password == null || password.length() < 8)
            return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c))
                hasUpper = true;
            else if (Character.isLowerCase(c))
                hasLower = true;
            else if (Character.isDigit(c))
                hasDigit = true;
            else
                hasSpecial = true;
        }
        return hasUpper && hasLower && (hasDigit || hasSpecial);
    }

    private boolean isPasswordUsed(String username, String newPassword) {
        String sql = "SELECT password_hash FROM password_history WHERE username = ? ORDER BY created_at DESC LIMIT 5";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    if (BCrypt.checkpw(newPassword, rs.getString("password_hash"))) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateUser(String username, String newPassword, Role role) {
        updateUser(username, newPassword, role, false);
    }

    private void updateUser(String username, String newPassword, Role role, boolean bypassHistory) {
        try (Connection conn = getConnection()) {
            if (newPassword != null) {
                // Enforce password history check unless bypassed
                if (!bypassHistory && isPasswordUsed(username, newPassword)) {
                    throw new RuntimeException("Password was used recently");
                }

                String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));

                // Start Transaction for multiple updates
                conn.setAutoCommit(false);
                try {
                    String sql = "INSERT INTO users(username, password_hash, role) VALUES(?, ?, ?) " +
                            "ON CONFLICT(username) DO UPDATE SET password_hash = excluded.password_hash" +
                            (role != null ? ", role = excluded.role" : "");
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, username);
                        pstmt.setString(2, hashedPassword);
                        // Security: Only 'admin' username can have ADMIN role
                        Role roleToSet = (role == Role.ADMIN && !"admin".equals(username)) ? Role.ACCOUNTANT
                                : (role != null ? role : Role.READ_ONLY);
                        pstmt.setString(3, roleToSet.name());
                        pstmt.executeUpdate();
                    }

                    // Save to history
                    String historySql = "INSERT INTO password_history(username, password_hash) VALUES(?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(historySql)) {
                        pstmt.setString(1, username);
                        pstmt.setString(2, hashedPassword);
                        pstmt.executeUpdate();
                    }

                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            } else if (role != null) {
                // Security: Only 'admin' username can have ADMIN role
                Role roleToSet = (role == Role.ADMIN && !"admin".equals(username)) ? Role.ACCOUNTANT : role;
                String sql = "UPDATE users SET role = ? WHERE username = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, roleToSet.name());
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
        String sql = "SELECT * FROM users";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(
                        rs.getString("username"),
                        Role.fromString(rs.getString("role")),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("profile_photo"),
                        rs.getBoolean("is_active"),
                        rs.getString("last_login"),
                        rs.getInt("failed_attempts"),
                        rs.getString("lockout_until")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public void setUserStatus(String username, boolean active) {
        String sql = "UPDATE users SET is_active = ? WHERE username = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, active);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
