package tp.gestion_cleints;

import java.sql.*;

public class AdminDAO {

    public AdminInfo getAdminInfo() {
        String sql = "SELECT * FROM admin_info WHERE id = 1";
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                AdminInfo info = new AdminInfo();
                info.setRaisonSociale(rs.getString("raison_sociale"));
                info.setNomPrenom(rs.getString("nom_prenom"));
                info.setAdresse(rs.getString("adresse"));
                info.setVille(rs.getString("ville"));
                info.setIce(rs.getString("ice"));
                info.setRc(rs.getString("rc"));
                info.setTp(rs.getString("tp"));
                info.setIdentifiantTva(rs.getString("identifiant_tva"));
                info.setRegimeTva(rs.getString("regime_tva"));
                info.setEmail(rs.getString("email"));
                info.setPhone(rs.getString("phone"));
                info.setLogoPath(rs.getString("logo_path"));
                return info;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new AdminInfo();
    }

    public boolean updateAdminInfo(AdminInfo info) {
        String sql = "INSERT OR REPLACE INTO admin_info (id, raison_sociale, nom_prenom, adresse, ville, " +
                "ice, rc, tp, identifiant_tva, regime_tva, email, phone, logo_path) " +
                "VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, info.getRaisonSociale());
            pstmt.setString(2, info.getNomPrenom());
            pstmt.setString(3, info.getAdresse());
            pstmt.setString(4, info.getVille());
            pstmt.setString(5, info.getIce());
            pstmt.setString(6, info.getRc());
            pstmt.setString(7, info.getTp());
            pstmt.setString(8, info.getIdentifiantTva());
            pstmt.setString(9, info.getRegimeTva());
            pstmt.setString(10, info.getEmail());
            pstmt.setString(11, info.getPhone());
            pstmt.setString(12, info.getLogoPath());

            int affectedRows = pstmt.executeUpdate();
            System.out.println("Admin Info saved/updated: " + affectedRows + " row(s) affected.");
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating admin info: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
