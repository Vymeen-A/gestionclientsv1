package tp.gestion_cleints;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.ResourceBundle;
import java.util.List;

public class ClientDetailsController {

    @FXML
    public Label clientNameLabel;
    @FXML
    public Label clientIceLabel;
    @FXML
    public Label clientUsernameLabel;
    @FXML
    public Label clientPasswordLabel;
    @FXML
    public Label totalFeesLabel;
    @FXML
    public Label totalChargesLabel;
    @FXML
    public Label totalPaymentsLabel;
    @FXML
    public Label netBalanceLabel;
    @FXML
    public TableView<Transaction> transactionTable;
    @FXML
    public TableColumn<Transaction, String> receiptNumberColumn;
    @FXML
    public TableColumn<Transaction, String> dateColumn;
    @FXML
    public TableColumn<Transaction, String> typeColumn;
    @FXML
    public TableColumn<Transaction, String> notesColumn;
    @FXML
    public TableColumn<Transaction, Double> debitColumn;
    @FXML
    public TableColumn<Transaction, Double> creditColumn;
    @FXML
    public TableColumn<Transaction, Double> balanceColumn;

    private Client currentClient;
    private FinancialDAO financialDAO = new FinancialDAO();
    private PaymentTypeDAO paymentTypeDAO = new PaymentTypeDAO();
    private ResourceBundle bundle;
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        bundle = ResourceBundle.getBundle("tp.gestion_cleints.messages", java.util.Locale.getDefault());
        receiptNumberColumn.setCellValueFactory(new PropertyValueFactory<>("receiptNumber"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        // Setup numeric columns
        debitColumn.setCellFactory(tc -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                Transaction t = empty ? null : getTableView().getItems().get(getIndex());
                if (empty || t == null) {
                    setText(null);
                } else if (Transaction.TYPE_CHARGE.equals(t.getType())
                        || Transaction.TYPE_HONORAIRE_EXTRA.equals(t.getType())) {
                    setText(String.format("%.2f", t.getAmount()));
                } else {
                    setText(null);
                }
            }
        });

