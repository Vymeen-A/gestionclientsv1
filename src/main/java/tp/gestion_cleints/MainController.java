package tp.gestion_cleints;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import java.util.ResourceBundle;
import java.io.File;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyEvent;
import javafx.application.Platform;

public class MainController {

    @FXML
    private BorderPane contentArea;
    @FXML
    private MenuButton currentYearMenu, userMenu;
    @FXML
    private ImageView userImageView;
    @FXML
    private Label userNameLabel;
    @FXML
    private MenuButton clientsMenu, paymentsMenu, yearsMenu, settingsMenu;
    @FXML
    private MenuItem userManagementItem, backupItem, auditLogsItem;

    private String currentView = "client-names.fxml";
    private ResourceBundle bundle;
    private Timeline inactivityTimeline;

    @FXML
    public void initialize() {
        setLocale(Locale.FRENCH); // Default
        refreshYearMenu();
        updateUserDisplay();
        applyPermissions();

        Platform.runLater(this::setupInactivityTimer);
    }

    private void setupInactivityTimer() {
        if (contentArea.getScene() == null)
            return;

        inactivityTimeline = new Timeline(new KeyFrame(Duration.minutes(15), e -> handleLogout()));
        inactivityTimeline.setCycleCount(1);

        contentArea.getScene().addEventFilter(MouseEvent.ANY, e -> resetInactivityTimer());
        contentArea.getScene().addEventFilter(KeyEvent.ANY, e -> resetInactivityTimer());

        inactivityTimeline.play();
    }

    private void resetInactivityTimer() {
        if (inactivityTimeline != null) {
            inactivityTimeline.playFromStart();
        }
    }

    private void applyPermissions() {
        User user = SessionContext.getInstance().getCurrentUser();
        PermissionManager.applyAdminOnlyForMenuItems(user, userManagementItem, backupItem, auditLogsItem);
    }

    public void updateUserDisplay() {
        User user = SessionContext.getInstance().getCurrentUser();
        if (user != null && userNameLabel != null) {
            userNameLabel.setText(user.getFullName());
            if (user.getProfilePhoto() != null) {
                File photoFile = new File(user.getProfilePhoto());
                if (photoFile.exists()) {
                    userImageView.setImage(new Image(photoFile.toURI().toString()));
                }
            }
        }
    }

    public void refreshYearMenu() {
        YearDAO yearDAO = new YearDAO();
        List<Year> years = yearDAO.getAllYears();

        currentYearMenu.getItems().clear();

        for (Year year : years) {
            MenuItem item = new MenuItem(year.getName());
            item.setOnAction(e -> {
                SessionContext.getInstance().setCurrentYear(year);
                updateYearLabel();
                loadCurrentView(); // Refresh current view with new year's data
            });
            currentYearMenu.getItems().add(item);
        }

        if (SessionContext.getInstance().getCurrentYear() == null && !years.isEmpty()) {
            SessionContext.getInstance().setCurrentYear(years.get(0));
        }
        updateYearLabel();
    }

    public void updateYearLabel() {
        Year currentYear = SessionContext.getInstance().getCurrentYear();
        if (currentYear != null) {
            currentYearMenu.setText(bundle.getString("nav.year_prefix") + " " + currentYear.getName());
        } else {
            currentYearMenu.setText(bundle.getString("nav.select_year"));
        }
    }

    private void setLocale(Locale locale) {
        Locale.setDefault(locale);
        bundle = ResourceBundle.getBundle("tp.gestion_cleints.messages", locale);
        updateYearLabel();
        loadCurrentView();
    }

    @FXML
    private void switchEnglish() {
        setLocale(Locale.ENGLISH);
    }

    @FXML
    private void switchFrench() {
        setLocale(Locale.FRENCH);
    }

    @FXML
    public void showDashboard() {
        currentView = "dashboard.fxml";
        loadCurrentView();
    }

    @FXML
    public void showSecurity() {
        currentView = "change-password.fxml";
        loadCurrentView();
    }

