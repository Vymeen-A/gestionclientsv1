package tp.gestion_cleints;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ClientFormController {

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
    private TextField taxeHabitField;
    @FXML
    private TextField tvaField;
    @FXML
    private TextField regimeTvaField;
    @FXML
    private TextField faxField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField ttcField;
    @FXML
    private TextField ribField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField secteurField;
    @FXML
    private TextField debutActField;
    @FXML
    private TextField fixedTotalAmountField;

    private ClientDAO clientDAO;
    private Client currentClient;
    private java.util.ResourceBundle bundle;

    public void initialize() {
        clientDAO = new ClientDAO();
        bundle = java.util.ResourceBundle.getBundle("tp.gestion_cleints.messages", java.util.Locale.getDefault());

        // Add 15 character constraint to ICE field
        iceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > 15) {
                iceField.setText(oldValue);
            }
        });
    }

    public void setClient(Client client) {
        this.currentClient = client;
        if (client != null) {
            raisonSocialeField.setText(client.getRaisonSociale());
            nomPrenomField.setText(client.getNomPrenom());
            adresseField.setText(client.getAdresse());
            villeField.setText(client.getVille());
            iceField.setText(client.getIce());
            rcField.setText(client.getRc());
            tpField.setText(client.getTp());
            taxeHabitField.setText(client.getTaxeHabit());
            tvaField.setText(client.getTva());
            regimeTvaField.setText(client.getRegimeTva());
            faxField.setText(client.getFax());
            emailField.setText(client.getEmail());
            ttcField.setText(String.valueOf(client.getTtc()));
            ribField.setText(client.getRib());
            usernameField.setText(client.getUsername());
            passwordField.setText(client.getPassword());
            secteurField.setText(client.getSecteur());
            debutActField.setText(client.getDebutAct());
            fixedTotalAmountField.setText(String.valueOf(client.getFixedTotalAmount()));
        }
    }

    @FXML
    private void handleSave() {
        String raison = raisonSocialeField.getText();
        if (raison == null || raison.trim().isEmpty()) {
            showAlert(bundle.getString("alert.validation_error"), bundle.getString("alert.raison_required"));
            return;
        }

        String ice = iceField.getText();
        if (ice != null && !ice.trim().isEmpty() && ice.length() != 15) {
            showAlert(bundle.getString("alert.validation_error"), "I.C.E must be exactly 15 characters.");
            return;
        }

        double amount = 0.0;
        try {
            if (!fixedTotalAmountField.getText().isEmpty()) {
                amount = Double.parseDouble(fixedTotalAmountField.getText());
            }
        } catch (Exception e) {
            showAlert(bundle.getString("alert.error"), bundle.getString("alert.invalid_amount"));
            return;
        }

        double ttc = 0.0;
        try {
            if (!ttcField.getText().isEmpty()) {
                ttc = Double.parseDouble(ttcField.getText());
            }
        } catch (Exception e) {
            showAlert("Selection Error", "Invalid TTC amount format.");
            return;
        }

        boolean success;
        if (currentClient == null) {
            int yearId = SessionContext.getInstance().getCurrentYear() != null
                    ? SessionContext.getInstance().getCurrentYear().getId()
                    : 1;
            Client newClient = new Client(0, raison, nomPrenomField.getText(), adresseField.getText(),
                    villeField.getText(), iceField.getText(), rcField.getText(), tpField.getText(),
                    taxeHabitField.getText(), tvaField.getText(), regimeTvaField.getText(),
                    faxField.getText(), emailField.getText(),
                    ribField.getText(), usernameField.getText(), passwordField.getText(), secteurField.getText(),
                    debutActField.getText(), amount, ttc, yearId, false);
            success = clientDAO.addClient(newClient);
        } else {
            currentClient.setRaisonSociale(raison);
            currentClient.setNomPrenom(nomPrenomField.getText());
            currentClient.setAdresse(adresseField.getText());
            currentClient.setVille(villeField.getText());
            currentClient.setIce(iceField.getText());
            currentClient.setRc(rcField.getText());
            currentClient.setTp(tpField.getText());
            currentClient.setTaxeHabit(taxeHabitField.getText());
            currentClient.setTva(tvaField.getText());
            currentClient.setRegimeTva(regimeTvaField.getText());
            currentClient.setFax(faxField.getText());
            currentClient.setEmail(emailField.getText());
            currentClient.setRib(ribField.getText());
            currentClient.setUsername(usernameField.getText());
            currentClient.setPassword(passwordField.getText());
            currentClient.setSecteur(secteurField.getText());
            currentClient.setDebutAct(debutActField.getText());
            currentClient.setFixedTotalAmount(amount);
            currentClient.setTtc(ttc);
            success = clientDAO.updateClient(currentClient);
        }

        if (success) {
            closeWindow();
        } else {
            showAlert(bundle.getString("alert.db_error"), bundle.getString("alert.db_save_failed"));
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) raisonSocialeField.getScene().getWindow();
        stage.close();
    }
}
