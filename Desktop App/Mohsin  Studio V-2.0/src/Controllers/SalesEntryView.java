package Controllers;

import Database.SalesEntryDAO;
import Models.SalesEntry;
import Models.User;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.sql.Date;
import java.time.LocalDate;

public class SalesEntryView {

    private User currentUser;
    private TableView<SalesEntry> table;
    private Label totalLabel;
    private LocalDate lastLoadedDate;

    public SalesEntryView(User currentUser) {
        this.currentUser = currentUser;
    }

    public VBox getView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        Label title = new Label("Sales Entry");
        title.getStyleClass().add("page-title");

        // ===== Entry Form =====
        ComboBox<String> entryTypeBox = new ComboBox<>();
        entryTypeBox.getItems().addAll(Database.CategoryDAO.getNamesByType("SALES"));
        entryTypeBox.setPromptText("Entry Type");
        entryTypeBox.setEditable(true);

        TextField descField = new TextField();
        descField.setPromptText("Description (e.g. 10 copies A4)");

        TextField amountField = new TextField();
        amountField.setPromptText("Amount (Rs.)");

        TextField notesField = new TextField();
        notesField.setPromptText("Notes (optional)");

        Label messageLabel = new Label();

        Button addBtn = new Button("Entry Add Karein");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> {
            String type = entryTypeBox.getEditor().getText().trim();
            String desc = descField.getText().trim();
            String amountText = amountField.getText().trim();
            String notes = notesField.getText().trim();

            if (type.isEmpty() || amountText.isEmpty()) {
                messageLabel.getStyleClass().setAll("error-text");
                messageLabel.setText("Entry Type aur Amount zaroori hain.");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountText);
            } catch (NumberFormatException ex) {
                messageLabel.getStyleClass().setAll("error-text");
                messageLabel.setText("Amount sirf number mein likhein.");
                return;
            }

            SalesEntry entry = new SalesEntry();
            entry.setEntryType(type);
            entry.setDescription(desc);
            entry.setAmount(amount);
            entry.setEnteredBy(currentUser.getId());
            entry.setEntryDate(Date.valueOf(LocalDate.now()));
            entry.setNotes(notes);

            boolean success = SalesEntryDAO.addEntry(entry);

            if (success) {

                Database.AuditLogDAO.log(currentUser.getId(), "ADDED_SALES_ENTRY", "sales_entries", null,
                        type + " - Rs. " + amount);
                messageLabel.getStyleClass().setAll("success-text");
                messageLabel.setText("Entry add ho gayi!");
                entryTypeBox.getEditor().clear();
                descField.clear();
                amountField.clear();
                notesField.clear();
                loadTodayEntries();
            } else {
                messageLabel.getStyleClass().setAll("error-text");
                messageLabel.setText("Entry add nahi ho saki. Dobara koshish karein.");
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("Entry Type:"), 0, 0);
        form.add(entryTypeBox, 1, 0);
        form.add(new Label("Description:"), 0, 1);
        form.add(descField, 1, 1);
        form.add(new Label("Amount:"), 0, 2);
        form.add(amountField, 1, 2);
        form.add(new Label("Notes:"), 0, 3);
        form.add(notesField, 1, 3);

