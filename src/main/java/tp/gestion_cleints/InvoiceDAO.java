package tp.gestion_cleints;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {

    public List<Invoice> getAllInvoices() {
        int yearId = SessionContext.getInstance().getCurrentYear() != null
                ? SessionContext.getInstance().getCurrentYear().getId()
                : 1;
        String sql = "SELECT i.*, c.raison_sociale FROM invoices i " +
                "JOIN clients c ON i.client_id = c.id " +
                "WHERE i.year_id = ? ORDER BY i.date DESC";
        List<Invoice> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, yearId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Invoice inv = new Invoice(
                            rs.getInt("id"),
                            rs.getInt("client_id"),
                            rs.getString("number"),
                            rs.getString("date"),
                            rs.getString("due_date"),
                            rs.getDouble("total_ht"),
                            rs.getDouble("total_tva"),
                            rs.getDouble("total_ttc"),
                            rs.getString("status"),
                            rs.getInt("year_id"));
                    inv.setClientName(rs.getString("raison_sociale"));
                    list.add(inv);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addInvoice(Invoice inv) {
        String sql = "INSERT INTO invoices (client_id, number, date, due_date, total_ht, total_tva, total_ttc, status, year_id) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, inv.getClientId());
            pstmt.setString(2, inv.getNumber());
            pstmt.setString(3, inv.getDate());
            pstmt.setString(4, inv.getDueDate());
            pstmt.setDouble(5, inv.getTotalHt());
            pstmt.setDouble(6, inv.getTotalTva());
            pstmt.setDouble(7, inv.getTotalTtc());
            pstmt.setString(8, inv.getStatus());
            pstmt.setInt(9, inv.getYearId());
            pstmt.executeUpdate();

            AuditLogger.log("CREATE", "INVOICE", inv.getNumber(), "Invoice created for client " + inv.getClientId());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getNextInvoiceNumber() {
        String yearName = SessionContext.getInstance().getCurrentYear() != null
                ? SessionContext.getInstance().getCurrentYear().getName()
                : "2025";
        String prefix = "INV-" + yearName + "-";
        String sql = "SELECT number FROM invoices WHERE number LIKE ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, prefix + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String last = rs.getString(1);
                    int seq = Integer.parseInt(last.substring(prefix.length()));
                    return String.format("%s%04d", prefix, seq + 1);
                }
            }
        } catch (Exception e) {
            // Fallback or handle error
        }
        return prefix + "0001";
    }

    public void updateStatus(int id, String status) {
        String sql = "UPDATE invoices SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteInvoice(int id) {
        String sql = "DELETE FROM invoices WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
