package tp.gestion_cleints;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FinancialDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }

    // Transactions
    public void addTransaction(Transaction t) {
        String sql = "INSERT INTO transactions(client_id, amount, date, notes, type) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, t.getClientId());
            pstmt.setDouble(2, t.getAmount());
            pstmt.setString(3, t.getDate());
            pstmt.setString(4, t.getNotes());
            pstmt.setString(5, t.getType() != null ? t.getType() : Transaction.TYPE_PAYMENT);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding transaction: " + e.getMessage());
        }
    }

    public void updateTransaction(Transaction t) {
        String sql = "UPDATE transactions SET amount = ?, date = ?, notes = ?, type = ? WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, t.getAmount());
            pstmt.setString(2, t.getDate());
            pstmt.setString(3, t.getNotes());
            pstmt.setString(4, t.getType());
            pstmt.setInt(5, t.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating transaction: " + e.getMessage());
        }
    }

    public void deleteTransaction(int id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
        }
    }

    public List<Transaction> getTransactionsByClient(int clientId) {
        String sql = "SELECT * FROM transactions WHERE client_id = ? ORDER BY date DESC";
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, clientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("type");
                    if (type == null)
                        type = Transaction.TYPE_PAYMENT; // Backward compatibility

                    list.add(new Transaction(
                            rs.getInt("id"),
                            rs.getInt("client_id"),
                            rs.getDouble("amount"),
                            rs.getString("date"),
                            rs.getString("notes"),
                            type));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
        }
        return list;
    }

    public double getTotalPaidByClient(int clientId) {
        // Only sum actual payments
        return getTotalByClientAndType(clientId, Transaction.TYPE_PAYMENT);
    }

    public double getTotalByClientAndType(int clientId, String type) {
        String sql = "SELECT SUM(amount) FROM transactions WHERE client_id = ? AND type = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, clientId);
            pstmt.setString(2, type);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error total by type: " + e.getMessage());
        }
        return 0.0;
    }

}
