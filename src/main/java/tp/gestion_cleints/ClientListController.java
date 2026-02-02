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
    private TextField searchField;
    @FXML
    private TableView<Client> clientTable;
    @FXML
    private TableColumn<Client, Integer> idColumn;
    @FXML
    private TableColumn<Client, String> raisonSocialeColumn;
    @FXML
    private TableColumn<Client, String> nomPrenomColumn;
    @FXML
    private TableColumn<Client, String> villeColumn;
    @FXML
    private TableColumn<Client, Double> fixedAmountColumn;
    @FXML
    private TableColumn<Client, Double> ttcColumn;
    @FXML
    private TableColumn<Client, Void> actionsColumn;

    private ClientDAO clientDAO;
    private ObservableList<Client> clientList;
    private ResourceBundle bundle;
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void initialize() {
        clientDAO = new ClientDAO();
        bundle = ResourceBundle.getBundle("tp.gestion_cleints.messages", java.util.Locale.getDefault());
        setupColumns();
        loadClients();

        // Real-time search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterClients(newVal);
        });

        // Double click interaction
        clientTable.setRowFactory(tv -> {
            TableRow<Client> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Client rowData = row.getItem();
                    handleViewDetails(rowData);
                }
            });
            return row;
        });
    }

    private void setupColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        raisonSocialeColumn.setCellValueFactory(new PropertyValueFactory<>("raisonSociale"));
        nomPrenomColumn.setCellValueFactory(new PropertyValueFactory<>("nomPrenom"));
        villeColumn.setCellValueFactory(new PropertyValueFactory<>("ville"));
        fixedAmountColumn.setCellValueFactory(new PropertyValueFactory<>("fixedTotalAmount"));
        ttcColumn.setCellValueFactory(new PropertyValueFactory<>("ttc"));

        String currency = bundle != null ? bundle.getString("currency") : "MAD";
        fixedAmountColumn.setCellFactory(tc -> new CurrencyCell(currency));
        ttcColumn.setCellFactory(tc -> new CurrencyCell(currency));

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button hideBtn = new Button(bundle.getString("btn.hide"));

            {
                hideBtn.getStyleClass().addAll("button-secondary", "hide-badge-button");
                hideBtn.setTooltip(new Tooltip(bundle.getString("btn.hide")));
                hideBtn.setOnAction(event -> {
                    Client client = getTableView().getItems().get(getIndex());
                    clientDAO.setClientVisibility(client.getId(), true);
                    loadClients();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hideBtn);
                }
            }
        });
    }

    private class CurrencyCell extends TableCell<Client, Double> {
        private final String currency;

        public CurrencyCell(String currency) {
            this.currency = currency;
        }

        @Override
        protected void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null)
                setText(null);
            else
                setText(String.format("%.2f %s", item, currency));
        }
    }

    private void loadClients() {
        clientList = FXCollections.observableArrayList(clientDAO.getAllVisibleClients());
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
    public void handleViewDetails() {
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            handleViewDetails(selected);
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
                AdminDAO adminDAO = new AdminDAO();
                PdfExporter.exportClients(clientList, adminDAO.getAdminInfo(), file.getAbsolutePath(), bundle);
                showAlert(bundle.getString("alert.success"), bundle.getString("pdf.export_success"));
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(bundle.getString("alert.error"),
                        MessageFormat.format(bundle.getString("pdf.export_failed"), e.getMessage()));
            }
        }
    }

    @FXML
    public void handleExportData() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle(bundle.getString("btn.export_data"));
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("JSON Files", "*.json"));
        fileChooser.setInitialFileName("Clients_Data.json");

        java.io.File file = fileChooser.showSaveDialog(clientTable.getScene().getWindow());
        if (file != null) {
            try {
                DataExporter.exportClients(clientList, file.getAbsolutePath());
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle(bundle.getString("alert.success"));
                successAlert.setContentText(bundle.getString("data.export_success"));
                successAlert.show();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(bundle.getString("alert.error"),
                        MessageFormat.format(bundle.getString("data.export_failed"), e.getMessage()));
            }
        }
    }

    @FXML
    public void handleImportData() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle(bundle.getString("btn.import_data"));
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("JSON Files", "*.json"));

        java.io.File file = fileChooser.showOpenDialog(clientTable.getScene().getWindow());
        if (file != null) {
            try {
                java.util.List<Client> importedClients = DataExporter.importClients(file.getAbsolutePath());
                if (importedClients == null || importedClients.isEmpty())
                    return;

                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle(bundle.getString("btn.import_data"));
                confirm.setContentText(
                        MessageFormat.format(bundle.getString("data.import_confirm"), importedClients.size()));

                if (confirm.showAndWait().get() == ButtonType.OK) {
                    int count = 0;
                    for (Client c : importedClients) {
                        // Reset ID to let DB generate new one
                        c.setId(0);
                        if (clientDAO.addClient(c)) {
                            count++;
                        }
                    }
                    loadClients();
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle(bundle.getString("alert.success"));
                    successAlert.setContentText(MessageFormat.format(bundle.getString("data.import_success"), count));
                    successAlert.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(bundle.getString("alert.error"),
                        MessageFormat.format(bundle.getString("data.import_failed"), e.getMessage()));
            }
        }
    }

    @FXML
    public void handlePreviewPdf() {
        try {
            AdminDAO adminDAO = new AdminDAO();
            PreviewDialog dialog = new PreviewDialog(adminDAO.getAdminInfo());
            dialog.initOwner(clientTable.getScene().getWindow());
            dialog.showClientList(clientList);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(bundle.getString("pdf.preview_error"),
                    MessageFormat.format(bundle.getString("pdf.preview_failed"), e.getMessage()));
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
        }
    }

    private void handleViewDetails(Client selected) {
        if (mainController != null) {
            mainController.showClientDetails(selected);
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
            stage.initOwner(clientTable.getScene().getWindow());
            stage.initModality(Modality.APPLICATION_MODAL);

            // Set icon based on action
            try {
                String iconPath = (client == null) ? "images/add.png" : "images/modify.png";
                stage.getIcons().add(new javafx.scene.image.Image(
                        getClass().getResourceAsStream(iconPath)));
            } catch (Exception e) {
                System.err.println("Could not load form icon: " + e.getMessage());
            }

            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadClients();
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
