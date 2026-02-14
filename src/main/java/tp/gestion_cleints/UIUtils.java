package tp.gestion_cleints;

import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.geometry.Orientation;

public class UIUtils {

    /**
     * Boosts the scroll speed of a Node by a multiplier.
     * Works for ScrollPane and TableView.
     */
    public static void applyFastScroll(Node node) {
        final double multiplier = 3.5; // Optimized for "Fast" feel

        if (node instanceof TableView) {
            TableView<?> table = (TableView<?>) node;
            table.addEventFilter(ScrollEvent.SCROLL, event -> {
                if (event.getDeltaY() != 0 && event.getTouchCount() == 0) {
                    ScrollBar vBar = findScrollBar(table, Orientation.VERTICAL);
                    if (vBar != null) {
                        double delta = event.getDeltaY() * multiplier;
                        // ScrollBar value is 0.0 to 1.0.
                        // Standard scroll is about 10-20 pixels.
                        // We scale the value change.
                        double currValue = vBar.getValue();
                        double newValue = currValue - (delta / 1000.0); // Rough estimation
                        vBar.setValue(Math.max(0, Math.min(vBar.getMax(), newValue)));
                        event.consume();
                    }
                }
            });
        } else if (node instanceof ScrollPane) {
            ScrollPane sp = (ScrollPane) node;
            sp.addEventFilter(ScrollEvent.SCROLL, event -> {
                if (event.getDeltaY() != 0 && event.getTouchCount() == 0) {
                    double delta = event.getDeltaY() * multiplier;
                    double vValue = sp.getVvalue();
                    // ScrollPane VValue is also 0.0 to 1.0
                    double newValue = vValue - (delta / 1000.0);
                    sp.setVvalue(Math.max(0, Math.min(1.0, newValue)));
                    event.consume();
                }
            });
        }
    }

    private static ScrollBar findScrollBar(Node node, Orientation orientation) {
        for (Node n : node.lookupAll(".scroll-bar")) {
            if (n instanceof ScrollBar) {
                ScrollBar bar = (ScrollBar) n;
                if (bar.getOrientation() == orientation) {
                    return bar;
                }
            }
        }
        return null;
    }

    /**
     * Ensures all TableViews in the application feel premium and fast.
     */
    public static void enhanceTable(TableView<?> table) {
        applyFastScroll(table);
    }

    public static void setStageIcon(Stage stage) {
        if (stage == null)
            return;
        try {
            // Using a path that works from both Main and other controllers
            String logoPath = "/tp/gestion_cleints/images/logo1.png";
            String fallbackPath = "/tp/gestion_cleints/images/add.png";

            java.io.InputStream stream = UIUtils.class.getResourceAsStream(logoPath);
            if (stream == null) {
                stream = UIUtils.class.getResourceAsStream(fallbackPath);
            }

            if (stream != null) {
                Image icon = new Image(stream);
                stage.getIcons().clear();
                stage.getIcons().add(icon);
            }
        } catch (Exception e) {
            System.err.println("Could not set stage icon: " + e.getMessage());
        }
    }
}
