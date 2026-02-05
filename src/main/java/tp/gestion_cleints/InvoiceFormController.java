package tp.gestion_cleints;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.time.LocalDate;

public class InvoiceFormController {

    @FXML
    private ComboBox<Client> clientCombo;
    @FXML
    private TextField numberField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private DatePicker dueDatePicker;
    @FXML
    private TextField htField;
    @FXML
    private TextField tvaField;
    @FXML
    private TextField ttcField;

    private InvoiceDAO invoiceDAO;
    private ClientDAO clientDAO;

    @FXML
    public void initialize() {
        invoiceDAO = new InvoiceDAO();
        clientDAO = new ClientDAO();

        setupClientCombo();
        numberField.setText(invoiceDAO.getNextInvoiceNumber());
        datePicker.setValue(LocalDate.now());
        dueDatePicker.setValue(LocalDate.now().plusMonths(1));

        // Auto-calculate TTC
        htField.textProperty().addListener((obs, old, val) -> calculateTtc());
        tvaField.textProperty().addListener((obs, old, val) -> calculateTtc());
    }

    private void setupClientCombo() {
        clientCombo.setItems(FXCollections.observableArrayList(clientDAO.getAllVisibleClients()));
        clientCombo.setConverter(new StringConverter<Client>() {
            @Override
            public String toString(Client client) {
                return client == null ? "" : client.getRaisonSociale();
            }

            @Override
            public Client fromString(String string) {
                return null;
            }
        });
    }

    private void calculateTtc() {
        try {
            double ht = Double.parseDouble(htField.getText());
            double tvaPercent = Double.parseDouble(tvaField.getText());
            double ttc = ht * (1 + tvaPercent / 100);
            ttcField.setText(String.format("%.2f", ttc));
        } catch (NumberFormatException e) {
            ttcField.setText("");
        }
    }

    @FXML
    public void handleSave() {
        if (clientCombo.getValue() == null || htField.getText().isEmpty()) {
            return;
        }

        try {
            double ht = Double.parseDouble(htField.getText());
            double tva = Double.parseDouble(tvaField.getText());
            double ttc = Double.parseDouble(ttcField.getText().replace(",", "."));

            Invoice inv = new Invoice(
                    0,
                    clientCombo.getValue().getId(),
                    numberField.getText(),
                    datePicker.getValue().toString(),
                    dueDatePicker.getValue().toString(),
                    ht,
                    ht * (tva / 100),
                    ttc,
                    "SENT",
                    SessionContext.getInstance().getCurrentYear() != null
                            ? SessionContext.getInstance().getCurrentYear().getId()
                            : 1);

            if (invoiceDAO.addInvoice(inv)) {
                ((Stage) htField.getScene().getWindow()).close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCancel() {
        ((Stage) htField.getScene().getWindow()).close();
    }
}
