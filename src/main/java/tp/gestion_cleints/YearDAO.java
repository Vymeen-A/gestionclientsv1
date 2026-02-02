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

    public void addYear(Year year) {
        String sql = "INSERT INTO years(name) VALUES(?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, year.getName());
            pstmt.executeUpdate();
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
