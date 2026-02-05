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
    }

    private void setupColumns() {
        userColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        entityColumn.setCellValueFactory(new PropertyValueFactory<>("entityType"));
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
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
            java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("tp.gestion_cleints.messages");
            String path = System.getProperty("user.home") + "/audit_log_" + System.currentTimeMillis() + ".pdf";
            PdfExporter.exportAuditLogs(logs, path, bundle);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Audit log exported to: " + path);
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
