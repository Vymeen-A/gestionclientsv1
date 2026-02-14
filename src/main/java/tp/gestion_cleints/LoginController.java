package tp.gestion_cleints;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
    @FXML
    private VBox loadingOverlay;
    @FXML
    private Button loginButton;
    @FXML
    private Button changePasswordButton;
    @FXML
    private Button resetPasswordButton;

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
            // Copy password to text field before switching
            passwordTextField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            togglePasswordButton.setText("🔒");
            passwordTextField.requestFocus();
        } else {
            // Copy password back to password field before switching
            passwordField.setText(passwordTextField.getText());
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
        User selectedUser = userComboBox.getValue();
        if (selectedUser == null) {
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            errorLabel.setText(bundle.getString("login.select_user_error"));
            return;
        }

        // Clear existing messages when starting reset
        errorLabel.setText("");

        if (!"admin".equals(selectedUser.getUsername())) {
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            errorLabel.setText(bundle.getString("login.admin_only_reset"));
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(passwordField.getScene().getWindow());
        alert.setTitle(bundle.getString("login.btn_reset"));
        alert.setHeaderText(bundle.getString("login.btn_reset"));
        alert.setContentText(
                bundle.getString("alert.delete_content").replace("{0}", selectedUser.getUsername() + " password"));

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userDAO.resetPassword(selectedUser.getUsername(), selectedUser.getUsername());
                    errorLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    errorLabel.setText(bundle.getString("login.success_reset"));
                    passwordField.clear();
                    passwordTextField.clear();
                } catch (Exception e) {
                    errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    errorLabel.setText("Error: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleChangePassword() {
        // Clear existing messages
        errorLabel.setText("");

        User selectedUser = userComboBox.getValue();
        if (selectedUser == null) {
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            errorLabel.setText(bundle.getString("login.select_user_error"));
            return;
        }

        // Read from whichever field is currently visible
        String password = passwordField.isVisible() ? passwordField.getText() : passwordTextField.getText();

        if (password.isEmpty()) {
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            errorLabel.setText(bundle.getString("login.password_prompt"));
            return;
        }

        if (userDAO.authenticate(selectedUser.getUsername(), password)) {
            showChangePasswordDialog(selectedUser.getUsername(), password);
        } else {
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            errorLabel.setText(bundle.getString("login.error"));
        }
    }

    private void showChangePasswordDialog(String username, String currentPassword) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(bundle.getString("login.btn_change"));
        dialog.initOwner(passwordField.getScene().getWindow());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("change-password.fxml"), bundle);
            Parent root = loader.load();
            ChangePasswordController controller = loader.getController();
            controller.setUser(username);
            controller.currentPasswordField.setText(currentPassword);
            controller.currentPasswordField.setEditable(false);

            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.setContent(root);
            dialogPane.getButtonTypes().add(ButtonType.CLOSE);

            // Style the close button
            Button closeButton = (Button) dialogPane.lookupButton(ButtonType.CLOSE);
            closeButton.getStyleClass().add("button-secondary");

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
        // Read from whichever field is currently visible
        String password = passwordField.isVisible() ? passwordField.getText() : passwordTextField.getText();
        Year selectedYear = yearComboBox.getValue();
        User selectedUser = userComboBox.getValue();

        if (selectedYear == null) {
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            errorLabel.setText(bundle.getString("nav.select_year"));
            return;
        }

        if (selectedUser == null) {
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            errorLabel.setText(bundle.getString("login.select_user_error"));
            return;
        }

        // Show loading and disable UI
        setLoadingState(true);
        errorLabel.setText("");

        javafx.concurrent.Task<Boolean> authTask = new javafx.concurrent.Task<>() {
            @Override
            protected Boolean call() throws Exception {
                // Background thread
                return userDAO.authenticate(selectedUser.getUsername(), password);
            }
        };

        authTask.setOnSucceeded(e -> {
            setLoadingState(false);
            if (authTask.getValue()) {
                SessionContext.getInstance().setCurrentYear(selectedYear);
                loadMainApp();
            } else {
                showAuthError(selectedUser.getUsername());
            }
        });

        authTask.setOnFailed(e -> {
            setLoadingState(false);
            errorLabel.setText(bundle.getString("alert.system_error"));
            authTask.getException().printStackTrace();
        });

        new Thread(authTask).start();
    }

    private void setLoadingState(boolean loading) {
        loadingOverlay.setVisible(loading);
        loginButton.setDisable(loading);
        changePasswordButton.setDisable(loading);
        resetPasswordButton.setDisable(loading);
        passwordField.setDisable(loading);
        passwordTextField.setDisable(loading);
        userComboBox.setDisable(loading);
        yearComboBox.setDisable(loading);
    }

    private void showAuthError(String username) {
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        // Check if user is locked or inactive to give better feedback
        // Note: getAllUsers() might also be slow, but it's okay here for error feedback
        User user = userDAO.getAllUsers().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst().orElse(null);

        if (user != null) {
            if (!user.isActive()) {
                errorLabel.setText(bundle.getString("login.account_deactivated"));
            } else if (user.isLocked()) {
                errorLabel.setText(bundle.getString("login.account_locked"));
            } else {
                errorLabel.setText(bundle.getString("login.error"));
            }
        } else {
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
            UIUtils.setStageIcon(stage);

            stage.show();
        } catch (Exception e) {
            System.err.println("Error loading main app: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText(bundle.getString("alert.system_error"));
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
