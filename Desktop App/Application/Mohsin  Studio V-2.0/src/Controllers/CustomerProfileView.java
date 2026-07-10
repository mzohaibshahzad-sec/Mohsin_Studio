package Controllers;

import Database.CustomerDAO;
import Database.CustomerDriveDAO;
import Models.Customer;
import Models.CustomerDrive;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class CustomerProfileView {

    // onUpdated - jab info save ho jaye to parent table refresh karne ke liye (CustomersView se loadAllCustomers pass hota hai)
    public static void show(Customer customer, Runnable onUpdated) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Customer Profile - " + customer.getName());

        // ===== Customer Info Section =====
        Label infoTitle = new Label("👤 Customer Info");
        infoTitle.getStyleClass().add("page-title");
        infoTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField nameField = new TextField(customer.getName());
        TextField phoneField = new TextField(customer.getPhone());
        TextField emailField = new TextField(customer.getEmail() != null ? customer.getEmail() : "");
        TextField addressField = new TextField(customer.getAddress() != null ? customer.getAddress() : "");

        Label infoErrorLabel = new Label();
        infoErrorLabel.setWrapText(true);

        Button saveInfoBtn = new Button("✅ Info Save Karein");
        saveInfoBtn.getStyleClass().add("btn-primary");
        saveInfoBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            if (name.isEmpty() || phone.isEmpty()) {
                infoErrorLabel.getStyleClass().setAll("error-text");
                infoErrorLabel.setText("Naam aur Phone zaroori hain.");
                return;
            }
            customer.setName(name);
            customer.setPhone(phone);
            customer.setEmail(emailField.getText().trim().isEmpty() ? null : emailField.getText().trim());
            customer.setAddress(addressField.getText().trim().isEmpty() ? null : addressField.getText().trim());

            boolean success = CustomerDAO.updateCustomer(customer);
            if (success) {
                infoErrorLabel.getStyleClass().setAll("muted-text");
                infoErrorLabel.setText("✅ Info update ho gayi.");
                stage.setTitle("Customer Profile - " + customer.getName());
                if (onUpdated != null) onUpdated.run();
            } else {
                infoErrorLabel.getStyleClass().setAll("error-text");
                infoErrorLabel.setText("Save nahi ho saka. Dobara try karein.");
            }
        });

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(8);
        infoGrid.addRow(0, new Label("Naam:"), nameField);
        infoGrid.addRow(1, new Label("Phone:"), phoneField);
        infoGrid.addRow(2, new Label("Email:"), emailField);
        infoGrid.addRow(3, new Label("Address:"), addressField);

        VBox infoSection = new VBox(10, infoTitle, infoGrid, saveInfoBtn, infoErrorLabel);

        // ===== Data Drives Section =====
        Label drivesTitle = new Label("💾 Data Drives");
        drivesTitle.getStyleClass().add("page-title");
        drivesTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TableView<CustomerDrive> drivesTable = new TableView<>();
        drivesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        drivesTable.setPrefHeight(180);

        TableColumn<CustomerDrive, String> driveNameCol = new TableColumn<>("Drive Naam");
        driveNameCol.setCellValueFactory(new PropertyValueFactory<>("driveName"));

        TableColumn<CustomerDrive, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));

        TableColumn<CustomerDrive, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));

        drivesTable.getColumns().addAll(driveNameCol, locationCol, notesCol);

        Runnable reloadDrives = () -> {
            List<CustomerDrive> drives = CustomerDriveDAO.getDrivesForCustomer(customer.getId());
            drivesTable.setItems(FXCollections.observableArrayList(drives));
        };
        reloadDrives.run();

        Label drivesErrorLabel = new Label();
        drivesErrorLabel.setWrapText(true);

        Button addDriveBtn = new Button("+ Drive Add Karein");
        addDriveBtn.getStyleClass().add("btn-primary");
        addDriveBtn.setOnAction(e -> openDriveForm(customer, null, reloadDrives));

        Button editDriveBtn = new Button("✏ Edit Drive");
        editDriveBtn.getStyleClass().add("btn-secondary");
        editDriveBtn.setOnAction(e -> {
            CustomerDrive selected = drivesTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openDriveForm(customer, selected, reloadDrives);
            } else {
                drivesErrorLabel.getStyleClass().setAll("error-text");
                drivesErrorLabel.setText("Pehle koi drive select karein.");
            }
        });

        Button deleteDriveBtn = new Button("🗑 Delete Drive");
        deleteDriveBtn.getStyleClass().add("btn-danger");
        deleteDriveBtn.setOnAction(e -> {
            CustomerDrive selected = drivesTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                drivesErrorLabel.getStyleClass().setAll("error-text");
                drivesErrorLabel.setText("Pehle koi drive select karein.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Kya aap is drive record ko delete karna chahte hain?\n" + selected.getDriveName());
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean success = CustomerDriveDAO.deleteDrive(selected.getId());
                    if (success) {
                        reloadDrives.run();
                    } else {
                        drivesErrorLabel.getStyleClass().setAll("error-text");
                        drivesErrorLabel.setText("Delete nahi ho saka.");
                    }
                }
            });
        });

        HBox driveBtnBar = new HBox(10, addDriveBtn, editDriveBtn, deleteDriveBtn);
        driveBtnBar.setAlignment(Pos.CENTER_LEFT);

        VBox drivesSection = new VBox(10, drivesTitle, drivesTable, driveBtnBar, drivesErrorLabel);

        // ===== Close Button =====
        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("btn-secondary");
        closeBtn.setOnAction(e -> stage.close());
        HBox closeBar = new HBox(closeBtn);
        closeBar.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(18, infoSection, new Separator(), drivesSection, closeBar);
        layout.setPadding(new Insets(20));

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 520, 600);
        Utils.ThemeManager.applyTheme(scene);
        stage.setScene(scene);
        stage.showAndWait();
    }

    // ===== Drive Add/Edit Form =====
    private static void openDriveForm(Customer customer, CustomerDrive existingDrive, Runnable onSaved) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(existingDrive == null ? "Naya Drive Add Karein" : "Drive Edit Karein");

        TextField driveNameField = new TextField(existingDrive != null ? existingDrive.getDriveName() : "");
        driveNameField.setPromptText("Jaise: Seagate 1TB #3");

        TextField locationField = new TextField(
                existingDrive != null && existingDrive.getLocation() != null ? existingDrive.getLocation() : "");
        locationField.setPromptText("Jaise: Office Shelf, Ghar wala drawer");

        TextArea notesArea = new TextArea(
                existingDrive != null && existingDrive.getNotes() != null ? existingDrive.getNotes() : "");
        notesArea.setPromptText("Koi additional note...");
        notesArea.setPrefRowCount(3);
        notesArea.setWrapText(true);

        Label errorLabel = new Label();
        errorLabel.setWrapText(true);

        Button saveBtn = new Button("✅ Save Karein");
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> {
            String driveName = driveNameField.getText().trim();
            if (driveName.isEmpty()) {
                errorLabel.getStyleClass().setAll("error-text");
                errorLabel.setText("Drive ka naam zaroori hai.");
                return;
            }
            String location = locationField.getText().trim().isEmpty() ? null : locationField.getText().trim();
            String notes = notesArea.getText().trim().isEmpty() ? null : notesArea.getText().trim();

            boolean success;
            if (existingDrive == null) {
                CustomerDrive newDrive = new CustomerDrive();
                newDrive.setCustomerId(customer.getId());
                newDrive.setDriveName(driveName);
                newDrive.setLocation(location);
                newDrive.setNotes(notes);
                success = CustomerDriveDAO.addDrive(newDrive) > 0;
            } else {
                existingDrive.setDriveName(driveName);
                existingDrive.setLocation(location);
                existingDrive.setNotes(notes);
                success = CustomerDriveDAO.updateDrive(existingDrive);
            }

            if (success) {
                if (onSaved != null) onSaved.run();
                stage.close();
            } else {
                errorLabel.getStyleClass().setAll("error-text");
                errorLabel.setText("Save nahi ho saka. Dobara try karein.");
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-secondary");
        cancelBtn.setOnAction(e -> stage.close());

        HBox btnRow = new HBox(10, saveBtn, cancelBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(10,
                new Label("Drive Naam:"), driveNameField,
                new Label("Location:"), locationField,
                new Label("Notes:"), notesArea,
                new Separator(),
                btnRow, errorLabel
        );
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 380, 420);
        Utils.ThemeManager.applyTheme(scene);
        stage.setScene(scene);
        stage.showAndWait();
    }
}
