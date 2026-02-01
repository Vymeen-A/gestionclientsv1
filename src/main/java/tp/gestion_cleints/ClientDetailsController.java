package tp.gestion_cleints;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
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
    private AdminDAO adminDAO = new AdminDAO();
    private ResourceBundle bundle;

    @FXML
    public void initialize() {
        try {
            bundle = ResourceBundle.getBundle("tp.gestion_cleints.messages", java.util.Locale.getDefault());
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));

            typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            typeColumn.setCellFactory(col -> new TableCell<Transaction, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        // Style based on type
                        if (Transaction.TYPE_CHARGE.equals(item)) {
                            setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;"); // Red
                        } else if (Transaction.TYPE_PAYMENT.equals(item)) {
                            setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;"); // Green
                        } else if (Transaction.TYPE_HONORAIRE_CONTRACT.equals(item)
                                || Transaction.TYPE_HONORAIRE_EXTRA.equals(item)) {
                            setStyle("-fx-text-fill: #1565c0; -fx-font-weight: bold;"); // Blue
                        } else {
                            setStyle("");
                        }
                    }
                }
            });

            // Debit Column (Client Owes): Charges + Honoraires
            debitColumn.setCellValueFactory(new PropertyValueFactory<>("amount")); // Value fallback
            debitColumn.setCellFactory(column -> new TableCell<Transaction, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
                        setText(null);
                    } else {
                        Transaction t = getTableRow().getItem();
                        // Show ONLY if type implies Debit
                        if (Transaction.TYPE_CHARGE.equals(t.getType()) ||
                                Transaction.TYPE_HONORAIRE_CONTRACT.equals(t.getType()) ||
                                Transaction.TYPE_HONORAIRE_EXTRA.equals(t.getType())) {
                            setText(String.format("%.2f", item));
                        } else {
                            setText("-");
                        }
                    }
                }
            });

            // Credit Column (Client Pays): Payments + Produits
            creditColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
            creditColumn.setCellFactory(column -> new TableCell<Transaction, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
                        setText(null);
                    } else {
                        Transaction t = getTableRow().getItem();
                        // Show ONLY if type implies Credit
                        if (Transaction.TYPE_PAYMENT.equals(t.getType()) ||
                                Transaction.TYPE_PRODUIT.equals(t.getType())) {
                            setText(String.format("%.2f", item));
                        } else {
                            setText("-");
                        }
                    }
                }
            });

            // Balance Column - This requires calculation relative to the row index
            // For simplicity in this step, we will assume we can calculate it in
            // refreshData
            // or use a custom cell factory that calculates it on the fly (expensive for
            // large lists).
            // A better approach: The list passed to TableView should effectively have
            // limits or be pre-calculated.
            // We will stick to displaying a simple "-" for now until we implement the
            // Running Balance logic in refreshData wrapper.
            balanceColumn.setCellFactory(column -> new TableCell<Transaction, Double>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(String.format("%.2f", item)); // We will put the calculated balance in 'amount' or a
                                                              // map?
                        // Actually, 'amount' is the transaction amount. We need a way to pass the
                        // balance.
                        // We will add a transient map or use UserData.
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

    private java.util.Map<Integer, Double> transactionBalances = new java.util.HashMap<>();

    private void refreshData() {
        if (currentClient == null)
            return;

        clientNameLabel.setText(currentClient.getRaisonSociale());
        clientIceLabel.setText("I.C.E.: " + (currentClient.getIce() != null ? currentClient.getIce() : "-"));
        if (clientUsernameLabel != null)
            clientUsernameLabel.setText(currentClient.getUsername() != null ? currentClient.getUsername() : "-");
        if (clientPasswordLabel != null)
            clientPasswordLabel.setText(currentClient.getPassword() != null ? currentClient.getPassword() : "-");

        // 1. Fetch transactions
        List<Transaction> transactions = financialDAO.getTransactionsByClient(currentClient.getId());

        // 2. Calculate Totals
        double fixedFees = currentClient.getFixedTotalAmount();
        double extraFees = 0.0;
        double charges = 0.0;
        double payments = 0.0;
        double produits = 0.0;

        // Calculate Running Balances (Requires ASC order traverse)
        // Sort by Date ASC for calculation
        transactions.sort((t1, t2) -> t1.getDate().compareTo(t2.getDate()));

        double runningBalance = 0.0;
        transactionBalances.clear();

        // Initial Balance from Fixed Contract?
        // "Honoraires contractuels" - Auto-generated from contract?
        // The plan says: "Honoraires contractuels: Auto-generated".
        // For now, let's treat the 'Fixed Amount' as the starting balance or just sum
        // it up?
        // "Total honoraires = Fixed + Sum(Extra)".
        // Meaning Fixed Amount is separate from the transaction list?
        // If so, the running balance should start with the Fixed Amount?
        // Let's assume the "Fixed Amount" is an annual/monthly fee that contributes to
        // what they owe.
        // If it's not in the transaction list, we should add it to the 'Debt'.
        // Let's start runningBalance with fixedFees.
        runningBalance = fixedFees;

        for (Transaction t : transactions) {
            double amount = t.getAmount();
            String type = t.getType();
            if (type == null)
                type = Transaction.TYPE_PAYMENT;

            if (Transaction.TYPE_HONORAIRE_EXTRA.equals(type)) {
                extraFees += amount;
                runningBalance += amount;
            } else if (Transaction.TYPE_CHARGE.equals(type)) {
                charges += amount;
                runningBalance += amount;
            } else if (Transaction.TYPE_PAYMENT.equals(type)) {
                payments += amount;
                runningBalance -= amount;
            } else if (Transaction.TYPE_PRODUIT.equals(type)) {
                produits += amount;
                runningBalance -= amount;
            }

            transactionBalances.put(t.getId(), runningBalance);
        }

        double totalFees = fixedFees + extraFees;
        double netBalance = (totalFees + charges) - (payments + produits);

        // Update Labels
        String currency = bundle != null ? bundle.getString("currency") : "MAD";
        totalFeesLabel.setText(String.format("%.2f %s", totalFees, currency));
        totalChargesLabel.setText(String.format("%.2f %s", charges, currency));
        totalPaymentsLabel.setText(String.format("%.2f %s", payments, currency));
        netBalanceLabel.setText(String.format("%.2f %s", netBalance, currency));

        // Restore DESC order for display (Newest first)
        transactions.sort((t1, t2) -> t2.getDate().compareTo(t1.getDate()));

        transactionTable.setItems(FXCollections.observableArrayList(transactions));

        // Update Balance Column Cell Factory to use map
        balanceColumn.setCellFactory(column -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Transaction t = getTableRow().getItem();
                    Double bal = transactionBalances.get(t.getId());
                    if (bal != null) {
                        setText(String.format("%.2f %s", bal, currency));
                        if (bal > 0)
                            setStyle("-fx-text-fill: #ef6c00; -fx-font-weight: bold;"); // Owe money
                        else
                            setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;"); // Clean/Credit
                    } else {
                        setText("-");
                    }
                }
            }
        });
    }

    @FXML
    public void handleAddTransaction() {
        Dialog<Transaction> dialog = new Dialog<>();
        dialog.setTitle("Add Transaction");
        dialog.setHeaderText("Create a new transaction for " + currentClient.getRaisonSociale());

        ButtonType saveButtonType = new ButtonType(bundle.getString("btn.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(
                Transaction.TYPE_PAYMENT,
                Transaction.TYPE_CHARGE,
                Transaction.TYPE_HONORAIRE_EXTRA,
                Transaction.TYPE_HONORAIRE_CONTRACT);
        typeCombo.setValue(Transaction.TYPE_PAYMENT); // Default

        TextField amountField = new TextField();
        amountField.setPromptText(bundle.getString("expense.amount"));

        TextArea notesField = new TextArea();
        notesField.setPromptText(bundle.getString("form.notes"));
        notesField.setPrefHeight(60);

        grid.add(new Label("Type:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label(bundle.getString("expense.amount") + ":"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label(bundle.getString("form.notes") + ":"), 0, 2);
        grid.add(notesField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Convert result to Transaction
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double amount = Double.parseDouble(amountField.getText());
                    String type = typeCombo.getValue();
                    String notes = notesField.getText();
                    String date = new java.sql.Date(System.currentTimeMillis()).toString();
                    return new Transaction(currentClient.getId(), amount, date, notes, type);
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid Amount");
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
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, bundle.getString("alert.no_selection"),
                    bundle.getString("alert.select_transaction"));
            return;
        }

        Dialog<Transaction> dialog = new Dialog<>();
        dialog.setTitle(bundle.getString("details.edit_transaction"));
        dialog.setHeaderText("Edit transaction - " + selected.getDate());

        ButtonType saveButtonType = new ButtonType(bundle.getString("btn.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(
                Transaction.TYPE_PAYMENT,
                Transaction.TYPE_CHARGE,
                Transaction.TYPE_HONORAIRE_EXTRA,
                Transaction.TYPE_HONORAIRE_CONTRACT);
        typeCombo.setValue(selected.getType() != null ? selected.getType() : Transaction.TYPE_PAYMENT);

        TextField amountField = new TextField(String.valueOf(selected.getAmount()));
        TextArea notesField = new TextArea(selected.getNotes());
        notesField.setPrefHeight(60);

        grid.add(new Label("Type:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label(bundle.getString("expense.amount") + ":"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label(bundle.getString("form.notes") + ":"), 0, 2);
        grid.add(notesField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double amount = Double.parseDouble(amountField.getText());
                    selected.setAmount(amount);
                    selected.setType(typeCombo.getValue());
                    selected.setNotes(notesField.getText());
                    return selected;
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid Amount");
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
    public void handlePreviewReceipt() {
        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, bundle.getString("alert.no_selection"),
                    bundle.getString("alert.select_transaction"));
            return;
        }

        try {
            PreviewDialog dialog = new PreviewDialog(adminDAO.getAdminInfo());
            dialog.showTransactionReceipt(selected, currentClient);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Preview Error", "Failed to generate preview: " + e.getMessage());
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
            PreviewDialog dialog = new PreviewDialog(adminDAO.getAdminInfo());
            dialog.showStatement(currentClient, transactions);
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
