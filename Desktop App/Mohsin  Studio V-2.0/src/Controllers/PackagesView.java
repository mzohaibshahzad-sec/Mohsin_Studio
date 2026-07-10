package Controllers;

import Database.CustomerDAO;
import Database.OrderDAO;
import Database.PackageDAO;
import Models.Customer;
import Models.Order;
import Models.Package;
import Models.User;
import Services.EmailService;
import Services.PosterGenerator;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PackagesView {

    private User currentUser;
    private TableView<Package> table;

    private static final String[] SERVICES = {
            "Photography", "Videography", "Drone Movie", "Sound System", "Photo Album", "Short Video"
    };

    public PackagesView(User currentUser) {
        this.currentUser = currentUser;
    }

    public VBox getView() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        VBox.setVgrow(container, Priority.ALWAYS);

        Label title = new Label("Packages");
        title.getStyleClass().add("page-title");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        tabPane.setMaxHeight(Double.MAX_VALUE);

        Tab packagesTab = new Tab("Packages List");
        packagesTab.setContent(buildPackagesTable());
        tabPane.getTabs().add(packagesTab);

        if (currentUser.isCEO() || currentUser.isCoFounder()) {
            Tab customTab = new Tab("Custom Package Banao");
            customTab.setContent(buildCustomPackageCreator());
            tabPane.getTabs().add(customTab);
        }

        container.getChildren().addAll(title, tabPane);
        return container;
    }

    // =================== TAB 1: PACKAGES TABLE ===================
    // =================== TAB 1: PACKAGES TABLE ===================
    private VBox buildPackagesTable() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(12));
        VBox.setVgrow(box, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("Package naam se search karein...");
        searchField.setPrefWidth(260);

        HBox searchBar = new HBox(10, new Label("Search:"), searchField);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(0, 0, 4, 0));

        Button bookOrderBtn = new Button("\uD83D\uDCCB Order Banao");
        bookOrderBtn.getStyleClass().add("btn-success");
        bookOrderBtn.setOnAction(e -> {
            Package selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) openOrderFromPackage(selected);
            else showAlert("Pehle koi package select karein.");
        });

        HBox buttonBar = new HBox(8, bookOrderBtn);
        buttonBar.setAlignment(Pos.CENTER_LEFT);
        buttonBar.setPadding(new Insets(4, 0, 4, 0));

        if (currentUser.isCEO() || currentUser.isCoFounder()) {
            Button addBtn = new Button("+ Naya Package");
            addBtn.getStyleClass().add("btn-primary");
            addBtn.setOnAction(e -> openPackageForm(null));

            Button editBtn = new Button("\u270F Edit");
            editBtn.getStyleClass().add("btn-secondary");
            editBtn.setOnAction(e -> {
                Package selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) openPackageForm(selected);
                else showAlert("Pehle koi package select karein.");
            });

            Button deleteBtn = new Button("\uD83D\uDDD1 Delete");
            deleteBtn.getStyleClass().add("btn-danger");
            deleteBtn.setOnAction(e -> {
                Package selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Kya aap '" + selected.getPackageName() + "' delete karna chahte hain?");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            PackageDAO.deletePackage(selected.getId());
                            Database.AuditLogDAO.log(currentUser.getId(), "DELETED_PACKAGE", "packages",
                                    selected.getId(), "Package deleted: " + selected.getPackageName());
                            loadPackages();
                        }
                    });
                } else showAlert("Pehle koi package select karein.");
            });

            Button previewBtn = new Button("\uD83D\uDC41 Poster Preview");
            previewBtn.getStyleClass().add("btn-secondary");
            previewBtn.setOnAction(e -> {
                Package selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) showPosterPreview(selected, "", "", 1);
                else showAlert("Pehle koi package select karein.");
            });

            buttonBar.getChildren().addAll(addBtn, editBtn, deleteBtn, previewBtn);
        }

        table = new TableView<>();
        setupTable();
        loadPackages();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(table, Priority.ALWAYS);

        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                loadPackages();
            } else {
                List<Package> all = PackageDAO.getAllPackages();
                List<Package> filtered = new ArrayList<>();
                for (Package p : all) {
                    if (p.getPackageName().toLowerCase().contains(newVal.toLowerCase()) ||
                            (p.getCategory() != null && p.getCategory().toLowerCase().contains(newVal.toLowerCase())) ||
                            (p.getServices() != null && p.getServices().toLowerCase().contains(newVal.toLowerCase()))) {
                        filtered.add(p);
                    }
                }
                table.setItems(FXCollections.observableArrayList(filtered));
            }
        });

        box.getChildren().addAll(searchBar, buttonBar, table);
        return box;
    }


    @SuppressWarnings("unchecked")
    private void setupTable() {
        TableColumn<Package, String> nameCol = new TableColumn<>("Package Naam");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("packageName"));
        nameCol.setPrefWidth(180);

        TableColumn<Package, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(110);

        TableColumn<Package, String> servicesCol = new TableColumn<>("Services");
        servicesCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getServiceNamesOnly()));
        servicesCol.setPrefWidth(220);

        TableColumn<Package, String> priceCol = new TableColumn<>("Price (Rs.)");
        priceCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(String.format("%.0f", data.getValue().getPrice())));
        priceCol.setPrefWidth(100);

        TableColumn<Package, String> discountCol = new TableColumn<>("Discount %");
        discountCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getDiscount() > 0 ? data.getValue().getDiscount() + "%" : "-"));
        discountCol.setPrefWidth(90);

        TableColumn<Package, String> finalPriceCol = new TableColumn<>("Final Price (Rs.)");
        finalPriceCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(String.format("%.0f", data.getValue().getFinalPrice())));
        finalPriceCol.setPrefWidth(120);

        TableColumn<Package, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(200);

        table.getColumns().addAll(nameCol, categoryCol, servicesCol, priceCol, discountCol, finalPriceCol, descCol);
    }

    private void loadPackages() {
        table.setItems(FXCollections.observableArrayList(PackageDAO.getAllPackages()));
    }

    // =================== PACKAGE FORM (CEO + CoFounder) ===================
    private void openPackageForm(Package existingPackage) {
        Stage formStage = new Stage();
        formStage.initModality(Modality.APPLICATION_MODAL);
        formStage.setTitle(existingPackage == null ? "Naya Package Banao" : "Package Edit Karein");

        TextField nameField = new TextField();
        nameField.setPromptText("Package naam");

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(Database.CategoryDAO.getNamesByType("PACKAGE"));
        categoryBox.setPromptText("Category Chunein");
        categoryBox.setMaxWidth(Double.MAX_VALUE);

        TextArea descField = new TextArea();
        descField.setPromptText("Package description");
        descField.setPrefRowCount(3);

        CheckBox[] checkBoxes = new CheckBox[SERVICES.length];
        TextField[] perDayFields = new TextField[SERVICES.length];
        TextField[] daysFields = new TextField[SERVICES.length];
        Label[] lineTotalLabels = new Label[SERVICES.length];
        VBox servicesBox = new VBox(8);

        TextField discountField = new TextField("0");
        discountField.setPromptText("Discount %");

        Label totalLabel = new Label("Total: Rs. 0");
        totalLabel.setStyle("-fx-font-size:13px; -fx-font-weight:bold;");

        Label finalPriceLabel = new Label("Final Price: Rs. 0");
        finalPriceLabel.setStyle("-fx-font-weight:bold; -fx-text-fill:#27ae60; -fx-font-size:15px;");

        Runnable updateCalc = () -> {
            double total = 0;
            for (int i = 0; i < SERVICES.length; i++) {
                if (checkBoxes[i].isSelected()) {
                    try {
                        double pd = Double.parseDouble(perDayFields[i].getText().trim());
                        int svcDays = 1;
                        try { svcDays = Integer.parseInt(daysFields[i].getText().trim()); } catch (Exception ignored) {}
                        double lt = pd * svcDays;
                        total += lt;
                        lineTotalLabels[i].setText("= Rs. " + String.format("%.0f", lt));
                    } catch (Exception ignored) {
                        lineTotalLabels[i].setText("= Rs. 0");
                    }
                } else if (lineTotalLabels[i] != null) {
                    lineTotalLabels[i].setText("");
                }
            }
            totalLabel.setText("Total: Rs. " + String.format("%.0f", total));
            try {
                double disc = Double.parseDouble(discountField.getText().trim());
                double finalP = total - (total * disc / 100.0);
                finalPriceLabel.setText("Final Price: Rs. " + String.format("%.0f", finalP)
                        + (disc > 0 ? " (" + (int)disc + "% off)" : ""));
            } catch (Exception ignored) {
                finalPriceLabel.setText("Final Price: Rs. " + String.format("%.0f", total));
            }
        };

        for (int i = 0; i < SERVICES.length; i++) {
            CheckBox cb = new CheckBox(SERVICES[i]);
            cb.setStyle("-fx-font-size:13px;");
            checkBoxes[i] = cb;

            TextField pf = new TextField();
            pf.setPromptText("Per day price (Rs.)");
            pf.setPrefWidth(140);
            pf.setDisable(true);
            perDayFields[i] = pf;

            TextField df = new TextField("1");
            df.setPromptText("Din");
            df.setPrefWidth(55);
            df.setDisable(true);
            daysFields[i] = df;

            Label lt = new Label("");
            lt.setStyle("-fx-text-fill:#d4a574; -fx-font-weight:bold; -fx-font-size:12px;");
            lineTotalLabels[i] = lt;

            cb.selectedProperty().addListener((obs, was, now) -> {
                pf.setDisable(!now);
                df.setDisable(!now);
                if (!now) { pf.clear(); df.setText("1"); lt.setText(""); }
                updateCalc.run();
            });
            pf.textProperty().addListener((obs, was, now) -> updateCalc.run());
            df.textProperty().addListener((obs, was, now) -> updateCalc.run());

            HBox row = new HBox(10, cb, pf, new Label("x"), df, new Label("din"), lt);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(5, 8, 5, 8));
            servicesBox.getChildren().add(row);
        }

        discountField.textProperty().addListener((o, w, n) -> updateCalc.run());

        if (existingPackage != null) {
            nameField.setText(existingPackage.getPackageName());
            descField.setText(existingPackage.getDescription() != null ? existingPackage.getDescription() : "");
            discountField.setText(String.valueOf(existingPackage.getDiscount()));
            categoryBox.setValue(existingPackage.getCategory());
            List<String[]> parsed = existingPackage.getParsedServices();
            for (String[] svc : parsed) {
                for (int i = 0; i < SERVICES.length; i++) {
                    if (svc[0].equalsIgnoreCase(SERVICES[i])) {
                        checkBoxes[i].setSelected(true);
                        if (svc.length > 1 && !svc[1].isEmpty()) perDayFields[i].setText(svc[1]);
                        if (svc.length > 2 && !svc[2].isEmpty()) daysFields[i].setText(svc[2]);
                    }
                }
            }
            updateCalc.run();
        }

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-text");
        errorLabel.setWrapText(true);

        Button saveBtn = new Button("Package Save Karein");
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String category = categoryBox.getValue();
            if (name.isEmpty() || category == null) { errorLabel.setText("Naam aur Category zaroori hain."); return; }

            List<String> svcEntries = new ArrayList<>();
            double total = 0;
            for (int i = 0; i < SERVICES.length; i++) {
                if (checkBoxes[i].isSelected()) {
                    String pd = perDayFields[i].getText().trim();
                    int svcDays = 1;
                    try { svcDays = Integer.parseInt(daysFields[i].getText().trim()); } catch (Exception ignored) {}
                    if (!pd.isEmpty()) {
                        svcEntries.add(SERVICES[i] + ":" + pd + ":" + svcDays);
                        try { total += Double.parseDouble(pd) * svcDays; } catch (Exception ignored) {}
                    } else {
                        svcEntries.add(SERVICES[i]);
                    }
                }
            }
            if (svcEntries.isEmpty()) { errorLabel.setText("Kam az kam ek service select karein."); return; }

            double discount = 0;
            try { discount = Double.parseDouble(discountField.getText().trim()); } catch (Exception ignored) {}

            Package pkg = existingPackage != null ? existingPackage : new Package();
            pkg.setPackageName(name);
            pkg.setDescription(descField.getText().trim());
            pkg.setPrice(total);
            pkg.setDiscount(discount);
            pkg.setCategory(category);
            pkg.setServices(String.join(",", svcEntries));
            if (existingPackage != null) pkg.setActive(existingPackage.isActive());

            boolean isNew = (existingPackage == null);
            boolean success = isNew
                    ? PackageDAO.addPackage(pkg, currentUser.getId())
                    : PackageDAO.updatePackage(pkg);

            if (success) {
                Database.AuditLogDAO.log(currentUser.getId(), isNew ? "CREATED_PACKAGE" : "UPDATED_PACKAGE",
                        "packages", pkg.getId(), "Package: " + name + " - Rs. " + total + " (" + category + ")");
                loadPackages();
                formStage.close();
            } else {
                errorLabel.setText("Save nahi ho saka.");
            }
        });

        VBox formLayout = new VBox(10,
                new Label("Package Naam:"), nameField,
                new Label("Category:"), categoryBox,
                new Label("Description:"), descField,
                new Separator(),
                new Label("Services, Per Day Price aur Din:"), servicesBox,
                new Separator(),
                totalLabel,
                new Label("Discount (%):"), discountField,
                finalPriceLabel,
                new Separator(),
                saveBtn, errorLabel
        );
        formLayout.setPadding(new Insets(20));
        ScrollPane scroll = new ScrollPane(formLayout);
        scroll.setFitToWidth(true);
        Scene formScene = new Scene(scroll, 420, 620);
        Utils.ThemeManager.applyTheme(formScene);
        formStage.setScene(formScene);
        formStage.showAndWait();
    }

    // =================== TAB 2: CUSTOM PACKAGE CREATOR ===================
    private ScrollPane buildCustomPackageCreator() {
        VBox main = new VBox(18);
        main.setPadding(new Insets(24));

        Label heading = new Label("Custom Package Banao");
        heading.getStyleClass().add("page-title");

        Label subHeading = new Label("Services chunein, har service ki per day price enter karein, days batao — total auto calculate hoga.");
        subHeading.getStyleClass().add("muted-text");
        subHeading.setWrapText(true);

        TextField nameField = new TextField();
        nameField.setPromptText("Custom package naam");

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(Database.CategoryDAO.getNamesByType("PACKAGE"));
        categoryBox.setPromptText("Category Chunein");
        categoryBox.setMaxWidth(Double.MAX_VALUE);

        TextArea descField = new TextArea();
        descField.setPromptText("Description (optional)");
        descField.setPrefRowCount(2);

        Label servicesTitle = new Label("Services, Per Day Price aur Din:");
        servicesTitle.setStyle("-fx-font-weight:bold; -fx-font-size:13px;");

        CheckBox[] checkBoxes = new CheckBox[SERVICES.length];
        TextField[] perDayFields = new TextField[SERVICES.length];
        TextField[] daysFields = new TextField[SERVICES.length];
        Label[] lineTotalLabels = new Label[SERVICES.length];
        VBox servicesBox = new VBox(8);

        Label totalLabel = new Label("Total: Rs. 0");
        totalLabel.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");

        TextField discountField = new TextField("0");
        discountField.setPromptText("Discount %");
        discountField.setPrefWidth(100);

        Label finalPriceLabel = new Label("Final Price: Rs. 0");
        finalPriceLabel.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:#27ae60;");

        Runnable updateTotals = () -> {
            double total = 0;
            for (int i = 0; i < SERVICES.length; i++) {
                if (checkBoxes[i] != null && checkBoxes[i].isSelected()) {
                    try {
                        double pd = Double.parseDouble(perDayFields[i].getText().trim());
                        int svcDays = 1;
                        try { svcDays = Integer.parseInt(daysFields[i].getText().trim()); } catch (Exception ignored) {}
                        double lt = pd * svcDays;
                        total += lt;
                        lineTotalLabels[i].setText("= Rs. " + String.format("%.0f", lt));
                    } catch (Exception ignored) {
                        lineTotalLabels[i].setText("= Rs. 0");
                    }
                } else if (lineTotalLabels[i] != null) {
                    lineTotalLabels[i].setText("");
                }
            }
            totalLabel.setText("Sub Total: Rs. " + String.format("%.0f", total));
            try {
                double disc = Double.parseDouble(discountField.getText().trim());
                double finalP = total - (total * disc / 100.0);
                finalPriceLabel.setText("Final Price: Rs. " + String.format("%.0f", finalP)
                        + (disc > 0 ? "  (" + (int)disc + "% off)" : ""));
            } catch (Exception ignored) {
                finalPriceLabel.setText("Final Price: Rs. " + String.format("%.0f", total));
            }
        };

        for (int i = 0; i < SERVICES.length; i++) {
            CheckBox cb = new CheckBox(SERVICES[i]);
            cb.setStyle("-fx-font-size:13px; -fx-font-weight:bold;");
            checkBoxes[i] = cb;

            TextField pf = new TextField();
            pf.setPromptText("Per day price (Rs.)");
            pf.setPrefWidth(140);
            pf.setDisable(true);
            perDayFields[i] = pf;

            TextField df = new TextField("1");
            df.setPromptText("Din");
            df.setPrefWidth(55);
            df.setDisable(true);
            daysFields[i] = df;

            Label lt = new Label("");
            lt.setStyle("-fx-text-fill:#d4a574; -fx-font-weight:bold; -fx-font-size:12px;");
            lineTotalLabels[i] = lt;

            cb.selectedProperty().addListener((obs, was, now) -> {
                pf.setDisable(!now);
                df.setDisable(!now);
                if (!now) { pf.clear(); df.setText("1"); lt.setText(""); }
                updateTotals.run();
            });
            pf.textProperty().addListener((obs, was, now) -> updateTotals.run());
            df.textProperty().addListener((obs, was, now) -> updateTotals.run());

            HBox row = new HBox(10, cb, pf, new Label("x"), df, new Label("din"), lt);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(7, 10, 7, 10));
            row.setStyle("-fx-background-color:rgba(255,255,255,0.03); -fx-background-radius:6;");
            servicesBox.getChildren().add(row);
        }

        discountField.textProperty().addListener((o, w, n) -> updateTotals.run());

        Label errorLabel = new Label();
        errorLabel.setWrapText(true);

        Button previewBtn = new Button("👁 Poster Preview");
        previewBtn.getStyleClass().add("btn-secondary");
        previewBtn.setOnAction(e -> {
            Package temp = buildTempPackage(checkBoxes, perDayFields, daysFields, discountField, nameField.getText().trim());
            showPosterPreview(temp, "", "", 1);
        });

        Button saveBtn = new Button("✅ Package Save Karein");
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.setStyle("-fx-font-size:13px; -fx-padding:10 20 10 20;");

        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String category = categoryBox.getValue();
            if (name.isEmpty()) { errorLabel.getStyleClass().setAll("error-text"); errorLabel.setText("Package naam zaroori hai."); return; }
            if (category == null) { errorLabel.getStyleClass().setAll("error-text"); errorLabel.setText("Category select karein."); return; }

            List<String> svcEntries = new ArrayList<>();
            double total = 0;
            for (int i = 0; i < SERVICES.length; i++) {
                if (checkBoxes[i].isSelected()) {
                    String pd = perDayFields[i].getText().trim();
                    int svcDays = 1;
                    try { svcDays = Integer.parseInt(daysFields[i].getText().trim()); } catch (Exception ignored) {}
                    if (!pd.isEmpty()) {
                        svcEntries.add(SERVICES[i] + ":" + pd + ":" + svcDays);
                        try { total += Double.parseDouble(pd) * svcDays; } catch (Exception ignored) {}
                    } else {
                        svcEntries.add(SERVICES[i]);
                    }
                }
            }

            if (svcEntries.isEmpty()) { errorLabel.getStyleClass().setAll("error-text"); errorLabel.setText("Kam az kam ek service select karein."); return; }
            if (total <= 0) { errorLabel.getStyleClass().setAll("error-text"); errorLabel.setText("Services ki prices fill karein."); return; }

            double discount = 0;
            try { discount = Double.parseDouble(discountField.getText().trim()); } catch (Exception ignored) {}

            Package pkg = new Package();
            pkg.setPackageName(name);
            pkg.setDescription(descField.getText().trim());
            pkg.setPrice(total);
            pkg.setDiscount(discount);
            pkg.setCategory(category);
            pkg.setServices(String.join(",", svcEntries));

            boolean success = PackageDAO.addPackage(pkg, currentUser.getId());
            if (success) {
                errorLabel.getStyleClass().setAll("success-text");
                errorLabel.setText("Package '" + name + "' save ho gaya!");
                nameField.clear(); categoryBox.setValue(null); descField.clear(); discountField.setText("0");
                for (int i = 0; i < SERVICES.length; i++) {
                    checkBoxes[i].setSelected(false); perDayFields[i].clear(); daysFields[i].setText("1"); lineTotalLabels[i].setText("");
                }
                totalLabel.setText("Sub Total: Rs. 0"); finalPriceLabel.setText("Final Price: Rs. 0");
                if (table != null) loadPackages();
            } else {
                errorLabel.getStyleClass().setAll("error-text");
                errorLabel.setText("Save nahi ho saka.");
            }
        });

        HBox discountRow = new HBox(12, new Label("Discount (%):"), discountField);
        discountRow.setAlignment(Pos.CENTER_LEFT);

        HBox btnRow = new HBox(12, previewBtn, saveBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        main.getChildren().addAll(
                heading, subHeading, new Separator(),
                new Label("Package Naam:"), nameField,
                new Label("Category:"), categoryBox,
                new Label("Description (optional):"), descField,
                new Separator(),
                servicesTitle, servicesBox,
                new Separator(),
                totalLabel, discountRow, finalPriceLabel,
                new Separator(),
                btnRow, errorLabel
        );

        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        return scroll;
    }

    private Package buildTempPackage(CheckBox[] cbs, TextField[] pfs, TextField[] dfs, TextField discountField, String name) {
        Package p = new Package();
        p.setPackageName(name.isEmpty() ? "Custom Package" : name);
        double total = 0;
        List<String> entries = new ArrayList<>();
        for (int i = 0; i < SERVICES.length; i++) {
            if (cbs[i].isSelected()) {
                String pd = pfs[i].getText().trim();
                int svcDays = 1;
                try { svcDays = Integer.parseInt(dfs[i].getText().trim()); } catch (Exception ignored) {}
                if (!pd.isEmpty()) {
                    entries.add(SERVICES[i] + ":" + pd + ":" + svcDays);
                    try { total += Double.parseDouble(pd) * svcDays; } catch (Exception ignored) {}
                } else {
                    entries.add(SERVICES[i]);
                }
            }
        }
        double discount = 0;
        try { discount = Double.parseDouble(discountField.getText().trim()); } catch (Exception ignored) {}
        p.setServices(String.join(",", entries));
        p.setPrice(total);
        p.setDiscount(discount);
        p.setCategory("OTHER");
        return p;
    }

    // =================== ORDER FROM PACKAGE ===================
    private void openOrderFromPackage(Package pkg) {
        Stage orderStage = new Stage();
        orderStage.initModality(Modality.APPLICATION_MODAL);
        orderStage.setTitle("Order Banao - " + pkg.getPackageName());

        TextField nameField = new TextField(); nameField.setPromptText("Customer naam");
        TextField phoneField = new TextField(); phoneField.setPromptText("03001234567");
        TextField emailField = new TextField(); emailField.setPromptText("email");
        TextField addressField = new TextField(); addressField.setPromptText("Address");
        Label autoFillLabel = new Label(); autoFillLabel.getStyleClass().add("success-text");

        phoneField.focusedProperty().addListener((obs, was, now) -> {
            if (!now && !phoneField.getText().trim().isEmpty()) {
                Customer ex = CustomerDAO.findByPhone(phoneField.getText().trim());
                if (ex != null) {
                    nameField.setText(ex.getName());
                    emailField.setText(ex.getEmail() != null ? ex.getEmail() : "");
                    addressField.setText(ex.getAddress() != null ? ex.getAddress() : "");
                    autoFillLabel.setText("Customer mil gaya - auto fill ho gaya!");
                }
            }
        });

        Label pkgPriceLabel = new Label("Total: Rs. " + String.format("%.0f", pkg.getFinalPrice()));
        pkgPriceLabel.setStyle("-fx-font-size:13px; -fx-font-weight:bold;");

        DatePicker eventDatePicker = new DatePicker();
        DatePicker deliveryDatePicker = new DatePicker();
        TextArea notesArea = new TextArea(); notesArea.setPromptText("Extra notes"); notesArea.setPrefRowCount(2);

        Label errorLabel = new Label(); errorLabel.getStyleClass().add("error-text");

        Button previewBtn = new Button("👁 Poster Preview");
        previewBtn.getStyleClass().add("btn-secondary");
        previewBtn.setOnAction(e -> {
            showPosterPreview(pkg, nameField.getText().trim(),
                    eventDatePicker.getValue() != null ? eventDatePicker.getValue().toString() : "", 1);
        });

        // orderLayout pehle banao taake bookBtn mein use ho sake
        VBox orderLayout = new VBox(10);
        orderLayout.setPadding(new Insets(20));

        Button bookBtn = new Button("📋 Order Book Karein");
        bookBtn.getStyleClass().add("btn-primary");
        bookBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            if (name.isEmpty() || phone.isEmpty()) { errorLabel.setText("Naam aur phone zaroori hai."); return; }

            int days = 1;
            double total = pkg.getFinalPrice() * days;

            Customer customer = CustomerDAO.findByPhone(phone);
            int customerId;
            if (customer == null) {
                Customer newC = new Customer();
                newC.setName(name); newC.setPhone(phone);
                newC.setEmail(emailField.getText().trim().isEmpty() ? null : emailField.getText().trim());
                newC.setAddress(addressField.getText().trim().isEmpty() ? null : addressField.getText().trim());
                customerId = CustomerDAO.addCustomer(newC);
            } else { customerId = customer.getId(); }

            if (customerId == -1) { errorLabel.setText("Customer save nahi ho saka."); return; }

            StringBuilder notes = new StringBuilder();
            notes.append("Package: ").append(pkg.getPackageName()).append("\n");
            notes.append("Kitne Din: ").append(days).append("\n");
            if (pkg.getServices() != null && !pkg.getServices().isEmpty())
                notes.append("Services: ").append(pkg.getServiceNamesOnly()).append("\n");
            if (pkg.getDiscount() > 0) notes.append("Discount: ").append(pkg.getDiscount()).append("%\n");
            if (!notesArea.getText().trim().isEmpty()) notes.append("Notes: ").append(notesArea.getText().trim());

            Order order = new Order();
            order.setCustomerId(customerId);
            order.setPackageId(pkg.getId());
            order.setOrderType(pkg.getPackageName());
            order.setAmount(total);
            order.setOrderDate(Date.valueOf(LocalDate.now()));
            order.setEventDate(eventDatePicker.getValue() != null ? Date.valueOf(eventDatePicker.getValue()) : null);
            order.setDeliveryDate(deliveryDatePicker.getValue() != null ? Date.valueOf(deliveryDatePicker.getValue()) : null);
            order.setNotes(notes.toString());
            order.setCreatedBy(currentUser.getId());

            int orderId = OrderDAO.createOrder(order);
            if (orderId != -1) {
                order.setId(orderId);
                order.setCustomerName(name);

                errorLabel.getStyleClass().setAll("success-text");
                errorLabel.setText("Order #" + orderId + " book ho gaya! CEO approval pending.");

                String customerEmail = emailField.getText().trim();

                Button posterBtn2 = new Button("👁 Poster Preview");
                posterBtn2.getStyleClass().add("btn-secondary");
                posterBtn2.setOnAction(ev -> showPosterPreview(pkg, name,
                        eventDatePicker.getValue() != null ? eventDatePicker.getValue().toString() : "", 1));

                Button emailBtn2 = new Button("📧 Email Bhejo");
                emailBtn2.getStyleClass().add("btn-primary");
                emailBtn2.setOnAction(ev -> {
                    if (customerEmail.isEmpty()) { showAlert("Customer ka email nahi hai!"); return; }
                    String html = PosterGenerator.generateOrderConfirmationEmail(order, pkg);
                    boolean sent = EmailService.sendHtmlEmail(customerEmail, "Order Confirm - Mohsin Studio", html);
                    showAlert(sent ? "Email bhej di gayi! " + customerEmail : "Email send nahi ho saki.");
                });

                HBox postOrderBtns = new HBox(10, posterBtn2, emailBtn2);
                postOrderBtns.setId("postOrderBtns");
                orderLayout.getChildren().removeIf(n -> "postOrderBtns".equals(n.getId()));
                orderLayout.getChildren().add(postOrderBtns);

            } else { errorLabel.setText("Order book nahi ho saka."); }
        });

        // Ab orderLayout mein children add karo
        orderLayout.getChildren().addAll(
                new Label("Package: " + pkg.getPackageName()), new Separator(),
                new Label("Customer Naam:"), nameField,
                new Label("Phone:"), phoneField,
                new Label("Email:"), emailField,
                new Label("Address:"), addressField, autoFillLabel, new Separator(),
                pkgPriceLabel,
                new Label("Event Date:"), eventDatePicker,
                new Label("Delivery Date:"), deliveryDatePicker,
                new Label("Notes:"), notesArea, new Separator(),
                new HBox(10, previewBtn, bookBtn), errorLabel
        );

        ScrollPane scroll = new ScrollPane(orderLayout);
        scroll.setFitToWidth(true);
        Scene scene = new Scene(scroll, 420, 600);
        Utils.ThemeManager.applyTheme(scene);
        orderStage.setScene(scene);
        orderStage.showAndWait();
    }

    // =================== POSTER PREVIEW (WebView removed - Windows compatible) ===================
    private void showPosterPreview(Package pkg, String customerName, String eventDate, int days) {
        Stage posterStage = new Stage();
        posterStage.initModality(Modality.APPLICATION_MODAL);
        posterStage.setTitle("Mohsin Studio - Package Preview");

        // ===== OUTER WRAPPER =====
        VBox outerWrapper = new VBox();
        outerWrapper.setStyle("-fx-background-color: linear-gradient(to bottom, #0d0d1a, #1a1a2e, #0d0d1a);");
        outerWrapper.setPadding(new Insets(20));
        outerWrapper.setSpacing(0);

        // ===== TOP GOLD STRIP =====
        HBox topStrip = new HBox();
        topStrip.setStyle("-fx-background-color: linear-gradient(to right, #b8860b, #ffd700, #b8860b); -fx-pref-height:4px; -fx-min-height:4px;");

        // ===== HEADER SECTION =====
        VBox headerBox = new VBox(4);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(22, 20, 18, 20));
        headerBox.setStyle("-fx-background-color: linear-gradient(to bottom, #12122a, #1a1a3e);");

        Label cameraIcon = new Label("🎬");
        cameraIcon.setStyle("-fx-font-size:36px;");

        Label studioName = new Label("MOHSIN MOVIES & PHOTO STUDIO");
        studioName.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill: #ffd700; -fx-letter-spacing: 2px;");

        Label studioTagline = new Label("Premium Photography & Videography Services");
        studioTagline.setStyle("-fx-font-size:11px; -fx-text-fill:#c0a060; -fx-letter-spacing:1px;");

        // Gold divider line
        HBox goldLine1 = new HBox();
        goldLine1.setStyle("-fx-background-color: linear-gradient(to right, transparent, #ffd700, transparent); -fx-pref-height:1px; -fx-min-height:1px; -fx-pref-width:300px;");
        goldLine1.setAlignment(Pos.CENTER);
        goldLine1.setPadding(new Insets(8, 40, 8, 40));

        headerBox.getChildren().addAll(cameraIcon, studioName, studioTagline, goldLine1);

        // ===== PACKAGE TITLE BANNER =====
        VBox pkgTitleBox = new VBox(4);
        pkgTitleBox.setAlignment(Pos.CENTER);
        pkgTitleBox.setPadding(new Insets(14, 20, 14, 20));
        pkgTitleBox.setStyle("-fx-background-color: linear-gradient(to right, #1a1200, #2d2000, #1a1200);");

        Label pkgBadge = new Label("★  PACKAGE  ★");
        pkgBadge.setStyle("-fx-font-size:10px; -fx-text-fill:#b8860b; -fx-letter-spacing:3px;");

        Label pkgTitleLabel = new Label(pkg.getPackageName().toUpperCase());
        pkgTitleLabel.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#ffd700;");
        pkgTitleLabel.setWrapText(true);
        pkgTitleLabel.setAlignment(Pos.CENTER);

        if (pkg.getCategory() != null && !pkg.getCategory().isEmpty()) {
            Label catBadge = new Label("[ " + pkg.getCategory() + " ]");
            catBadge.setStyle("-fx-font-size:11px; -fx-text-fill:#a08040; -fx-letter-spacing:1px;");
            pkgTitleBox.getChildren().addAll(pkgBadge, pkgTitleLabel, catBadge);
        } else {
            pkgTitleBox.getChildren().addAll(pkgBadge, pkgTitleLabel);
        }

        // ===== CUSTOMER INFO (if provided) =====
        VBox customerBox = new VBox(6);
        customerBox.setPadding(new Insets(12, 20, 12, 20));
        customerBox.setStyle("-fx-background-color:rgba(255,215,0,0.04);");

        boolean hasCustomerInfo = !customerName.isEmpty() || !eventDate.isEmpty() || days > 1;
        if (hasCustomerInfo) {
            Label custHeader = new Label("— Booking Details —");
            custHeader.setStyle("-fx-font-size:11px; -fx-text-fill:#b8860b; -fx-letter-spacing:2px;");
            custHeader.setAlignment(Pos.CENTER);
            customerBox.getChildren().add(custHeader);
            customerBox.setAlignment(Pos.CENTER);

            if (!customerName.isEmpty()) {
                Label custLabel = new Label("👤  " + customerName);
                custLabel.setStyle("-fx-font-size:13px; -fx-text-fill:#fff; -fx-font-weight:bold;");
                customerBox.getChildren().add(custLabel);
            }
            if (!eventDate.isEmpty()) {
                Label dateLabel = new Label("📅  Event: " + eventDate);
                dateLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#ddd;");
                customerBox.getChildren().add(dateLabel);
            }
            if (days > 1) {
                Label daysLabel = new Label("📆  Duration: " + days + " Din");
                daysLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#ddd;");
                customerBox.getChildren().add(daysLabel);
            }
        }

        // ===== SERVICES SECTION =====
        VBox servicesSection = new VBox(0);
        servicesSection.setPadding(new Insets(0, 20, 0, 20));

        Label svcHeader = new Label("✦  INCLUDED SERVICES  ✦");
        svcHeader.setStyle("-fx-font-size:11px; -fx-text-fill:#ffd700; -fx-letter-spacing:2px; -fx-padding: 12 0 8 0;");
        svcHeader.setAlignment(Pos.CENTER);
        servicesSection.setAlignment(Pos.CENTER);
        servicesSection.getChildren().add(svcHeader);

        if (pkg.getServices() != null && !pkg.getServices().isEmpty()) {
            boolean alternate = false;
            for (String[] svc : pkg.getParsedServices()) {
                HBox svcRow = new HBox(10);
                svcRow.setPadding(new Insets(7, 12, 7, 12));
                svcRow.setAlignment(Pos.CENTER_LEFT);
                String rowBg = alternate
                        ? "-fx-background-color:rgba(255,215,0,0.04);"
                        : "-fx-background-color:rgba(255,255,255,0.02);";
                svcRow.setStyle(rowBg + " -fx-background-radius:4;");

                Label checkIcon = new Label("✔");
                checkIcon.setStyle("-fx-font-size:12px; -fx-text-fill:#ffd700; -fx-min-width:18px;");

                Label svcName = new Label(svc[0]);
                svcName.setStyle("-fx-font-size:13px; -fx-text-fill:#eee; -fx-font-weight:bold;");

                HBox spacer = new HBox();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                if (svc.length > 1 && !svc[1].isEmpty()) {
                    int svcDays2 = svc.length > 2 ? Integer.parseInt(svc[2]) : 1;
                    double lineTotal = Double.parseDouble(svc[1]) * svcDays2;
                    String priceStr = "Rs. " + String.format("%.0f", lineTotal);
                    if (svcDays2 > 1) priceStr += " (" + svcDays2 + "d)";
                    Label svcPrice = new Label(priceStr);
                    svcPrice.setStyle("-fx-font-size:12px; -fx-text-fill:#d4a574;");
                    svcRow.getChildren().addAll(checkIcon, svcName, spacer, svcPrice);
                } else {
                    svcRow.getChildren().addAll(checkIcon, svcName);
                }
                servicesSection.getChildren().add(svcRow);
                alternate = !alternate;
            }
        }

        // ===== PRICE SECTION =====
        double finalPrice = pkg.getFinalPrice() * days;
        VBox priceBox = new VBox(6);
        priceBox.setAlignment(Pos.CENTER);
        priceBox.setPadding(new Insets(16, 20, 16, 20));
        priceBox.setStyle("-fx-background-color: linear-gradient(to bottom, #0d1a0d, #0a150a);");

        if (pkg.getDiscount() > 0) {
            double originalPrice = pkg.getPrice() * days;
            Label originalPriceLabel = new Label("Original: Rs. " + String.format("%.0f", originalPrice));
            originalPriceLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#888; -fx-strikethrough:true;");

            Label discountLabel = new Label("🏷  " + (int)pkg.getDiscount() + "% Discount Applied!");
            discountLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#e74c3c; -fx-font-weight:bold;");

            priceBox.getChildren().addAll(originalPriceLabel, discountLabel);
        }

        Label totalLabel2 = new Label("TOTAL PRICE");
        totalLabel2.setStyle("-fx-font-size:10px; -fx-text-fill:#5a8a5a; -fx-letter-spacing:2px;");

        Label finalPriceLabel = new Label("Rs. " + String.format("%.0f", finalPrice));
        finalPriceLabel.setStyle("-fx-font-size:28px; -fx-font-weight:bold; -fx-text-fill:#2ecc71;");

        priceBox.getChildren().addAll(totalLabel2, finalPriceLabel);

        // ===== BOTTOM GOLD STRIP =====
        HBox midGoldLine = new HBox();
        midGoldLine.setStyle("-fx-background-color: linear-gradient(to right, transparent, #ffd700, transparent); -fx-pref-height:1px; -fx-min-height:1px;");

        // ===== SOCIAL MEDIA FOOTER =====
        VBox footerBox = new VBox(6);
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setPadding(new Insets(14, 20, 16, 20));
        footerBox.setStyle("-fx-background-color:#0a0a18;");

        Label followUs = new Label("FOLLOW US");
        followUs.setStyle("-fx-font-size:10px; -fx-text-fill:#666; -fx-letter-spacing:3px;");

        HBox socialRow = new HBox(20);
        socialRow.setAlignment(Pos.CENTER);

        Label tiktokLabel = new Label("TikTok: @mohsinmovies89");
        tiktokLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#ee1d52; -fx-font-weight:bold;");

        Label instaLabel = new Label("Instagram: @mohsinmovies89");
        instaLabel.setStyle("-fx-font-size:12px; -fx-text-fill:#c13584; -fx-font-weight:bold;");

        socialRow.getChildren().addAll(tiktokLabel, new Label("|"), instaLabel);

        Label copyrightLabel = new Label("Mohsin Movies & Photo Studio  •  Professional Media Services");
        copyrightLabel.setStyle("-fx-font-size:10px; -fx-text-fill:#444;");

        footerBox.getChildren().addAll(followUs, socialRow, copyrightLabel);

        // ===== BOTTOM GOLD STRIP =====
        HBox bottomStrip = new HBox();
        bottomStrip.setStyle("-fx-background-color: linear-gradient(to right, #b8860b, #ffd700, #b8860b); -fx-pref-height:4px; -fx-min-height:4px;");

        // ===== CLOSE BUTTON =====
        Button closeBtn = new Button("✕  Close");
        closeBtn.setStyle("-fx-background-color:#2a2a3e; -fx-text-fill:#aaa; -fx-font-size:12px; -fx-padding:8 20 8 20; -fx-background-radius:4; -fx-cursor:hand;");
        closeBtn.setOnAction(e -> posterStage.close());

        HBox btnRow2 = new HBox(closeBtn);
        btnRow2.setAlignment(Pos.CENTER_RIGHT);
        btnRow2.setPadding(new Insets(12, 20, 8, 20));
        btnRow2.setStyle("-fx-background-color:#0d0d1a;");

        // ===== ASSEMBLE =====
        outerWrapper.getChildren().addAll(
                topStrip,
                headerBox,
                pkgTitleBox,
                customerBox,
                servicesSection,
                midGoldLine,
                priceBox,
                footerBox,
                bottomStrip,
                btnRow2
        );

        ScrollPane scroll = new ScrollPane(outerWrapper);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:#0d0d1a; -fx-background-color:#0d0d1a;");

        Scene scene = new Scene(scroll, 460, 620);
        posterStage.setScene(scene);
        posterStage.show();
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
}