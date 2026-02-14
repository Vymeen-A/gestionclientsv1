package tp.gestion_cleints;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class HiddenClientsController {

    @FXML
    private TableView<Client> clientTable;
    @FXML
    private TableColumn<Client, Integer> idColumn;
    @FXML
    private TableColumn<Client, String> raisonSocialeColumn;
    @FXML
    private TableColumn<Client, String> nomPrenomColumn;
    @FXML
    private TableColumn<Client, String> villeColumn;
    @FXML
    private TextField searchField;

    private ClientDAO clientDAO = new ClientDAO();
    private ObservableList<Client> hiddenClients;
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        raisonSocialeColumn.setCellValueFactory(new PropertyValueFactory<>("raisonSociale"));
        nomPrenomColumn.setCellValueFactory(new PropertyValueFactory<>("nomPrenom"));
        villeColumn.setCellValueFactory(new PropertyValueFactory<>("ville"));

        loadHiddenClients();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterClients(newVal);
        });
    }

    private void filterClients(String query) {
        if (query == null || query.isEmpty()) {
            loadHiddenClients();
        } else {
            ObservableList<Client> filtered = hiddenClients.filtered(c -> (c.getRaisonSociale() != null
                    && c.getRaisonSociale().toLowerCase().contains(query.toLowerCase())) ||
                    (c.getNomPrenom() != null && c.getNomPrenom().toLowerCase().contains(query.toLowerCase())) ||
                    (c.getVille() != null && c.getVille().toLowerCase().contains(query.toLowerCase())));
            clientTable.setItems(filtered);
        }
    }

    private void loadHiddenClients() {
        hiddenClients = FXCollections.observableArrayList(clientDAO.getHiddenClients());
        clientTable.setItems(hiddenClients);
    }

    @FXML
    private void handleUnhideClient() {
        Client selected = clientTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            clientDAO.setClientVisibility(selected.getId(), false);
            loadHiddenClients();
        }
    }

    @FXML
    private void handleBack() {
        if (mainController != null) {
            mainController.showClients();
        }
    }
}
