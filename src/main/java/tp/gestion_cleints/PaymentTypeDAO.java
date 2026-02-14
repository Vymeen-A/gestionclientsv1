package tp.gestion_cleints;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentTypeDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }

    public boolean addPaymentType(PaymentType type) {
        String sql = "INSERT INTO payment_types(name) VALUES(?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type.getName());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding payment type: " + e.getMessage());
            return false;
        }
    }

    public List<PaymentType> getAllPaymentTypes() {
        List<PaymentType> types = new ArrayList<>();
        String sql = "SELECT * FROM payment_types";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                types.add(new PaymentType(
                        rs.getInt("id"),
                        rs.getString("name")));
            }
        } catch (SQLException e) {
            System.err.println("Error getting payment types: " + e.getMessage());
        }
        return types;
    }

    public boolean deletePaymentType(int id) {
        String sql = "DELETE FROM payment_types WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting payment type: " + e.getMessage());
            return false;
        }
    }

    public boolean isPaymentTypeUsed(int id) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE payment_type_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
