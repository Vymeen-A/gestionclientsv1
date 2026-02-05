package tp.gestion_cleints;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class InvoiceAgingController {

    @FXML
    private TableView<Invoice> agingTable;
    @FXML
    private TableColumn<Invoice, String> numberColumn;
    @FXML
    private TableColumn<Invoice, String> clientColumn;
    @FXML
    private TableColumn<Invoice, String> dueDateColumn;
    @FXML
    private TableColumn<Invoice, Long> daysOverdueColumn;
    @FXML
    private TableColumn<Invoice, Double> totalColumn;
    @FXML
    private TableColumn<Invoice, String> statusColumn;

    private InvoiceDAO invoiceDAO;
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        invoiceDAO = new InvoiceDAO();
        setupColumns();
        loadAgingInvoices();
    }

    private void setupColumns() {
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));
        clientColumn.setCellValueFactory(new PropertyValueFactory<>("clientName"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalTtc"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        daysOverdueColumn.setCellValueFactory(cellData -> {
            try {
                LocalDate due = LocalDate.parse(cellData.getValue().getDueDate());
                if ("PAID".equals(cellData.getValue().getStatus()))
                    return null;
                long days = ChronoUnit.DAYS.between(due, LocalDate.now());
                return new javafx.beans.property.SimpleLongProperty(Math.max(0, days)).asObject();
            } catch (Exception e) {
                return null;
            }
        });

        statusColumn.setCellFactory(tc -> new TableCell<Invoice, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("OVERDUE".equals(item))
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    else if ("PAID".equals(item))
                        setStyle("-fx-text-fill: green;");
                    else
                        setStyle("");
                }
            }
        });
    }

    private void loadAgingInvoices() {
        List<Invoice> all = invoiceDAO.getAllInvoices();
        List<Invoice> aging = all.stream()
                .filter(i -> !"PAID".equals(i.getStatus()))
                .peek(i -> {
                    try {
                        LocalDate due = LocalDate.parse(i.getDueDate());
                        if (due.isBefore(LocalDate.now())) {
                            i.setStatus("OVERDUE");
                        }
                    } catch (Exception e) {
                    }
                })
                .collect(Collectors.toList());
        agingTable.setItems(FXCollections.observableArrayList(aging));
    }

    @FXML
    public void handleRefresh() {
        loadAgingInvoices();
    }

    @FXML
    public void handleMarkAsPaid() {
        Invoice selected = agingTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            invoiceDAO.updateStatus(selected.getId(), "PAID");

            // Create a payment transaction for consistency
            FinancialDAO fDAO = new FinancialDAO();
            Transaction t = new Transaction(0, selected.getClientId(), selected.getTotalTtc(),
                    LocalDate.now().toString(), "Payment for Invoice " + selected.getNumber(),
                    Transaction.TYPE_PAYMENT, selected.getYearId(), 1, null);
            fDAO.addTransaction(t);

            loadAgingInvoices();
            AuditLogger.log("UPDATE", "INVOICE", selected.getNumber(), "Marked as PAID and transaction created");
        }
    }

    @FXML
    public void handleBack() {
        if (mainController != null) {
            mainController.showInvoices();
        }
    }
}
