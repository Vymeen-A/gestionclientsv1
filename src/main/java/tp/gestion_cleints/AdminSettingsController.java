package tp.gestion_cleints;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.ResourceBundle;

public class AdminSettingsController {

    @FXML
    private ImageView logoImageView;
    @FXML
    private Label noLogoLabel;
    @FXML
    private TextField raisonSocialeField;
    @FXML
    private TextField nomPrenomField;
    @FXML
    private TextField adresseField;
    @FXML
    private TextField villeField;
    @FXML
    private TextField iceField;
    @FXML
    private TextField rcField;
    @FXML
    private TextField tpField;
    @FXML
    private TextField identifiantTvaField;
    @FXML
    private ComboBox<String> regimeTvaBox;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private Button saveButton;

    private String currentLogoPath;
    private AdminDAO adminDAO = new AdminDAO();
    private ResourceBundle bundle;

    @FXML
    public void initialize() {
        bundle = ResourceBundle.getBundle("tp.gestion_cleints.messages", java.util.Locale.getDefault());

        // Initialize ComboBox
        regimeTvaBox.getItems().addAll("Encaissement", "Débit");

        // Add 15 character constraint to ICE field
        iceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > 15) {
                iceField.setText(oldValue);
            }
        });

        loadAdminInfo();
    }

    private void loadAdminInfo() {
        AdminInfo info = adminDAO.getAdminInfo();
        raisonSocialeField.setText(info.getRaisonSociale());
        nomPrenomField.setText(info.getNomPrenom());
        adresseField.setText(info.getAdresse());
        villeField.setText(info.getVille());
        iceField.setText(info.getIce());
        rcField.setText(info.getRc());
        tpField.setText(info.getTp());
        identifiantTvaField.setText(info.getIdentifiantTva());
        regimeTvaBox.setValue(info.getRegimeTva());
        emailField.setText(info.getEmail());
        phoneField.setText(info.getPhone());
        currentLogoPath = info.getLogoPath();
        updateLogoPreview();
    }

    private void updateLogoPreview() {
        if (currentLogoPath != null && !currentLogoPath.isEmpty()) {
            File file = new File(currentLogoPath);
            if (file.exists()) {
                try {
                    logoImageView.setImage(new Image(file.toURI().toString()));
                    noLogoLabel.setVisible(false);
                } catch (Exception e) {
                    System.err.println("Error loading logo: " + e.getMessage());
                    logoImageView.setImage(null);
                    noLogoLabel.setVisible(true);
                }
            } else {
                logoImageView.setImage(null);
                noLogoLabel.setVisible(true);
            }
        } else {
            logoImageView.setImage(null);
            noLogoLabel.setVisible(true);
        }
    }

    @FXML
    private void handleChangeLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("admin.btn.change_logo"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(saveButton.getScene().getWindow());
        if (selectedFile != null) {
            currentLogoPath = selectedFile.getAbsolutePath();
            updateLogoPreview();
        }
    }

    @FXML
    private void handleSave() {
        AdminInfo info = new AdminInfo();
        String email = emailField.getText();
        if (email != null && !email.trim().isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(bundle.getString("alert.validation_error"), bundle.getString("alert.invalid_email"));
            return;
        }

        String ice = iceField.getText();
        if (ice != null && !ice.trim().isEmpty() && ice.length() != 15) {
            showAlert(bundle.getString("alert.validation_error"), bundle.getString("alert.invalid_ice"));
            return;
        }

        info.setRaisonSociale(raisonSocialeField.getText());
        info.setNomPrenom(nomPrenomField.getText());
        info.setAdresse(adresseField.getText());
        info.setVille(villeField.getText());
        info.setIce(ice);
        info.setRc(rcField.getText());
        info.setTp(tpField.getText());
        info.setIdentifiantTva(identifiantTvaField.getText());
        info.setRegimeTva(regimeTvaBox.getValue());
        info.setEmail(email);
        info.setPhone(phoneField.getText());
        info.setLogoPath(currentLogoPath);

        boolean success = adminDAO.updateAdminInfo(info);

        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(bundle.getString("alert.success"));
            alert.setHeaderText(null);
            alert.setContentText(bundle.getString("admin.save_success"));
            alert.showAndWait();
        } else {
            showAlert(bundle.getString("alert.error"), "Erreur lors de l'enregistrement des informations.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
