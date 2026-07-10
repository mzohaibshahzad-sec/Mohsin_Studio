package Controllers;

import Database.OrderDAO;
import Database.PackageDAO;
import Models.Order;
import Models.Package;
import Models.User;
import Services.EmailService;
import Services.PosterGenerator;
import Services.WhatsAppHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;

public class PendingApprovalsView {

    private User currentUser;
    private TableView<Order> table;

    public PendingApprovalsView(User currentUser) {
        this.currentUser = currentUser;
    }

    public VBox getView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        Label title = new Label("Pending Approvals");
        title.getStyleClass().add("page-title");

        table = new TableView<>();
        setupTable();
        loadOrders();
        VBox.setVgrow(table, Priority.ALWAYS);

        Button approveBtn = new Button("Approve Selected");
        approveBtn.getStyleClass().add("btn-success");
        approveBtn.setOnAction(e -> handleApprove());

        Button rejectBtn = new Button("Reject Selected");
        rejectBtn.getStyleClass().add("btn-danger");
        rejectBtn.setOnAction(e -> handleReject());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> loadOrders());

        HBox buttonBar = new HBox(10, approveBtn, rejectBtn, refreshBtn);
        container.getChildren().addAll(title, table, buttonBar);
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

        TableColumn<Order, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("customerPhone"));
        phoneCol.setPrefWidth(120);

        TableColumn<Order, String> typeCol = new TableColumn<>("Order Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("orderType"));
        typeCol.setPrefWidth(180);

        TableColumn<Order, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(100);

        TableColumn<Order, String> createdByCol = new TableColumn<>("Booked By");
        createdByCol.setCellValueFactory(new PropertyValueFactory<>("createdByName"));
        createdByCol.setPrefWidth(130);

        table.getColumns().addAll(idCol, customerCol, phoneCol, typeCol, amountCol, createdByCol);
    }

    private void loadOrders() {
        ObservableList<Order> data = FXCollections.observableArrayList(
                OrderDAO.getOrdersByStatus("PENDING_APPROVAL")
        );
        table.setItems(data);
    }

    private void handleApprove() {
        Order selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Pehle koi order select karein."); return; }

        boolean success = OrderDAO.approveOrder(selected.getId(), currentUser.getId());

        if (success) {
            Database.AuditLogDAO.log(currentUser.getId(), "APPROVED_ORDER", "orders", selected.getId(),
                    "Order #" + selected.getId() + " approved for " + selected.getCustomerName());

            String eventDateStr = selected.getEventDate() != null ? selected.getEventDate().toString() : null;
            String deliveryDateStr = selected.getDeliveryDate() != null ? selected.getDeliveryDate().toString() : null;

            // Package fetch karo agar hai
            Package pkg = null;
            if (selected.getPackageId() != null) {
                pkg = PackageDAO.getById(selected.getPackageId());
            }

            // HTML email banao
            String htmlBody = PosterGenerator.generateOrderConfirmationEmail(selected, pkg);

            // Email bhejo - HTML ke saath
            boolean emailSent = false;
            if (selected.getCustomerEmail() != null && !selected.getCustomerEmail().isEmpty()) {
                emailSent = EmailService.sendHtmlEmail(
                        selected.getCustomerEmail(),
                        "Order Confirmed - Mohsin Movies and Photo Studio",
                        htmlBody
                );
            }

            // WhatsApp link
            String waLink = WhatsAppHelper.buildOrderConfirmationLink(
                    selected.getCustomerPhone(),
                    selected.getCustomerName(),
                    selected.getOrderType(),
                    selected.getAmount(),
                    eventDateStr,
                    deliveryDateStr
            );

            showWhatsAppLinkDialog(waLink, selected.getCustomerName(), emailSent);
            loadOrders();
        } else {
            showAlert("Order approve nahi ho saka.");
        }
    }

    private void handleReject() {
        Order selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Pehle koi order select karein."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Kya aap is order ko reject karna chahte hain?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                OrderDAO.rejectOrder(selected.getId(), currentUser.getId());
                Database.AuditLogDAO.log(currentUser.getId(), "REJECTED_ORDER", "orders", selected.getId(),
                        "Order #" + selected.getId() + " rejected for " + selected.getCustomerName());
                loadOrders();
            }
        });
    }

    private void showWhatsAppLinkDialog(String link, String customerName, boolean emailSent) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Order Confirmed");

        String emailStatus = emailSent ? "Email bhi customer ko bhej di gayi hai." : "Email nahi ja saki (check karein).";

        Label msg = new Label("Order confirm ho gaya!\n" + emailStatus + "\n\n"
                + customerName + " ko WhatsApp pe bhi confirmation bhejna chahte hain?");
        msg.setWrapText(true);

        Button openWhatsAppBtn = new Button("WhatsApp Par Bhejein");
        openWhatsAppBtn.getStyleClass().add("btn-success");
        openWhatsAppBtn.setOnAction(e -> { openLinkInBrowser(link); dialog.close(); });

        Button closeBtn = new Button("Band Karein");
        closeBtn.getStyleClass().add("btn-secondary");
        closeBtn.setOnAction(e -> dialog.close());

        VBox layout = new VBox(15, msg, openWhatsAppBtn, closeBtn);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        javafx.scene.Scene scene = new javafx.scene.Scene(layout, 380, 230);
        Utils.ThemeManager.applyTheme(scene);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void openLinkInBrowser(String url) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("nux") || os.contains("nix")) {
                new ProcessBuilder("xdg-open", url).start();
            } else if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception ex) {
            System.out.println("Error opening browser: " + ex.getMessage());
        }
    }

    private void showAlert(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }
}