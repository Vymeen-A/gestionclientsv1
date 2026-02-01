package tp.gestion_cleints;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class PreviewDialog extends Dialog<Void> {

    private final ResourceBundle bundle;
    private final AdminInfo adminInfo;

    // Toolbar controls
    private ComboBox<Printer> printerSelector;
    private Button printButton;
    private Button exportButton;
    private Button cancelButton;

    // Current content node for printing
    private Node contentNode;
    // Current export action
    private Consumer<File> exportAction;

    public PreviewDialog(AdminInfo adminInfo) {
        this.adminInfo = adminInfo;
        this.bundle = ResourceBundle.getBundle("tp.gestion_cleints.messages", java.util.Locale.getDefault());

        setTitle("Aperçu et Impression");

        // Apply Global Styles
        getDialogPane().getStylesheets().add(getClass().getResource("/tp/gestion_cleints/styles.css").toExternalForm());
        getDialogPane().getStyleClass().add("preview-dialog");

        // Setup Toolbar
        ToolBar toolBar = createToolBar();
        getDialogPane().setHeader(toolBar);

        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Node closeBtn = getDialogPane().lookupButton(ButtonType.CLOSE);
        if (closeBtn != null)
            closeBtn.setVisible(false);

        getDialogPane().setPrefSize(700, 900);
    }

    private ToolBar createToolBar() {
        // Printer Selector
        printerSelector = new ComboBox<>();
        printerSelector.setItems(FXCollections.observableArrayList(Printer.getAllPrinters()));
        printerSelector.setValue(Printer.getDefaultPrinter());
        printerSelector.setCellFactory(lv -> new ListCell<Printer>() {
            @Override
            protected void updateItem(Printer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        printerSelector.setButtonCell(new ListCell<Printer>() {
            @Override
            protected void updateItem(Printer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        printerSelector.setPrefWidth(250);

        // Buttons
        printButton = new Button("Imprimer");
        printButton.getStyleClass().add("button-primary"); // Use app theme class
        printButton.setOnAction(e -> handlePrint());

        exportButton = new Button("Exporter PDF");
        exportButton.getStyleClass().add("button-action"); // Use app theme class
        exportButton.setOnAction(e -> handleExport());

        cancelButton = new Button("Fermer");
        cancelButton.getStyleClass().add("button-secondary");
        cancelButton.setOnAction(e -> close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ToolBar tb = new ToolBar(
                new Label("Imprimante: "),
                printerSelector,
                printButton,
                new Separator(),
                exportButton,
                spacer,
                cancelButton);
        tb.setPadding(new Insets(10));
        tb.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #eeeeee; -fx-border-width: 0 0 1 0;");
        return tb;
    }

    private void handlePrint() {
        Printer selectedPrinter = printerSelector.getValue();
        if (selectedPrinter == null) {
            showAlert("Erreur", "Veuillez sélectionner une imprimante.");
            return;
        }

        if (contentNode == null)
            return;

        PrinterJob job = PrinterJob.createPrinterJob(selectedPrinter);
        if (job != null) {
            PageLayout pageLayout = job.getJobSettings().getPageLayout();

            // 1. Calculate Scale
            double printableWidth = pageLayout.getPrintableWidth();
            double printableHeight = pageLayout.getPrintableHeight();

            // Use BoundsInParent to capture transforms/styles
            double nodeWidth = contentNode.getBoundsInParent().getWidth();
            double nodeHeight = contentNode.getBoundsInParent().getHeight();

            // Check if scaling is needed (only shrink, usually don't scale up too much)
            double scaleX = printableWidth / nodeWidth;
            double scaleY = printableHeight / nodeHeight;
            double scaleFactor = Math.min(scaleX, scaleY);

            // Keep at 1.0 if it fits, unless we want to force full width
            // Let's protect against massive shrinking of tiny content,
            // but primarily we want to fit LARGE content.
            // If scaleFactor < 1, means content is too big -> Scale down
            // If scaleFactor > 1, means content is small -> layout might justify it, but
            // let's default to fitting width if reasonable

            // Safety margin
            if (scaleFactor > 1.0)
                scaleFactor = 1.0;

            Scale scale = new Scale(scaleFactor, scaleFactor);
            contentNode.getTransforms().add(scale); // Apply scale

            boolean success = job.printPage(contentNode);

            contentNode.getTransforms().remove(scale); // Remove scale after printing to restore UI

            if (success) {
                job.endJob();
                showAlert("Succès", "Impression envoyée à " + selectedPrinter.getName());
                close();
            } else {
                showAlert("Erreur", "Échec de l'impression via " + selectedPrinter.getName());
            }
        } else {
            showAlert("Erreur", "Impossible de créer la tâche d'impression.");
        }
    }

    private void handleExport() {
        if (exportAction == null)
            return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Document_" + System.currentTimeMillis() + ".pdf");

        File file = fileChooser.showSaveDialog(getDialogPane().getScene().getWindow());
        if (file != null) {
            try {
                exportAction.accept(file);
                showAlert("Succès", "Exportation réussie vers PDF.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Échec de l'exportation: " + e.getMessage());
            }
        }
    }

    // --- Content Builders ---

    public void showClientList(List<Client> clients) {
        VBox content = new VBox(0); // Removing default spacing, handle via padding
        content.setPadding(new Insets(40));
        content.setStyle("-fx-background-color: white;");

        content.getChildren().add(createAdminHeader());
        content.getChildren().add(createTitle(bundle.getString("pdf.title")));

        GridPane table = new GridPane();
        table.setHgap(15);
        table.setVgap(8);
        table.setStyle("-fx-border-color: #ecf0f1; -fx-border-width: 1; -fx-padding: 10;");

        String[] headers = { "Raison Sociale", "Ville", "ICE", "Total HT", "Total TTC" };
        for (int i = 0; i < headers.length; i++) {
            Label h = new Label(headers[i]);
            h.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            h.setStyle("-fx-text-fill: #2c3e50; -fx-border-color: #bdc3c7; -fx-border-width: 0 0 2 0; -fx-padding: 5;");
            table.add(h, i, 0);
        }

        String currency = bundle.getString("currency");
        int row = 1;
        for (Client c : clients) {
            addStyledCell(table, c.getRaisonSociale(), 0, row, false);
            addStyledCell(table, c.getVille(), 1, row, false);
            addStyledCell(table, c.getIce(), 2, row, false);
            addStyledCell(table, String.format("%.2f %s", c.getFixedTotalAmount(), currency), 3, row, true);
            addStyledCell(table, String.format("%.2f %s", c.getTtc(), currency), 4, row, true);
            row++;
        }

        content.getChildren().add(table);
        setContent(content);

        this.exportAction = (file) -> {
            try {
                PdfExporter.exportClients(clients, adminInfo, file.getAbsolutePath(), bundle);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        showAndWait();
    }

    public void showTransactionReceipt(Transaction t, Client c) {
        VBox content = new VBox(25);
        content.setPadding(new Insets(50));
        content.setStyle(
                "-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        content.getChildren().add(createAdminHeader());

        // Receipt Badge
        Label badge = new Label("REÇU DE PAIEMENT");
        badge.setStyle(
                "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-padding: 10 20; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 16px;");
        badge.setMaxWidth(Double.MAX_VALUE);
        badge.setAlignment(Pos.CENTER);
        content.getChildren().add(badge);

        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(15);

        // Left Column (Client)
        VBox clientBox = new VBox(5);
        Label cLabel = new Label("Facturé à:");
        cLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        Label cName = new Label(c.getRaisonSociale());
        cName.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        clientBox.getChildren().addAll(cLabel, cName, new Label("ICE: " + (c.getIce() != null ? c.getIce() : "-")));
        grid.add(clientBox, 0, 0);

        // Right Column (Date)
        VBox dateBox = new VBox(5);
        dateBox.setAlignment(Pos.TOP_RIGHT);
        Label dLabel = new Label("Date de paiement:");
        dLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        Label dVal = new Label(t.getDate());
        dVal.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        dateBox.getChildren().addAll(dLabel, dVal);
        grid.add(dateBox, 1, 0);

        GridPane.setHgrow(clientBox, Priority.ALWAYS);
        content.getChildren().add(grid);

        content.getChildren().add(new Separator());

        // Amount Box
        VBox amountBox = new VBox(10);
        amountBox.setAlignment(Pos.CENTER);
        amountBox.setStyle(
                "-fx-background-color: #f8f9fa; -fx-padding: 30; -fx-border-radius: 10; -fx-border-style: dashed; -fx-border-color: #bdc3c7; -fx-border-width: 2;");

        Label amtLabel = new Label(String.format("%.2f %s", t.getAmount(), bundle.getString("currency")));
        amtLabel.setStyle("-fx-font-size: 38px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        amountBox.getChildren().addAll(new Label("MONTANT TOTAL"), amtLabel);
        content.getChildren().add(amountBox);

        // Notes
        VBox notesBox = new VBox(5);
        Label nLabel = new Label("Description / Notes:");
        nLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
        Label nVal = new Label(t.getNotes());
        nVal.setWrapText(true);
        nVal.setStyle("-fx-font-size: 14px;");
        notesBox.getChildren().addAll(nLabel, nVal);
        content.getChildren().add(notesBox);

        setContent(content);

        this.exportAction = (file) -> {
            try {
                PdfExporter.exportTransactionReceipt(t, c, adminInfo, file.getAbsolutePath(), bundle);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        showAndWait();
    }

    public void showStatement(Client c, List<Transaction> transactions) {
        VBox content = new VBox(15); // Reduced spacing
        content.setPadding(new Insets(40));
        content.setStyle("-fx-background-color: white;");

        content.getChildren().add(createAdminHeader());
        content.getChildren().add(createTitle("RELEVÉ DE COMPTE"));

        // Info Card
        HBox infoCard = new HBox(40);
        infoCard.setStyle("-fx-background-color: #f1f8e9; -fx-padding: 20; -fx-background-radius: 5;");

        VBox clientSec = new VBox(5);
        Label clTitle = new Label("CLIENT");
        clTitle.setStyle("-fx-font-size: 10px; -fx-text-fill: #558b2f; -fx-font-weight: bold;");
        Label clName = new Label(c.getRaisonSociale());
        clName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        clientSec.getChildren().addAll(clTitle, clName, new Label(c.getAdresse()), new Label("ICE: " + c.getIce()));

        infoCard.getChildren().add(clientSec);
        content.getChildren().add(infoCard);

        // Transactions Table
        GridPane table = new GridPane();
        table.setHgap(20);
        table.setVgap(10);

        // Headers with bottom border
        Label h1 = new Label("DATE");
        h1.setStyle("-fx-font-weight: bold; -fx-border-color: #cfd8dc; -fx-border-width: 0 0 2 0;");
        Label h2 = new Label("DESCRIPTION");
        h2.setStyle("-fx-font-weight: bold; -fx-border-color: #cfd8dc; -fx-border-width: 0 0 2 0;");
        Label h3 = new Label("MONTANT");
        h3.setStyle("-fx-font-weight: bold; -fx-border-color: #cfd8dc; -fx-border-width: 0 0 2 0;");

        table.add(h1, 0, 0);
        table.add(h2, 1, 0);
        table.add(h3, 2, 0);

        String currency = bundle.getString("currency");
        double total = 0;
        int row = 1;

        for (Transaction t : transactions) {
            Label d = new Label(t.getDate());
            Label n = new Label(t.getNotes());
            n.setPrefWidth(300);
            n.setWrapText(true);
            Label a = new Label(String.format("%.2f %s", t.getAmount(), currency));
            a.setStyle("-fx-font-family: 'Consolas'; -fx-font-weight: bold;");

            // Add light grey decoration for rows
            String style = "-fx-padding: 5 0; -fx-border-color: #eceff1; -fx-border-width: 0 0 1 0;";
            d.setStyle(style);
            n.setStyle(style);
            a.setStyle(style);

            table.add(d, 0, row);
            table.add(n, 1, row);
            table.add(a, 2, row);

            total += t.getAmount();
            row++;
        }

        content.getChildren().add(table);

        HBox totalBox = new HBox(15);
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        totalBox.setPadding(new Insets(20, 0, 0, 0));

        Label lblTot = new Label("TOTAL:");
        lblTot.setStyle("-fx-font-size: 14px; -fx-text-fill: #78909c;");
        Label valTot = new Label(String.format("%.2f %s", total, currency));
        valTot.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #263238;");

        totalBox.getChildren().addAll(lblTot, valTot);
        content.getChildren().add(totalBox);

        setContent(content);

        this.exportAction = (file) -> {
            try {
                PdfExporter.exportTransactionStatement(c, transactions, adminInfo, file.getAbsolutePath(), bundle);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        showAndWait();
    }

    // --- Helpers ---

    private void setContent(Node content) {
        this.contentNode = content;

        // Wrap in a StackPane to center it visually in the scroll pane like a paper
        StackPane paperContainer = new StackPane(content);
        paperContainer.setStyle("-fx-padding: 20; -fx-background-color: #cfd8dc;"); // Darker background
        paperContainer.setAlignment(Pos.TOP_CENTER);

        // Give the content a shadow to look like paper
        // Effect handled inside show... methods

        ScrollPane scroll = new ScrollPane(paperContainer);
        scroll.setFitToWidth(true);
        scroll.setPannable(true);

        getDialogPane().setContent(scroll);
    }

    private void addStyledCell(GridPane grid, String text, int col, int row, boolean alignRight) {
        Label l = new Label(text);
        l.setPadding(new Insets(8));
        l.setStyle("-fx-border-color: #ecf0f1; -fx-border-width: 0 0 1 0;");
        if (alignRight)
            l.setAlignment(Pos.CENTER_RIGHT);
        grid.add(l, col, row);
    }

    private Node createAdminHeader() {
        if (adminInfo == null || adminInfo.getRaisonSociale() == null)
            return new Region();

        VBox header = new VBox(2);
        Label company = new Label(adminInfo.getRaisonSociale().toUpperCase());
        company.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        company.setStyle("-fx-text-fill: #2c3e50;");

        String contact = (adminInfo.getAdresse() != null ? adminInfo.getAdresse() : "") + " " +
                (adminInfo.getVille() != null ? adminInfo.getVille() : "");

        Label addr = new Label(contact);
        addr.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");

        header.getChildren().addAll(company, addr);
        return header;
    }

    private Label createTitle(String text) {
        Label title = new Label(text);
        title.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 24px; -fx-font-weight: bold; -fx-padding: 10 0 20 0; -fx-text-fill: #34495e;");
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);
        return title;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
