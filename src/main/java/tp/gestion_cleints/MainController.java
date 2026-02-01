package tp.gestion_cleints;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainController {

    @FXML
    private BorderPane contentArea;
    private String currentView = "dashboard.fxml";
    private ResourceBundle bundle;

    @FXML
    public void initialize() {
        setLocale(Locale.ENGLISH); // Default
    }

    private void setLocale(Locale locale) {
        Locale.setDefault(locale);
        bundle = ResourceBundle.getBundle("tp.gestion_cleints.messages", locale);
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
    private void showDashboard() {
        currentView = "dashboard.fxml";
        loadCurrentView();
    }

    @FXML
    public void showExpenses() {
        currentView = "expenses-list.fxml";
        loadCurrentView();
    }

    @FXML
    public void showSecurity() {
        currentView = "change-password.fxml";
        loadCurrentView();
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

    private void loadCurrentView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(currentView));
            loader.setResources(bundle);
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof DashboardController) {
                ((DashboardController) controller).setMainController(this);
                // The initialize already ran, but we might need to refresh with the bundle
                ((DashboardController) controller).refresh();
            }

            contentArea.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
