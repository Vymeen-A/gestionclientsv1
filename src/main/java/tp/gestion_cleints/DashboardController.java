package tp.gestion_cleints;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
// PieChart doesn't need XYChart
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.ResourceBundle;

public class DashboardController {

    @FXML
    public Label totalClientsLabel;
    @FXML
    public Label totalRevenueLabel;
    @FXML
    public Label totalDueLabel;
    @FXML
    public Label totalPaidLabel;

    @FXML
    public TableView<Client> recentClientsTable;
    @FXML
    public TableColumn<Client, String> recentNameColumn;
    @FXML
    public TableColumn<Client, Double> recentRevenueColumn;

    @FXML
    public PieChart invoiceChart;

    private ClientDAO clientDAO;
    private InvoiceDAO invoiceDAO;
    private MainController mainController;
    private ResourceBundle bundle;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void initialize() {
        clientDAO = new ClientDAO();
        invoiceDAO = new InvoiceDAO();
        // stats are loaded in refresh() which is called by MainController
    }

    public void refresh() {
        bundle = ResourceBundle.getBundle("tp.gestion_cleints.messages", java.util.Locale.getDefault());
        updateStatistics();
        setupRecentClients();
        setupChart();
    }

    private void updateStatistics() {
        int totalClients = clientDAO.getTotalClients();
        double totalRevenue = clientDAO.getTotalRevenue();
        double totalDue = clientDAO.getTotalAmountDue();
        double totalPaid = clientDAO.getTotalPaid();
        String currency = bundle != null ? bundle.getString("currency") : "DH";
        totalClientsLabel.setText(String.valueOf(totalClients));
        totalRevenueLabel.setText(String.format("%.2f %s", totalRevenue, currency));

        if (totalDueLabel != null) {
            totalDueLabel.setText(String.format("%.2f %s", totalDue, currency));
        }
        if (totalPaidLabel != null) {
            totalPaidLabel.setText(String.format("%.2f %s", totalPaid, currency));
        }
    }

    private void setupRecentClients() {
        recentNameColumn.setCellValueFactory(new PropertyValueFactory<>("raisonSociale"));
        recentRevenueColumn.setCellValueFactory(new PropertyValueFactory<>("fixedTotalAmount"));

        recentClientsTable.setItems(FXCollections.observableArrayList(
                clientDAO.getRecentClients(10)));

        // Handle click to navigate
        recentClientsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && mainController != null) {
                Client selected = recentClientsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    mainController.showClients();
                }
            }
        });
    }

    @FXML
    public javafx.scene.chart.BarChart<String, Number> clientGrowthChart;

    @FXML
    public javafx.scene.chart.CategoryAxis xAxis;

    @FXML
    public javafx.scene.chart.NumberAxis yAxis;

    private void setupChart() {
        if (clientGrowthChart == null)
            return;

        clientGrowthChart.getData().clear();

        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("Nouveaux Clients");

        // Fetch real data from database
        java.util.Map<String, Integer> growthData = clientDAO.getRecentClientGrowth();

        for (java.util.Map.Entry<String, Integer> entry : growthData.entrySet()) {
            series.getData().add(new javafx.scene.chart.XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        clientGrowthChart.getData().add(series);
    }
}
