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
            PaymentType type = new PaymentType(0, name);
            typeDAO.addPaymentType(type);
            loadTypes();
        });
    }

    @FXML
    private void handleDeleteType() {
        PaymentType selected = typeTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            typeDAO.deletePaymentType(selected.getId());
            loadTypes();
        }
    }
}
