package tp.gestion_cleints;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.Optional;

public class ExpenseController {

    @FXML
    public TableView<Expense> expenseTable;
    @FXML
    public TableColumn<Expense, Integer> idColumn;
    @FXML
    public TableColumn<Expense, String> dateColumn;
    @FXML
    public TableColumn<Expense, String> descriptionColumn;
    @FXML
    public TableColumn<Expense, Double> amountColumn;
    @FXML
    public Label totalExpensesLabel;

    private FinancialDAO financialDAO = new FinancialDAO();
    private java.util.ResourceBundle bundle;

    @FXML
    public void initialize() {
        bundle = java.util.ResourceBundle.getBundle("tp.gestion_cleints.messages", java.util.Locale.getDefault());
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        loadExpenses();
    }

    private void loadExpenses() {
        expenseTable.setItems(FXCollections.observableArrayList(financialDAO.getAllExpenses()));
        String currency = bundle.getString("currency");
        totalExpensesLabel.setText(String.format("%.2f %s", financialDAO.getTotalExpenses(), currency));
    }

    @FXML
    public void handleAddExpense() {
        Dialog<Expense> dialog = new Dialog<>();
        dialog.setTitle(bundle.getString("expense.add_title"));
        dialog.setHeaderText(bundle.getString("expense.add_header"));

        // Set window icon
        try {
            ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(
                    new javafx.scene.image.Image(getClass().getResourceAsStream("images/for-add-expense.png")));
        } catch (Exception e) {
            System.err.println("Could not load expense icon: " + e.getMessage());
        }

        ButtonType saveButtonType = new ButtonType(bundle.getString("btn.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField descField = new TextField();
        descField.setPromptText(bundle.getString("expense.description"));
        TextField amountField = new TextField();
        amountField.setPromptText(bundle.getString("expense.amount"));

        grid.add(new Label(bundle.getString("expense.description") + ":"), 0, 0);
        grid.add(descField, 1, 0);
        grid.add(new Label(bundle.getString("expense.amount") + ":"), 0, 1);
        grid.add(amountField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    double amount = Double.parseDouble(amountField.getText());
                    return new Expense(0, descField.getText(), amount, LocalDate.now().toString());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Expense> result = dialog.showAndWait();
        result.ifPresent(expense -> {
            financialDAO.addExpense(expense);
            loadExpenses();
        });
    }
}
