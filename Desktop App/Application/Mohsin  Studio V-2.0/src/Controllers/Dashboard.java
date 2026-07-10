package Controllers;

import Models.User;
import Utils.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dashboard {

    private Stage stage;
    private User currentUser;
    private ScrollPane contentWrapper;
    private Scene scene;
    private List<Button> sidebarButtons = new ArrayList<>();
    private Button themeToggleBtn;
    private Map<String, Button> sectionButtonMap = new HashMap<>();

    public Dashboard(Stage stage, User currentUser) {
        this.stage = stage;
        this.currentUser = currentUser;
    }

    public void show() {
        // ===== Top Bar =====
        Label welcomeLabel = new Label("Khush aamdeed, " + currentUser.getFullName() + " (" + roleDisplayName() + ")");
        welcomeLabel.getStyleClass().add("top-bar-title");

        themeToggleBtn = new Button(ThemeManager.isDarkMode() ? "Light Mode" : "Dark Mode");
        themeToggleBtn.getStyleClass().add("theme-toggle-btn");
        themeToggleBtn.setOnAction(e -> {
            ThemeManager.toggleTheme(scene);
            themeToggleBtn.setText(ThemeManager.isDarkMode() ? "Light Mode" : "Dark Mode");
        });

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("btn-danger");
        logoutButton.setOnAction(e -> new LoginScreen(stage).show());

        HBox topBar = new HBox(15);
        topBar.getStyleClass().add("top-bar");
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.getChildren().addAll(welcomeLabel, spacer, themeToggleBtn, logoutButton);

        // ===== Sidebar =====
        VBox sidebar = new VBox(6);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(20, 14, 20, 14));
        sidebar.setPrefWidth(230);

        Label menuTitle = new Label("MENU");
        menuTitle.getStyleClass().add("sidebar-title");
        VBox.setMargin(menuTitle, new Insets(0, 0, 8, 10));
        sidebar.getChildren().add(menuTitle);

        contentWrapper = new ScrollPane();
        contentWrapper.setFitToWidth(true);
        contentWrapper.getStyleClass().add("scroll-pane");
        HBox.setHgrow(contentWrapper, Priority.ALWAYS);

        // Dashboard
        Button dashBtn = menuButton("Dashboard", () ->
                showContent(new DashboardHomeView(currentUser, this::navigateTo).getView()));
        sidebar.getChildren().add(dashBtn);
        sectionButtonMap.put("Dashboard", dashBtn);

        // Sales Entry
        Button salesBtn = menuButton("Sales Entry", () ->
                showContent(new SalesEntryView(currentUser).getView()));
        sidebar.getChildren().add(salesBtn);
        sectionButtonMap.put("Sales Entry", salesBtn);

        // Packages
        Button pkgBtn = menuButton("Packages", () ->
                showContent(new PackagesView(currentUser).getView()));
        sidebar.getChildren().add(pkgBtn);
        sectionButtonMap.put("Packages", pkgBtn);

        // ===== Calculator - SAB ROLES KO DIKHEGA =====
        Button calcBtn = menuButton("🧮 Calculator", () ->
                showContent(new CalculatorView().getView()));
        sidebar.getChildren().add(calcBtn);
        sectionButtonMap.put("Calculator", calcBtn);

        // CoFounder + CEO
        if (currentUser.isCoFounder() || currentUser.isCEO()) {
            Button orderBookBtn = menuButton("Order Booking", () ->
                    showContent(new OrderBookingView(currentUser).getView()));
            sidebar.getChildren().add(orderBookBtn);
            sectionButtonMap.put("Order Booking", orderBookBtn);

            Button manageOrderBtn = menuButton("Manage Orders", () ->
                    showContent(new OrderManagementView(currentUser).getView()));
            sidebar.getChildren().add(manageOrderBtn);
            sectionButtonMap.put("Manage Orders", manageOrderBtn);

            Button custBtn = menuButton("Customers", () ->
                    showContent(new CustomersView(currentUser).getView()));
            sidebar.getChildren().add(custBtn);
            sectionButtonMap.put("Customers", custBtn);
        }

        if (currentUser.isAdminOrCEO()) {
            Button pendingApprovalsBtn = menuButton("Pending Approvals", () ->
                    showContent(new PendingApprovalsView(currentUser).getView()));
            sidebar.getChildren().add(pendingApprovalsBtn);
            sectionButtonMap.put("Pending Approvals", pendingApprovalsBtn);

            Button statsBtn = menuButton("Sales Stats", () ->
                    showContent(new StatsView(currentUser).getView()));
            sidebar.getChildren().add(statsBtn);
            sectionButtonMap.put("Sales Stats", statsBtn);

            Button usersBtn = menuButton("Manage Users", () ->
                    showContent(new ManageUsersView(currentUser).getView()));
            sidebar.getChildren().add(usersBtn);
            sectionButtonMap.put("Manage Users", usersBtn);

            Button expBtn = menuButton("Expenses", () ->
                    showContent(new ExpensesView(currentUser).getView()));
            sidebar.getChildren().add(expBtn);
            sectionButtonMap.put("Expenses", expBtn);

            Button auditBtn = menuButton("Audit Logs", () ->
                    showContent(new AuditLogsView().getView()));
            sidebar.getChildren().add(auditBtn);
            sectionButtonMap.put("Audit Logs", auditBtn);

            Button categoriesBtn = menuButton("Manage Categories", () ->
                    showContent(new CategoryManagementView().getView()));
            sidebar.getChildren().add(categoriesBtn);
            sectionButtonMap.put("Manage Categories", categoriesBtn);
        }

        // ===== Profile - neeche =====
        Region sidebarSpacer = new Region();
        VBox.setVgrow(sidebarSpacer, Priority.ALWAYS);

        Separator profileSep = new Separator();
        profileSep.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(profileSep, new Insets(6, 0, 6, 0));

        Button profileBtn = menuButton("👤 My Profile", () ->
                showContent(new ProfileView(currentUser).getView()));

        sidebar.getChildren().addAll(sidebarSpacer, profileSep, profileBtn);

        // ===== Default Content =====
        contentWrapper.setContent(new DashboardHomeView(currentUser, this::navigateTo).getView());

        if (!sidebarButtons.isEmpty()) {
            sidebarButtons.get(0).getStyleClass().add("sidebar-button-active");
        }

        HBox mainLayout = new HBox(sidebar, contentWrapper);
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(mainLayout);

        scene = new Scene(root, 1050, 680);
        ThemeManager.applyTheme(scene);

        stage.setTitle("Mohsin Studio - Dashboard");
        stage.setScene(scene);
        stage.show();

        root.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(350), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    public void navigateTo(String sectionName) {
        Button target = sectionButtonMap.get(sectionName);
        if (target != null) {
            target.fire();
        }
    }

    private void showContent(javafx.scene.Node view) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(120), contentWrapper.getContent());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            contentWrapper.setContent(view);
            view.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(220), view);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private Button menuButton(String text, Runnable onClick) {
        Button btn = new Button(text);
        btn.getStyleClass().add("sidebar-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> {
            for (Button b : sidebarButtons) {
                b.getStyleClass().remove("sidebar-button-active");
            }
            btn.getStyleClass().add("sidebar-button-active");
            onClick.run();
        });
        sidebarButtons.add(btn);
        return btn;
    }

    private String roleDisplayName() {
        switch (currentUser.getRole()) {
            case "CEO": return "CEO";
            case "CO_FOUNDER": return "Co-Founder";
            case "CLERK": return "Clerk";
            default: return currentUser.getRole();
        }
    }
}