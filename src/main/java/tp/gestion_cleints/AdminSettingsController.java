package tp.gestion_cleints;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.ResourceBundle;

public class AdminSettingsController {

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

    private AdminDAO adminDAO = new AdminDAO();
    private ResourceBundle bundle;

    @FXML
    public void initialize() {
        bundle = ResourceBundle.getBundle("tp.gestion_cleints.messages", java.util.Locale.getDefault());

        // Initialize ComboBox
        regimeTvaBox.getItems().addAll("Encaissement", "Débit");

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
    }

    @FXML
    private void handleSave() {
        AdminInfo info = new AdminInfo();
        info.setRaisonSociale(raisonSocialeField.getText());
        info.setNomPrenom(nomPrenomField.getText());
        info.setAdresse(adresseField.getText());
        info.setVille(villeField.getText());
        info.setIce(iceField.getText());
        info.setRc(rcField.getText());
        info.setTp(tpField.getText());
        info.setIdentifiantTva(identifiantTvaField.getText());
        info.setRegimeTva(regimeTvaBox.getValue());
        info.setEmail(emailField.getText());
        info.setPhone(phoneField.getText());

        adminDAO.updateAdminInfo(info);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(bundle.getString("alert.success"));
        alert.setHeaderText(null);
        alert.setContentText(bundle.getString("admin.save_success"));
        alert.showAndWait();
    }
}
