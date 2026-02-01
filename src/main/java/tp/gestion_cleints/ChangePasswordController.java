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

    @FXML
    public void handleUpdatePassword() {
        String current = currentPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            setStatus("All fields are required.", true);
            return;
        }

        if (!newPass.equals(confirm)) {
            setStatus("New passwords do not match.", true);
            return;
        }

        // Validate current password
        if (userDAO.authenticate("administrator", current)) {
            userDAO.updatePassword("administrator", newPass);
            setStatus("Password updated successfully!", false);
            clearFields();
        } else {
            setStatus("Incorrect current password.", true);
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
