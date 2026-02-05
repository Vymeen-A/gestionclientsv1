package tp.gestion_cleints;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ProfileController {

    @FXML
    private ImageView profilePhotoView;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField roleField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;

    private MainController mainController;
    private UserDAO userDAO = new UserDAO();
    private User currentUser;
    private java.util.ResourceBundle bundle;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
        this.bundle = java.util.ResourceBundle.getBundle("tp.gestion_cleints.messages", java.util.Locale.getDefault());
        loadUserData();
    }

    private void loadUserData() {
        currentUser = SessionContext.getInstance().getCurrentUser();
        if (currentUser != null) {
            usernameField.setText(currentUser.getUsername());
            fullNameField.setText(currentUser.getFullName());
            roleField.setText(currentUser.getRole().name());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhone());

            if (currentUser.getProfilePhoto() != null) {
                File photoFile = new File(currentUser.getProfilePhoto());
                if (photoFile.exists()) {
                    profilePhotoView.setImage(new Image(photoFile.toURI().toString()));
                }
            }
        }
    }

    @FXML
    private void handleChangePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(profilePhotoView.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Create profile_photos directory if it doesn't exist
                File photosDir = new File(System.getProperty("user.home") + "/.gestion_clients/profile_photos");
                if (!photosDir.exists())
                    photosDir.mkdirs();

                // Copy photo to local app data
                File destFile = new File(photosDir, currentUser.getUsername() + "_" + selectedFile.getName());
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                currentUser.setProfilePhoto(destFile.getAbsolutePath());
                profilePhotoView.setImage(new Image(destFile.toURI().toString()));
                if (mainController != null)
                    mainController.updateUserDisplay();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(bundle.getString("alert.error"), "Could not save profile photo.");
            }
        }
    }

    @FXML
    private void handleSave() {
        currentUser.setFullName(fullNameField.getText());
        currentUser.setEmail(emailField.getText());
        currentUser.setPhone(phoneField.getText());

        if (userDAO.updateUserProfile(currentUser)) {
            if (mainController != null)
                mainController.updateUserDisplay();
            showAlert(bundle.getString("alert.success"), bundle.getString("admin.save_success"));
        } else {
            showAlert(bundle.getString("alert.error"), bundle.getString("alert.db_save_failed"));
        }
    }

    @FXML
    private void handleBack() {
        if (mainController != null) {
            mainController.showDashboard();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
