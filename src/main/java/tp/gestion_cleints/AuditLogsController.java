package tp.gestion_cleints;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogsController {

    @FXML
    private TableView<AuditLog> auditTable;
    @FXML
    private TableColumn<AuditLog, String> userColumn;
    @FXML
    private TableColumn<AuditLog, String> actionColumn;
    @FXML
    private TableColumn<AuditLog, String> entityColumn;
    @FXML
    private TableColumn<AuditLog, String> detailsColumn;
    @FXML
    private TableColumn<AuditLog, String> dateColumn;

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        setupColumns();
        loadLogs();
        UIUtils.applyFastScroll(auditTable);
    }

    private void setupColumns() {
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("tp.gestion_cleints.messages",
                java.util.Locale.getDefault());

        userColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        actionColumn.setCellFactory(tc -> new javafx.scene.control.TableCell<AuditLog, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String key = "audit.action." + item.toLowerCase();
                    try {
                        setText(bundle.getString(key));
                    } catch (Exception e) {
                        setText(item);
                    }
                }
            }
        });

        entityColumn.setCellValueFactory(new PropertyValueFactory<>("entityType"));
        entityColumn.setCellFactory(tc -> new javafx.scene.control.TableCell<AuditLog, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String key = "audit.entity." + item.toLowerCase();
                    try {
                        setText(bundle.getString(key));
                    } catch (Exception e) {
                        setText(item);
                    }
                }
            }
        });

        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
        detailsColumn.setCellFactory(tc -> new javafx.scene.control.TableCell<AuditLog, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Smart translation for common patterns
                    if (item.contains("Account activated"))
                        setText(bundle.getString("audit.detail.activated"));
                    else if (item.contains("Account deactivated"))
                        setText(bundle.getString("audit.detail.deactivated"));
                    else if (item.contains("User deleted"))
                        setText(bundle.getString("audit.detail.user_deleted"));
                    else if (item.contains("Admin reset password"))
                        setText(bundle.getString("audit.detail.password_reset"));
                    else if (item.startsWith("User created with role ")) {
                        String role = item.substring("User created with role ".length());
                        setText(java.text.MessageFormat.format(bundle.getString("audit.detail.user_created"), role));
                    } else if (item.startsWith("Client restored from trash: ")) {
                        String name = item.substring("Client restored from trash: ".length());
                        setText(java.text.MessageFormat.format(bundle.getString("audit.detail.client_restored"), name));
                    } else if (item.startsWith("Client permanently deleted: ")) {
                        String name = item.substring("Client permanently deleted: ".length());
                        setText(java.text.MessageFormat.format(bundle.getString("audit.detail.client_purged"), name));
                    } else if (item.startsWith("Manual backup created: ")) {
                        String file = item.substring("Manual backup created: ".length());
                        setText(java.text.MessageFormat.format(bundle.getString("audit.detail.backup_created"), file));
                    } else if (item.startsWith("Database restored from: ")) {
                        String file = item.substring("Database restored from: ".length());
                        setText(java.text.MessageFormat.format(bundle.getString("audit.detail.database_restored"),
                                file));
                    } else if (item.startsWith("Invoice created for client ")) {
                        String id = item.substring("Invoice created for client ".length());
                        setText(java.text.MessageFormat.format(bundle.getString("audit.detail.invoice_created"), id));
                    } else if (item.contains("Marked as PAID"))
                        setText(bundle.getString("audit.detail.invoice_paid"));
                    else
                        setText(item);
                }
            }
        });

        dateColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
    }

    private void loadLogs() {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 500";
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(new AuditLog(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("action"),
                        rs.getString("entity_type"),
                        rs.getString("entity_id"),
                        rs.getString("details"),
                        rs.getString("timestamp")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        auditTable.setItems(FXCollections.observableArrayList(logs));
    }

    @FXML
    public void handleRefresh() {
        loadLogs();
    }

    @FXML
    public void handleExport() {
        List<AuditLog> logs = auditTable.getItems();
        if (logs.isEmpty())
            return;

        try {
            java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("tp.gestion_cleints.messages",
                    java.util.Locale.getDefault());
            String path = System.getProperty("user.home") + "/audit_log_" + System.currentTimeMillis() + ".pdf";
            PdfExporter.exportAuditLogs(logs, path, bundle);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(bundle.getString("audit.export_success"));
            alert.setHeaderText(null);
            alert.setContentText(java.text.MessageFormat.format(bundle.getString("audit.export_at"), path));
            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBack() {
        if (mainController != null) {
            mainController.showDashboard();
        }
    }
}
