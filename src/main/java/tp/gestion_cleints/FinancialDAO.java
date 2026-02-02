package tp.gestion_cleints;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FinancialDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }

    private Connection conn;

    public FinancialDAO() {
        try {
            this.conn = getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Transactions
    public void addTransaction(Transaction t) {
        String sql = "INSERT INTO transactions(client_id, amount, date, notes, type, year_id, payment_type_id, receipt_number) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, t.getClientId());
            pstmt.setDouble(2, t.getAmount());
            pstmt.setString(3, t.getDate());
            pstmt.setString(4, t.getNotes());
            pstmt.setString(5, t.getType() != null ? t.getType() : Transaction.TYPE_PAYMENT);
            pstmt.setInt(6,
                    SessionContext.getInstance().getCurrentYear() != null
                            ? SessionContext.getInstance().getCurrentYear().getId()
                            : 1);
            pstmt.setInt(7, t.getPaymentTypeId() > 0 ? t.getPaymentTypeId() : 1);
            pstmt.setString(8, t.getReceiptNumber());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding transaction: " + e.getMessage());
        }
    }

    public void updateTransaction(Transaction t) {
        String sql = "UPDATE transactions SET amount = ?, date = ?, notes = ?, type = ?, payment_type_id = ?, receipt_number = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, t.getAmount());
            pstmt.setString(2, t.getDate());
            pstmt.setString(3, t.getNotes());
            pstmt.setString(4, t.getType());
            pstmt.setInt(5, t.getPaymentTypeId());
            pstmt.setString(6, t.getReceiptNumber());
            pstmt.setInt(7, t.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating transaction: " + e.getMessage());
        }
    }

    public void deleteTransaction(int id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
        }
    }

    public List<Transaction> getTransactionsByClient(int clientId) {
        int yearId = SessionContext.getInstance().getCurrentYear() != null
                ? SessionContext.getInstance().getCurrentYear().getId()
                : 1;
        String sql = "SELECT * FROM transactions WHERE client_id = ? AND year_id = ? ORDER BY date DESC";
        List<Transaction> list = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, clientId);
            pstmt.setInt(2, yearId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(
                            rs.getInt("id"),
                            rs.getInt("client_id"),
                            rs.getDouble("amount"),
                            rs.getString("date"),
                            rs.getString("notes"),
                            rs.getString("type"),
                            rs.getInt("year_id"),
                            rs.getInt("payment_type_id"),
                            rs.getString("receipt_number")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
        }
        return list;
    }

    public double getTotalPaidByClient(int clientId) {
        int yearId = SessionContext.getInstance().getCurrentYear() != null
                ? SessionContext.getInstance().getCurrentYear().getId()
                : 1;
        String sql = "SELECT SUM(amount) FROM transactions WHERE client_id = ? AND type = ? AND year_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, clientId);
            pstmt.setString(2, Transaction.TYPE_PAYMENT);
            pstmt.setInt(3, yearId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public double getTotalByClientAndType(int clientId, String type) {
        int yearId = SessionContext.getInstance().getCurrentYear() != null
                ? SessionContext.getInstance().getCurrentYear().getId()
                : 1;
        String sql = "SELECT SUM(amount) FROM transactions WHERE client_id = ? AND type = ? AND year_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, clientId);
            pstmt.setString(2, type);
            pstmt.setInt(3, yearId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public String getNextReceiptNumber() {
        int yearId = SessionContext.getInstance().getCurrentYear() != null
                ? SessionContext.getInstance().getCurrentYear().getId()
                : 1;
        String yearName = SessionContext.getInstance().getCurrentYear() != null
                ? SessionContext.getInstance().getCurrentYear().getName()
                : "2025";

        String sql = "SELECT receipt_number FROM transactions WHERE year_id = ? AND receipt_number LIKE ? ORDER BY id DESC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, yearId);
            pstmt.setString(2, yearName + "/%");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String last = rs.getString(1);
                    if (last != null && last.contains("/")) {
                        try {
                            String[] parts = last.split("/");
                            int seq = Integer.parseInt(parts[parts.length - 1]);
                            return String.format("%s/%04d", yearName, seq + 1);
                        } catch (Exception e) {
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return yearName + "/0001";
    }
}
