package tp.gestion_cleints;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.ResourceBundle;
import java.util.Locale;

public class LoginController {

    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private Button togglePasswordButton;
    @FXML
    private Label errorLabel;
    @FXML
    private ComboBox<Year> yearComboBox;
    @FXML
    private ComboBox<User> userComboBox;

    private UserDAO userDAO = new UserDAO();
    private YearDAO yearDAO = new YearDAO();
    private ResourceBundle bundle;

    @FXML
    public void initialize() {
        bundle = ResourceBundle.getBundle("tp.gestion_cleints.messages", Locale.getDefault());

        // Sync password field and text field
        passwordField.textProperty().bindBidirectional(passwordTextField.textProperty());

        // Load Years
        loadYears();

        // Load Users
        loadUsers();
    }

    private void loadYears() {
        yearComboBox.setItems(javafx.collections.FXCollections.observableArrayList(yearDAO.getAllYears()));
        yearComboBox.setCellFactory(lv -> new ListCell<Year>() {
            @Override
            protected void updateItem(Year item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });
        yearComboBox.setButtonCell(new ListCell<Year>() {
            @Override
            protected void updateItem(Year item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });

        // Select first if available
        if (!yearComboBox.getItems().isEmpty()) {
            yearComboBox.getSelectionModel().select(0);
        }
    }

    @FXML
    private void togglePasswordVisibility() {
        if (passwordField.isVisible()) {
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            togglePasswordButton.setText("🔒");
            passwordTextField.requestFocus();
        } else {
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            togglePasswordButton.setText("👁");
            passwordField.requestFocus();
        }
    }

    @FXML
    private void handleResetPassword() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(bundle.getString("login.btn_reset"));
        alert.setHeaderText(bundle.getString("login.btn_reset"));
        alert.setContentText(bundle.getString("alert.delete_content").replace("{0}", "password"));

        if (alert.showAndWait().get() == ButtonType.OK) {
            userDAO.updatePassword("admin", "admin");
            errorLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            errorLabel.setText(bundle.getString("login.success_reset"));
        }
    }

    @FXML
    private void handleChangePassword() {
        String password = passwordField.getText();

        if (password.isEmpty()) {
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            errorLabel.setText(bundle.getString("login.password_prompt"));
            return;
        }

        if (userDAO.authenticate("admin", password)) {
            showChangePasswordDialog(password);
        } else {
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            errorLabel.setText(bundle.getString("login.error"));
        }
    }

    private void showChangePasswordDialog(String currentPassword) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(bundle.getString("login.btn_change"));
        dialog.initOwner(passwordField.getScene().getWindow());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("change-password.fxml"), bundle);
            Parent root = loader.load();
            ChangePasswordController controller = loader.getController();
            controller.currentPasswordField.setText(currentPassword);
            controller.currentPasswordField.setEditable(false); // Locked to verified password

            dialog.getDialogPane().setContent(root);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadUsers() {
        userComboBox.setItems(javafx.collections.FXCollections.observableArrayList(userDAO.getAllUsers()));
        userComboBox.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getUsername());
            }
        });
        userComboBox.setButtonCell(new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getUsername());
            }
        });
        if (!userComboBox.getItems().isEmpty()) {
            userComboBox.getSelectionModel().select(0);
        }
    }

    @FXML
    private void handleLogin() {
        String password = passwordField.getText();
        Year selectedYear = yearComboBox.getValue();
        User selectedUser = userComboBox.getValue();

        if (selectedYear == null) {
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            errorLabel.setText(bundle.getString("nav.select_year"));
            return;
        }

        if (selectedUser == null) {
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            errorLabel.setText("Please select a user");
            return;
        }

        if (userDAO.authenticate(selectedUser.getUsername(), password)) {
            SessionContext.getInstance().setCurrentYear(selectedYear);
            loadMainApp();
        } else {
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            errorLabel.setText(bundle.getString("login.error"));
        }
    }

    private void loadMainApp() {
        try {
            ResourceBundle appBundle = ResourceBundle.getBundle("tp.gestion_cleints.messages", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-layout.fxml"), appBundle);
            Parent root = loader.load();

            Stage stage = (Stage) passwordField.getScene().getWindow();
            stage.setTitle("Gestion Clients - Dashboard");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.setResizable(true);

            // Maintain window icon
            try {
                stage.getIcons().clear();
                stage.getIcons().add(new javafx.scene.image.Image(
                        getClass().getResourceAsStream("images/logo.png")));
            } catch (Exception e) {
                System.err.println("Could not maintain main icon: " + e.getMessage());
            }

            stage.show();
        } catch (Exception e) {
            System.err.println("Error loading main app: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText("System error: Could not load application.");
        }
    }

    @FXML
    private void handleMinimize() {
        ((Stage) passwordField.getScene().getWindow()).setIconified(true);
    }

    @FXML
    private void handleExit() {
        javafx.application.Platform.exit();
        System.exit(0);
    }
}
