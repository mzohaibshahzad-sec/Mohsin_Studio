package Controllers;

import Database.UserDAO;
import Models.User;
import Security.PasswordUtil;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginScreen {

    private Stage stage;

    public LoginScreen(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        javafx.scene.image.ImageView logoView = null;
        try {
            javafx.scene.image.Image logoImg = new javafx.scene.image.Image(getClass().getResourceAsStream("/resources/images/logo.png"));
            logoView = new javafx.scene.image.ImageView(logoImg);
            logoView.setFitHeight(110);
            logoView.setFitWidth(110);
            logoView.setSmooth(true);
            logoView.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Logo load nahi ho saka: " + e.getMessage());
        }

        Label titleLabel = new Label("Mohsin Movies and Photo Studio");
        titleLabel.getStyleClass().add("login-title");

        Label subtitleLabel = new Label("ORDER MANAGEMENT SYSTEM");
        subtitleLabel.getStyleClass().add("login-subtitle");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(280);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(280);

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("btn-primary");
        loginButton.setMaxWidth(280);
        loginButton.setDefaultButton(true);

        Label messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(280);
        messageLabel.setAlignment(Pos.CENTER);

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                showMessage(messageLabel, "Username aur Password dono zaroori hain", false);
                shake(loginButton);
                return;
            }

            User user = UserDAO.getUserByUsername(username);

            if (user == null) {
                showMessage(messageLabel, "Username ya Password ghalat hai", false);
                shake(loginButton);
                return;
            }

            if (user.isLocked()) {
                showMessage(messageLabel, "Ye account lock hai. CEO se rabta karein.", false);
                return;
            }

            if (!user.isActive()) {
                showMessage(messageLabel, "Ye account active nahi hai.", false);
                return;
            }

            boolean passwordMatches = PasswordUtil.verifyPassword(password, user.getPasswordHash());

            if (passwordMatches) {
                UserDAO.resetLoginAttempts(user.getId());
                Database.AuditLogDAO.log(user.getId(), "LOGIN_SUCCESS", "users", user.getId(),
                        user.getFullName() + " (" + user.getRole() + ") logged in successfully");
                showMessage(messageLabel, "Login ho gaya! Dashboard khul raha hai...", true);

                PauseTransition pause = new PauseTransition(Duration.millis(400));
                pause.setOnFinished(ev -> new Dashboard(stage, user).show());
                pause.play();

            }  else {
                UserDAO.incrementLoginAttempts(user.getId());
                int attemptsLeft = 5 - (user.getLoginAttempts() + 1);

                if (attemptsLeft <= 0) {
                    UserDAO.lockAccount(user.getId());
                    Database.AuditLogDAO.log(user.getId(), "ACCOUNT_LOCKED", "users", user.getId(),
                            user.getUsername() + " - too many failed attempts");
                    showMessage(messageLabel, "Bohat zyada ghalat attempts. Account lock ho gaya.", false);
                } else {
                    showMessage(messageLabel, "Ghalat password. " + attemptsLeft + " attempts baqi hain.", false);
                }
                shake(loginButton);
            }
        });

        VBox cardContent = new VBox(14);
        cardContent.setAlignment(Pos.CENTER);
        if (logoView != null) {
            cardContent.getChildren().add(logoView);
        }
        cardContent.getChildren().addAll(titleLabel, subtitleLabel, new javafx.scene.layout.Region(), usernameField, passwordField, loginButton, messageLabel);

        VBox card = new VBox(cardContent);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(400);
        card.setAlignment(Pos.CENTER);

        StackPane root = new StackPane(card);
        root.getStyleClass().add("login-container");
        root.setPadding(new Insets(40));

        Scene scene = new Scene(root, 480, 480);
        Utils.ThemeManager.applyTheme(scene);

        stage.setTitle("Mohsin Studio - Login");
        stage.setScene(scene);
        stage.show();

        // Fade-in animation
        card.setOpacity(0);
        card.setScaleX(0.92);
        card.setScaleY(0.92);
        FadeTransition fade = new FadeTransition(Duration.millis(450), card);
        fade.setFromValue(0);
        fade.setToValue(1);
        ScaleTransition scale = new ScaleTransition(Duration.millis(450), card);
        scale.setFromX(0.92);
        scale.setFromY(0.92);
        scale.setToX(1);
        scale.setToY(1);
        ParallelTransition parallel = new ParallelTransition(fade, scale);
        parallel.play();
    }

    private void showMessage(Label label, String text, boolean success) {
        label.setText(text);
        label.getStyleClass().removeAll("success-text", "error-text");
        label.getStyleClass().add(success ? "success-text" : "error-text");
    }

    // Galat login pe button ko halka sa shake karwata hai (visual feedback)
    private void shake(javafx.scene.Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(60), node);
        shake.setFromX(0);
        shake.setByX(8);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> node.setTranslateX(0));
        shake.play();
    }
}