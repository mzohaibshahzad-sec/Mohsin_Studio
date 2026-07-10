package Controllers;

import Database.UserDAO;
import Models.User;
import Security.PasswordUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ManageUsersView {

    private TableView<User> table;
    private User currentUser;

    public ManageUsersView(User currentUser) {
        this.currentUser = currentUser;
    }

    public VBox getView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        Label title = new Label("Manage Users");
        title.getStyleClass().add("page-title");

        table = new TableView<>();
        setupTable();
        loadUsers();
        VBox.setVgrow(table, Priority.ALWAYS);

        Button addBtn = new Button("+ Naya User Add Karein");
        addBtn.getStyleClass().add("btn-success");
        addBtn.setOnAction(e -> openAddUserForm());

        Button toggleActiveBtn = new Button("Activate / Deactivate Selected");
        toggleActiveBtn.getStyleClass().add("btn-secondary");
        toggleActiveBtn.setOnAction(e -> handleToggleActive());

        Button unlockBtn = new Button("Unlock Selected");
        unlockBtn.getStyleClass().add("btn-primary");
        unlockBtn.setOnAction(e -> handleUnlock());

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> loadUsers());

        HBox buttonBar = new HBox(10, addBtn, toggleActiveBtn, unlockBtn, refreshBtn);

        container.getChildren().addAll(title, table, buttonBar);
        return container;
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        TableColumn<User, String> nameCol = new TableColumn<>("Naam");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameCol.setPrefWidth(150);

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(120);

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(110);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(180);

        TableColumn<User, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(110);

        TableColumn<User, Boolean> activeCol = new TableColumn<>("Active?");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeCol.setPrefWidth(70);

        TableColumn<User, Boolean> lockedCol = new TableColumn<>("Locked?");
        lockedCol.setCellValueFactory(new PropertyValueFactory<>("locked"));
        lockedCol.setPrefWidth(70);

        table.getColumns().addAll(nameCol, usernameCol, roleCol, emailCol, phoneCol, activeCol, lockedCol);
    }

    private void loadUsers() {
        java.util.List<User> allUsers = UserDAO.getAllUsers();

        // Agar CEO dekh raha hai (Admin nahi), to Admin accounts list se hata dein
        if (!currentUser.isAdmin()) {
            allUsers.removeIf(User::isAdmin);
        }

        ObservableList<User> data = FXCollections.observableArrayList(allUsers);
        table.setItems(data);
    }

    private void handleToggleActive() {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Pehle koi user select karein.");
            return;
        }

        if (selected.getRole().equals("CEO") || selected.getRole().equals("ADMIN")) {
            showAlert("CEO ya Admin ka account is tarah deactivate nahi kiya ja sakta.");
            return;
        }

        boolean newStatus = !selected.isActive();
        UserDAO.setUserActive(selected.getId(), newStatus);
        Database.AuditLogDAO.log(currentUser.getId(), newStatus ? "ACTIVATED_USER" : "DEACTIVATED_USER",
                "users", selected.getId(), selected.getFullName() + " (" + selected.getUsername() + ")");
        loadUsers();
    }

    private void handleUnlock() {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Pehle koi user select karein.");
            return;
        }
        UserDAO.unlockAccount(selected.getId());
        Database.AuditLogDAO.log(currentUser.getId(), "UNLOCKED_USER", "users", selected.getId(),
                selected.getFullName() + " (" + selected.getUsername() + ")");

        loadUsers();
    }

    private void openAddUserForm() {
        Stage formStage = new Stage();
        formStage.initModality(Modality.APPLICATION_MODAL);
        formStage.setTitle("Naya User Add Karein");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Pura Naam");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username (login ke liye)");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("CO_FOUNDER", "CLERK");
        roleBox.setPromptText("Role Chunein");
        roleBox.setMaxWidth(Double.MAX_VALUE);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-text");
        errorLabel.setWrapText(true);

        Button saveBtn = new Button("Save");
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.setOnAction(e -> {
            String fullName = fullNameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String role = roleBox.getValue();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()
                    || role == null || email.isEmpty() || phone.isEmpty()) {
                errorLabel.setText("Sab fields zaroori hain.");
                return;
            }

            if (UserDAO.usernameExists(username)) {
                errorLabel.setText("Ye username pehle se mojood hai. Koi aur username chunein.");
                return;
            }

            String hashedPassword = PasswordUtil.hashPassword(password);
            boolean success = UserDAO.createUser(fullName, username, hashedPassword, role, email, phone);

            if (success) {
                Database.AuditLogDAO.log(currentUser.getId(), "CREATED_USER", "users", null,
                        "New " + role + " created: " + fullName + " (" + username + ")");
                loadUsers();
                formStage.close();
            } else {
                errorLabel.setText("User create nahi ho saka. Dobara koshish karein.");
            }
        });

        VBox formLayout = new VBox(10, fullNameField, usernameField, passwordField, roleBox, emailField, phoneField, saveBtn, errorLabel);
        formLayout.setPadding(new Insets(20));
        formLayout.setAlignment(Pos.CENTER_LEFT);

        javafx.scene.Scene formScene = new javafx.scene.Scene(formLayout, 350, 440);
        Utils.ThemeManager.applyTheme(formScene);
        formStage.setScene(formScene);
        formStage.showAndWait();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.showAndWait();
    }
}