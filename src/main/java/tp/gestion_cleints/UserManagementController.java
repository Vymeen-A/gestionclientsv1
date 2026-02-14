package tp.gestion_cleints;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.ResourceBundle;

public class UserManagementController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<Role> roleCombo;
    @FXML
    private TextField searchField;
    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private TableColumn<User, Boolean> statusColumn;
    @FXML
    private TableColumn<User, Void> actionsColumn;

    private UserDAO userDAO;
    private MainController mainController;
    private ResourceBundle resources;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        resources = ResourceBundle.getBundle("tp.gestion_cleints.messages");

        // UI Restriction: Filter ADMIN role from selection
        java.util.List<Role> allowedRoles = java.util.stream.Stream.of(Role.values())
                .filter(r -> r != Role.ADMIN)
                .collect(java.util.stream.Collectors.toList());
        roleCombo.setItems(FXCollections.observableArrayList(allowedRoles));
        roleCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(resources.getString("role." + item.name().toLowerCase()));
                }
            }
        });
        roleCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(resources.getString("role." + item.name().toLowerCase()));
                }
            }
        });
        roleCombo.setValue(Role.READ_ONLY);

        setupColumns();
        loadUsers();
    }

    private void setupColumns() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(param -> {
            Role role = param.getValue().getRole();
            String key = "role." + (role != null ? role.name().toLowerCase() : "read_only");
            return new javafx.beans.property.SimpleStringProperty(resources.getString(key));
        });

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        statusColumn.setCellFactory(param -> new TableCell<>() {
            private final Button toggleBtn = new Button();
            {
                toggleBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    if ("admin".equals(user.getUsername())) {
                        showAlert(Alert.AlertType.WARNING, resources.getString("alert.validation_error"),
                                resources.getString("user.admin_protection"));
                        return;
                    }
                    boolean newStatus = !user.isActive();
                    userDAO.setUserStatus(user.getUsername(), newStatus);
                    user.setActive(newStatus);
                    updateLabel(newStatus);
                    AuditLogger.log("UPDATE", "USER", user.getUsername(),
                            "Account " + (newStatus ? "activated" : "deactivated"));
                });
            }

            private void updateLabel(boolean active) {
                setText(null);
                toggleBtn.setText(active ? resources.getString("user.status_active")
                        : resources.getString("user.status_inactive"));
                toggleBtn.setStyle(
                        "-fx-background-color: " + (active ? "#27ae60" : "#e74c3c") + "; -fx-text-fill: white;");
                setGraphic(toggleBtn);
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    updateLabel(item);
                }
            }
        });

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");
            private final Button resetBtn = new Button("Reset");
            private final javafx.scene.layout.HBox container = new javafx.scene.layout.HBox(5, resetBtn, deleteBtn);

            {
                deleteBtn.getStyleClass().add("button-danger");
                deleteBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    if ("admin".equals(user.getUsername())) {
                        showAlert(Alert.AlertType.ERROR, resources.getString("alert.db_error"),
                                resources.getString("user.confirm_delete_admin"));
                        return;
                    }
                    userDAO.deleteUser(user.getUsername());
                    loadUsers();
                    AuditLogger.log("DELETE", "USER", user.getUsername(), "User deleted");
                });

                resetBtn.getStyleClass().add("button-secondary");
                resetBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle(resources.getString("user.password_reset_title"));
                    dialog.setHeaderText(java.text.MessageFormat
                            .format(resources.getString("user.password_reset_header"), user.getUsername()));
                    dialog.setContentText(resources.getString("user.password") + ":");
                    dialog.showAndWait().ifPresent(newPass -> {
                        if (userDAO.validatePassword(newPass)) {
                            userDAO.updatePassword(user.getUsername(), newPass);
                            showAlert(Alert.AlertType.INFORMATION, resources.getString("alert.success"),
                                    resources.getString("user.password_success"));
                            AuditLogger.log("UPDATE", "USER", user.getUsername(), "Admin reset password");
                        } else {
                            showAlert(Alert.AlertType.WARNING, resources.getString("alert.validation_error"),
                                    resources.getString("user.password_policy"));
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void loadUsers() {
        java.util.List<User> users = userDAO.getAllUsers();
        javafx.collections.transformation.FilteredList<User> filteredUsers = new javafx.collections.transformation.FilteredList<>(
                FXCollections.observableArrayList(users), p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredUsers.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (user.getUsername().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (user.getRole().name().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        userTable.setItems(filteredUsers);
    }

    @FXML
    public void handleAddUser() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        Role role = roleCombo.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, resources.getString("alert.validation_error"),
                    resources.getString("user.fields_required"));
            return;
        }

        userDAO.updateUser(username, password, role);
        usernameField.clear();
        passwordField.clear();
        loadUsers();
        showAlert(Alert.AlertType.INFORMATION, resources.getString("alert.success"),
                resources.getString("user.add_success"));
        AuditLogger.log("CREATE", "USER", username, "User created with role " + role);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(userTable.getScene().getWindow());
        alert.showAndWait();
    }

    @FXML
    public void handleBack() {
        if (mainController != null) {
            mainController.showDashboard();
        }
    }
}
