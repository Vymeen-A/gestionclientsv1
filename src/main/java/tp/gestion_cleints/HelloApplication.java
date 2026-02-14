package tp.gestion_cleints;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Initialize DB
        DatabaseManager.initializeDatabase();
        new UserDAO().ensureAdminExists("admin");

        // Load bundle
        Locale.setDefault(Locale.FRENCH);
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle("tp.gestion_cleints.messages", Locale.getDefault());
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle("tp.gestion_cleints.messages", Locale.FRENCH);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login.fxml"));
        fxmlLoader.setResources(bundle);
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Login - Gestion Clients");
        stage.setScene(scene);

        // Set application icon
        UIUtils.setStageIcon(stage);

        stage.initStyle(javafx.stage.StageStyle.UNDECORATED);
        javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
        javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
