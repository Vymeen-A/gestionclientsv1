package tp.gestion_cleints;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
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
    public Label avgRevenueLabel;
    @FXML
    public Label topClientLabel;

    @FXML
    public TableView<Client> recentClientsTable;
    @FXML
    public TableColumn<Client, String> recentNameColumn;
    @FXML
    public TableColumn<Client, Double> recentRevenueColumn;

    @FXML
    public BarChart<String, Number> revenueChart;

    private ClientDAO clientDAO;
    private MainController mainController;
    private ResourceBundle bundle;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void initialize() {
        clientDAO = new ClientDAO();
    }

    public void refresh() {
        bundle = ResourceBundle.getBundle("tp.gestion_cleints.messages", java.util.Locale.getDefault());
        updateStatistics();
        setupRecentClients();
        setupChart();
    }

    private void updateStatistics() {
        int total = clientDAO.getTotalClients();
        double revenue = clientDAO.getTotalRevenue();
        String topClient = clientDAO.getTopClientName();
        double avgRevenue = total > 0 ? revenue / total : 0.0;
        String currency = bundle != null ? bundle.getString("currency") : "MAD";

        totalClientsLabel.setText(String.valueOf(total));
        totalRevenueLabel.setText(String.format("%.2f %s", revenue, currency));

        if (avgRevenueLabel != null) {
            avgRevenueLabel.setText(String.format("%.2f %s", avgRevenue, currency));
        }
        if (topClientLabel != null) {
            topClientLabel.setText(topClient);
        }
    }

    private void setupRecentClients() {
        recentNameColumn.setCellValueFactory(new PropertyValueFactory<>("raisonSociale"));
        recentRevenueColumn.setCellValueFactory(new PropertyValueFactory<>("fixedTotalAmount"));

        recentClientsTable.setItems(FXCollections.observableArrayList(
                clientDAO.getRecentClients(5)));

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

    private void setupChart() {
        revenueChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String seriesName = bundle != null ? bundle.getString("chart.series") : "Revenue";
        series.setName(seriesName);

        for (Client c : clientDAO.getRecentClients(5)) {
            series.getData().add(new XYChart.Data<>(c.getRaisonSociale(), c.getFixedTotalAmount()));
        }
        revenueChart.getData().add(series);
    }
}
