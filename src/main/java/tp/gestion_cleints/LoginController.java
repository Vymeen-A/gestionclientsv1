package tp.gestion_cleints;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private UserDAO userDAO = new UserDAO();
    private java.util.ResourceBundle bundle;

    @FXML
    public void initialize() {
        // Default to English if not specified, or use system default
        bundle = java.util.ResourceBundle.getBundle("tp.gestion_cleints.messages", java.util.Locale.getDefault());
    }

    @FXML
    private void handleResetPassword() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle(bundle.getString("login.btn_reset"));
        alert.setHeaderText(bundle.getString("login.btn_reset"));
        alert.setContentText(bundle.getString("alert.delete_content").replace("{0}", "password")); // A bit hacky but
                                                                                                   // works for now

        if (alert.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
            userDAO.updatePassword("administrator", "admin");
            errorLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            errorLabel.setText(bundle.getString("login.success_reset"));
        }
    }

    @FXML
    private void handleLogin() {
        String password = passwordField.getText();
        if (userDAO.authenticate("administrator", password)) {
            loadMainApp();
        } else {
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            errorLabel.setText(bundle.getString("login.error"));
        }
    }

    private void loadMainApp() {
        try {
            // Main app needs the resource bundle for translations
            java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("tp.gestion_cleints.messages",
                    java.util.Locale.ENGLISH);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-layout.fxml"), bundle);
            Parent root = loader.load();

            Stage stage = (Stage) passwordField.getScene().getWindow();
            stage.setTitle("Gestion Clients - Dashboard");
            stage.setScene(new Scene(root, 1000, 700));
            stage.setResizable(true);
            stage.centerOnScreen();

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
}
