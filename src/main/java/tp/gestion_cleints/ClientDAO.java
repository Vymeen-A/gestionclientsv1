package tp.gestion_cleints;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }

    public boolean addClient(Client client) {
        String sql = "INSERT INTO clients(raison_sociale, nom_prenom, adresse, ville, ice, rc, tp, taxe_habit, tva, regime_tva, fax, email, rib, username, password, secteur, debut_act, fixed_total_amount, ttc) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, client.getRaisonSociale());
            pstmt.setString(2, client.getNomPrenom());
            pstmt.setString(3, client.getAdresse());
            pstmt.setString(4, client.getVille());
            pstmt.setString(5, client.getIce());
            pstmt.setString(6, client.getRc());
            pstmt.setString(7, client.getTp());
            pstmt.setString(8, client.getTaxeHabit());
            pstmt.setString(9, client.getTva());
            pstmt.setString(10, client.getRegimeTva());
            pstmt.setString(11, client.getFax());
            pstmt.setString(12, client.getEmail());
            pstmt.setString(13, client.getRib());
            pstmt.setString(14, client.getUsername());
            pstmt.setString(15, client.getPassword());
            pstmt.setString(16, client.getSecteur());
            pstmt.setString(17, client.getDebutAct());
            pstmt.setDouble(18, client.getFixedTotalAmount());
            pstmt.setDouble(19, client.getTtc());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding client: " + e.getMessage());
            return false;
        }
    }

    public List<Client> getAllClients() {
        return getClientsByQuery("SELECT * FROM clients");
    }

    public List<Client> searchClients(String query) {
        String sql = "SELECT * FROM clients WHERE raison_sociale LIKE ? OR nom_prenom LIKE ? OR email LIKE ?";
        List<Client> clients = new ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + query + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    clients.add(mapResultSetToClient(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching clients: " + e.getMessage());
        }
        return clients;
    }

    private List<Client> getClientsByQuery(String sql) {
        List<Client> clients = new ArrayList<>();
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting clients: " + e.getMessage());
        }
        return clients;
    }

    private Client mapResultSetToClient(ResultSet rs) throws SQLException {
        return new Client(
                rs.getInt("id"),
                rs.getString("raison_sociale"),
                rs.getString("nom_prenom"),
                rs.getString("adresse"),
                rs.getString("ville"),
                rs.getString("ice"),
                rs.getString("rc"),
                rs.getString("tp"),
                rs.getString("taxe_habit"),
                rs.getString("tva"),
                rs.getString("regime_tva"),
                rs.getString("fax"),
                rs.getString("email"),
                rs.getString("rib"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("secteur"),
                rs.getString("debut_act"),
                rs.getDouble("fixed_total_amount"),
                rs.getDouble("ttc"));
    }

    public boolean updateClient(Client client) {
        String sql = "UPDATE clients SET raison_sociale = ?, nom_prenom = ?, adresse = ?, ville = ?, ice = ?, rc = ?, tp = ?, taxe_habit = ?, tva = ?, regime_tva = ?, fax = ?, email = ?, rib = ?, username = ?, password = ?, secteur = ?, debut_act = ?, fixed_total_amount = ?, ttc = ? WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, client.getRaisonSociale());
            pstmt.setString(2, client.getNomPrenom());
            pstmt.setString(3, client.getAdresse());
            pstmt.setString(4, client.getVille());
            pstmt.setString(5, client.getIce());
            pstmt.setString(6, client.getRc());
            pstmt.setString(7, client.getTp());
            pstmt.setString(8, client.getTaxeHabit());
            pstmt.setString(9, client.getTva());
            pstmt.setString(10, client.getRegimeTva());
            pstmt.setString(11, client.getFax());
            pstmt.setString(12, client.getEmail());
            pstmt.setString(13, client.getRib());
            pstmt.setString(14, client.getUsername());
            pstmt.setString(15, client.getPassword());
            pstmt.setString(16, client.getSecteur());
            pstmt.setString(17, client.getDebutAct());
            pstmt.setDouble(18, client.getFixedTotalAmount());
            pstmt.setDouble(19, client.getTtc());
            pstmt.setInt(20, client.getId());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating client: " + e.getMessage());
            return false;
        }
    }

    public void deleteClient(int id) {
        String sql = "DELETE FROM clients WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting client: " + e.getMessage());
        }
    }

    public int getTotalClients() {
        String sql = "SELECT COUNT(*) FROM clients";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error stats: " + e.getMessage());
        }
        return 0;
    }

    public double getTotalRevenue() {
        String sql = "SELECT SUM(fixed_total_amount) FROM clients";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error stats: " + e.getMessage());
        }
        return 0.0;
    }

    public String getTopClientName() {
        String sql = "SELECT raison_sociale FROM clients ORDER BY fixed_total_amount DESC LIMIT 1";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return rs.getString(1);
        } catch (SQLException e) {
            System.err.println("Error stats: " + e.getMessage());
        }
        return "N/A";
    }

    public List<Client> getRecentClients(int limit) {
        String sql = "SELECT * FROM clients ORDER BY id DESC LIMIT ?";
        List<Client> clients = new ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    clients.add(mapResultSetToClient(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error recent: " + e.getMessage());
        }
        return clients;
    }
}
