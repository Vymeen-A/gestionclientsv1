package tp.gestion_cleints;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class ChangePasswordController {

    @FXML
    public PasswordField currentPasswordField;
    @FXML
    public PasswordField newPasswordField;
    @FXML
    public PasswordField confirmPasswordField;
    @FXML
    public Label statusLabel;

    private UserDAO userDAO = new UserDAO();
    private java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("tp.gestion_cleints.messages",
            java.util.Locale.getDefault());

    @FXML
    public void handleUpdatePassword() {
        String current = currentPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            setStatus(bundle.getString("login.all_fields"), true);
            return;
        }

        if (!newPass.equals(confirm)) {
            setStatus(bundle.getString("login.update_match_error"), true);
            return;
        }

        // Validate current password - user 'admin'
        if (userDAO.authenticate("admin", current)) {
            userDAO.updatePassword("admin", newPass);
            setStatus(bundle.getString("login.update_success"), false);
            clearFields();
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
    }
}
