package tp.gestion_cleints;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class HelloController {
    // Fields for input
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField revenueField;
    @FXML
    private TextArea notesArea;

    // Fields for table
    @FXML
    private TableView<Client> clientTable;
    @FXML
    private TableColumn<Client, Integer> idColumn;
    @FXML
    private TableColumn<Client, String> nameColumn;
    @FXML
    private TableColumn<Client, String> emailColumn;
    @FXML
    private TableColumn<Client, String> phoneColumn;
    @FXML
    private TableColumn<Client, String> addressColumn;
    @FXML
    private TableColumn<Client, Double> revenueColumn;

    // Fields for statistics
    @FXML
    private Label totalClientsLabel;
    @FXML
    private Label totalRevenueLabel;

    private ClientDAO clientDAO;
    private ObservableList<Client> clientList;

    public void initialize() {
        clientDAO = new ClientDAO();
        DatabaseManager.initializeDatabase(); // ensure DB exists

        // Set up columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        revenueColumn.setCellValueFactory(new PropertyValueFactory<>("revenue"));

        loadClients();
        updateStatistics();
    }

    private void loadClients() {
        clientList = FXCollections.observableArrayList(clientDAO.getAllClients());
        clientTable.setItems(clientList);
    }

    private void updateStatistics() {
        int totalClients = clientDAO.getTotalClients();
        double totalRevenue = clientDAO.getTotalRevenue();

        totalClientsLabel.setText(String.valueOf(totalClients));
        totalRevenueLabel.setText(String.format("$%.2f", totalRevenue));
    }

    @FXML
    protected void handleAddClient() {
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String address = addressField.getText();
        String notes = notesArea.getText();
        String revenueText = revenueField.getText();

        if (name.isEmpty()) {
            showAlert("Validation Error", "Name is required.");
            return;
        }

        double revenue = 0.0;
        try {
            if (revenueText != null && !revenueText.isEmpty()) {
                revenue = Double.parseDouble(revenueText);
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Revenue must be a valid number.");
            return;
        }

        Client newClient = new Client(0, name, email, phone, address, notes, revenue);
        clientDAO.addClient(newClient);

        clearFields();
        loadClients();
        updateStatistics();
    }

    @FXML
    protected void handleDeleteClient() {
        Client selectedClient = clientTable.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showAlert("No Selection", "Please select a client to delete.");
            return;
        }

        clientDAO.deleteClient(selectedClient.getId());
        loadClients();
        updateStatistics();
    }

    private void clearFields() {
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        addressField.clear();
        notesArea.clear();
        revenueField.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
