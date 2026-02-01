package tp.gestion_cleints;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ResourceBundle;
import java.text.MessageFormat;

public class ClientListController {

    @FXML
    public TextField searchField;
    @FXML
    public TableView<Client> clientTable;
    @FXML
    public TableColumn<Client, Integer> idColumn;
    @FXML
    public TableColumn<Client, String> raisonSocialeColumn;
    @FXML
    public TableColumn<Client, String> nomPrenomColumn;
    @FXML
    public TableColumn<Client, String> villeColumn;
    @FXML
    public TableColumn<Client, Double> fixedAmountColumn;
    @FXML
    public TableColumn<Client, Double> ttcColumn;

    private ClientDAO clientDAO;
    private AdminDAO adminDAO = new AdminDAO(); // Injected
    private ObservableList<Client> clientList;
    private ResourceBundle bundle;

    public void initialize() {
        clientDAO = new ClientDAO();
        bundle = ResourceBundle.getBundle("tp.gestion_cleints.messages", java.util.Locale.getDefault());
        setupColumns();
        loadClients();

        // Real-time search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterClients(newVal);
        });
    }

    private void setupColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        raisonSocialeColumn.setCellValueFactory(new PropertyValueFactory<>("raisonSociale"));
        nomPrenomColumn.setCellValueFactory(new PropertyValueFactory<>("nomPrenom"));
        villeColumn.setCellValueFactory(new PropertyValueFactory<>("ville"));
        fixedAmountColumn.setCellValueFactory(new PropertyValueFactory<>("fixedTotalAmount"));
        ttcColumn.setCellValueFactory(new PropertyValueFactory<>("ttc"));

        // Format amount
        String currency = bundle != null ? bundle.getString("currency") : "MAD";
        fixedAmountColumn.setCellFactory(tc -> new CurrencyCell(currency));
        ttcColumn.setCellFactory(tc -> new CurrencyCell(currency));
    }

    private class CurrencyCell extends TableCell<Client, Double> {
        private final String currency;

        public CurrencyCell(String currency) {
            this.currency = currency;
        }

        @Override
        protected void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(String.format("%.2f %s", item, currency));
            }
        }
    }

    private void loadClients() {
        clientList = FXCollections.observableArrayList(clientDAO.getAllClients());
        clientTable.setItems(clientList);
    }

    private void filterClients(String query) {
        if (query == null || query.isEmpty()) {
            loadClients();
        } else {
            clientList = FXCollections.observableArrayList(clientDAO.searchClients(query));
            clientTable.setItems(clientList);
        }
    }

    @FXML
    public void handleAddClient() {
        openClientForm(null);
    }

    @FXML
    public void handleEditClient() {
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openClientForm(selected);
        } else {
            showAlert(bundle.getString("alert.no_selection_title"), bundle.getString("alert.no_selection_content"));
        }
    }

    @FXML
    public void handleDeleteClient() {
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(bundle.getString("alert.delete_title"));
            String content = MessageFormat.format(bundle.getString("alert.delete_content"),
                    selected.getRaisonSociale());
            alert.setContentText(content);
            if (alert.showAndWait().get() == ButtonType.OK) {
                clientDAO.deleteClient(selected.getId());
                loadClients();
            }
        } else {
            showAlert(bundle.getString("alert.no_selection_title"), bundle.getString("alert.no_selection_content"));
        }
    }

    @FXML
    public void handleViewDetails() {
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("client-details.fxml"));
                loader.setResources(bundle);
                Parent root = loader.load();

                ClientDetailsController controller = loader.getController();
                controller.setClient(selected);

                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Client Details - " + selected.getRaisonSociale());

                // Set window icon
                try {
                    stage.getIcons().add(new javafx.scene.image.Image(
                            getClass().getResourceAsStream("images/for-detailles-window.png")));
                } catch (Exception e) {
                    System.err.println("Could not load details icon: " + e.getMessage());
                }

                stage.setScene(new Scene(root));
                stage.showAndWait();

                loadClients(); // Refresh in case totals changed
            } catch (IOException e) {
                System.err.println("CRITICAL ERROR LOADING FXML:");
                e.printStackTrace();
                showAlert("Error", "Could not open client details: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "An unexpected error occurred: " + e.getMessage());
            }
        } else {
            showAlert(bundle.getString("alert.no_selection_title"), bundle.getString("alert.no_selection_content"));
        }
    }

    @FXML
    public void handleExportPdf() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle(bundle.getString("btn.export_pdf"));
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Client_Report.pdf");

        java.io.File file = fileChooser.showSaveDialog(clientTable.getScene().getWindow());
        if (file != null) {
            try {
                PdfExporter.exportClients(clientList, adminDAO.getAdminInfo(), file.getAbsolutePath(), bundle);
                showAlert(bundle.getString("alert.success"), "Report exported successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(bundle.getString("alert.error"), "Failed to export PDF: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handlePreviewPdf() {
        try {
            PreviewDialog dialog = new PreviewDialog(adminDAO.getAdminInfo());
            dialog.showClientList(clientList);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Preview Error", "Failed to generate preview: " + e.getMessage());
        }
    }

    private void openClientForm(Client client) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("client-form.fxml"));
            loader.setResources(bundle);
            Parent root = loader.load();

            ClientFormController controller = loader.getController();
            controller.setClient(client);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(client == null ? bundle.getString("btn.add_client") : bundle.getString("btn.edit"));

            // Set specific icon for add/modify
            String iconPath = client == null ? "images/add.png" : "images/modify.png";
            try {
                stage.getIcons().add(new javafx.scene.image.Image(
                        getClass().getResourceAsStream(iconPath)));
            } catch (Exception e) {
                System.err.println("Could not load form icon (" + iconPath + "): " + e.getMessage());
            }

            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadClients(); // Refresh after close
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }
}
