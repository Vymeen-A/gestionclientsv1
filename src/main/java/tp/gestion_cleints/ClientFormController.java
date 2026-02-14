package tp.gestion_cleints;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.awt.Desktop;
import java.util.List;
import javafx.collections.FXCollections;

public class ClientFormController {

    @FXML
    private TextField raisonSocialeField;
    @FXML
    private TextField nomPrenomField;
    @FXML
    private TextField adresseField;
    @FXML
    private TextField villeField;
    @FXML
    private TextField iceField;
    @FXML
    private TextField rcField;
    @FXML
    private TextField tpField;
    @FXML
    private TextField taxeHabitField;
    @FXML
    private TextField tvaField;
    @FXML
    private TextField regimeTvaField;
    @FXML
    private TextField faxField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField ttcField;
    @FXML
    private TextField ribField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField secteurField;
    @FXML
    private TextField debutActField;
    @FXML
    private TextField fixedTotalAmountField;
    @FXML
    private TableView<Client.ClientDocument> documentsTable;
    @FXML
    private TableColumn<Client.ClientDocument, String> docNameCol;
    @FXML
    private TableColumn<Client.ClientDocument, String> docDateCol;
    @FXML
    private TableColumn<Client.ClientDocument, Void> docActionCol;

    private ClientDAO clientDAO;
    private Client currentClient;
    private java.util.ResourceBundle bundle;

