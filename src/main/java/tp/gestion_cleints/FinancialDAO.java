package tp.gestion_cleints;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FinancialDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }

    // Transactions (Payments)
    public void addTransaction(Transaction t) {
        String sql = "INSERT INTO transactions(client_id, amount, date, notes) VALUES(?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, t.getClientId());
            pstmt.setDouble(2, t.getAmount());
            pstmt.setString(3, t.getDate());
            pstmt.setString(4, t.getNotes());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding transaction: " + e.getMessage());
        }
    }

    public void updateTransaction(Transaction t) {
        String sql = "UPDATE transactions SET amount = ?, date = ?, notes = ? WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, t.getAmount());
            pstmt.setString(2, t.getDate());
            pstmt.setString(3, t.getNotes());
            pstmt.setInt(4, t.getId());
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
                    list.add(new Transaction(
                            rs.getInt("id"),
                            rs.getInt("client_id"),
                            rs.getDouble("amount"),
                            rs.getString("date"),
                            rs.getString("notes")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
        }
        return list;
    }

    public double getTotalPaidByClient(int clientId) {
        String sql = "SELECT SUM(amount) FROM transactions WHERE client_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, clientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error total paid: " + e.getMessage());
        }
        return 0.0;
    }

    // Expenses
    public void addExpense(Expense e) {
        String sql = "INSERT INTO expenses(description, amount, date) VALUES(?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, e.getDescription());
            pstmt.setDouble(2, e.getAmount());
            pstmt.setString(3, e.getDate());
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Error adding expense: " + ex.getMessage());
        }
    }

    public List<Expense> getAllExpenses() {
        String sql = "SELECT * FROM expenses ORDER BY date DESC";
        List<Expense> list = new ArrayList<>();
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Expense(
                        rs.getInt("id"),
                        rs.getString("description"),
                        rs.getDouble("amount"),
                        rs.getString("date")));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching expenses: " + e.getMessage());
        }
        return list;
    }

    public double getTotalExpenses() {
        String sql = "SELECT SUM(amount) FROM expenses";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error total expenses: " + e.getMessage());
        }
        return 0.0;
    }
}