        creditColumn.setCellFactory(tc -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                Transaction t = empty ? null : getTableView().getItems().get(getIndex());
                if (empty || t == null) {
                    setText(null);
                } else if (Transaction.TYPE_PAYMENT.equals(t.getType())) {
                    setText(String.format("%.2f", t.getAmount()));
                } else {
                    setText(null);
                }
            }
        });
    }

    public void setClient(Client client) {
        this.currentClient = client;
        refreshData();
    }

    private void refreshData() {
        if (currentClient == null)
            return;

        clientNameLabel.setText(currentClient.getRaisonSociale());
        clientIceLabel.setText("I.C.E.: " + (currentClient.getIce() != null ? currentClient.getIce() : "-"));
        clientUsernameLabel.setText(currentClient.getUsername() != null ? currentClient.getUsername() : "-");
        clientPasswordLabel.setText(currentClient.getPassword() != null ? currentClient.getPassword() : "-");

        List<Transaction> transactions = financialDAO.getTransactionsByClient(currentClient.getId());
        transactionTable.setItems(FXCollections.observableArrayList(transactions));

        // Update totals
        // Use TTC as the base contract amount if available, otherwise fallback to fixed
        // (HT)
        double baseAmount = currentClient.getTtc() > 0 ? currentClient.getTtc() : currentClient.getFixedTotalAmount();
        double totalFees = baseAmount
                + financialDAO.getTotalByClientAndType(currentClient.getId(), Transaction.TYPE_HONORAIRE_EXTRA);
        double charges = financialDAO.getTotalByClientAndType(currentClient.getId(), Transaction.TYPE_CHARGE);
        double payments = financialDAO.getTotalPaidByClient(currentClient.getId());

        String currency = bundle != null ? bundle.getString("currency") : "MAD";
        totalFeesLabel.setText(String.format("%.2f %s", totalFees, currency));
        totalChargesLabel.setText(String.format("%.2f %s", charges, currency));
        totalPaymentsLabel.setText(String.format("%.2f %s", payments, currency));
        netBalanceLabel.setText(String.format("%.2f %s", (totalFees + charges - payments), currency));
    }

    @FXML
    public void handleAddTransaction() {
        Dialog<Transaction> dialog = new Dialog<>();
        dialog.initOwner(clientNameLabel.getScene().getWindow());

        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new javafx.scene.image.Image(
                    getClass().getResourceAsStream("images/for-add-transaction.png")));
        } catch (Exception e) {
        }

        dialog.setTitle(bundle.getString("details.btn.record_payment"));

        ButtonType saveButtonType = new ButtonType(bundle.getString("btn.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 40, 10, 10));

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(Transaction.TYPE_PAYMENT, Transaction.TYPE_CHARGE,
                Transaction.TYPE_HONORAIRE_EXTRA);
        typeCombo.setValue(Transaction.TYPE_PAYMENT);

        ComboBox<PaymentType> paymentTypeCombo = new ComboBox<>();
        paymentTypeCombo.setItems(FXCollections.observableArrayList(paymentTypeDAO.getAllPaymentTypes()));
        // Select first one by default if available
        if (!paymentTypeCombo.getItems().isEmpty()) {
            paymentTypeCombo.setValue(paymentTypeCombo.getItems().get(0));
        }

        TextField amountField = new TextField();
        TextField receiptNoField = new TextField(financialDAO.getNextReceiptNumber());
        TextArea notesField = new TextArea();
        notesField.setPrefRowCount(3);

        grid.add(new Label("Type:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label(bundle.getString("payment.mode") + ":"), 0, 1);
        grid.add(paymentTypeCombo, 1, 1);
        grid.add(new Label(bundle.getString("details.column.receipt_no") + ":"), 0, 2);
        grid.add(receiptNoField, 1, 2);
        grid.add(new Label("Amount:"), 0, 3);
        grid.add(amountField, 1, 3);
        grid.add(new Label("Notes:"), 0, 4);
        grid.add(notesField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double amount = Double.parseDouble(amountField.getText());
                    String date = new java.sql.Date(System.currentTimeMillis()).toString();
                    int ptId = paymentTypeCombo.getValue() != null ? paymentTypeCombo.getValue().getId() : 1;
                    return new Transaction(currentClient.getId(), amount, date, notesField.getText(),
                            typeCombo.getValue(), SessionContext.getInstance().getCurrentYear().getId(), ptId,
                            receiptNoField.getText());
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(t -> {
            if (t != null) {
                financialDAO.addTransaction(t);
                refreshData();
            }
        });
    }

    @FXML
    public void handleEditTransaction() {
        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        Dialog<Transaction> dialog = new Dialog<>();
        dialog.initOwner(clientNameLabel.getScene().getWindow());

        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("images/modify.png")));
        } catch (Exception e) {
        }

        dialog.setTitle(bundle.getString("btn.edit"));

        ButtonType saveButtonType = new ButtonType(bundle.getString("btn.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 40, 10, 10));

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(Transaction.TYPE_PAYMENT, Transaction.TYPE_CHARGE,
                Transaction.TYPE_HONORAIRE_EXTRA);
        typeCombo.setValue(selected.getType());

        ComboBox<PaymentType> paymentTypeCombo = new ComboBox<>();
        paymentTypeCombo.setItems(FXCollections.observableArrayList(paymentTypeDAO.getAllPaymentTypes()));
        // Try to match current payment type
        for (PaymentType pt : paymentTypeCombo.getItems()) {
            if (pt.getId() == selected.getPaymentTypeId()) {
                paymentTypeCombo.setValue(pt);
                break;
            }
        }

        TextField amountField = new TextField(String.valueOf(selected.getAmount()));
        TextField receiptNoField = new TextField(selected.getReceiptNumber());
        TextArea notesField = new TextArea(selected.getNotes());
        notesField.setPrefRowCount(3);

        grid.add(new Label("Type:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label(bundle.getString("payment.mode") + ":"), 0, 1);
        grid.add(paymentTypeCombo, 1, 1);
        grid.add(new Label(bundle.getString("details.column.receipt_no") + ":"), 0, 2);
        grid.add(receiptNoField, 1, 2);
        grid.add(new Label("Amount:"), 0, 3);
        grid.add(amountField, 1, 3);
        grid.add(new Label("Notes:"), 0, 4);
        grid.add(notesField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    selected.setAmount(Double.parseDouble(amountField.getText()));
                    selected.setNotes(notesField.getText());
                    selected.setType(typeCombo.getValue());
                    selected.setReceiptNumber(receiptNoField.getText());
                    if (paymentTypeCombo.getValue() != null) {
                        selected.setPaymentTypeId(paymentTypeCombo.getValue().getId());
                    }
                    return selected;
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(t -> {
            if (t != null) {
                financialDAO.updateTransaction(t);
                refreshData();
            }
        });
    }

    @FXML
    public void handlePreviewReceipt() {
        try {
            AdminDAO adminDAO = new AdminDAO();
            PreviewDialog dialog = new PreviewDialog(adminDAO.getAdminInfo());
            dialog.initOwner(clientNameLabel.getScene().getWindow());
            Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                dialog.showReceipt(currentClient, selected);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handlePreviewStatement() {
        try {
            AdminDAO adminDAO = new AdminDAO();
            PreviewDialog dialog = new PreviewDialog(adminDAO.getAdminInfo());
            dialog.initOwner(clientNameLabel.getScene().getWindow());
            dialog.showStatement(currentClient, transactionTable.getItems());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleClose() {
        if (mainController != null) {
            mainController.showClients();
        }
    }

    @FXML
    public void handleDeleteTransaction() {
        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            financialDAO.deleteTransaction(selected.getId());
            refreshData();
        }
    }
}
