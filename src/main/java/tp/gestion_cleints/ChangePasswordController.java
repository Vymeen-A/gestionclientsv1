package tp.gestion_cleints;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ChangePasswordController {

    @FXML
    public PasswordField currentPasswordField;
    @FXML
    public TextField currentTextField;
    @FXML
    public PasswordField newPasswordField;
    @FXML
    public TextField newTextField;
    @FXML
    public PasswordField confirmPasswordField;
    @FXML
    public TextField confirmTextField;
    @FXML
    public Button toggleNewButton;
    @FXML
    public Button toggleConfirmButton;
    @FXML
    public Label statusLabel;

    private UserDAO userDAO = new UserDAO();
    private java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("tp.gestion_cleints.messages",
            java.util.Locale.getDefault());
    private String username;

    @FXML
    public void initialize() {
        // Sync password fields and text fields
        currentPasswordField.textProperty().bindBidirectional(currentTextField.textProperty());
        newPasswordField.textProperty().bindBidirectional(newTextField.textProperty());
        confirmPasswordField.textProperty().bindBidirectional(confirmTextField.textProperty());
    }

    public void setUser(String username) {
        this.username = username;
    }

    @FXML
    private void toggleNewVisibility() {
        toggleVisibility(newPasswordField, newTextField, toggleNewButton);
    }

    @FXML
    private void toggleConfirmVisibility() {
        toggleVisibility(confirmPasswordField, confirmTextField, toggleConfirmButton);
    }

    private void toggleVisibility(PasswordField pf, TextField tf, Button btn) {
        if (pf.isVisible()) {
            pf.setVisible(false);
            pf.setManaged(false);
            tf.setVisible(true);
            tf.setManaged(true);
            btn.setText("🔒");
        } else {
            pf.setVisible(true);
            pf.setManaged(true);
            tf.setVisible(false);
            tf.setManaged(false);
            btn.setText("👁");
        }
    }

    @FXML
    public void handleUpdatePassword() {
        String current = currentPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            setStatus(bundle.getString("login.all_fields"), true);
            return;
        }

        // Validate complexity
        if (!userDAO.validatePassword(newPass)) {
            setStatus("Password too weak (min 8 chars, must include upper, lower, and digit/special)", true);
            return;
        }

        // Get username: provided by controller set, otherwise current session,
        // otherwise fallback
        String activeUsername = this.username;
        if (activeUsername == null) {
            User currentUser = SessionContext.getInstance().getCurrentUser();
            activeUsername = (currentUser != null) ? currentUser.getUsername() : "admin";
        }

        // Validate current password
        if (userDAO.authenticate(activeUsername, current)) {
            try {
                userDAO.updatePassword(activeUsername, newPass);
                setStatus(bundle.getString("login.update_success"), false);
                clearFields();
            } catch (RuntimeException e) {
                setStatus(e.getMessage(), true); // Likely "Password was used recently"
            }
        } else {
            setStatus(bundle.getString("login.error"), true);
        }
    }

    private void setStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + (isError ? "#e74c3c;" : "#2ecc71;"));
    }

    private void clearFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        currentTextField.clear();
        newTextField.clear();
        confirmTextField.clear();
    }
}
