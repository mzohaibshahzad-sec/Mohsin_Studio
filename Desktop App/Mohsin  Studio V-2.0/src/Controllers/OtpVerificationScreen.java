package Controllers;

import Database.OtpDAO;
import Models.User;
import Services.EmailService;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class OtpVerificationScreen {

    private Stage stage;
    private User user;

    public OtpVerificationScreen(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
    }

    public void show() {
        // OTP generate karein, save karein, email bhejein
        String otp = OtpDAO.generateOtp();
        OtpDAO.saveOtp(user.getId(), otp);
        sendOtpEmailInBackground(otp);

        Label titleLabel = new Label("Email Verification");
        titleLabel.getStyleClass().add("login-title");

        Label subtitleLabel = new Label("OTP CODE VERIFICATION");
        subtitleLabel.getStyleClass().add("login-subtitle");

        Label infoLabel = new Label("6-digit code " + maskEmail(user.getEmail()) + " par bheja gaya hai.");
        infoLabel.setWrapText(true);
        infoLabel.setMaxWidth(300);
        infoLabel.setAlignment(Pos.CENTER);
        infoLabel.getStyleClass().add("muted-text");
        infoLabel.setStyle("-fx-text-alignment: center;");

        TextField otpField = new TextField();
        otpField.setPromptText("6-digit OTP");
        otpField.setMaxWidth(200);
        otpField.setStyle("-fx-alignment: center; -fx-font-size: 18px; -fx-letter-spacing: 4px;");
        otpField.setAlignment(Pos.CENTER);

        // Sirf numbers allow karein, max 6 digits
        otpField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                otpField.setText(newVal.replaceAll("[^\\d]", ""));
            }
            if (otpField.getText().length() > 6) {
                otpField.setText(otpField.getText().substring(0, 6));
            }
        });

        Button verifyBtn = new Button("Verify Karein");
        verifyBtn.getStyleClass().add("btn-primary");
        verifyBtn.setMaxWidth(280);
        verifyBtn.setDefaultButton(true);

        Label messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(280);
        messageLabel.setAlignment(Pos.CENTER);

        Button resendBtn = new Button("Code Dobara Bhejein");
        resendBtn.getStyleClass().add("btn-secondary");
        resendBtn.setMaxWidth(280);

        Button backBtn = new Button("Wapas Login Par Jayein");
        backBtn.getStyleClass().add("btn-secondary");
        backBtn.setMaxWidth(280);
        backBtn.setOnAction(e -> new LoginScreen(stage).show());

        verifyBtn.setOnAction(e -> {
            String entered = otpField.getText().trim();

            if (entered.length() != 6) {
                messageLabel.getStyleClass().setAll("error-text");
                messageLabel.setText("6-digit code daalein.");
                return;
            }

            boolean valid = OtpDAO.verifyOtp(user.getId(), entered);

            if (valid) {
                Database.AuditLogDAO.log(user.getId(), "LOGIN_SUCCESS", "users", user.getId(),
                        user.getFullName() + " (" + user.getRole() + ") logged in successfully");
                messageLabel.getStyleClass().setAll("success-text");
                messageLabel.setText("Verified! Dashboard khul raha hai...");

                new Thread(() -> {
                    try { Thread.sleep(400); } catch (InterruptedException ignored) {}
                    javafx.application.Platform.runLater(() -> new Dashboard(stage, user).show());
                }).start();

            } else {
                messageLabel.getStyleClass().setAll("error-text");
                messageLabel.setText("Code ghalat hai ya expire ho gaya hai. Dobara try karein.");
            }
        });

        resendBtn.setOnAction(e -> {
            resendBtn.setDisable(true);
            resendBtn.setText("Bhej rahe hain...");
            String newOtp = OtpDAO.generateOtp();
            OtpDAO.saveOtp(user.getId(), newOtp);

            new Thread(() -> {
                EmailService.sendEmail(user.getEmail(), "Mohsin Studio - Naya Login Code",
                        "Aapka naya verification code: " + newOtp + "\n\nYe code 5 minute ke liye valid hai.");

                javafx.application.Platform.runLater(() -> {
                    messageLabel.getStyleClass().setAll("success-text");
                    messageLabel.setText("Naya code bhej diya gaya hai.");
                    resendBtn.setDisable(false);
                    resendBtn.setText("Code Dobara Bhejein");
                });
            }).start();
        });

        VBox cardContent = new VBox(14);
        cardContent.setAlignment(Pos.CENTER);
        cardContent.getChildren().addAll(titleLabel, subtitleLabel, infoLabel, new Region(),
                otpField, verifyBtn, messageLabel, resendBtn, backBtn);

        VBox card = new VBox(cardContent);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(400);
        card.setAlignment(Pos.CENTER);

        StackPane root = new StackPane(card);
        root.getStyleClass().add("login-container");
        root.setPadding(new Insets(40));

        Scene scene = new Scene(root, 480, 560);
        Utils.ThemeManager.applyTheme(scene);

        stage.setTitle("Mohsin Studio - Verification");
        stage.setScene(scene);
        stage.show();

        card.setOpacity(0);
        card.setScaleX(0.92);
        card.setScaleY(0.92);
        FadeTransition fade = new FadeTransition(Duration.millis(400), card);
        fade.setFromValue(0);
        fade.setToValue(1);
        ScaleTransition scale = new ScaleTransition(Duration.millis(400), card);
        scale.setFromX(0.92);
        scale.setFromY(0.92);
        scale.setToX(1);
        scale.setToY(1);
        new ParallelTransition(fade, scale).play();
    }

    private void sendOtpEmailInBackground(String otp) {
        new Thread(() -> {
            String subject = "Mohsin Studio - Login Verification Code";
            String body = "Assalam o Alaikum " + user.getFullName() + ",\n\n" +
                    "Aapka login verification code: " + otp + "\n\n" +
                    "Ye code 5 minute ke liye valid hai. Agar ye aapne request nahi kiya, to is email ko ignore karein.\n\n" +
                    "Mohsin Movies and Photo Studio";
            EmailService.sendEmail(user.getEmail(), subject, body);
        }).start();
    }

    // Email ko partially chupata hai display ke liye, e.g. ab***@gmail.com
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String namePart = parts[0];
        String visiblePart = namePart.length() > 2 ? namePart.substring(0, 2) : namePart;
        return visiblePart + "***@" + parts[1];
    }
}