    public void initialize() {
        clientDAO = new ClientDAO();
        bundle = java.util.ResourceBundle.getBundle("tp.gestion_cleints.messages", java.util.Locale.getDefault());

        // Initialize Documents Table
        docNameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("fileName"));
        docDateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("uploadDate"));
        setupDocActionColumn();

        // Add 15 character constraint to ICE field
        iceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > 15) {
                iceField.setText(oldValue);
            }
        });

        // Auto-calculate TTC
        javafx.beans.value.ChangeListener<String> calculationListener = (obs, oldVal, newVal) -> calculateTTC();
        fixedTotalAmountField.textProperty().addListener(calculationListener);
        tvaField.textProperty().addListener(calculationListener);
    }

    private void setupDocActionColumn() {
        docActionCol.setCellFactory(param -> new TableCell<>() {
            private final Button openButton = new Button("Open");
            private final HBox pane = new HBox(openButton);

            {
                openButton.setOnAction(event -> {
                    Client.ClientDocument doc = getTableView().getItems().get(getIndex());
                    openDocument(doc);
                });
                openButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 10px;");
                pane.setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void openDocument(Client.ClientDocument doc) {
        try {
            File file = new File(doc.getFilePath());
            if (file.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                } else {
                    showAlert("Error", "Desktop API is not supported on this platform.");
                }
            } else {
                showAlert("Error", "File not found: " + doc.getFilePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open file: " + e.getMessage());
        }
    }

    @FXML
    private void handleUploadDocument() {
        if (currentClient == null) {
            showAlert("Action Required", "Please save the client before adding documents.");
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Select Document");
        File file = fileChooser.showOpenDialog(raisonSocialeField.getScene().getWindow());

        if (file != null) {
            try {
                // Create directory: ~/.gestion_clients/documents/{clientId}/
                String userHome = System.getProperty("user.home");
                Path documentsDir = Paths.get(userHome, ".gestion_clients", "documents",
                        String.valueOf(currentClient.getId()));
                if (!Files.exists(documentsDir)) {
                    Files.createDirectories(documentsDir);
                }

                // Copy file
                String newFileName = System.currentTimeMillis() + "_" + file.getName();
                Path targetPath = documentsDir.resolve(newFileName);
                Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                // Save to DB
                clientDAO.addDocument(currentClient.getId(), file.getName(), targetPath.toString());

                // Refresh table
                refreshDocuments();

                showAlert("Success", "Document added successfully!");

            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "Failed to upload document: " + e.getMessage());
            }
        }
    }

    private void refreshDocuments() {
        if (currentClient != null) {
            try {
                List<Client.ClientDocument> docs = clientDAO.getDocuments(currentClient.getId());
                documentsTable.setItems(FXCollections.observableArrayList(docs));
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
                showAlert("Error", "Could not load documents: " + e.getMessage());
            }
        }
    }

    private void calculateTTC() {
        try {
            double ht = fixedTotalAmountField.getText().isEmpty() ? 0.0
                    : Double.parseDouble(fixedTotalAmountField.getText().replace(",", "."));

            String tvaStr = tvaField.getText().replace("%", "").trim().replace(",", ".");
            double tvaPercent = tvaStr.isEmpty() ? 0.0 : Double.parseDouble(tvaStr);

            double ttc = ht * (1 + tvaPercent / 100.0);
            ttcField.setText(String.format(java.util.Locale.US, "%.2f", ttc));
        } catch (NumberFormatException e) {
            // Log it for debugging but don't show alert to user while they are typing
            // System.err.println("Typing error: " + e.getMessage());
        }
    }

    public void setClient(Client client) {
        this.currentClient = client;
        if (client != null) {
            raisonSocialeField.setText(client.getRaisonSociale());
            nomPrenomField.setText(client.getNomPrenom());
            adresseField.setText(client.getAdresse());
            villeField.setText(client.getVille());
            iceField.setText(client.getIce());
            rcField.setText(client.getRc());
            tpField.setText(client.getTp());
            taxeHabitField.setText(client.getTaxeHabit());
            tvaField.setText(client.getTva());
            regimeTvaField.setText(client.getRegimeTva());
            faxField.setText(client.getFax());
            emailField.setText(client.getEmail());
            ttcField.setText(String.valueOf(client.getTtc()));
            ribField.setText(client.getRib());
            usernameField.setText(client.getUsername());
            passwordField.setText(client.getPassword());
            secteurField.setText(client.getSecteur());
            debutActField.setText(client.getDebutAct());
            fixedTotalAmountField.setText(String.valueOf(client.getFixedTotalAmount()));
            refreshDocuments();
        }
    }

    @FXML
    private void handleSave() {
        System.out.println("[FORM] handleSave() called");
        String raison = raisonSocialeField.getText();
        if (raison == null || raison.trim().isEmpty()) {
            showAlert(bundle.getString("alert.validation_error"), bundle.getString("alert.raison_required"));
            return;
        }

        String email = emailField.getText();
        if (email != null && !email.trim().isEmpty()) {
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                showAlert(bundle.getString("alert.validation_error"), "Invalid email format.");
                return;
            }
        }

        String ice = iceField.getText();
        if (ice != null && !ice.trim().isEmpty()) {
            if (ice.length() != 15) {
                showAlert(bundle.getString("alert.validation_error"), "I.C.E must be exactly 15 characters.");
                return;
            }
            if (currentClient == null && clientDAO.existsByIce(ice)) {
                showAlert(bundle.getString("alert.validation_error"), "Un client avec cet I.C.E existe déjà.");
                return;
            }
        }

        double amount = 0.0;
        try {
            if (!fixedTotalAmountField.getText().isEmpty()) {
                amount = Double.parseDouble(fixedTotalAmountField.getText().replace(",", "."));
            }
        } catch (Exception e) {
            System.err.println("Parse error for amount: " + e.getMessage());
            showAlert(bundle.getString("alert.error"), bundle.getString("alert.invalid_amount"));
            return;
        }

        double ttc = 0.0;
        try {
            if (!ttcField.getText().isEmpty()) {
                ttc = Double.parseDouble(ttcField.getText().replace(",", "."));
            }
        } catch (Exception e) {
            System.err.println("Parse error for TTC: " + e.getMessage());
            showAlert("Selection Error", "Invalid TTC amount format.");
            return;
        }

        System.out.println("[FORM] Validation passed. currentClient = "
                + (currentClient == null ? "null (new)" : "exists (edit)"));
        boolean success;
        if (currentClient == null) {
            int yearId = SessionContext.getInstance().getCurrentYear() != null
                    ? SessionContext.getInstance().getCurrentYear().getId()
                    : 1;
            Client newClient = new Client(0, raison, nomPrenomField.getText(), adresseField.getText(),
                    villeField.getText(), iceField.getText(), rcField.getText(), tpField.getText(),
                    taxeHabitField.getText(), tvaField.getText(), regimeTvaField.getText(),
                    faxField.getText(), emailField.getText(),
                    ribField.getText(), usernameField.getText(), passwordField.getText(), secteurField.getText(),
                    debutActField.getText(), amount, ttc, yearId, false,
                    null, null, false, null);
            System.out.println("[FORM] Calling clientDAO.addClient()...");
            success = clientDAO.addClient(newClient);
        } else {
            currentClient.setRaisonSociale(raison);
            currentClient.setNomPrenom(nomPrenomField.getText());
            currentClient.setAdresse(adresseField.getText());
            currentClient.setVille(villeField.getText());
            currentClient.setIce(iceField.getText());
            currentClient.setRc(rcField.getText());
            currentClient.setTp(tpField.getText());
            currentClient.setTaxeHabit(taxeHabitField.getText());
            currentClient.setTva(tvaField.getText());
            currentClient.setRegimeTva(regimeTvaField.getText());
            currentClient.setFax(faxField.getText());
            currentClient.setEmail(emailField.getText());
            currentClient.setRib(ribField.getText());
            currentClient.setUsername(usernameField.getText());
            currentClient.setPassword(passwordField.getText());
            currentClient.setSecteur(secteurField.getText());
            currentClient.setDebutAct(debutActField.getText());
            currentClient.setFixedTotalAmount(amount);
            currentClient.setTtc(ttc);
            System.out.println("[FORM] Calling clientDAO.updateClient()...");
            success = clientDAO.updateClient(currentClient);
        }

        System.out.println("[FORM] Save result: " + (success ? "SUCCESS" : "FAILED"));
        if (success) {
            closeWindow();
        } else {
            showAlert(bundle.getString("alert.db_error"), bundle.getString("alert.db_save_failed"));
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) raisonSocialeField.getScene().getWindow();
        stage.close();
    }
}