        // ===== Today's Entries Table =====
        Label tableTitle = new Label("Aaj Ki Entries");
        tableTitle.getStyleClass().add("section-title");

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> {
            loadTodayEntries();
            lastLoadedDate = LocalDate.now();
        });

        HBox tableHeaderBar = new HBox(12, tableTitle, refreshBtn);
        tableHeaderBar.setAlignment(Pos.CENTER_LEFT);

        table = new TableView<>();
        setupTable();
        loadTodayEntries();
        lastLoadedDate = LocalDate.now();
        startDateWatcher();
        VBox.setVgrow(table, Priority.ALWAYS);

        totalLabel = new Label();
        totalLabel.getStyleClass().add("section-title");
        updateTotalLabel();

        Label reportMsgLabel = new Label();

        Button sendReportBtn = new Button("Aaj Ki Report CEO Ko Bhejein (PDF)");
        sendReportBtn.getStyleClass().add("btn-primary");
        sendReportBtn.setOnAction(e -> {
            sendReportBtn.setDisable(true);
            sendReportBtn.setText("Bhej rahe hain...");

            new Thread(() -> {
                java.util.List<Models.SalesEntry> todayEntries = Database.SalesEntryDAO.getTodayEntriesByUser(currentUser.getId());

                if (todayEntries.isEmpty()) {
                    javafx.application.Platform.runLater(() -> {
                        reportMsgLabel.getStyleClass().setAll("error-text");
                        reportMsgLabel.setText("Aaj koi entry nahi hai bhejne ke liye.");
                        sendReportBtn.setDisable(false);
                        sendReportBtn.setText("Aaj Ki Report CEO Ko Bhejein (PDF)");
                    });
                    return;
                }

                java.time.LocalDate today = java.time.LocalDate.now();
                String pdfPath = Services.PdfReportService.generateDailySalesReport(todayEntries, currentUser.getFullName(), today);

                if (pdfPath == null) {
                    javafx.application.Platform.runLater(() -> {
                        reportMsgLabel.getStyleClass().setAll("error-text");
                        reportMsgLabel.setText("PDF banane mein masla aaya.");
                        sendReportBtn.setDisable(false);
                        sendReportBtn.setText("Aaj Ki Report CEO Ko Bhejein (PDF)");
                    });
                    return;
                }

                String ceoEmail = Database.UserDAO.getCeoEmail();
                double total = 0;
                for (Models.SalesEntry entry : todayEntries) total += entry.getAmount();

                String subject = "Daily Sales Report - " + today + " (" + currentUser.getFullName() + ")";
                String body = "Assalam o Alaikum,\n\nAaj ki sales report attached hai.\n\n" +
                        "Clerk: " + currentUser.getFullName() + "\n" +
                        "Total Entries: " + todayEntries.size() + "\n" +
                        "Total Sales: Rs. " + total + "\n\nShukriya.";

                boolean sent = Services.EmailService.sendEmailWithAttachment(ceoEmail, subject, body, pdfPath);

                if (sent) {
                    Database.AuditLogDAO.log(currentUser.getId(), "SENT_DAILY_REPORT", "daily_reports", null,
                            currentUser.getFullName() + " sent daily report for " + today);
                }

                javafx.application.Platform.runLater(() -> {
                    if (sent) {
                        reportMsgLabel.getStyleClass().setAll("success-text");
                        reportMsgLabel.setText("Report CEO ko email ho gayi!");
                    } else {
                        reportMsgLabel.getStyleClass().setAll("error-text");
                        reportMsgLabel.setText("Email bhejne mein masla aaya. Email settings check karein.");
                    }
                    sendReportBtn.setDisable(false);
                    sendReportBtn.setText("Aaj Ki Report CEO Ko Bhejein (PDF)");
                });
            }).start();
        });

        Button exportBtn = new Button("Excel Mein Export Karein");
        exportBtn.getStyleClass().add("btn-secondary");
        exportBtn.setOnAction(e -> {
            if (table.getItems().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Export karne ke liye koi entry nahi hai.");
                alert.showAndWait();
                return;
            }
            String filePath = Services.ExcelExportService.exportSalesEntries(table.getItems(), "Sales Entries");
            if (filePath != null) {
                try {
                    String os = System.getProperty("os.name").toLowerCase();
                    if (os.contains("nux") || os.contains("nix")) {
                        new ProcessBuilder("xdg-open", filePath).start();
                    } else {
                        java.awt.Desktop.getDesktop().open(new java.io.File(filePath));
                    }
                } catch (Exception ex) {
                    System.out.println("Could not open file: " + ex.getMessage());
                }
            }
        });

        container.getChildren().addAll(title, form, addBtn, messageLabel, tableHeaderBar, table, totalLabel, sendReportBtn, reportMsgLabel, exportBtn);
        return container;
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        TableColumn<SalesEntry, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("entryType"));
        typeCol.setPrefWidth(130);

        TableColumn<SalesEntry, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(200);

        TableColumn<SalesEntry, Double> amountCol = new TableColumn<>("Amount (Rs.)");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(110);

        TableColumn<SalesEntry, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));
        notesCol.setPrefWidth(180);

        table.getColumns().addAll(typeCol, descCol, amountCol, notesCol);
    }

    // Har minute check karta hai ke date change hui ya nahi (naya din shuru hua ya nahi)
    // Agar naya din shuru ho jaye to "Aaj Ki Entries" khud-ba-khud refresh ho jati hain
    private void startDateWatcher() {
        Timeline watcher = new Timeline(new KeyFrame(Duration.minutes(1), e -> {
            LocalDate today = LocalDate.now();
            if (!today.equals(lastLoadedDate)) {
                lastLoadedDate = today;
                loadTodayEntries();
            }
        }));
        watcher.setCycleCount(Animation.INDEFINITE);
        watcher.play();
    }

    private void loadTodayEntries() {
        ObservableList<SalesEntry> data = FXCollections.observableArrayList(
                SalesEntryDAO.getTodayEntriesByUser(currentUser.getId())
        );
        table.setItems(data);
        updateTotalLabel();
    }

    private void updateTotalLabel() {
        double total = 0;
        for (SalesEntry entry : table.getItems()) {
            total += entry.getAmount();
        }
        if (totalLabel != null) {
            totalLabel.setText("Aaj Ka Total: Rs. " + total);
        }
    }
}