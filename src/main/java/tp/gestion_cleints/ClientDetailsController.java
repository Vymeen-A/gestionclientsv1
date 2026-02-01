package tp.gestion_cleints;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.ResourceBundle;
import java.util.List;

public class ClientDetailsController {

    @FXML
    public Label clientNameLabel;
    @FXML
    public Label clientIceLabel;
    @FXML
    public Label fixedAmountLabel;
    @FXML
    public Label totalPaidLabel;
    @FXML
    public Label remainingLabel;
    @FXML
    public TableView<Transaction> transactionTable;
    @FXML
    public TableColumn<Transaction, String> dateColumn;
    @FXML
    public TableColumn<Transaction, Double> amountColumn;
    @FXML
    public TableColumn<Transaction, String> notesColumn;

    private Client currentClient;
    private FinancialDAO financialDAO = new FinancialDAO();
    private AdminDAO adminDAO = new AdminDAO();
    private ResourceBundle bundle;

    @FXML
    public void initialize() {
        try {
            bundle = ResourceBundle.getBundle("tp.gestion_cleints.messages", java.util.Locale.getDefault());
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
            notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));

            amountColumn.setCellFactory(column -> new TableCell<Transaction, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null)
                        setText(null);
                    else {
                        String currency = bundle != null ? bundle.getString("currency") : "MAD";
                        setText(String.format("%.2f %s", item, currency));
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR IN ClientDetailsController.initialize:");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Initialization Error",
                    "Failed to initialize details view: " + e.getMessage());
        }
    }

    public void setClient(Client client) {
        this.currentClient = client;
        try {
            refreshData();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Data Error", "Failed to load client data: " + e.getMessage());
        }
    }

    private void refreshData() {
        if (currentClient == null)
            return;

        clientNameLabel.setText(currentClient.getRaisonSociale());
        clientIceLabel.setText("I.C.E.: " + (currentClient.getIce() != null ? currentClient.getIce() : "-"));
        double fixed = currentClient.getFixedTotalAmount();
        double paid = financialDAO.getTotalPaidByClient(currentClient.getId());
        double remaining = fixed - paid;

        String currency = bundle != null ? bundle.getString("currency") : "MAD";
        fixedAmountLabel.setText(String.format("%.2f %s", fixed, currency));
        totalPaidLabel.setText(String.format("%.2f %s", paid, currency));
        remainingLabel.setText(String.format("%.2f %s", remaining, currency));

        transactionTable.setItems(FXCollections.observableArrayList(
                financialDAO.getTransactionsByClient(currentClient.getId())));
    }

    @FXML
    public void handleRecordPayment() {
        TextInputDialog amountDialog = new TextInputDialog();
        amountDialog.setTitle(bundle.getString("details.record_payment"));
        amountDialog.setHeaderText(java.text.MessageFormat.format(bundle.getString("details.payment_for"),
                currentClient.getRaisonSociale()));
        amountDialog.setContentText(bundle.getString("expense.amount") + " (" + bundle.getString("currency") + "):");

        // Set window icon
        try {
            ((Stage) amountDialog.getDialogPane().getScene().getWindow()).getIcons().add(
                    new javafx.scene.image.Image(getClass().getResourceAsStream("images/for-add-transaction.png")));
        } catch (Exception e) {
            System.err.println("Could not load payment amount icon: " + e.getMessage());
        }

        amountDialog.showAndWait().ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                TextInputDialog notesDialog = new TextInputDialog("Payment");
                notesDialog.setTitle(bundle.getString("details.btn.record_payment"));
                notesDialog.setHeaderText(bundle.getString("details.notes_header"));
                notesDialog.setContentText(bundle.getString("form.notes") + ":");

                // Set window icon
                try {
                    ((Stage) notesDialog.getDialogPane().getScene().getWindow()).getIcons().add(
                            new javafx.scene.image.Image(
                                    getClass().getResourceAsStream("images/for-add-transaction.png")));
                } catch (Exception e) {
                    System.err.println("Could not load payment notes icon: " + e.getMessage());
                }

                notesDialog.showAndWait().ifPresent(notes -> {
                    Transaction t = new Transaction(currentClient.getId(), amount,
                            new java.sql.Date(System.currentTimeMillis()).toString(), notes);
                    financialDAO.addTransaction(t);
                    refreshData();
                });
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, bundle.getString("alert.error"),
                        bundle.getString("alert.invalid_amount"));
            }
        });
    }

    @FXML
    public void handleEditTransaction() {
        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, bundle.getString("alert.no_selection"),
                    bundle.getString("alert.select_transaction"));
            return;
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(selected.getAmount()));
        dialog.setTitle(bundle.getString("details.edit_transaction"));
        dialog.setHeaderText(bundle.getString("details.edit_transaction") + " - " + selected.getDate());
        dialog.setContentText(bundle.getString("expense.amount") + " (" + bundle.getString("currency") + "):");

        dialog.showAndWait().ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                selected.setAmount(amount);
                financialDAO.updateTransaction(selected);
                refreshData();
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, bundle.getString("alert.error"),
                        bundle.getString("alert.invalid_amount"));
            }
        });
    }

    @FXML
    public void handleDeleteTransaction() {
        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, bundle.getString("alert.no_selection"),
                    bundle.getString("alert.select_transaction"));
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(bundle.getString("details.confirm_delete"));
        confirm.setHeaderText(bundle.getString("details.confirm_delete"));
        String content = java.text.MessageFormat.format(bundle.getString("details.delete_transaction_confirm"),
                selected.getAmount(), bundle.getString("currency"));
        confirm.setContentText(content);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            financialDAO.deleteTransaction(selected.getId());
            refreshData();
        }
    }

    @FXML
    public void handleExportReceipt() {
        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, bundle.getString("alert.no_selection"),
                    bundle.getString("alert.select_transaction"));
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Receipt");
        fileChooser.setInitialFileName(
                "Receipt_" + currentClient.getRaisonSociale().replace(" ", "_") + "_" + selected.getId() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(transactionTable.getScene().getWindow());
        if (file != null) {
            try {
                PdfExporter.exportTransactionReceipt(selected, currentClient, adminDAO.getAdminInfo(),
                        file.getAbsolutePath(), bundle);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Receipt exported successfully!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to export receipt: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handlePreviewReceipt() {
        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, bundle.getString("alert.no_selection"),
                    bundle.getString("alert.select_transaction"));
            return;
        }

        try {
            File tempFile = File.createTempFile("Receipt_Preview_", ".pdf");
            PdfExporter.exportTransactionReceipt(selected, currentClient, adminDAO.getAdminInfo(),
                    tempFile.getAbsolutePath(), bundle);
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(tempFile);
            } else {
                showAlert(Alert.AlertType.WARNING, "Preview", "Desktop operations not supported.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Preview Error", "Failed to generate preview: " + e.getMessage());
        }
    }

    @FXML
    public void handleExportStatement() {
        List<Transaction> transactions = transactionTable.getItems();
        if (transactions.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, bundle.getString("alert.no_selection_title"),
                    bundle.getString("alert.no_selection_content"));
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Statement");
        fileChooser.setInitialFileName("Statement_" + currentClient.getRaisonSociale().replace(" ", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(transactionTable.getScene().getWindow());
        if (file != null) {
            try {
                PdfExporter.exportTransactionStatement(currentClient, transactions, adminDAO.getAdminInfo(),
                        file.getAbsolutePath(), bundle);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Statement exported successfully!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to export statement: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handlePreviewStatement() {
        List<Transaction> transactions = transactionTable.getItems();
        if (transactions.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, bundle.getString("alert.no_selection_title"),
                    bundle.getString("alert.no_selection_content"));
            return;
        }

        try {
            File tempFile = File.createTempFile("Statement_Preview_", ".pdf");
            PdfExporter.exportTransactionStatement(currentClient, transactions, adminDAO.getAdminInfo(),
                    tempFile.getAbsolutePath(), bundle);
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(tempFile);
            } else {
                showAlert(Alert.AlertType.WARNING, "Preview", "Desktop operations not supported.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Preview Error", "Failed to generate preview: " + e.getMessage());
        }
    }

    @FXML
    public void handleClose() {
        ((Stage) clientNameLabel.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
