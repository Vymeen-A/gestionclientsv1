package tp.gestion_cleints;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    public void addClient(Client client) {
        String sql = "INSERT INTO clients(name, email, phone, address, notes, revenue) VALUES(?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, client.getName());
            pstmt.setString(2, client.getEmail());
            pstmt.setString(3, client.getPhone());
            pstmt.setString(4, client.getAddress());
            pstmt.setString(5, client.getNotes());
            pstmt.setDouble(6, client.getRevenue());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding client: " + e.getMessage());
        }
    }

    public List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT * FROM clients";

        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Client client = new Client(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("notes"),
                        rs.getDouble("revenue"));
                clients.add(client);
            }
        } catch (SQLException e) {
            System.err.println("Error getting clients: " + e.getMessage());
        }
        return clients;
    }

    public void updateClient(Client client) {
        String sql = "UPDATE clients SET name = ?, email = ?, phone = ?, address = ?, notes = ?, revenue = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, client.getName());
            pstmt.setString(2, client.getEmail());
            pstmt.setString(3, client.getPhone());
            pstmt.setString(4, client.getAddress());
            pstmt.setString(5, client.getNotes());
            pstmt.setDouble(6, client.getRevenue());
            pstmt.setInt(7, client.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating client: " + e.getMessage());
        }
    }

    public void deleteClient(int id) {
        String sql = "DELETE FROM clients WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting client: " + e.getMessage());
        }
    }

    // Statistics Methods
    public int getTotalClients() {
        String sql = "SELECT COUNT(*) FROM clients";
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total clients: " + e.getMessage());
        }
        return 0;
    }

    public double getTotalRevenue() {
        String sql = "SELECT SUM(revenue) FROM clients";
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total revenue: " + e.getMessage());
        }
        return 0.0;
    }
}
