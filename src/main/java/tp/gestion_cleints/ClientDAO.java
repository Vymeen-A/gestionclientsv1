package tp.gestion_cleints;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }

    private Connection conn;

    public ClientDAO() {
        try {
            this.conn = getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getCurrentYearId() {
        return SessionContext.getInstance().getCurrentYear() != null
                ? SessionContext.getInstance().getCurrentYear().getId()
                : 1;
    }

    public boolean addClient(Client client) {
        String sql = "INSERT INTO clients(raison_sociale, nom_prenom, adresse, ville, ice, rc, tp, taxe_habit, tva, regime_tva, fax, email, rib, username, password, secteur, debut_act, is_hidden) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
            pstmt.setBoolean(18, client.isHidden());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int newId = rs.getInt(1);
                updateYearlyData(newId, getCurrentYearId(), client.getFixedTotalAmount(), client.getTtc());
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding client: " + e.getMessage());
            return false;
        }
    }

    private void updateYearlyData(int clientId, int yearId, double amount, double ttc) throws SQLException {
        String sql = "INSERT OR REPLACE INTO client_year_data (client_id, year_id, fixed_total_amount, ttc) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, clientId);
            pstmt.setInt(2, yearId);
            pstmt.setDouble(3, amount);
            pstmt.setDouble(4, ttc);
            pstmt.executeUpdate();
        }
    }

    public List<Client> getAllVisibleClients() {
        int yearId = getCurrentYearId();
        String sql = "SELECT c.*, COALESCE(cy.fixed_total_amount, 0) as yearly_amount, COALESCE(cy.ttc, 0) as yearly_ttc "
                +
                "FROM clients c LEFT JOIN client_year_data cy ON c.id = cy.client_id AND cy.year_id = ? " +
                "WHERE c.is_hidden = 0";
        return getClientsWithParams(sql, yearId);
    }

    public List<Client> getHiddenClients() {
        int yearId = getCurrentYearId();
        String sql = "SELECT c.*, COALESCE(cy.fixed_total_amount, 0) as yearly_amount, COALESCE(cy.ttc, 0) as yearly_ttc "
                +
                "FROM clients c LEFT JOIN client_year_data cy ON c.id = cy.client_id AND cy.year_id = ? " +
                "WHERE c.is_hidden = 1";
        return getClientsWithParams(sql, yearId);
    }

    public void setClientVisibility(int clientId, boolean isHidden) {
        String sql = "UPDATE clients SET is_hidden = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, isHidden);
            pstmt.setInt(2, clientId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean updateClient(Client client) {
        String sql = "UPDATE clients SET raison_sociale=?, nom_prenom=?, adresse=?, ville=?, ice=?, rc=?, tp=?, taxe_habit=?, tva=?, regime_tva=?, fax=?, email=?, rib=?, username=?, password=?, secteur=?, debut_act=?, is_hidden=? WHERE id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
            pstmt.setBoolean(18, client.isHidden());
            pstmt.setInt(19, client.getId());
            pstmt.executeUpdate();

            updateYearlyData(client.getId(), getCurrentYearId(), client.getFixedTotalAmount(), client.getTtc());
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating client: " + e.getMessage());
            return false;
        }
    }

    public void deleteClient(int id) {
        String sql = "DELETE FROM clients WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting client: " + e.getMessage());
        }
    }

    public int getTotalClients() {
        String sql = "SELECT COUNT(*) FROM clients WHERE is_hidden = 0";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getTotalRevenue() {
        int yearId = getCurrentYearId();
        String sql = "SELECT SUM(fixed_total_amount) FROM client_year_data WHERE year_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, yearId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public String getTopClientName() {
        int yearId = getCurrentYearId();
        String sql = "SELECT c.raison_sociale FROM clients c JOIN client_year_data cy ON c.id = cy.client_id " +
                "WHERE cy.year_id = ? AND c.is_hidden = 0 ORDER BY cy.fixed_total_amount DESC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, yearId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    public List<Client> getRecentClients(int limit) {
        int yearId = getCurrentYearId();
        String sql = "SELECT c.*, COALESCE(cy.fixed_total_amount, 0) as yearly_amount, COALESCE(cy.ttc, 0) as yearly_ttc "
                +
                "FROM clients c LEFT JOIN client_year_data cy ON c.id = cy.client_id AND cy.year_id = ? " +
                "WHERE c.is_hidden = 0 ORDER BY c.id DESC LIMIT ?";
        List<Client> clients = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, yearId);
            pstmt.setInt(2, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next())
                    clients.add(mapResultSetToClient(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clients;
    }

    public List<Client> searchClients(String query) {
        int yearId = getCurrentYearId();
        String sql = "SELECT c.*, COALESCE(cy.fixed_total_amount, 0) as yearly_amount, COALESCE(cy.ttc, 0) as yearly_ttc "
                +
                "FROM clients c LEFT JOIN client_year_data cy ON c.id = cy.client_id AND cy.year_id = ? " +
                "WHERE (c.raison_sociale LIKE ? OR c.nom_prenom LIKE ?) AND c.is_hidden = 0";
        List<Client> clients = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String pattern = "%" + query + "%";
            pstmt.setInt(1, yearId);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next())
                    clients.add(mapResultSetToClient(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clients;
    }

    private List<Client> getClientsWithParams(String sql, int yearId) {
        List<Client> clients = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, yearId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next())
                    clients.add(mapResultSetToClient(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clients;
    }

    private Client mapResultSetToClient(ResultSet rs) throws SQLException {
        // We use year-specific amounts if available in the result set
        double amount = rs.getDouble("fixed_total_amount"); // Fallback
        double ttc = rs.getDouble("ttc"); // Fallback

        // Try to get yearly data if joined
        try {
            amount = rs.getDouble("yearly_amount");
        } catch (Exception e) {
        }
        try {
            ttc = rs.getDouble("yearly_ttc");
        } catch (Exception e) {
        }

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
                amount,
                ttc,
                getCurrentYearId(),
                rs.getBoolean("is_hidden"));
    }
}
