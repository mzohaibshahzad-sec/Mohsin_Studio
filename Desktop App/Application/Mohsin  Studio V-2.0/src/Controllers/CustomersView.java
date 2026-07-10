package Controllers;

import Database.CustomerDAO;
import Models.Customer;
import Models.User;
import Services.ExcelExportService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

public class CustomersView {

    private User currentUser;
    private TableView<Customer> table;
    private Label statusLabel;

    public CustomersView(User currentUser) {
        this.currentUser = currentUser;
    }

    public VBox getView() {
        VBox container = new VBox(12);
        container.setPadding(new Insets(20));
        VBox.setVgrow(container, Priority.ALWAYS);

        Label title = new Label("Customers");
        title.getStyleClass().add("page-title");

        // ===== Search Bar =====
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Naam ya Phone se search karein...");
        searchField.setPrefWidth(320);

        searchField.textProperty().addListener((obs, was, now) -> {
            if (now.trim().isEmpty()) {
                loadAllCustomers();
            } else {
                List<Customer> results = CustomerDAO.searchByNameOrPhone(now.trim());
                table.setItems(FXCollections.observableArrayList(results));
                statusLabel.setText("Results: " + results.size());
            }
        });

        HBox searchBar = new HBox(10, new Label("🔍 Search:"), searchField);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        // ===== Status Label =====
        statusLabel = new Label();
        statusLabel.getStyleClass().add("muted-text");

        // ===== Table =====
        table = new TableView<>();
        setupTable();
        loadAllCustomers();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        // ===== Buttons =====
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> {
            searchField.clear();
            loadAllCustomers();
        });

        Button historyBtn = new Button("📋 Order History");
        historyBtn.getStyleClass().add("btn-primary");
        historyBtn.setOnAction(e -> {
            Customer selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                CustomerHistoryView.show(selected);
            } else {
                new Alert(Alert.AlertType.INFORMATION, "Pehle koi customer select karein.").showAndWait();
            }
        });

        HBox buttonBar = new HBox(10, refreshBtn, historyBtn);
        buttonBar.setAlignment(Pos.CENTER_LEFT);
        buttonBar.setPadding(new Insets(5, 0, 0, 0));

        // Profile (info edit + data drives) - sirf CEO aur CoFounder
        if (currentUser.isCEO() || currentUser.isCoFounder()) {
            Button profileBtn = new Button("👤 Profile");
            profileBtn.getStyleClass().add("btn-secondary");
            profileBtn.setOnAction(e -> {
                Customer selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    CustomerProfileView.show(selected, this::loadAllCustomers);
                } else {
                    new Alert(Alert.AlertType.INFORMATION, "Pehle koi customer select karein.").showAndWait();
                }
            });
            buttonBar.getChildren().add(profileBtn);
        }

        // Export - sirf CEO
        if (currentUser.isCEO()) {
            Button exportBtn = new Button("📥 Export Excel");
            exportBtn.getStyleClass().add("btn-primary");
            exportBtn.setOnAction(e -> handleExportAllCustomers());
            buttonBar.getChildren().add(exportBtn);
        }

        container.getChildren().addAll(title, searchBar, table, buttonBar, statusLabel);
        return container;
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        TableColumn<Customer, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(55);

        TableColumn<Customer, String> nameCol = new TableColumn<>("Naam");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(180);

        TableColumn<Customer, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(130);

        TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        TableColumn<Customer, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        addressCol.setPrefWidth(220);

        table.getColumns().addAll(idCol, nameCol, phoneCol, emailCol, addressCol);
    }

    private void loadAllCustomers() {
        List<Customer> all = CustomerDAO.getAllCustomers();
        table.setItems(FXCollections.observableArrayList(all));
        statusLabel.setText("Total Customers: " + all.size());
    }

    private void handleExportAllCustomers() {
        statusLabel.setText("⏳ Export ho raha hai...");
        new Thread(() -> {
            List<Customer> all = CustomerDAO.getAllCustomersForBackup();
            String filePath = ExcelExportService.exportCustomers(all);
            javafx.application.Platform.runLater(() -> {
                if (filePath != null) {
                    statusLabel.setText("✅ Export ho gaya! Total: " + all.size() + " customers");
                    openFile(filePath);
                } else {
                    statusLabel.setText("❌ Export nahi ho saka.");
                }
            });
        }).start();
    }

    private void openFile(String filePath) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("nux") || os.contains("nix"))
                new ProcessBuilder("xdg-open", filePath).start();
            else
                java.awt.Desktop.getDesktop().open(new java.io.File(filePath));
        } catch (Exception ex) {
            statusLabel.setText("File ban gayi: " + filePath);
        }
    }
}