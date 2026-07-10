package Controllers;

import Database.OrderDAO;
import Database.PaymentDAO;
import Models.Order;
import Models.Payment;
import Models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Date;
import java.time.LocalDate;

public class OrderManagementView {

    private User currentUser;
    private TableView<Order> table;

    public OrderManagementView(User currentUser) {
        this.currentUser = currentUser;
    }

    public VBox getView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        Label title = new Label("Orders Manage Karein");
        title.getStyleClass().add("page-title");

        ComboBox<String> filterBox = new ComboBox<>();
        filterBox.getItems().addAll("Sab Active Orders", "CONFIRMED", "IN_PROGRESS", "READY", "DELIVERED");
        filterBox.setValue("Sab Active Orders");
        filterBox.setOnAction(e -> loadOrders(filterBox.getValue()));

        table = new TableView<>();
        setupTable();
        loadOrders("Sab Active Orders");
        VBox.setVgrow(table, Priority.ALWAYS);

        Button updateStatusBtn = new Button("Status Update Karein");
        updateStatusBtn.getStyleClass().add("btn-primary");
        updateStatusBtn.setOnAction(e -> handleStatusUpdate(filterBox.getValue()));

        Button recordPaymentBtn = new Button("Payment Record Karein");
        recordPaymentBtn.getStyleClass().add("btn-success");
        recordPaymentBtn.setOnAction(e -> handleRecordPayment());

        Button printReceiptBtn = new Button("Receipt Print Karein");
        printReceiptBtn.getStyleClass().add("btn-secondary");
        printReceiptBtn.setOnAction(e -> handlePrintReceipt());