    @FXML
    public void showUserManagement() {
        if (SessionContext.getInstance().isAdmin()) {
            currentView = "user-management.fxml";
            loadCurrentView();
        }
    }

    @FXML
    public void showAdminSettings() {
        currentView = "admin-settings.fxml";
        loadCurrentView();
    }

    @FXML
    public void showClients() {
        currentView = "client-list.fxml";
        loadCurrentView();
    }

    @FXML
    public void showClientSummary() {
        currentView = "client-names.fxml";
        loadCurrentView();
    }

    @FXML
    public void showHiddenClients() {
        currentView = "hidden-clients.fxml";
        loadCurrentView();
    }

    @FXML
    public void showDeletedClients() {
        currentView = "deleted-clients.fxml";
        loadCurrentView();
    }

    @FXML
    public void showInvoiceAging() {
        currentView = "invoice-aging.fxml";
        loadCurrentView();
    }

    @FXML
    public void showInvoices() {
        currentView = "invoice-list.fxml";
        loadCurrentView();
    }

    @FXML
    public void showPaymentTypes() {
        currentView = "payment-types.fxml";
        loadCurrentView();
    }

    @FXML
    public void showYearManagement() {
        currentView = "year-management.fxml";
        loadCurrentView();
    }

    @FXML
    public void showAuditLogs() {
        currentView = "audit-logs.fxml";
        loadCurrentView();
    }

    @FXML
    public void handleBackupRestore() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(bundle.getString("backup.title"));
        confirm.setHeaderText(bundle.getString("backup.choose_action"));

        ButtonType btnBackupType = new ButtonType(bundle.getString("btn.create_backup"));
        ButtonType btnRestoreType = new ButtonType(bundle.getString("btn.restore_backup"));
        confirm.getButtonTypes().setAll(btnBackupType, btnRestoreType, ButtonType.CANCEL);

        confirm.showAndWait().ifPresent(type -> {
            if (type == btnBackupType) {
                try {
                    String path = DatabaseBackup.backup(new java.io.File(System.getProperty("user.home"), "backups"));
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle(bundle.getString("alert.success"));
                    info.setHeaderText(null);
                    info.setContentText(
                            java.text.MessageFormat.format(bundle.getString("backup.create_success"), path));
                    info.show();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, bundle.getString("alert.error"), e.getMessage());
                }
            } else if (type == btnRestoreType) {
                javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
                chooser.setTitle(bundle.getString("btn.restore_backup"));
                chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("DB Files", "*.db"));
                java.io.File file = chooser.showOpenDialog(contentArea.getScene().getWindow());
                if (file != null) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("restore-wizard.fxml"));
                        loader.setResources(bundle);
                        Parent root = loader.load();
                        RestoreWizardController wizard = loader.getController();
                        wizard.setBackupFile(file);

                        Stage stage = new Stage();
                        stage.initOwner(contentArea.getScene().getWindow());
                        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                        stage.setTitle(bundle.getString("restore.wizard"));
                        stage.setScene(new javafx.scene.Scene(root));
                        UIUtils.setStageIcon(stage);
                        stage.showAndWait();

