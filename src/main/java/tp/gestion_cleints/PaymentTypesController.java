package tp.gestion_cleints;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.Optional;
import java.util.ResourceBundle;

public class PaymentTypesController {

    @FXML
    private TableView<PaymentType> typeTable;
    @FXML
    private TableColumn<PaymentType, Integer> idColumn;
    @FXML
    private TableColumn<PaymentType, String> nameColumn;
    @FXML
    private ResourceBundle resources;
    private PaymentTypeDAO typeDAO = new PaymentTypeDAO();
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        loadTypes();
    }

    private void loadTypes() {
        typeTable.setItems(FXCollections.observableArrayList(typeDAO.getAllPaymentTypes()));
    }

    @FXML
    private void handleAddType() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.initOwner(typeTable.getScene().getWindow());

        // Set icon
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new javafx.scene.image.Image(
                    getClass().getResourceAsStream("images/logo.png")));
        } catch (Exception e) {
            System.err.println("Could not load logo: " + e.getMessage());
        }

        dialog.setTitle(resources.getString("payment.add_new"));
        dialog.setHeaderText(resources.getString("payment.prompt_header"));
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String trimmedName = name.trim();
            if (trimmedName.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, resources.getString("alert.validation_error"),
                        "Name cannot be empty.");
                return;
            }

            // Check duplicate
            boolean exists = typeDAO.getAllPaymentTypes().stream()
                    .anyMatch(t -> t.getName().equalsIgnoreCase(trimmedName));
            if (exists) {
                showAlert(Alert.AlertType.WARNING, resources.getString("alert.validation_error"),
                        "This payment type already exists.");
                return;
            }

            PaymentType type = new PaymentType(0, trimmedName);
            if (typeDAO.addPaymentType(type)) {
                loadTypes();
            } else {
                showAlert(Alert.AlertType.ERROR, resources.getString("alert.db_error"),
                        resources.getString("alert.db_save_failed"));
            }
        });
    }

    @FXML
    private void handleDeleteType() {
        PaymentType selected = typeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, resources.getString("alert.no_selection"),
                    resources.getString("alert.no_selection_content"));
            return;
        }

        // Confirmation
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(resources.getString("alert.delete_title"));
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this payment type?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Check usage
            if (typeDAO.isPaymentTypeUsed(selected.getId())) {
                showAlert(Alert.AlertType.ERROR, "Deletion Blocked",
                        "This payment type is currently being used in transactions and cannot be deleted.");
                return;
            }

            if (typeDAO.deletePaymentType(selected.getId())) {
                loadTypes();
            } else {
                showAlert(Alert.AlertType.ERROR, resources.getString("alert.db_error"),
                        "Could not delete payment type.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