        Button exportBtn = new Button("Excel Export Karein");
        exportBtn.getStyleClass().add("btn-secondary");
        exportBtn.setOnAction(e -> handleExportExcel());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> loadOrders(filterBox.getValue()));
        Button viewPaymentsBtn = new Button("Payment History (Delete)");
        viewPaymentsBtn.getStyleClass().add("btn-secondary");
        viewPaymentsBtn.setOnAction(e -> handleViewPayments());

        HBox buttonBar = new HBox(10, updateStatusBtn, recordPaymentBtn, viewPaymentsBtn, printReceiptBtn, exportBtn, refreshBtn);

        container.getChildren().addAll(title, filterBox, table, buttonBar);
        return container;
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        TableColumn<Order, Integer> idCol = new TableColumn<>("Order #");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(70);

        TableColumn<Order, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerCol.setPrefWidth(150);

        TableColumn<Order, String> typeCol = new TableColumn<>("Order Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("orderType"));
        typeCol.setPrefWidth(180);

        TableColumn<Order, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(90);

        TableColumn<Order, Double> paidCol = new TableColumn<>("Paid");
        paidCol.setCellValueFactory(new PropertyValueFactory<>("totalPaid"));
        paidCol.setPrefWidth(90);

        TableColumn<Order, Double> balanceCol = new TableColumn<>("Balance");
        balanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));
        balanceCol.setPrefWidth(90);

        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);

        TableColumn<Order, Date> deliveryCol = new TableColumn<>("Delivery Date");
        deliveryCol.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));
        deliveryCol.setPrefWidth(110);

        table.getColumns().addAll(idCol, customerCol, typeCol, amountCol, paidCol, balanceCol, statusCol, deliveryCol);
    }

    private void loadOrders(String filter) {
        ObservableList<Order> data;
        if (filter.equals("Sab Active Orders")) {
            data = FXCollections.observableArrayList();
            data.addAll(OrderDAO.getOrdersByStatus("CONFIRMED"));
            data.addAll(OrderDAO.getOrdersByStatus("IN_PROGRESS"));
            data.addAll(OrderDAO.getOrdersByStatus("READY"));
        } else {
            data = FXCollections.observableArrayList(OrderDAO.getOrdersByStatus(filter));
        }
        table.setItems(data);
    }

    private void handleStatusUpdate(String currentFilter) {
        Order selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Pehle koi order select karein.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(selected.getStatus(),
                "CONFIRMED", "IN_PROGRESS", "READY", "DELIVERED");
        dialog.setTitle("Status Update Karein");
        dialog.setHeaderText("Order #" + selected.getId() + " - " + selected.getCustomerName());
        dialog.setContentText("Naya Status:");

        dialog.showAndWait().ifPresent(newStatus -> {
            Database.AuditLogDAO.log(currentUser.getId(), "UPDATED_ORDER_STATUS", "orders", selected.getId(),
                    "Order #" + selected.getId() + " status changed to " + newStatus);
            OrderDAO.updateStatus(selected.getId(), newStatus);
            loadOrders(currentFilter);
        });
    }

    private void handleRecordPayment() {
        Order selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Pehle koi order select karein.");
            return;
        }

        double alreadyPaid = PaymentDAO.getTotalPaidForOrder(selected.getId());
        double remaining = selected.getAmount() - alreadyPaid;

        Stage formStage = new Stage();
        formStage.initModality(Modality.APPLICATION_MODAL);
        formStage.setTitle("Payment Record Karein");

        // ===== Order Info =====
        Label infoLabel = new Label("Order #" + selected.getId() + " - " + selected.getCustomerName() +
                "\nTotal Amount: Rs. " + selected.getAmount() +
                "\nAb Tak Paid: Rs. " + alreadyPaid +
                "\nBaqi: Rs. " + remaining);
        infoLabel.setWrapText(true);

        // ===== Amount Field =====
        Label amountLabel = new Label("Amount (Rs.):");
        TextField paidField = new TextField();
        paidField.setPromptText("Kitna paisa mil raha hai abhi (Rs.)");

        // ===== Payment Method =====
        Label methodLabel = new Label("Payment Method:");
        ComboBox<String> methodBox = new ComboBox<>();
        methodBox.getItems().addAll("CASH", "BANK_TRANSFER", "EASYPAISA", "JAZZCASH", "OTHER");
        methodBox.setValue("CASH");
        methodBox.setMaxWidth(Double.MAX_VALUE);

        // ===== Transaction ID Field (conditional) =====
        Label txnLabel = new Label("Transaction ID:");
        TextField txnField = new TextField();
        txnField.setPromptText("Transaction ID / Reference number likhein");
        txnField.setMaxWidth(Double.MAX_VALUE);

        // Pehle CASH selected hai toh transaction ID hide karo
        txnLabel.setVisible(false);
        txnLabel.setManaged(false);
        txnField.setVisible(false);
        txnField.setManaged(false);

        // Jab method change ho toh Transaction ID show/hide karo
        methodBox.setOnAction(e -> {
            String selectedMethod = methodBox.getValue();
            boolean showTxn = !selectedMethod.equals("CASH");
            txnLabel.setVisible(showTxn);
            txnLabel.setManaged(showTxn);
            txnField.setVisible(showTxn);
            txnField.setManaged(showTxn);
        });

        // ===== Error Label =====
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-text");
        errorLabel.setWrapText(true);

        // ===== Save Button =====
        Button saveBtn = new Button("Save Payment");
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.setMaxWidth(Double.MAX_VALUE);

        saveBtn.setOnAction(e -> {
            String paidText = paidField.getText().trim();
            if (paidText.isEmpty()) {
                errorLabel.setText("Amount likhna zaroori hai.");
                return;
            }

            double paidAmount;
            try {
                paidAmount = Double.parseDouble(paidText);
            } catch (NumberFormatException ex) {
                errorLabel.setText("Sirf number likhein.");
                return;
            }

            // Transaction ID validation - CASH ke ilawa baaki sab ke liye required
            String selectedMethod = methodBox.getValue();
            String transactionId = txnField.getText().trim();
            if (!selectedMethod.equals("CASH") && transactionId.isEmpty()) {
                errorLabel.setText("Transaction ID zaroori hai " + selectedMethod + " payment ke liye.");
                return;
            }

            Payment payment = new Payment();
            payment.setOrderId(selected.getId());
            payment.setTotalAmount(selected.getAmount());
            payment.setAdvancePaid(paidAmount);
            payment.setPaymentMethod(selectedMethod);
            payment.setPaymentDate(Date.valueOf(LocalDate.now()));
            payment.setRecordedBy(currentUser.getId());

            // Transaction ID set karo (CASH ke liye null rahega)
            if (!selectedMethod.equals("CASH") && !transactionId.isEmpty()) {
                payment.setTransactionId(transactionId);
            }

            boolean success = PaymentDAO.addPayment(payment);
            if (success) {
                Database.AuditLogDAO.log(currentUser.getId(), "RECORDED_PAYMENT", "payments", selected.getId(),
                        "Order #" + selected.getId() + " - Rs. " + paidAmount + " via " + methodBox.getValue());
                formStage.close();
                loadOrders("Sab Active Orders");
            } else {
                errorLabel.setText("Payment save nahi ho saka.");
            }
        });

        VBox layout = new VBox(10,
                infoLabel,
                new Separator(),
                amountLabel, paidField,
                methodLabel, methodBox,
                txnLabel, txnField,
                saveBtn,
                errorLabel
        );
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER_LEFT);

        javafx.scene.Scene formScene = new javafx.scene.Scene(layout, 370, 380);
        Utils.ThemeManager.applyTheme(formScene);
        formStage.setScene(formScene);
        formStage.showAndWait();
    }


    private void handleViewPayments() {
        Order selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Pehle koi order select karein.");
            return;
        }

        java.util.List<Models.Payment> payments = PaymentDAO.getPaymentsByOrder(selected.getId());

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Payment History - Order #" + selected.getId());

        Label title = new Label("Order #" + selected.getId() + " - " + selected.getCustomerName());
        title.getStyleClass().add("section-title");

        TableView<Models.Payment> payTable = new TableView<>();

        TableColumn<Models.Payment, java.sql.Date> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        dateCol.setPrefWidth(100);

        TableColumn<Models.Payment, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("advancePaid"));
        amountCol.setPrefWidth(100);

        TableColumn<Models.Payment, String> methodCol = new TableColumn<>("Method");
        methodCol.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        methodCol.setPrefWidth(110);

        TableColumn<Models.Payment, String> txnCol = new TableColumn<>("Transaction ID");
        txnCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        txnCol.setPrefWidth(140);

        payTable.getColumns().addAll(dateCol, amountCol, methodCol, txnCol);
        payTable.setItems(javafx.collections.FXCollections.observableArrayList(payments));
        payTable.setPrefHeight(250);

        Button deleteBtn = new Button("Selected Payment Delete Karein");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setOnAction(e -> {
            Models.Payment selectedPayment = payTable.getSelectionModel().getSelectedItem();
            if (selectedPayment == null) {
                showAlert("Pehle koi payment select karein.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Kya aap Rs. " + selectedPayment.getAdvancePaid() + " ki ye payment delete karna chahte hain?\n" +
                            "Ye action wapas nahi ho sakta.");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean success = PaymentDAO.deletePayment(selectedPayment.getId());
                    if (success) {
                        Database.AuditLogDAO.log(currentUser.getId(), "DELETED_PAYMENT", "payments",
                                selectedPayment.getId(), "Order #" + selected.getId() + " - deleted Rs. " + selectedPayment.getAdvancePaid() + " payment");
                        payTable.setItems(javafx.collections.FXCollections.observableArrayList(
                                PaymentDAO.getPaymentsByOrder(selected.getId())));
                        loadOrders("Sab Active Orders");
                    } else {
                        showAlert("Payment delete nahi ho saka.");
                    }
                }
            });
        });

        Button closeBtn = new Button("Band Karein");
        closeBtn.getStyleClass().add("btn-secondary");
        closeBtn.setOnAction(e -> dialog.close());

        VBox layout = new VBox(12, title, payTable, deleteBtn, closeBtn);
        layout.setPadding(new Insets(20));

        javafx.scene.Scene scene = new javafx.scene.Scene(layout, 500, 420);
        Utils.ThemeManager.applyTheme(scene);
        dialog.setScene(scene);
        dialog.showAndWait();
    }


    private void handlePrintReceipt() {
        Order selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Pehle koi order select karein.");
            return;
        }

        double alreadyPaid = PaymentDAO.getTotalPaidForOrder(selected.getId());
        java.util.List<Models.Payment> paymentHistory = PaymentDAO.getPaymentsByOrder(selected.getId());
        String filePath = Services.ReceiptService.generateReceipt(selected, alreadyPaid, paymentHistory);

        if (filePath != null) {
            openFile(filePath);
        } else {
            showAlert("Receipt banane mein masla aaya.");
        }
    }

    private void handleExportExcel() {
        if (table.getItems().isEmpty()) {
            showAlert("Export karne ke liye koi data nahi hai.");
            return;
        }

        String filePath = Services.ExcelExportService.exportOrders(table.getItems());

        if (filePath != null) {
            openFile(filePath);
        } else {
            showAlert("Excel export mein masla aaya.");
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
            showAlert("File ban gayi hai, lekin khol nahi saki. Path: " + filePath);
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.showAndWait();
    }
}