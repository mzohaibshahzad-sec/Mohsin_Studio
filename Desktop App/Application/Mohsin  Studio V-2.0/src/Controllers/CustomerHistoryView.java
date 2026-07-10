package Controllers;

import Database.OrderDAO;
import Models.Customer;
import Models.Order;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Date;
import java.util.List;

public class CustomerHistoryView {

    public static void show(Customer customer) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Order History - " + customer.getName());

        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        // ===== Customer Info Header =====
        Label title = new Label(customer.getName());
        title.getStyleClass().add("page-title");

        Label contactInfo = new Label(customer.getPhone() + "  •  " + customer.getEmail());
        contactInfo.getStyleClass().add("muted-text");

        Label addressInfo = new Label(customer.getAddress());
        addressInfo.getStyleClass().add("muted-text");

        // ===== Orders Table =====
        List<Order> orders = OrderDAO.getOrdersByCustomerId(customer.getId());

        TableView<Order> table = new TableView<>();
        setupTable(table);
        table.setItems(FXCollections.observableArrayList(orders));
        VBox.setVgrow(table, Priority.ALWAYS);

        // ===== Summary Stats =====
        double totalSpent = 0;
        int deliveredCount = 0;
        int activeCount = 0;

        for (Order o : orders) {
            if (!"REJECTED".equals(o.getStatus()) && !"PENDING_APPROVAL".equals(o.getStatus())) {
                totalSpent += o.getAmount();
            }
            if ("DELIVERED".equals(o.getStatus())) deliveredCount++;
            if ("CONFIRMED".equals(o.getStatus()) || "IN_PROGRESS".equals(o.getStatus()) || "READY".equals(o.getStatus())) {
                activeCount++;
            }
        }

        HBox summaryBar = new HBox(25);
        summaryBar.setAlignment(Pos.CENTER_LEFT);
        summaryBar.getChildren().addAll(
                summaryStat("Total Orders", String.valueOf(orders.size())),
                summaryStat("Total Spending", "Rs. " + totalSpent),
                summaryStat("Delivered", String.valueOf(deliveredCount)),
                summaryStat("Active Orders", String.valueOf(activeCount))
        );

        VBox summaryCard = new VBox(summaryBar);
        summaryCard.getStyleClass().add("card");

        Button closeBtn = new Button("Band Karein");
        closeBtn.getStyleClass().add("btn-secondary");
        closeBtn.setOnAction(e -> stage.close());

        if (orders.isEmpty()) {
            Label emptyLabel = new Label("Is customer ka abhi tak koi order nahi hai.");
            emptyLabel.getStyleClass().add("muted-text");
            container.getChildren().addAll(title, contactInfo, addressInfo, emptyLabel, closeBtn);
        } else {
            container.getChildren().addAll(title, contactInfo, addressInfo, summaryCard, table, closeBtn);
        }

        Scene scene = new Scene(container, 750, 550);
        Utils.ThemeManager.applyTheme(scene);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private static VBox summaryStat(String label, String value) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER_LEFT);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label nameLabel = new Label(label);
        nameLabel.getStyleClass().add("muted-text");

        box.getChildren().addAll(valueLabel, nameLabel);
        return box;
    }

    @SuppressWarnings("unchecked")
    private static void setupTable(TableView<Order> table) {
        TableColumn<Order, Integer> idCol = new TableColumn<>("Order #");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(70);

        TableColumn<Order, String> typeCol = new TableColumn<>("Order Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("orderType"));
        typeCol.setPrefWidth(200);

        TableColumn<Order, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(100);

        TableColumn<Order, Date> orderDateCol = new TableColumn<>("Order Date");
        orderDateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        orderDateCol.setPrefWidth(110);

        TableColumn<Order, Date> deliveryDateCol = new TableColumn<>("Delivery Date");
        deliveryDateCol.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));
        deliveryDateCol.setPrefWidth(110);

        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(130);

        table.getColumns().addAll(idCol, typeCol, amountCol, orderDateCol, deliveryDateCol, statusCol);
    }
}