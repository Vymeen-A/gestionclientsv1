package tp.gestion_cleints;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.Optional;
import java.util.ResourceBundle;

public class YearManagementController {

    @FXML
    private TableView<Year> yearTable;
    @FXML
    private TableColumn<Year, Integer> idColumn;
    @FXML
    private TableColumn<Year, String> nameColumn;

    @FXML
    private TableView<Year> deletedYearTable;
    @FXML
    private TableColumn<Year, Integer> deletedIdColumn;
    @FXML
    private TableColumn<Year, String> deletedNameColumn;
    @FXML
    private TableColumn<Year, String> deletedAtColumn;
    @FXML
    private ResourceBundle resources;

    private YearDAO yearDAO = new YearDAO();
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // Active Table
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Deleted Table
        deletedIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        deletedNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        deletedAtColumn.setCellValueFactory(new PropertyValueFactory<>("deletedAt"));

        loadYears();
    }

    private void loadYears() {
        yearTable.setItems(FXCollections.observableArrayList(yearDAO.getAllYears()));
        deletedYearTable.setItems(FXCollections.observableArrayList(yearDAO.getDeletedYears()));
    }

    @FXML
    private void handleAddYear() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.initOwner(yearTable.getScene().getWindow());

        // Set icon
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new javafx.scene.image.Image(
                    getClass().getResourceAsStream("images/logo.png")));
        } catch (Exception e) {
            System.err.println("Could not load logo: " + e.getMessage());
        }

        dialog.setTitle(resources.getString("year.add_new"));
        dialog.setHeaderText(resources.getString("year.prompt_header"));
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String trimmedName = name.trim();
            if (trimmedName.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, resources.getString("alert.validation_error"),
                        resources.getString("year.error_empty"));
                return;
            }

            // Check duplicate
            boolean exists = yearDAO.getAllYearsIncludingDeleted().stream()
                    .anyMatch(y -> y.getName().equalsIgnoreCase(trimmedName));
            if (exists) {
                showAlert(Alert.AlertType.WARNING, resources.getString("alert.validation_error"),
                        resources.getString("year.error_duplicate"));
                return;
            }

            Year year = new Year(0, trimmedName);
            yearDAO.addYear(year, resources.getString("year.carry_over_note"));
            loadYears();
            if (mainController != null) {
                mainController.refreshYearMenu();
            }
        });
    }

    @FXML
    private void handleSetActive() {
        Year selected = yearTable.getSelectionModel().getSelectedItem();
        if (selected != null && !selected.isSoftDeleted()) {
            SessionContext.getInstance().setCurrentYear(selected);
            if (mainController != null) {
                mainController.updateYearLabel();
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(yearTable.getScene().getWindow());

            // Set icon
            try {
                javafx.stage.Stage stage = (javafx.stage.Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new javafx.scene.image.Image(
                        getClass().getResourceAsStream("images/logo.png")));
            } catch (Exception e) {
                System.err.println("Could not load logo: " + e.getMessage());
            }

            alert.setTitle(resources.getString("year.changed_title"));
            alert.setContentText(
                    java.text.MessageFormat.format(resources.getString("year.changed_content"), selected.getName()));
            alert.showAndWait();
        }
    }

    @FXML
    private void handleDeleteYear() {
        Year selected = yearTable.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        // Prevent deleting active year
        Year activeYear = SessionContext.getInstance().getCurrentYear();
        if (activeYear != null && activeYear.getId() == selected.getId()) {
            showAlert(Alert.AlertType.ERROR, resources.getString("alert.db_error"),
                    resources.getString("year.active_delete_warning"));
            return;
        }

        // Generate 3-digit code
        int code = (int) (Math.random() * 900) + 100;

        TextInputDialog dialog = new TextInputDialog();
        dialog.initOwner(yearTable.getScene().getWindow());
        dialog.setTitle(resources.getString("details.confirm_delete"));
        dialog.setHeaderText(resources.getString("year.delete_code_header"));

        String prompt = java.text.MessageFormat.format(resources.getString("year.delete_code_prompt"),
                selected.getName(), code);
        dialog.setContentText(prompt);

        // Set icon
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("images/logo.png")));
        } catch (Exception e) {
        }

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && result.get().equals(String.valueOf(code))) {
            yearDAO.softDeleteYear(selected.getId());
            loadYears();
            if (mainController != null) {
                mainController.refreshYearMenu();
            }
        } else if (result.isPresent()) {
            showAlert(Alert.AlertType.ERROR, resources.getString("alert.db_error"),
                    resources.getString("year.error_code_mismatch"));
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(yearTable.getScene().getWindow());
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("images/logo.png")));
        } catch (Exception e) {
        }
        alert.showAndWait();
    }

    @FXML
    private void handleRestoreYear() {
        Year selected = deletedYearTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            yearDAO.restoreYear(selected.getId());
            loadYears();
            if (mainController != null) {
                mainController.refreshYearMenu();
            }
        }
    }
}
