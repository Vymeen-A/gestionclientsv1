package tp.gestion_cleints;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class YearDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }

    public List<Year> getAllYears() {
        return getYearsByQuery("SELECT * FROM years WHERE soft_deleted = 0");
    }

    public List<Year> getAllYearsIncludingDeleted() {
        return getYearsByQuery("SELECT * FROM years");
    }

    private List<Year> getYearsByQuery(String sql) {
        List<Year> years = new ArrayList<>();
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                years.add(new Year(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getBoolean("soft_deleted"),
                        rs.getString("deleted_at")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return years;
    }

    public void addYear(Year year, String carryOverNote) {
        String insertYearSql = "INSERT INTO years(name) VALUES(?)";
        String carryOverSql = "INSERT INTO client_year_data (client_id, year_id, fixed_total_amount, ttc) " +
                "SELECT client_id, ?, fixed_total_amount, ttc FROM client_year_data " +
                "WHERE year_id = (SELECT id FROM years WHERE soft_deleted = 0 ORDER BY id DESC LIMIT 1)";

        // Logical Balance Calculation for Previous Year
        String balanceSql = "SELECT c.id as client_id, " +
                "(COALESCE(cy.ttc, cy.fixed_total_amount, 0) + " +
                " SUM(CASE WHEN t.type = 'HONORAIRE_EXTRA' THEN t.amount ELSE 0 END) + " +
                " SUM(CASE WHEN t.type = 'CHARGE' THEN t.amount ELSE 0 END) + " +
                " SUM(CASE WHEN t.type = 'PRODUIT' THEN t.amount ELSE 0 END) + " +
                " SUM(CASE WHEN t.type = 'SOLDE_ANTERIEUR' THEN t.amount ELSE 0 END) - " +
                " SUM(CASE WHEN t.type = 'PAYMENT' THEN t.amount ELSE 0 END)) as closing_balance " +
                "FROM clients c " +
                "LEFT JOIN client_year_data cy ON c.id = cy.client_id AND cy.year_id = ? " +
                "LEFT JOIN transactions t ON c.id = t.client_id AND t.year_id = ? " +
                "GROUP BY c.id HAVING closing_balance <> 0";

        String insertSoldeSql = "INSERT INTO transactions (client_id, year_id, amount, type, date, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                int lastYearId = -1;
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(
                                "SELECT id FROM years WHERE soft_deleted = 0 ORDER BY id DESC LIMIT 1")) {
                    if (rs.next())
                        lastYearId = rs.getInt(1);
                }

                int newYearId = -1;
                try (PreparedStatement pstmt = conn.prepareStatement(insertYearSql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, year.getName());
                    pstmt.executeUpdate();
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next())
                            newYearId = rs.getInt(1);
                    }
                }

                if (newYearId != -1) {
                    // 1. Copy Contract Fees
                    try (PreparedStatement pstmt = conn.prepareStatement(carryOverSql)) {
                        pstmt.setInt(1, newYearId);
                        pstmt.executeUpdate();
                    }

                    // 2. Carry over Balances
                    if (lastYearId != -1) {
                        try (PreparedStatement bPstmt = conn.prepareStatement(balanceSql);
                                PreparedStatement iPstmt = conn.prepareStatement(insertSoldeSql)) {
                            bPstmt.setInt(1, lastYearId);
                            bPstmt.setInt(2, lastYearId);
                            try (ResultSet rs = bPstmt.executeQuery()) {
                                while (rs.next()) {
                                    double bal = rs.getDouble("closing_balance");
                                    iPstmt.setInt(1, rs.getInt("client_id"));
                                    iPstmt.setInt(2, newYearId);
                                    iPstmt.setDouble(3, bal);
                                    iPstmt.setString(4, Transaction.TYPE_SOLDE_ANTERIEUR);
                                    iPstmt.setString(5, java.time.LocalDate.now().withDayOfYear(1).toString()); // Jan
                                                                                                                // 1st
                                    iPstmt.setString(6, carryOverNote);
                                    iPstmt.addBatch();
                                }
                                iPstmt.executeBatch();
                            }
                        }
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void softDeleteYear(int id) {
        String sql = "UPDATE years SET soft_deleted = 1, deleted_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void restoreYear(int id) {
        String sql = "UPDATE years SET soft_deleted = 0, deleted_at = NULL WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Year> getDeletedYears() {
        return getYearsByQuery("SELECT * FROM years WHERE soft_deleted = 1");
    }
}
