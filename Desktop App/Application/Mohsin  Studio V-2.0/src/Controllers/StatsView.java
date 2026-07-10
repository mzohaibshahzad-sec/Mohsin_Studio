package Controllers;

import Database.StatsDAO;
import Models.SalesStatPoint;
import Models.User;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatsView {

    private User currentUser;

    // ===== Daily section =====
    private ComboBox<Integer> monthBox;
    private ComboBox<Integer> yearBox;
    private TableView<SalesStatPoint> dailyTable;
    private BarChart<String, Number> dailyChart;
    private Label shopTotalLabel;
    private Label ordersTotalLabel;
    private Label combinedTotalLabel;

    // ===== Monthly section =====
    private TableView<SalesStatPoint> monthlyTable;
    private BarChart<String, Number> monthlyChart;
    private List<SalesStatPoint> currentMonthlyPoints = new ArrayList<>();
    private Label monthlyExportMsg;

    // ===== Yearly section =====
    private TableView<SalesStatPoint> yearlyTable;
    private BarChart<String, Number> yearlyChart;
    private List<SalesStatPoint> currentYearlyPoints = new ArrayList<>();
    private Label yearlyExportMsg;

    public StatsView(User currentUser) {
        this.currentUser = currentUser;
    }

    public VBox getView() {
        VBox container = new VBox(24);
        container.setPadding(new Insets(20));
        VBox.setVgrow(container, Priority.ALWAYS);

        Label title = new Label("📊 Sales Stats");
        title.getStyleClass().add("page-title");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        // ===== DAILY SECTION =====
        Label dailySectionTitle = sectionLabel("📅 Daily Breakdown (Dukan Sale alag, Orders alag)");

        LocalDate now = LocalDate.now();
        monthBox = new ComboBox<>();
        for (int m = 1; m <= 12; m++) monthBox.getItems().add(m);
        monthBox.setValue(now.getMonthValue());
        monthBox.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer m) {
                if (m == null) return "";
                return Month.of(m).getDisplayName(TextStyle.FULL, new Locale("en"));
            }

            @Override
            public Integer fromString(String s) {
                return null;
            }
        });

        yearBox = new ComboBox<>();
        for (int y = now.getYear() - 4; y <= now.getYear(); y++) yearBox.getItems().add(y);
        yearBox.setValue(now.getYear());

        Button loadBtn = new Button("🔍 Dekhein");
        loadBtn.getStyleClass().add("btn-primary");
        loadBtn.setOnAction(e -> loadDailyStats());

        HBox selectorBar = new HBox(10, new Label("Month:"), monthBox, new Label("Year:"), yearBox, loadBtn);
        selectorBar.setAlignment(Pos.CENTER_LEFT);

        shopTotalLabel = summaryValueLabel("#27ae60");
        ordersTotalLabel = summaryValueLabel("#2980b9");
        combinedTotalLabel = summaryValueLabel("#8e44ad");

        HBox summaryBar = new HBox(16,
                summaryCard("🏪 Dukan Ki Sale (Is Month)", shopTotalLabel),
                summaryCard("📋 Orders Revenue (Is Month)", ordersTotalLabel),
                summaryCard("💰 Combined Total (Is Month)", combinedTotalLabel));

        dailyTable = new TableView<>();
        setupStatsTable(dailyTable, "Date");
        dailyTable.setPrefHeight(200);

        CategoryAxis dailyX = new CategoryAxis();
        NumberAxis dailyY = new NumberAxis();
        dailyChart = new BarChart<>(dailyX, dailyY);
        dailyChart.setPrefHeight(260);
        dailyChart.setAnimated(false);

        VBox dailySection = new VBox(12, dailySectionTitle, selectorBar, summaryBar, dailyTable, dailyChart);

        // ===== MONTHLY SECTION =====
        Label monthlySectionTitle = sectionLabel("🗓 Monthly Breakdown (Last 12 Mahine - Sale alag, Orders alag)");

        monthlyTable = new TableView<>();
        setupStatsTable(monthlyTable, "Month");
        monthlyTable.setPrefHeight(220);

        CategoryAxis monthlyX = new CategoryAxis();
        NumberAxis monthlyY = new NumberAxis();
        monthlyChart = new BarChart<>(monthlyX, monthlyY);
        monthlyChart.setPrefHeight(260);
        monthlyChart.setAnimated(false);

        Button monthlySaleExportBtn = new Button("📥 Sale Export (Excel)");
        monthlySaleExportBtn.getStyleClass().add("btn-secondary");
        monthlySaleExportBtn.setOnAction(e -> exportAndOpen(
                Services.ExcelExportService.exportMonthlySales(currentMonthlyPoints), monthlyExportMsg));

        Button monthlyOrdersExportBtn = new Button("📥 Orders Export (Excel)");
        monthlyOrdersExportBtn.getStyleClass().add("btn-secondary");
        monthlyOrdersExportBtn.setOnAction(e -> exportAndOpen(
                Services.ExcelExportService.exportMonthlyOrders(currentMonthlyPoints), monthlyExportMsg));

        monthlyExportMsg = new Label();
        monthlyExportMsg.getStyleClass().add("muted-text");

        HBox monthlyExportBar = new HBox(10, monthlySaleExportBtn, monthlyOrdersExportBtn);
        monthlyExportBar.setAlignment(Pos.CENTER_LEFT);

        VBox monthlySection = new VBox(12, monthlySectionTitle, monthlyTable, monthlyChart,
                monthlyExportBar, monthlyExportMsg);

        // ===== YEARLY SECTION =====
        Label yearlySectionTitle = sectionLabel("📆 Yearly Breakdown (Sale alag, Orders alag)");

        yearlyTable = new TableView<>();
        setupStatsTable(yearlyTable, "Year");
        yearlyTable.setPrefHeight(160);

        CategoryAxis yearlyX = new CategoryAxis();
        NumberAxis yearlyY = new NumberAxis();
        yearlyChart = new BarChart<>(yearlyX, yearlyY);
        yearlyChart.setPrefHeight(260);
        yearlyChart.setAnimated(false);

        Button yearlySaleExportBtn = new Button("📥 Sale Export (Excel)");
        yearlySaleExportBtn.getStyleClass().add("btn-secondary");
        yearlySaleExportBtn.setOnAction(e -> exportAndOpen(
                Services.ExcelExportService.exportYearlySales(currentYearlyPoints), yearlyExportMsg));

        Button yearlyOrdersExportBtn = new Button("📥 Orders Export (Excel)");
        yearlyOrdersExportBtn.getStyleClass().add("btn-secondary");
        yearlyOrdersExportBtn.setOnAction(e -> exportAndOpen(
                Services.ExcelExportService.exportYearlyOrders(currentYearlyPoints), yearlyExportMsg));

        yearlyExportMsg = new Label();
        yearlyExportMsg.getStyleClass().add("muted-text");

        HBox yearlyExportBar = new HBox(10, yearlySaleExportBtn, yearlyOrdersExportBtn);
        yearlyExportBar.setAlignment(Pos.CENTER_LEFT);

        VBox yearlySection = new VBox(12, yearlySectionTitle, yearlyTable, yearlyChart,
                yearlyExportBar, yearlyExportMsg);

        container.getChildren().addAll(title, dailySection, new Separator(),
                monthlySection, new Separator(), yearlySection);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);

        loadDailyStats();
        loadMonthlyBreakdown();
        loadYearlyBreakdown();

        VBox wrapper = new VBox(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return wrapper;
    }

    @SuppressWarnings("unchecked")
    private void setupStatsTable(TableView<SalesStatPoint> tableView, String firstColLabel) {
        TableColumn<SalesStatPoint, String> labelCol = new TableColumn<>(firstColLabel);
        labelCol.setCellValueFactory(new PropertyValueFactory<>("label"));
        labelCol.setPrefWidth(150);

        TableColumn<SalesStatPoint, Double> shopCol = new TableColumn<>("Dukan Ki Sale (Rs.)");
        shopCol.setCellValueFactory(new PropertyValueFactory<>("shopSales"));
        shopCol.setPrefWidth(170);

        TableColumn<SalesStatPoint, Double> ordersCol = new TableColumn<>("Orders (Rs.)");
        ordersCol.setCellValueFactory(new PropertyValueFactory<>("ordersRevenue"));
        ordersCol.setPrefWidth(150);

        TableColumn<SalesStatPoint, Double> totalCol = new TableColumn<>("Total (Rs.)");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalCol.setPrefWidth(150);

        tableView.getColumns().addAll(labelCol, shopCol, ordersCol, totalCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadDailyStats() {
        int month = monthBox.getValue();
        int year = yearBox.getValue();

        List<SalesStatPoint> points = StatsDAO.getDailyBreakdown(year, month);
        dailyTable.setItems(FXCollections.observableArrayList(points));

        double shopTotal = 0, ordersTotal = 0;
        for (SalesStatPoint p : points) {
            shopTotal += p.getShopSales();
            ordersTotal += p.getOrdersRevenue();
        }
        shopTotalLabel.setText("Rs. " + (int) shopTotal);
        ordersTotalLabel.setText("Rs. " + (int) ordersTotal);
        combinedTotalLabel.setText("Rs. " + (int) (shopTotal + ordersTotal));

        populateChart(dailyChart, points);
    }

    private void loadMonthlyBreakdown() {
        List<SalesStatPoint> raw = StatsDAO.getMonthlyTrend(12);
        List<SalesStatPoint> display = withFormattedMonthLabels(raw);
        currentMonthlyPoints = display;
        monthlyTable.setItems(FXCollections.observableArrayList(display));
        populateChart(monthlyChart, display);
    }

    private void loadYearlyBreakdown() {
        List<SalesStatPoint> points = StatsDAO.getYearlyBreakdown(5);
        currentYearlyPoints = points;
        yearlyTable.setItems(FXCollections.observableArrayList(points));
        populateChart(yearlyChart, points);
    }

    private void populateChart(BarChart<String, Number> chart, List<SalesStatPoint> points) {
        chart.getData().clear();
        XYChart.Series<String, Number> shopSeries = new XYChart.Series<>();
        shopSeries.setName("Dukan Ki Sale");
        XYChart.Series<String, Number> ordersSeries = new XYChart.Series<>();
        ordersSeries.setName("Orders");

        for (SalesStatPoint p : points) {
            shopSeries.getData().add(new XYChart.Data<>(p.getLabel(), p.getShopSales()));
            ordersSeries.getData().add(new XYChart.Data<>(p.getLabel(), p.getOrdersRevenue()));
        }
        chart.getData().addAll(shopSeries, ordersSeries);
    }

    // Export ke baad file ko khol deta hai, ya empty data hone par message dikhata hai
    private void exportAndOpen(String filePath, Label msgLabel) {
        if (filePath == null) {
            msgLabel.getStyleClass().setAll("error-text");
            msgLabel.setText("❌ Export nahi ho saka.");
            return;
        }
        msgLabel.getStyleClass().setAll("success-text");
        msgLabel.setText("✅ Export ho gaya!");
        openFile(filePath);
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
            System.out.println("Could not open file: " + ex.getMessage());
        }
    }

    // "2026-06" jaisi raw key ko "June 2026" jaisa readable banata hai (display + export ke liye)
    private List<SalesStatPoint> withFormattedMonthLabels(List<SalesStatPoint> points) {
        List<SalesStatPoint> formatted = new ArrayList<>();
        for (SalesStatPoint p : points) {
            SalesStatPoint copy = new SalesStatPoint();
            copy.setLabel(formatMonthLabel(p.getLabel()));
            copy.setShopSales(p.getShopSales());
            copy.setOrdersRevenue(p.getOrdersRevenue());
            formatted.add(copy);
        }
        return formatted;
    }

    private String formatMonthLabel(String ym) {
        try {
            String[] parts = ym.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            return Month.of(month).getDisplayName(TextStyle.FULL, new Locale("en")) + " " + year;
        } catch (Exception e) {
            return ym;
        }
    }

    private Label sectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        lbl.getStyleClass().add("page-title");
        return lbl;
    }

    private Label summaryValueLabel(String color) {
        Label lbl = new Label();
        lbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        return lbl;
    }

    private VBox summaryCard(String titleText, Label valueLabel) {
        Label titleLbl = new Label(titleText);
        titleLbl.getStyleClass().add("muted-text");
        titleLbl.setStyle("-fx-font-size: 12px;");
        VBox card = new VBox(6, titleLbl, valueLabel);
        card.getStyleClass().add("stat-card");
        card.setPrefWidth(230);
        card.setPadding(new Insets(14, 16, 14, 16));
        return card;
    }
}