package tp.gestion_cleints;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class UserManagementController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<Role> roleCombo;
    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private TableColumn<User, Void> actionsColumn;

    private UserDAO userDAO;
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        userDAO = new UserDAO();

        roleCombo.setItems(FXCollections.observableArrayList(Role.values()));
        roleCombo.setValue(Role.READ_ONLY);

        setupColumns();
        loadUsers();
    }

    private void setupColumns() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.getStyleClass().add("button-danger");
                deleteBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    if ("admin".equals(user.getUsername())) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Cannot delete system administrator.");
                        alert.show();
                        return;
                    }
                    userDAO.deleteUser(user.getUsername());
                    loadUsers();
                    AuditLogger.log("DELETE", "USER", user.getUsername(), "User deleted");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
    }

    private void loadUsers() {
        userTable.setItems(FXCollections.observableArrayList(userDAO.getAllUsers()));
    }

    @FXML
    public void handleAddUser() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        Role role = roleCombo.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Username and password are required.");
            alert.show();
            return;
        }

        userDAO.updateUser(username, password, role);
        usernameField.clear();
        passwordField.clear();
        loadUsers();
        AuditLogger.log("CREATE", "USER", username, "User created with role " + role);
    }

    @FXML
    public void handleBack() {
        if (mainController != null) {
            mainController.showDashboard();
        }
    }
}
