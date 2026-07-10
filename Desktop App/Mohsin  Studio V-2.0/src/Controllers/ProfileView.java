package Controllers;

import Database.UserDAO;
import Models.User;
import Security.PasswordUtil;
import Services.ProfileImageService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * "My Profile" page - har role (CEO/Co-Founder/Clerk) yahan se:
 * - apni profile picture lagwa/badal sakta hai
 * - naam, email, phone update kar sakta hai
 * - password change kar sakta hai
 */
public class ProfileView {

    private User currentUser;
    private ImageView avatarView;

    public ProfileView(User currentUser) {
        this.currentUser = currentUser;
    }

    public ScrollPane getView() {
        VBox main = new VBox(18);
        main.setPadding(new Insets(24));
        main.setMaxWidth(480);

        Label title = new Label("My Profile");
        title.getStyleClass().add("page-title");

        // ===== Avatar =====
        avatarView = new ImageView();
        avatarView.setFitWidth(110);
        avatarView.setFitHeight(110);
        avatarView.setPreserveRatio(false);
        refreshAvatar();

        Circle clip = new Circle(55, 55, 55);
        avatarView.setClip(clip);

        StackPane avatarPane = new StackPane(avatarView);
        avatarPane.setPrefSize(110, 110);
        avatarPane.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 55;");

        Button changePicBtn = new Button("📷 Change Picture");
        changePicBtn.getStyleClass().add("btn-secondary");

        Label picStatusLabel = new Label();

        changePicBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Profile Picture Chunein");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png"));
            File selected = chooser.showOpenDialog(avatarPane.getScene().getWindow());
            if (selected == null) return;

            String savedPath = ProfileImageService.saveProfileImage(currentUser.getId(), selected);
            if (savedPath != null && UserDAO.updateProfilePicture(currentUser.getId(), savedPath)) {
                currentUser.setProfilePicturePath(savedPath);
                refreshAvatar();
                picStatusLabel.getStyleClass().setAll("success-text");
                picStatusLabel.setText("Profile picture update ho gayi!");
            } else {
                picStatusLabel.getStyleClass().setAll("error-text");
                picStatusLabel.setText("Picture save nahi ho saki.");
            }
        });

        VBox avatarBox = new VBox(10, avatarPane, changePicBtn, picStatusLabel);
        avatarBox.setAlignment(Pos.CENTER);

        // ===== Profile Details Form =====
        Label detailsTitle = new Label("Account Details");
        detailsTitle.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");

        TextField nameField = new TextField(currentUser.getFullName());
        TextField emailField = new TextField(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        TextField phoneField = new TextField(currentUser.getPhone() != null ? currentUser.getPhone() : "");
        TextField usernameField = new TextField(currentUser.getUsername());
        usernameField.setDisable(true); // username change nahi hota, sirf display

        Label roleLabel = new Label("Role: " + currentUser.getRole());
        roleLabel.getStyleClass().add("muted-text");

        Label detailsError = new Label();
        detailsError.setWrapText(true);

        Button saveDetailsBtn = new Button("✅ Details Save Karein");
        saveDetailsBtn.getStyleClass().add("btn-primary");
        saveDetailsBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();

            if (name.isEmpty()) {
                detailsError.getStyleClass().setAll("error-text");
                detailsError.setText("Naam zaroori hai.");
                return;
            }

            boolean success = UserDAO.updateProfile(currentUser.getId(), name,
                    email.isEmpty() ? null : email, phone.isEmpty() ? null : phone);

            if (success) {
                currentUser.setFullName(name);
                currentUser.setEmail(email.isEmpty() ? null : email);
                currentUser.setPhone(phone.isEmpty() ? null : phone);
                detailsError.getStyleClass().setAll("success-text");
                detailsError.setText("Details update ho gayi!");
            } else {
                detailsError.getStyleClass().setAll("error-text");
                detailsError.setText("Update nahi ho saka.");
            }
        });

        VBox detailsForm = new VBox(8,
                detailsTitle, roleLabel, new Separator(),
                new Label("Username:"), usernameField,
                new Label("Full Naam:"), nameField,
                new Label("Email:"), emailField,
                new Label("Phone:"), phoneField,
                saveDetailsBtn, detailsError
        );

        // ===== Change Password =====
        Label passTitle = new Label("Password Change Karein");
        passTitle.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");

        PasswordField currentPassField = new PasswordField();
        currentPassField.setPromptText("Current password");
        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("Naya password");
        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Naya password confirm karein");

        Label passError = new Label();
        passError.setWrapText(true);

        Button changePassBtn = new Button("🔒 Password Update Karein");
        changePassBtn.getStyleClass().add("btn-primary");
        changePassBtn.setOnAction(e -> {
            String current = currentPassField.getText();
            String newPass = newPassField.getText();
            String confirm = confirmPassField.getText();

            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                passError.getStyleClass().setAll("error-text");
                passError.setText("Sab fields fill karein.");
                return;
            }
            if (!PasswordUtil.verifyPassword(current, currentUser.getPasswordHash())) {
                passError.getStyleClass().setAll("error-text");
                passError.setText("Current password ghalat hai.");
                return;
            }
            if (!newPass.equals(confirm)) {
                passError.getStyleClass().setAll("error-text");
                passError.setText("Naya password match nahi karta.");
                return;
            }
            if (newPass.length() < 4) {
                passError.getStyleClass().setAll("error-text");
                passError.setText("Password kam az kam 4 characters ka ho.");
                return;
            }

            String hashed = PasswordUtil.hashPassword(newPass);
            if (UserDAO.updatePassword(currentUser.getId(), hashed)) {
                currentUser.setPasswordHash(hashed);
                passError.getStyleClass().setAll("success-text");
                passError.setText("Password update ho gaya!");
                currentPassField.clear(); newPassField.clear(); confirmPassField.clear();
            } else {
                passError.getStyleClass().setAll("error-text");
                passError.setText("Password update nahi ho saka.");
            }
        });

        VBox passForm = new VBox(8,
                passTitle, new Separator(),
                new Label("Current Password:"), currentPassField,
                new Label("Naya Password:"), newPassField,
                new Label("Confirm Password:"), confirmPassField,
                changePassBtn, passError
        );

        main.getChildren().addAll(title, avatarBox, new Separator(), detailsForm, new Separator(), passForm);

        ScrollPane scroll = new ScrollPane(main);
        scroll.setFitToWidth(true);
        return scroll;
    }




    private void refreshAvatar() {
        String path = currentUser.getProfilePicturePath();
        if (path != null && ProfileImageService.imageExists(path)) {
            avatarView.setImage(new Image("file:" + path));
        } else {
            var stream = getClass().getResourceAsStream("/images/logo.png");
            if (stream != null) {
                avatarView.setImage(new Image(stream));
            } else {
                avatarView.setImage(null); // crash nahi hoga
            }
        }
    }

}