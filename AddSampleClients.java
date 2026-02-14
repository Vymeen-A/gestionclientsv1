import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AddSampleClients {

    public static void main(String[] args) {
        String userHome = System.getProperty("user.home");
        String dbPath = userHome + "/.gestion_clients/gestion_clients.db";
        System.out.println("Using database: " + dbPath);

        // Sample client data - 10 French/Moroccan businesses
        // Format: raison_sociale, nom_prenom, adresse, ville, ice, rc, tp, taxe_habit,
        // regime_tva, fax, email, rib, username, password, secteur
        String[][] clients = {
                { "Café Marocain Atlas", "Hassan Benjelloun", "15 Avenue Mohammed V", "Casablanca", "001234567890001",
                        "12345", "67890", "20", "Encaissement", "0522-123456", "atlas@cafe.ma",
                        "MA64011234567890123456789012", "ATLAS2024", "pass123", "Restauration" },
                { "Boutique Artisanat Fès", "Fatima Alaoui", "23 Rue Talaa Kebira", "Fès", "001234567890002", "23456",
                        "78901", "20", "Débit", "0535-234567", "contact@artisanat-fes.ma",
                        "MA64022345678901234567890123", "ARTFES2024", "pass456", "Commerce" },
                { "Garage Auto Marrakech", "Ahmed El Fassi", "45 Route de Safi", "Marrakech", "001234567890003",
                        "34567", "89012", "20", "Encaissement", "0524-345678", "garage@automarrakech.ma",
                        "MA64033456789012345678901234", "GARAGE2024", "pass789", "Services" },
                { "Pharmacie Centrale", "Dr. Samira Bennani", "78 Boulevard Zerktouni", "Casablanca", "001234567890004",
                        "45678", "90123", "20", "Débit", "0522-456789", "pharmacie@centrale.ma",
                        "MA64044567890123456789012345", "PHARMA2024", "pass012", "Santé" },
                { "Librairie Moderne", "Youssef Tazi", "12 Avenue Hassan II", "Rabat", "001234567890005", "56789",
                        "01234", "20", "Encaissement", "0537-567890", "info@librairie-moderne.ma",
                        "MA64055678901234567890123456", "LIVRE2024", "pass345", "Commerce" },
                { "Hôtel Riad Salam", "Karim Idrissi", "34 Derb Sidi Bouloukat", "Marrakech", "001234567890006",
                        "67890", "12345", "20", "Débit", "0524-678901", "reservation@riadsalam.ma",
                        "MA64066789012345678901234567", "HOTEL2024", "pass678", "Hôtellerie" },
                { "Boulangerie Pâtisserie", "Mohammed Chraibi", "56 Rue de la Liberté", "Tanger", "001234567890007",
                        "78901", "23456", "20", "Encaissement", "0539-789012", "boulangerie@chraibi.ma",
                        "MA64077890123456789012345678", "BOUL2024", "pass901", "Alimentation" },
                { "Cabinet Comptable Expert", "Nadia Berrada", "89 Avenue des FAR", "Casablanca", "001234567890008",
                        "89012", "34567", "20", "Débit", "0522-890123", "cabinet@expert-compta.ma",
                        "MA64088901234567890123456789", "COMPTA2024", "pass234", "Services" },
                { "Société Import Export", "Rachid Alami", "67 Zone Industrielle", "Tanger", "001234567890009", "90123",
                        "45678", "20", "Encaissement", "0539-901234", "contact@importexport.ma",
                        "MA64099012345678901234567890", "IMPORT2024", "pass567", "Commerce" },
                { "École Privée Excellence", "Leila Mansouri", "101 Boulevard Moulay Youssef", "Rabat",
                        "001234567890010", "01234", "56789", "20", "Débit", "0537-012345", "info@ecole-excellence.ma",
                        "MA64100123456789012345678901", "ECOLE2024", "pass890", "Éducation" }
        };

        // Financial amounts for each client (HT, TTC)
        double[][] amounts = {
                { 15000.00, 18000.00 },
                { 25000.00, 30000.00 },
                { 35000.00, 42000.00 },
                { 50000.00, 60000.00 },
                { 18000.00, 21600.00 },
                { 75000.00, 90000.00 },
                { 12000.00, 14400.00 },
                { 40000.00, 48000.00 },
                { 120000.00, 144000.00 },
                { 65000.00, 78000.00 }
        };

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {
            System.out.println("Connexion à la base de données réussie!");

            // Get current year ID (assuming year 2026 exists)
            int yearId = 1; // Default year

            String insertSQL = "INSERT INTO clients (raison_sociale, nom_prenom, adresse, ville, ice, rc, tp, " +
                    "taxe_habit, tva, regime_tva, fax, email, rib, username, password, secteur, " +
                    "debut_act, fixed_total_amount, ttc, year_id, is_hidden) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";

            PreparedStatement pstmt = conn.prepareStatement(insertSQL);

            // Generate dates for the last 6 months
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate today = LocalDate.now();

            int count = 0;
            for (int i = 0; i < clients.length; i++) {
                String[] client = clients[i];

                // Calculate debut_act - spread over last 6 months
                LocalDate debutDate = today.minusMonths(5 - (i % 6));
                String debutAct = debutDate.format(formatter);

                pstmt.setString(1, client[0]); // raison_sociale
                pstmt.setString(2, client[1]); // nom_prenom
                pstmt.setString(3, client[2]); // adresse
                pstmt.setString(4, client[3]); // ville
                pstmt.setString(5, client[4]); // ice
                pstmt.setString(6, client[5]); // rc
                pstmt.setString(7, client[6]); // tp
                pstmt.setString(8, client[7]); // taxe_habit
                pstmt.setString(9, "20"); // tva (20%)
                pstmt.setString(10, client[8]); // regime_tva
                pstmt.setString(11, client[9]); // fax
                pstmt.setString(12, client[10]); // email
                pstmt.setString(13, client[11]); // rib
                pstmt.setString(14, client[12]); // username
                pstmt.setString(15, client[13]); // password
                pstmt.setString(16, client[14]); // secteur
                pstmt.setString(17, debutAct); // debut_act
                pstmt.setDouble(18, amounts[i][0]); // fixed_total_amount
                pstmt.setDouble(19, amounts[i][1]); // ttc
                pstmt.setInt(20, yearId); // year_id

                pstmt.executeUpdate();
                count++;
                System.out.println("✓ Client ajouté: " + client[0]);
            }

            System.out.println("\n========================================");
            System.out.println("✅ " + count + " clients ajoutés avec succès!");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