                        if (wizard.isConfirmed()) {
                            try {
                                DatabaseBackup.restore(file);
                                Alert info = new Alert(Alert.AlertType.INFORMATION);
                                info.setTitle(bundle.getString("alert.success"));
                                info.setHeaderText(null);
                                info.setContentText(bundle.getString("alert.restore_success"));
                                info.showAndWait();

                                // Reset session and reload
                                SessionContext.getInstance().setCurrentYear(null);
                                refreshYearMenu();
                                showDashboard();
                            } catch (IOException ex) {
                                Alert error = new Alert(Alert.AlertType.ERROR);
                                error.setTitle(bundle.getString("backup.restore_error_title"));
                                error.setHeaderText(bundle.getString("backup.restore_error_header"));
                                error.setContentText(java.text.MessageFormat
                                        .format(bundle.getString("backup.restore_error_content"), ex.getMessage()));
                                error.showAndWait();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, bundle.getString("alert.error"), e.getMessage());
                    }
                }
            }
        });
    }

    @FXML
    public void handleAddClientFromNav() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("client-form.fxml"));
            loader.setResources(bundle);
            Parent root = loader.load();

            ClientFormController controller = loader.getController();
            controller.setClient(null);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.initOwner(contentArea.getScene().getWindow());
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            // Set icon
            try {
                stage.getIcons().add(new javafx.scene.image.Image(
                        getClass().getResourceAsStream("images/add.png")));
            } catch (Exception e) {
                System.err.println("Could not load add icon: " + e.getMessage());
            }

            stage.setTitle(bundle.getString("btn.add_client"));
            stage.setScene(new javafx.scene.Scene(root));
            UIUtils.setStageIcon(stage);
            stage.showAndWait();

            // Reload the view if we are on client list
            if ("client-list.fxml".equals(currentView)) {
                loadCurrentView();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadCurrentView() {
        try {
            java.net.URL resource = getClass().getResource(currentView);
            if (resource == null) {
                System.err.println("Could not find FXML file: " + currentView);
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            loader.setResources(bundle);
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof DashboardController) {
                ((DashboardController) controller).setMainController(this);
                ((DashboardController) controller).refresh();
            } else if (controller instanceof ClientListController) {
                ((ClientListController) controller).setMainController(this);
            } else if (controller instanceof ClientNamesController) {
                ((ClientNamesController) controller).setMainController(this);
            } else if (controller instanceof HiddenClientsController) {
                ((HiddenClientsController) controller).setMainController(this);
            } else if (controller instanceof YearManagementController) {
                ((YearManagementController) controller).setMainController(this);
            } else if (controller instanceof AuditLogsController) {
                ((AuditLogsController) controller).setMainController(this);
            } else if (controller instanceof InvoiceListController) {
                ((InvoiceListController) controller).setMainController(this);
            } else if (controller instanceof UserManagementController) {
                ((UserManagementController) controller).setMainController(this);
            } else if (controller instanceof InvoiceAgingController) {
                ((InvoiceAgingController) controller).setMainController(this);
            } else if (controller instanceof ProfileController) {
                ((ProfileController) controller).setMainController(this);
            } else if (controller instanceof PaymentTypesController) {
                ((PaymentTypesController) controller).setMainController(this);
            } else if (controller instanceof DeletedClientsController) {
                ((DeletedClientsController) controller).setMainController(this);
            }

            contentArea.setCenter(view);
        } catch (Throwable e) {
            System.err.println("Error loading view " + currentView + ": " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Loading View");
            alert.setHeaderText("Could not load " + currentView);
            alert.setContentText(e.toString());
            alert.showAndWait();
        }
    }

    public void showClientDetails(Client client) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("client-details.fxml"));
            loader.setResources(bundle);
            Parent view = loader.load();

            ClientDetailsController controller = loader.getController();
            controller.setMainController(this);
            controller.setClient(client);

            contentArea.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleMinimize() {
        javafx.stage.Stage stage = (javafx.stage.Stage) contentArea.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    public void handleLogout() {
        try {
            SessionContext.getInstance().setCurrentUser(null);
            ResourceBundle loginBundle = ResourceBundle.getBundle("tp.gestion_cleints.messages", Locale.getDefault());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"), loginBundle);
            Parent root = loader.load();

            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setTitle("Gestion Clients - Login");
            stage.setScene(new javafx.scene.Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showProfile() {
        currentView = "profile.fxml";
        loadCurrentView();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        if (contentArea.getScene() != null) {
            alert.initOwner(contentArea.getScene().getWindow());
        }
        alert.showAndWait();
    }

    @FXML
    public void handleExit() {
        javafx.application.Platform.exit();
        System.exit(0);
    }
}
