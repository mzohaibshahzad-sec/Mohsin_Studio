package Controllers;

import Database.DashboardStatsDAO;
import Database.SalesEntryDAO;
import Models.User;
import Services.BackupService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class DashboardHomeView {

    private User currentUser;
    private Consumer<String> navigationCallback;

    public DashboardHomeView(User currentUser, Consumer<String> navigationCallback) {
        this.currentUser = currentUser;
        this.navigationCallback = navigationCallback;
    }

    public VBox getView() {
        VBox container = new VBox(24);
        container.setPadding(new Insets(28, 32, 28, 32));

        // ===== Header =====
        String dayName  = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE", new Locale("en")));
        String fullDate = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("en")));

        Label welcomeTitle = new Label("Assalam o Alaikum, " + currentUser.getFullName() + " \uD83D\uDC4B");
        welcomeTitle.getStyleClass().add("page-title");

        Label dateLabel = new Label("\uD83D\uDCC5  " + dayName + ", " + fullDate);
        dateLabel.getStyleClass().add("muted-text");

        Label studioLabel = new Label("\uD83C\uDFAC  Mohsin Movies & Photo Studio");
        studioLabel.getStyleClass().add("login-subtitle");

        VBox headerBox = new VBox(6, welcomeTitle, dateLabel, studioLabel);
        headerBox.getStyleClass().add("card");

        // ===== Stats =====
        double monthEarning  = DashboardStatsDAO.getThisMonthEarning();
        double monthExpenses = Database.ExpenseDAO.getThisMonthTotal();
        double netProfit     = monthEarning - monthExpenses;

        FlowPane statsGrid = new FlowPane();
        statsGrid.setHgap(16);
        statsGrid.setVgap(16);

        statsGrid.getChildren().add(statCard("\uD83D\uDCB0", "Today Sales",        "Rs. " + fmt(DashboardStatsDAO.getTodaySalesTotal()), "#27ae60"));
        statsGrid.getChildren().add(statCard("\uD83D\uDCE6", "Total Active Orders", String.valueOf(DashboardStatsDAO.getActiveOrdersCount()), "#d4a574"));
        statsGrid.getChildren().add(statCard("\uD83D\uDC65", "Total Customers",     String.valueOf(DashboardStatsDAO.getTotalCustomersCount()), "#16a085"));

        if (currentUser.isCEO()) {
            statsGrid.getChildren().add(statCard("\uD83D\uDCCB", "Today Orders Amount",   "Rs. " + fmt(DashboardStatsDAO.getTodayOrdersTotal()),           "#2980b9"));
            statsGrid.getChildren().add(statCard("\uD83D\uDCB0", "(Order + Sale) Amount", "Rs. " + fmt(DashboardStatsDAO.getTodayCombinedEarning()),        "#27ae60"));
            statsGrid.getChildren().add(statCard("\uD83D\uDED2", "Monthly Orders",        "Rs. " + fmt(DashboardStatsDAO.getThisMonthOrdersTotal()),        "#8e44ad"));
            statsGrid.getChildren().add(statCard("\uD83D\uDCB5", "Monthly Sales",         "Rs. " + fmt(DashboardStatsDAO.getThisMonthSalesTotal()),         "#16a085"));
            statsGrid.getChildren().add(statCard("\uD83D\uDCC8", "Monthly Combined",      "Rs. " + fmt(DashboardStatsDAO.getThisMonthCombinedEarning()),    "#e67e22"));
            statsGrid.getChildren().add(statCard("\uD83D\uDDD3\uFE0F", "Yearly Orders",   "Rs. " + fmt(DashboardStatsDAO.getThisYearOrdersTotal()),         "#c0392b"));
            statsGrid.getChildren().add(statCard("\uD83D\uDCCA", "Yearly Sales",          "Rs. " + fmt(DashboardStatsDAO.getThisYearSalesTotal()),          "#1abc9c"));
            statsGrid.getChildren().add(statCard("\uD83C\uDFC6", "Yearly Combined",       "Rs. " + fmt(DashboardStatsDAO.getThisYearCombinedEarning()),     "#f39c12"));
            statsGrid.getChildren().add(statCard("\uD83D\uDCB8", "Monthly Expenses",      "Rs. " + fmt(monthExpenses),                                      "#e67e22"));
            statsGrid.getChildren().add(statCard("\uD83C\uDFE6", "Net Profit",
                    "Rs. " + fmt(netProfit), netProfit >= 0 ? "#27ae60" : "#e74c3c"));
            int pending = DashboardStatsDAO.getPendingOrdersCount();
            statsGrid.getChildren().add(statCard("\u23F3", "Pending Approvals",
                    String.valueOf(pending), pending > 0 ? "#e74c3c" : "#95a5a6"));
        }

        // ===== Quick Actions =====
        Label qaTitle = sectionLabel("\u26A1 Quick Actions");

        HBox quickActions = new HBox(12);
        quickActions.setAlignment(Pos.CENTER_LEFT);
        quickActions.getStyleClass().add("card");
        quickActions.setPadding(new Insets(14, 16, 14, 16));

        Button salesBtn = quickBtn("+ New Sales Entry", "#27ae60");
        salesBtn.setOnAction(e -> navigateTo("Sales Entry"));

        Button pkgBtn = quickBtn("\uD83D\uDCE6 View Packages", "#2980b9");
        pkgBtn.setOnAction(e -> navigateTo("Packages"));

        quickActions.getChildren().addAll(salesBtn, pkgBtn);

        if (currentUser.isCoFounder() || currentUser.isCEO()) {
            Button orderBtn = quickBtn("\uD83D\uDCCB Book Order", "#8e44ad");
            orderBtn.setOnAction(e -> navigateTo("Order Booking"));

            Button custQBtn = quickBtn("\uD83D\uDC65 Customers", "#16a085");
            custQBtn.setOnAction(e -> navigateTo("Customers"));

            quickActions.getChildren().addAll(orderBtn, custQBtn);
        }

        if (currentUser.isCEO()) {
            Button pendingBtn = quickBtn("\u23F3 Pending Approvals", "#e74c3c");
            pendingBtn.setOnAction(e -> navigateTo("Pending Approvals"));

            Button statsBtn = quickBtn("\uD83D\uDCCA Sales Stats", "#16a085");
            statsBtn.setOnAction(e -> navigateTo("Sales Stats"));

            Button rptBtn = quickBtn("\uD83D\uDCCA Combined Report", "#e67e22");
            rptBtn.setOnAction(e -> handleCombinedReport());

            quickActions.getChildren().addAll(pendingBtn, statsBtn, rptBtn);
        }

        // ===== Recent Orders =====
        Label recentOrdersTitle = sectionLabel("\uD83D\uDD50 Recent Orders");
        List<String[]> recentOrders = DashboardStatsDAO.getRecentOrders(5);

        VBox ordersBox = new VBox(0);
        ordersBox.getStyleClass().add("card");
        ordersBox.setPadding(new Insets(0));

        HBox orderHeader = tableRow(
                new String[]{"#", "Customer", "Event Date", "Amount", "Status"},
                new double[]{40, 180, 120, 110, 130}, true);
        ordersBox.getChildren().add(orderHeader);

        if (recentOrders.isEmpty()) {
            Label emptyLbl = new Label("Abhi koi orders nahi hain.");
            emptyLbl.getStyleClass().add("muted-text");
            emptyLbl.setPadding(new Insets(12, 10, 12, 10));
            ordersBox.getChildren().add(emptyLbl);
        } else {
            for (int i = 0; i < recentOrders.size(); i++) {
                String[] row = recentOrders.get(i);
                HBox rowBox = tableRow(row, new double[]{40, 180, 120, 110, 130}, false);
                if (i % 2 == 0)
                    rowBox.setStyle("-fx-background-color: rgba(128,128,128,0.05);");
                Label statusLbl = (Label) rowBox.getChildren().get(4);
                styleStatusLabel(statusLbl, row[4]);
                ordersBox.getChildren().add(rowBox);
            }
        }

        // ===== Recent Packages =====
        Label pkgSectionTitle = sectionLabel("\uD83D\uDCE6 Recent Packages");
        List<String[]> recentPkgs = DashboardStatsDAO.getRecentPackages(3);

        HBox pkgsRow = new HBox(14);
        pkgsRow.setAlignment(Pos.CENTER_LEFT);

        if (recentPkgs.isEmpty()) {
            Label emptyPkg = new Label("Koi package nahi bana abhi tak.");
            emptyPkg.getStyleClass().add("muted-text");
            pkgsRow.getChildren().add(emptyPkg);
        } else {
            for (String[] pkg : recentPkgs) {
                pkgsRow.getChildren().add(packageCard(pkg[0], pkg[1], pkg[2]));
            }
        }

        container.getChildren().addAll(headerBox, statsGrid, qaTitle, quickActions,
                recentOrdersTitle, ordersBox, pkgSectionTitle, pkgsRow);

        if (currentUser.isCEO()) {
            Button combinedReportBtn = new Button("\uD83D\uDCCA Combined Daily Report (Sab Clerks)");
            combinedReportBtn.getStyleClass().add("btn-primary");
            combinedReportBtn.setOnAction(e -> handleCombinedReport());
            HBox btnBox = new HBox(combinedReportBtn);
            btnBox.setPadding(new Insets(4, 0, 0, 0));
            container.getChildren().add(btnBox);
        }

        return container;
    }

    private void navigateTo(String section) {
        if (navigationCallback != null) navigationCallback.accept(section);
    }

    private Label sectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("section-title");
        VBox.setMargin(lbl, new Insets(6, 0, 0, 0));
        return lbl;
    }

    private VBox statCard(String icon, String label, String value, String color) {
        Label iconLbl  = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 22px;");

        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label nameLbl  = new Label(label);
        nameLbl.getStyleClass().add("muted-text");

        Region accent = new Region();
        accent.setPrefWidth(4);
        accent.setPrefHeight(60);
        accent.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 2;");

        VBox textBox = new VBox(4, iconLbl, valueLbl, nameLbl);
        textBox.setAlignment(Pos.CENTER_LEFT);

        HBox inner = new HBox(12, accent, textBox);
        inner.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(inner);
        card.getStyleClass().add("stat-card");
        card.setPrefWidth(200);
        card.setPrefHeight(100);
        card.setPadding(new Insets(14, 16, 14, 12));
        return card;
    }

    private Button quickBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "22; -fx-text-fill: " + color + "; " +
                "-fx-font-size: 12px; -fx-font-weight: bold; " +
                "-fx-padding: 9 18 9 18; -fx-background-radius: 8; -fx-cursor: hand; " +
                "-fx-border-color: " + color + "55; -fx-border-radius: 8; -fx-border-width: 1;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + color + "; -fx-text-fill: white; " +
                        "-fx-font-size: 12px; -fx-font-weight: bold; " +
                        "-fx-padding: 9 18 9 18; -fx-background-radius: 8; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + color + "22; -fx-text-fill: " + color + "; " +
                        "-fx-font-size: 12px; -fx-font-weight: bold; " +
                        "-fx-padding: 9 18 9 18; -fx-background-radius: 8; -fx-cursor: hand; " +
                        "-fx-border-color: " + color + "55; -fx-border-radius: 8; -fx-border-width: 1;"));
        return btn;
    }

    private HBox tableRow(String[] cols, double[] widths, boolean isHeader) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        for (int i = 0; i < cols.length; i++) {
            Label cell = new Label(cols[i]);
            cell.setPrefWidth(widths[i]);
            cell.setWrapText(false);
            if (isHeader) {
                cell.getStyleClass().add("table-header-cell");
            } else {
                cell.getStyleClass().add("muted-text");
                cell.setStyle("-fx-font-size: 12px;");
            }
            row.getChildren().add(cell);
        }
        if (isHeader) {
            row.getStyleClass().add("table-header-row");
        }
        return row;
    }

    private VBox packageCard(String name, String price, String discount) {
        Label nameLbl  = new Label(name);
        nameLbl.getStyleClass().add("section-title");
        nameLbl.setWrapText(true);

        Label priceLbl = new Label(price);
        priceLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        VBox card = new VBox(8, nameLbl, priceLbl);
        if (discount != null && !discount.isEmpty()) {
            Label discLbl = new Label(discount);
            discLbl.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 2 8 2 8; -fx-background-radius: 10; -fx-font-size: 11px;");
            card.getChildren().add(discLbl);
        }
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setPrefWidth(190);
        return card;
    }

    private void styleStatusLabel(Label lbl, String status) {
        String color;
        switch (status) {
            case "PENDING_APPROVAL": color = "#e67e22"; break;
            case "CONFIRMED":        color = "#2980b9"; break;
            case "IN_PROGRESS":      color = "#8e44ad"; break;
            case "READY":            color = "#27ae60"; break;
            case "DELIVERED":        color = "#95a5a6"; break;
            case "REJECTED":         color = "#e74c3c"; break;
            default:                 color = "#95a5a6"; break;
        }
        lbl.setText(status.replace("_", " "));
        lbl.setStyle("-fx-background-color: " + color + "22; -fx-text-fill: " + color + "; " +
                "-fx-padding: 3 10 3 10; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold; " +
                "-fx-border-color: " + color + "55; -fx-border-radius: 12; -fx-border-width: 1;");
    }

    private void handleCombinedReport() {
        new Thread(() -> {
            try {
                List<Models.SalesEntry> entries = SalesEntryDAO.getAllTodayEntries();
                if (entries.isEmpty()) {
                    javafx.application.Platform.runLater(() -> showAlert("Aaj ki koi entry nahi mili."));
                    return;
                }
                String filePath = BackupService.backupTodaySales(entries);
                if (filePath == null) {
                    javafx.application.Platform.runLater(() -> showAlert("Report banane mein masla aaya."));
                    return;
                }
                boolean emailSent = Services.EmailService.sendEmailWithAttachment(
                        "mohsinmoviesandphotostudio@gmail.com",
                        "Combined Daily Sales Report - " + LocalDate.now(),
                        "Assalam o Alaikum,\n\nAaj ki sab clerks ki combined report attached hai.\n\nShukria,\nMohsin Studio System",
                        filePath);
                final String fp = filePath;
                final boolean ok = emailSent;
                javafx.application.Platform.runLater(() -> {
                    showAlert("Report ban gayi!\nPath: " + fp +
                            (ok ? "\n\nEmail bhi CEO ko bhej di!" : "\n\nEmail nahi ja saki."));
                    openFile(fp);
                });
            } catch (Exception e) {
                final String err = e.getMessage();
                javafx.application.Platform.runLater(() -> showAlert("Error: " + err));
            }
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
            showAlert("File ban gayi lekin khul nahi saki.\nPath: " + filePath);
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private String fmt(double val) {
        return String.format("%.0f", val);
    }
}