package tp.gestion_cleints;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class ClientNamesController {

    @FXML
    private Label headerLabel;
    @FXML
    private TextField searchField;
    @FXML
    private ListView<Client> clientNamesList;

    private ClientDAO clientDAO;
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void initialize() {
        clientDAO = new ClientDAO();

        // Custom cell to show Raison Sociale with a Hide button
        clientNamesList.setCellFactory(lv -> new javafx.scene.control.ListCell<Client>() {
            private final javafx.scene.layout.HBox container = new javafx.scene.layout.HBox(10);
            private final javafx.scene.control.Label nameLabel = new javafx.scene.control.Label();
            private final javafx.scene.control.Button hideBtn = new javafx.scene.control.Button("Masquer");
            private final javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();

            {
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                hideBtn.getStyleClass().add("button-secondary");
                hideBtn.setStyle("-fx-font-size: 10px; -fx-padding: 3 8;");
                container.getChildren().addAll(nameLabel, spacer, hideBtn);

                hideBtn.setOnAction(event -> {
                    Client client = getItem();
                    if (client != null) {
                        clientDAO.setClientVisibility(client.getId(), true);
                        loadClients(); // Refresh list
                    }
                });
            }

            @Override
            protected void updateItem(Client item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    nameLabel.setText(
                            item.getRaisonSociale() + (item.getNomPrenom() != null && !item.getNomPrenom().isEmpty()
                                    ? " (" + item.getNomPrenom() + ")"
                                    : ""));

                    nameLabel.setStyle("-fx-text-fill: #2c3e50;");

                    setGraphic(container);
                }
            }
        });

        loadClients();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterClients(newVal);
        });

        clientNamesList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Client selected = clientNamesList.getSelectionModel().getSelectedItem();
                if (selected != null && mainController != null) {
                    mainController.showClientDetails(selected);
                }
            }
        });
    }

    private void loadClients() {
        clientNamesList.setItems(FXCollections.observableArrayList(clientDAO.getAllVisibleClients()));
    }

    private void filterClients(String query) {
        if (query == null || query.isEmpty()) {
            loadClients();
        } else {
            clientNamesList.setItems(FXCollections.observableArrayList(clientDAO.searchClients(query, false)));
        }
    }
}
