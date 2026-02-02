package tp.gestion_cleints;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import java.util.List;
import java.util.stream.Collectors;

public class ClientNamesController {

    @FXML
    private TextField searchField;
    @FXML
    private ListView<String> clientNamesList;

    private ClientDAO clientDAO;
    private List<Client> allClients;
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void initialize() {
        clientDAO = new ClientDAO();
        loadClients();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterClients(newVal);
        });

        clientNamesList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedName = clientNamesList.getSelectionModel().getSelectedItem();
                if (selectedName != null) {
                    Client client = allClients.stream()
                            .filter(c -> c.getRaisonSociale().equals(selectedName))
                            .findFirst().orElse(null);
                    if (client != null && mainController != null) {
                        mainController.showClientDetails(client);
                    }
                }
            }
        });
    }

    private void loadClients() {
        allClients = clientDAO.getAllVisibleClients();
        updateListView(allClients);
    }

    private void filterClients(String query) {
        if (query == null || query.isEmpty()) {
            updateListView(allClients);
        } else {
            List<Client> filtered = allClients.stream()
                    .filter(c -> c.getRaisonSociale().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            updateListView(filtered);
        }
    }

    private void updateListView(List<Client> clients) {
        ObservableList<String> names = FXCollections.observableArrayList(
                clients.stream().map(Client::getRaisonSociale).collect(Collectors.toList()));
        clientNamesList.setItems(names);
    }
}
