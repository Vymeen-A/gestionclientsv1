package tp.gestion_cleints;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DeletedClientsController {

    @FXML
    private TableView<Client> clientTable;
    @FXML
    private TableColumn<Client, Integer> idColumn;
    @FXML
    private TableColumn<Client, String> nomPrenomColumn;
    @FXML
    private TableColumn<Client, String> raisonSocialeColumn;
    @FXML
    private TableColumn<Client, String> deletedAtColumn;
    @FXML
    private TextField searchField;

    private ClientDAO clientDAO = new ClientDAO();
    private ObservableList<Client> deletedClients;
    private MainController mainController;
    private ResourceBundle bundle;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        bundle = ResourceBundle.getBundle("tp.gestion_cleints.messages", java.util.Locale.getDefault());
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomPrenomColumn.setCellValueFactory(new PropertyValueFactory<>("nomPrenom"));
        raisonSocialeColumn.setCellValueFactory(new PropertyValueFactory<>("raisonSociale"));
        deletedAtColumn.setCellValueFactory(new PropertyValueFactory<>("deletedAt"));

        loadDeletedClients();

        // Search logic
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterClients(newVal);
        });
    }

    private void loadDeletedClients() {
        deletedClients = FXCollections.observableArrayList(clientDAO.getAllClientsIncludingDeleted());
        clientTable.setItems(deletedClients);
    }

    private void filterClients(String query) {
        if (query == null || query.isEmpty()) {
            loadDeletedClients();
        } else {
            deletedClients = FXCollections.observableArrayList(clientDAO.searchClients(query, true));
            // Filter only deleted ones in search results if searchClients returns both
            deletedClients.removeIf(c -> !c.isDeleted());
            clientTable.setItems(deletedClients);
        }
    }

    @FXML
    private void handleRestoreClient() {
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, bundle.getString("alert.no_selection"),
                    bundle.getString("alert.no_selection_trash"));
            return;
        }

        try {
            clientDAO.restoreClient(selected.getId());
            loadDeletedClients();
            AuditLogger.log("RESTORE", "CLIENT", String.valueOf(selected.getId()),
                    "Client restored from trash: " + selected.getNomPrenom());

            // Proactive navigation back to active clients list
            handleBack();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, bundle.getString("alert.error"), e.getMessage());
        }
    }

    @FXML
    private void handlePermanentDelete() {
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, bundle.getString("alert.no_selection"),
                    bundle.getString("alert.no_selection_trash"));
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(bundle.getString("alert.delete_title"));
        alert.setHeaderText(bundle.getString("alert.purge_header"));
        alert.setContentText(
                java.text.MessageFormat.format(bundle.getString("alert.purge_content"), selected.getNomPrenom()));

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                clientDAO.permanentlyDeleteClient(selected.getId());
                loadDeletedClients();
                AuditLogger.log("PURGE", "CLIENT", String.valueOf(selected.getId()),
                        "Client permanently deleted: " + selected.getNomPrenom());
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, bundle.getString("alert.error"), e.getMessage());
            }
        }
    }

    @FXML
    private void handleBack() {
        if (mainController != null) {
            mainController.showClients();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }
}
