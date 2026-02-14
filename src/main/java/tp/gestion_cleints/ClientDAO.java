package tp.gestion_cleints;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    private Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }

    public ClientDAO() {
    }

    private int getCurrentYearId() {
        return SessionContext.getInstance().getCurrentYear() != null
                ? SessionContext.getInstance().getCurrentYear().getId()
                : 1;
    }

    public boolean addClient(Client client) {
        String sql = "INSERT INTO clients(raison_sociale, nom_prenom, adresse, ville, ice, rc, tp, taxe_habit, tva, regime_tva, fax, email, rib, username, password, secteur, debut_act, is_hidden, category, tags) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
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
                pstmt.setString(19, client.getCategory());
                pstmt.setString(20, client.getTags());
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int newId = rs.getInt(1);
                        updateYearlyData(conn, newId, getCurrentYearId(), client.getFixedTotalAmount(),
                                client.getTtc());
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            System.err.println("Error adding client: " + e.getMessage());
            return false;
        }
    }

    private void updateYearlyData(Connection conn, int clientId, int yearId, double amount, double ttc)
            throws SQLException {
        String sql = "INSERT OR REPLACE INTO client_year_data (client_id, year_id, fixed_total_amount, ttc) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, clientId);
            pstmt.setInt(2, yearId);
            pstmt.setDouble(3, amount);
            pstmt.setDouble(4, ttc);
            pstmt.executeUpdate();
        }
    }

    private static final String CLIENT_SELECT_WITH_STATS = "SELECT c.*, COALESCE(cy.fixed_total_amount, 0) as yearly_amount, COALESCE(cy.ttc, 0) as yearly_ttc, "
            + "COALESCE(stats.total_extras, 0) as total_extras, COALESCE(stats.total_charges, 0) as total_charges, "
            + "COALESCE(stats.total_produits, 0) as total_produits, COALESCE(stats.total_payments, 0) as total_payments, "
            + "COALESCE(stats.total_solde_ant, 0) as total_solde_ant, stats.last_payment_date "
            + "FROM clients c "
            + "LEFT JOIN client_year_data cy ON c.id = cy.client_id AND cy.year_id = ? "
            + "LEFT JOIN ( "
            + "    SELECT client_id, "
            + "    SUM(CASE WHEN type = ? THEN amount ELSE 0 END) as total_extras, "
            + "    SUM(CASE WHEN type = ? THEN amount ELSE 0 END) as total_charges, "
            + "    SUM(CASE WHEN type = ? THEN amount ELSE 0 END) as total_produits, "
            + "    SUM(CASE WHEN type = ? THEN amount ELSE 0 END) as total_payments, "
            + "    SUM(CASE WHEN type = ? THEN amount ELSE 0 END) as total_solde_ant, "
            + "    MAX(CASE WHEN type = ? THEN date ELSE NULL END) as last_payment_date "
            + "    FROM transactions WHERE year_id = ? GROUP BY client_id "
            + ") stats ON c.id = stats.client_id ";

    public List<Client> getAllVisibleClients() {
        String sql = CLIENT_SELECT_WITH_STATS + "WHERE c.is_hidden = 0 AND (c.is_deleted = 0 OR c.is_deleted IS NULL)";
        return getClientsWithParams(sql);
    }

    public List<Client> getAllClientsIncludingDeleted() {
        String sql = CLIENT_SELECT_WITH_STATS + "WHERE c.is_hidden = 0 AND c.is_deleted = 1";
        return getClientsWithParams(sql);
    }

    public List<Client> getHiddenClients() {
        String sql = CLIENT_SELECT_WITH_STATS + "WHERE c.is_hidden = 1";
        return getClientsWithParams(sql);
    }

    public void setClientVisibility(int clientId, boolean isHidden) {
        String sql = "UPDATE clients SET is_hidden = ? WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, isHidden);
            pstmt.setInt(2, clientId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean updateClient(Client client) {
        String sql = "UPDATE clients SET raison_sociale=?, nom_prenom=?, adresse=?, ville=?, ice=?, rc=?, tp=?, taxe_habit=?, tva=?, regime_tva=?, fax=?, email=?, rib=?, username=?, password=?, secteur=?, debut_act=?, is_hidden=?, category=?, tags=? WHERE id=?";
        try (Connection conn = getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
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
                pstmt.setString(19, client.getCategory());
                pstmt.setString(20, client.getTags());
                pstmt.setInt(21, client.getId());
                pstmt.executeUpdate();

                updateYearlyData(conn, client.getId(), getCurrentYearId(), client.getFixedTotalAmount(),
                        client.getTtc());
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            System.err.println("Error updating client: " + e.getMessage());
            return false;
        }
    }

    public void deleteClient(int id) throws SQLException {
        String sql = "UPDATE clients SET is_deleted = 1, deleted_at = ? WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, java.time.LocalDateTime.now().toString());
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            AuditLogger.log("SOFT_DELETE", "CLIENT", String.valueOf(id), "Client moved to trash");
        }
    }

    public void permanentlyDeleteClient(int id) throws SQLException {
        try (Connection conn = getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                // Manually delete related data since foreign keys might not have ON DELETE
                // CASCADE in all tables
                String[] tables = { "transactions", "invoices", "tasks", "client_documents", "client_year_data" };
                for (String table : tables) {
                    try (PreparedStatement pstmt = conn
                            .prepareStatement("DELETE FROM " + table + " WHERE client_id = ?")) {
                        pstmt.setInt(1, id);
                        pstmt.executeUpdate();
                    }
                }

                // Delete the client
                try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM clients WHERE id = ?")) {
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                }

                conn.commit();
                AuditLogger.log("PERMANENT_DELETE", "CLIENT", String.valueOf(id),
                        "Client purged from database");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        }
    }

    public void restoreClient(int id) throws SQLException {
        String sql = "UPDATE clients SET is_deleted = 0, deleted_at = NULL WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    public int getTotalClients() {
        String sql = "SELECT COUNT(*) FROM clients WHERE is_hidden = 0";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
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
        // Revenue = (Sum of Contract Amounts) + (Extras) + (Charges) + (Products) for
        // that year
        String sql = "SELECT " +
                "(SELECT SUM(CASE WHEN ttc > 0 THEN ttc ELSE fixed_total_amount END) FROM client_year_data WHERE year_id = ?) + "
                +
                "(SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE year_id = ? AND type IN (?, ?, ?))";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, yearId);
            pstmt.setInt(2, yearId);
            pstmt.setString(3, Transaction.TYPE_HONORAIRE_EXTRA);
            pstmt.setString(4, Transaction.TYPE_CHARGE);
            pstmt.setString(5, Transaction.TYPE_PRODUIT);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public double getTotalAmountDue() {
        int yearId = getCurrentYearId();
        double totalBalanceDue = 0;
        String sql = "SELECT c.id, " +
                "CASE WHEN cy.ttc > 0 THEN cy.ttc ELSE cy.fixed_total_amount END as base_amount, " +
                "SUM(CASE WHEN t.type = ? THEN t.amount ELSE 0 END) as total_extras, " +
                "SUM(CASE WHEN t.type = ? THEN t.amount ELSE 0 END) as total_charges, " +
                "SUM(CASE WHEN t.type = ? THEN t.amount ELSE 0 END) as total_produits, " +
                "SUM(CASE WHEN t.type = ? THEN t.amount ELSE 0 END) as total_payments, " +
                "SUM(CASE WHEN t.type = ? THEN t.amount ELSE 0 END) as total_solde_ant " +
                "FROM clients c " +
                "LEFT JOIN client_year_data cy ON c.id = cy.client_id AND cy.year_id = ? " +
                "LEFT JOIN transactions t ON c.id = t.client_id AND t.year_id = ? " +
                "WHERE c.is_hidden = 0 " +
                "GROUP BY c.id";

        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, Transaction.TYPE_HONORAIRE_EXTRA);
            pstmt.setString(2, Transaction.TYPE_CHARGE);
            pstmt.setString(3, Transaction.TYPE_PRODUIT);
            pstmt.setString(4, Transaction.TYPE_PAYMENT);
            pstmt.setString(5, Transaction.TYPE_SOLDE_ANTERIEUR);
            pstmt.setInt(6, yearId);
            pstmt.setInt(7, yearId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    double balance = (rs.getDouble("base_amount") + rs.getDouble("total_extras")
                            + rs.getDouble("total_charges") + rs.getDouble("total_produits")
                            + rs.getDouble("total_solde_ant"))
                            - rs.getDouble("total_payments");
                    if (balance > 0)
                        totalBalanceDue += balance;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalBalanceDue;
    }

    public double getTotalPaid() {
        int yearId = getCurrentYearId();
        String sql = "SELECT SUM(amount) FROM transactions WHERE year_id = ? AND type = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, yearId);
            pstmt.setString(2, Transaction.TYPE_PAYMENT);
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
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        String sql = CLIENT_SELECT_WITH_STATS + "WHERE c.is_hidden = 0 ORDER BY c.id DESC LIMIT ?";
        return getClientsWithParams(sql, limit);
    }

    public List<Client> searchClients(String query, boolean includeDeleted) {
        String deletedFilter = includeDeleted ? "" : " AND (c.is_deleted = 0 OR c.is_deleted IS NULL)";
        String sql = CLIENT_SELECT_WITH_STATS
                + "WHERE (c.raison_sociale LIKE ? OR c.nom_prenom LIKE ?) AND c.is_hidden = 0"
                + deletedFilter;
        String pattern = "%" + query + "%";
        return getClientsWithParams(sql, pattern, pattern);
    }

    private List<Client> getClientsWithParams(String sql, Object... params) {
        List<Client> clients = new ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int yearId = getCurrentYearId();
            int paramIndex = 1;
            pstmt.setInt(paramIndex++, yearId); // For cy join
            pstmt.setString(paramIndex++, Transaction.TYPE_HONORAIRE_EXTRA);
            pstmt.setString(paramIndex++, Transaction.TYPE_CHARGE);
            pstmt.setString(paramIndex++, Transaction.TYPE_PRODUIT);
            pstmt.setString(paramIndex++, Transaction.TYPE_PAYMENT);
            pstmt.setString(paramIndex++, Transaction.TYPE_SOLDE_ANTERIEUR);
            pstmt.setString(paramIndex++, Transaction.TYPE_PAYMENT); // For last_payment_date
            pstmt.setInt(paramIndex++, yearId); // For stats join

            for (Object param : params) {
                pstmt.setObject(paramIndex++, param);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Client c = mapClient(rs);
                    // Financial stats are now already in the ResultSet, we just need to set them
                    double baseAmount = c.getTtc() > 0 ? c.getTtc() : c.getFixedTotalAmount();
                    double tExtras = rs.getDouble("total_extras");
                    double tCharges = rs.getDouble("total_charges");
                    double tProduits = rs.getDouble("total_produits");
                    double tPayments = rs.getDouble("total_payments");
                    double tSoldeAnt = rs.getDouble("total_solde_ant");
                    String lastPaymentDate = rs.getString("last_payment_date");

                    double totalDue = baseAmount + tExtras + tCharges + tProduits + tSoldeAnt;
                    double balance = totalDue - tPayments;

                    c.setHonoraires(baseAmount + tExtras);
                    c.setAutres(tCharges + tProduits);
                    c.setTotalHonEtTt(totalDue);
                    c.setTotalAvance(tPayments);
                    c.setReste(balance);

                    if (c.isDeleted()) {
                        c.setStatus("DELETED");
                    } else if (balance <= 0.01) {
                        c.setStatus("GOOD");
                    } else if (lastPaymentDate == null) {
                        c.setStatus("LATE");
                    } else {
                        try {
                            java.time.LocalDate lastDate = java.time.LocalDate.parse(lastPaymentDate);
                            if (java.time.temporal.ChronoUnit.DAYS.between(lastDate, java.time.LocalDate.now()) > 90)
                                c.setStatus("RISKY");
                            else
                                c.setStatus("GOOD");
                        } catch (Exception e) {
                            c.setStatus("LATE");
                        }
                    }
                    clients.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clients;
    }

    private Client mapClient(ResultSet rs) throws SQLException {
        String category = null;
        try {
            category = rs.getString("category");
        } catch (SQLException e) {
        }
        String tags = null;
        try {
            tags = rs.getString("tags");
        } catch (SQLException e) {
        }
        boolean isDeleted = false;
        try {
            isDeleted = rs.getBoolean("is_deleted");
        } catch (SQLException e) {
        }
        String deletedAt = null;
        try {
            deletedAt = rs.getString("deleted_at");
        } catch (SQLException e) {
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
                rs.getDouble("yearly_amount"),
                rs.getDouble("yearly_ttc"),
                getCurrentYearId(),
                rs.getBoolean("is_hidden"),
                category,
                tags,
                isDeleted,
                deletedAt);
    }

    public boolean existsByIce(String ice) {
        if (ice == null || ice.isEmpty())
            return false;
        String sql = "SELECT COUNT(*) FROM clients WHERE ice = ? AND is_hidden = 0";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ice);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void ensureDocumentsTableExists() {
        String createTable = "CREATE TABLE IF NOT EXISTS client_documents ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "client_id INTEGER NOT NULL, "
                + "file_name TEXT, "
                + "file_path TEXT, "
                + "upload_date TEXT, "
                + "FOREIGN KEY(client_id) REFERENCES clients(id) ON DELETE CASCADE"
                + ")";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(createTable);
        } catch (SQLException e) {
            System.err.println("Warning: Could not create client_documents table: " + e.getMessage());
        }
    }

    public void addDocument(int clientId, String fileName, String filePath) throws SQLException {
        ensureDocumentsTableExists();
        String sql = "INSERT INTO client_documents (client_id, file_name, file_path, upload_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, clientId);
            pstmt.setString(2, fileName);
            pstmt.setString(3, filePath);
            pstmt.setString(4, java.time.LocalDate.now().toString());
            pstmt.executeUpdate();
        }
    }

    public List<Client.ClientDocument> getDocuments(int clientId) throws SQLException {
        List<Client.ClientDocument> documents = new ArrayList<>();
        String sql = "SELECT * FROM client_documents WHERE client_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, clientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    documents.add(new Client.ClientDocument(rs.getInt("id"), rs.getInt("client_id"),
                            rs.getString("file_name"), rs.getString("file_path"), rs.getString("upload_date")));
                }
            }
        }
        return documents;
    }

    public java.util.Map<String, Integer> getRecentClientGrowth() {
        java.util.Map<String, Integer> results = new java.util.LinkedHashMap<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter monthFmt = java.time.format.DateTimeFormatter.ofPattern("MMM",
                java.util.Locale.FRENCH);

        // Initialize last 6 months with 0
        for (int i = 5; i >= 0; i--) {
            results.put(today.minusMonths(i).format(monthFmt), 0);
        }

        // Optimization: Date filtering in Java is safer due to mixed formats (Y-M-D and
        // D/M/Y),
        // but we only select the column we need to reduce memory/IO.
        String sql = "SELECT debut_act FROM clients WHERE is_hidden = 0 AND is_deleted = 0";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String dStr = rs.getString("debut_act");
                if (dStr == null || dStr.trim().isEmpty())
                    continue;

                try {
                    java.time.LocalDate date = null;
                    if (dStr.contains("-")) {
                        date = java.time.LocalDate.parse(dStr);
                    } else if (dStr.contains("/")) {
                        String[] pts = dStr.split("/");
                        if (pts.length == 3) {
                            int day = Integer.parseInt(pts[0]);
                            int month = Integer.parseInt(pts[1]);
                            int year = Integer.parseInt(pts[2]);
                            date = java.time.LocalDate.of(year, month, day);
                        }
                    }

                    if (date != null) {
                        // Check if within the last 6 months
                        java.time.LocalDate startRange = today.minusMonths(5).withDayOfMonth(1);
                        if (!date.isBefore(startRange) && !date.isAfter(today)) {
                            String key = date.format(monthFmt);
                            if (results.containsKey(key)) {
                                results.put(key, results.get(key) + 1);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignore malformed dates
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
}
