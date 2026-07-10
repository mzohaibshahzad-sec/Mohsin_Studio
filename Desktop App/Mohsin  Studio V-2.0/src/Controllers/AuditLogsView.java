package Controllers;

import Database.AuditLogDAO;
import Models.AuditLog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.Timestamp;

public class AuditLogsView {

    private TableView<AuditLog> table;
    private Label statusLabel;

    public VBox getView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        Label title = new Label("Audit Logs");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        table = new TableView<>();
        setupTable();

        statusLabel = new Label();
        statusLabel.getStyleClass().add("muted-text");

        loadLogs();
        VBox.setVgrow(table, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> loadLogs());

        Button exportBtn = new Button("📥 Excel Mein Export Karein");
        exportBtn.getStyleClass().add("btn-secondary");
        exportBtn.setOnAction(e -> handleExport());

        HBox buttonBar = new HBox(10, refreshBtn, exportBtn);

        container.getChildren().addAll(title, table, buttonBar, statusLabel);
        return container;
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        TableColumn<AuditLog, Timestamp> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        timeCol.setPrefWidth(160);

        TableColumn<AuditLog, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        userCol.setPrefWidth(130);

        TableColumn<AuditLog, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(new PropertyValueFactory<>("action"));
        actionCol.setPrefWidth(180);

        TableColumn<AuditLog, String> tableCol = new TableColumn<>("Table");
        tableCol.setCellValueFactory(new PropertyValueFactory<>("targetTable"));
        tableCol.setPrefWidth(100);

        TableColumn<AuditLog, String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(new PropertyValueFactory<>("details"));
        detailsCol.setPrefWidth(280);

        table.getColumns().addAll(timeCol, userCol, actionCol, tableCol, detailsCol);
    }

    private void loadLogs() {
        ObservableList<AuditLog> data = FXCollections.observableArrayList(AuditLogDAO.getAllLogs());
        table.setItems(data);
        statusLabel.setText("Total Logs: " + data.size());
    }

    private void handleExport() {
        if (table.getItems().isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Export karne ke liye koi log nahi hai.").showAndWait();
            return;
        }

        statusLabel.setText("⏳ Export ho raha hai...");

        String filePath = Services.ExcelExportService.exportAuditLogs(table.getItems());

        if (filePath != null) {
            statusLabel.setText("✅ Export ho gaya! Total: " + table.getItems().size() + " logs");
            openFile(filePath);
        } else {
            statusLabel.setText("❌ Export nahi ho saka.");
        }
    }

    private void openFile(String filePath) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("nux") || os.contains("nix")) {
                new ProcessBuilder("xdg-open", filePath).start();
            } else {
                java.awt.Desktop.getDesktop().open(new java.io.File(filePath));
            }
        } catch (Exception ex) {
            statusLabel.setText("File ban gayi: " + filePath);
        }
    }
}