package tp.gestion_cleints;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ResourceBundle;

public class InvoiceListController {

    @FXML
    private TableView<Invoice> invoiceTable;
    @FXML
    private TableColumn<Invoice, String> numberColumn;
    @FXML
    private TableColumn<Invoice, String> clientColumn;
    @FXML
    private TableColumn<Invoice, String> dateColumn;
    @FXML
    private TableColumn<Invoice, Double> totalColumn;
    @FXML
    private TableColumn<Invoice, String> statusColumn;
    @FXML
    private TextField searchField;

    private InvoiceDAO invoiceDAO;
    @FXML
    private ResourceBundle resources;
    private ResourceBundle bundle;
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        invoiceDAO = new InvoiceDAO();
        this.bundle = resources;
        if (this.bundle == null) {
            this.bundle = ResourceBundle.getBundle("tp.gestion_cleints.messages", java.util.Locale.getDefault());
        }
        setupColumns();
        loadInvoices();
    }

    private void setupColumns() {
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        clientColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalTtc"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        String currency = bundle != null ? bundle.getString("currency") : "MAD";
        totalColumn.setCellFactory(tc -> new TableCell<Invoice, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null)
                    setText(null);
                else
                    setText(String.format("%.2f %s", item, currency));
            }
        });
    }

    private void loadInvoices() {
        invoiceTable.setItems(FXCollections.observableArrayList(invoiceDAO.getAllInvoices()));
    }

    @FXML
    public void handleShowAging() {
        if (mainController != null) {
            mainController.showInvoiceAging();
        }
    }

    @FXML
    public void handleAddInvoice() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("invoice-form.fxml"));
            loader.setResources(bundle);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initOwner(invoiceTable.getScene().getWindow());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(bundle.getString("btn.add_invoice"));
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadInvoices();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handlePrintInvoice() {
        Invoice selected = invoiceTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // TODO: Implement PDF Export for Invoice
            System.out.println("Printing invoice: " + selected.getNumber());
        }
    }

    @FXML
    public void handleDeleteInvoice() {
        Invoice selected = invoiceTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("Delete invoice " + selected.getNumber() + "?");
            if (alert.showAndWait().get() == ButtonType.OK) {
                invoiceDAO.deleteInvoice(selected.getId());
                loadInvoices();
            }
        }
    }
}
