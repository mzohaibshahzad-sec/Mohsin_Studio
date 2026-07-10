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
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.sql.Date;
import java.time.LocalDate;

public class OrderBookingView {

    private User currentUser;

    public OrderBookingView(User currentUser) {
        this.currentUser = currentUser;
    }

    public VBox getView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        Label title = new Label("Order Book Karein");
        title.getStyleClass().add("page-title");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(12);

        // ===== Customer Section =====
        TextField customerNameField = new TextField();
        customerNameField.setPromptText("Customer ka naam");

        TextField customerPhoneField = new TextField();
        customerPhoneField.setPromptText("03001234567 (Zaroori)");

        // Email optional - label mein (optional) likha
        TextField customerEmailField = new TextField();
        customerEmailField.setPromptText("customer@email.com (Optional)");

        TextField customerAddressField = new TextField();
        customerAddressField.setPromptText("Pura Address");

        Label searchInfoLabel = new Label();
        searchInfoLabel.getStyleClass().add("success-text");

        customerPhoneField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused && !customerPhoneField.getText().trim().isEmpty()) {
                Customer existing = CustomerDAO.findByPhone(customerPhoneField.getText().trim());
                if (existing != null) {
                    customerNameField.setText(existing.getName());
                    customerEmailField.setText(existing.getEmail() != null ? existing.getEmail() : "");
                    customerAddressField.setText(existing.getAddress());
                    searchInfoLabel.setText("✅ Existing customer mil gaya - details auto-fill ho gayi hain");
                } else {
                    searchInfoLabel.setText("");
                }
            }
        });

        // ===== Order Section =====
        ComboBox<Package> packageBox = new ComboBox<>();
        packageBox.setItems(FXCollections.observableArrayList(PackageDAO.getAllPackages()));
        packageBox.setPromptText("Package Chunein (optional)");
        packageBox.setMaxWidth(Double.MAX_VALUE);

        TextField orderTypeField = new TextField();
        orderTypeField.setPromptText("e.g. Shaadi Event Recording, Passport Photos");

        TextField amountField = new TextField();
        amountField.setPromptText("Total Amount (Rs.)");

        // ===== Advance Payment Fields =====
        TextField advanceField = new TextField();
        advanceField.setPromptText("Advance Payment (Rs.)");

        Label balanceLabel = new Label("Remaining Balance: Rs. 0");
        balanceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #e67e22; -fx-font-size: 13px;");

        Runnable calcBalance = () -> {
            try {
                double total = Double.parseDouble(amountField.getText().trim());
                double advance = advanceField.getText().trim().isEmpty() ? 0
                        : Double.parseDouble(advanceField.getText().trim());
                double balance = total - advance;
                balanceLabel.setText("Remaining Balance: Rs. " + String.format("%.0f", balance));
                balanceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; " +
                        "-fx-text-fill: " + (balance <= 0 ? "#27ae60" : "#e67e22") + ";");
            } catch (NumberFormatException ex) {
                balanceLabel.setText("Remaining Balance: Rs. -");
                balanceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #e67e22;");
            }
        };

        amountField.textProperty().addListener((obs, o, n) -> calcBalance.run());
        advanceField.textProperty().addListener((obs, o, n) -> calcBalance.run());

        packageBox.setOnAction(e -> {
            Package selected = packageBox.getValue();
            if (selected != null) {
                amountField.setText(String.valueOf((int) selected.getFinalPrice()));
                orderTypeField.setText(selected.getPackageName());
                calcBalance.run();
            }
        });

        DatePicker eventDatePicker = new DatePicker();
        eventDatePicker.setPromptText("Event Date");

        DatePicker deliveryDatePicker = new DatePicker();
        deliveryDatePicker.setPromptText("Delivery Date");

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Extra notes (optional)");
        notesArea.setPrefRowCount(3);

        // ===== Layout =====
        int row = 0;
        form.add(new Label("Customer Naam:"), 0, row);
        form.add(customerNameField, 1, row++);

        form.add(new Label("Phone: *"), 0, row);
        form.add(customerPhoneField, 1, row++);

        // Email label mein (Optional) likha
        form.add(new Label("Email (Optional):"), 0, row);
        form.add(customerEmailField, 1, row++);

        form.add(new Label("Address:"), 0, row);
        form.add(customerAddressField, 1, row++);

        form.add(searchInfoLabel, 1, row++);

        form.add(new Label("Package:"), 0, row);
        form.add(packageBox, 1, row++);

        form.add(new Label("Order Type:"), 0, row);
        form.add(orderTypeField, 1, row++);

        form.add(new Label("Total Amount:"), 0, row);
        form.add(amountField, 1, row++);

        form.add(new Label("Advance Payment:"), 0, row);
        form.add(advanceField, 1, row++);

        form.add(new Label(""), 0, row);
        form.add(balanceLabel, 1, row++);

        form.add(new Label("Event Date:"), 0, row);
        form.add(eventDatePicker, 1, row++);

        form.add(new Label("Delivery Date:"), 0, row);
        form.add(deliveryDatePicker, 1, row++);

        form.add(new Label("Notes:"), 0, row);
        form.add(notesArea, 1, row++);

        Label messageLabel = new Label();

        Button submitBtn = new Button("Order Book Karein");
        submitBtn.getStyleClass().add("btn-primary");

        submitBtn.setOnAction(e -> {
            String name    = customerNameField.getText().trim();
            String phone   = customerPhoneField.getText().trim();
            String email   = customerEmailField.getText().trim(); // optional
            String address = customerAddressField.getText().trim();
            String orderType  = orderTypeField.getText().trim();
            String amountText = amountField.getText().trim();
            String advanceText = advanceField.getText().trim();
            LocalDate eventDate    = eventDatePicker.getValue();
            LocalDate deliveryDate = deliveryDatePicker.getValue();

            // Email optional - sirf name, phone, address, orderType, amount zaroori
            if (name.isEmpty() || phone.isEmpty() || address.isEmpty()
                    || orderType.isEmpty() || amountText.isEmpty()) {
                messageLabel.getStyleClass().setAll("error-text");
                messageLabel.setText("Naam, Phone, Address, Order Type aur Amount zaroori hain. (Email optional hai)");
                return;
            }

            // Phone validation
            if (!phone.matches("\\d{10,13}")) {
                messageLabel.getStyleClass().setAll("error-text");
                messageLabel.setText("Phone number sahi likhein (10-13 digits).");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountText);
            } catch (NumberFormatException ex) {
                messageLabel.getStyleClass().setAll("error-text");
                messageLabel.setText("Amount sirf number mein likhein.");
                return;
            }

            double advance = 0;
            if (!advanceText.isEmpty()) {
                try {
                    advance = Double.parseDouble(advanceText);
                    if (advance > amount) {
                        messageLabel.getStyleClass().setAll("error-text");
                        messageLabel.setText("Advance payment total amount se zyada nahi ho sakti!");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    messageLabel.getStyleClass().setAll("error-text");
                    messageLabel.setText("Advance payment sirf number mein likhein.");
                    return;
                }
            }

            Customer customer = CustomerDAO.findByPhone(phone);
            int customerId;
            if (customer == null) {
                Customer newCustomer = new Customer();
                newCustomer.setName(name);
                newCustomer.setPhone(phone);
                newCustomer.setEmail(email.isEmpty() ? null : email);
                newCustomer.setAddress(address);
                customerId = CustomerDAO.addCustomer(newCustomer);
            } else {
                customerId = customer.getId();
            }

            if (customerId == -1) {
                messageLabel.getStyleClass().setAll("error-text");
                messageLabel.setText("Customer save nahi ho saka. Dobara koshish karein.");
                return;
            }

            Order order = new Order();
            order.setCustomerId(customerId);
            Package selectedPkg = packageBox.getValue();
            order.setPackageId(selectedPkg != null ? selectedPkg.getId() : null);
            order.setOrderType(orderType);
            order.setAmount(amount);
            order.setAdvancePaid(advance);
            order.setOrderDate(Date.valueOf(LocalDate.now()));
            order.setEventDate(eventDate != null ? Date.valueOf(eventDate) : null);
            order.setDeliveryDate(deliveryDate != null ? Date.valueOf(deliveryDate) : null);
            order.setNotes(notesArea.getText().trim());
            order.setCreatedBy(currentUser.getId());

            int orderId = OrderDAO.createOrder(order);

            if (orderId != -1) {
                double balance = amount - advance;
                Database.AuditLogDAO.log(currentUser.getId(), "CREATED_ORDER", "orders", orderId,
                        "Order #" + orderId + " booked for " + name + " (Rs. " + amount + ")");

                messageLabel.getStyleClass().setAll("success-text");
                messageLabel.setText("✅ Order book ho gaya! (Order #" + orderId + ") " +
                        (advance > 0 ? "Advance: Rs. " + (int)advance + " | Balance: Rs. " + (int)balance : ""));

                order.setId(orderId);
                order.setCustomerName(name);

                // ===== Action Buttons =====
                Button posterBtn = new Button("👁 Poster Preview");
                posterBtn.getStyleClass().add("btn-secondary");
                posterBtn.setOnAction(ev -> showPosterPreview(order, selectedPkg,
                        eventDate != null ? eventDate.toString() : ""));

                // WhatsApp Button - hamesha show hoga (phone compulsory hai)
                Button whatsappBtn = new Button("💬 WhatsApp Par Bhejo");
                whatsappBtn.setStyle("-fx-background-color: #25D366; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-padding: 8 16 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
                final double finalAdvance = advance;
                final double finalBalance = balance;
                whatsappBtn.setOnAction(ev -> {
                    String msg = buildWhatsAppMessage(order, selectedPkg, eventDate, deliveryDate, finalAdvance, finalBalance);
                    sendWhatsApp(phone, msg);
                });

                // Email Button - sirf tab show hoga jab email ho
                HBox actionBtns = new HBox(10, posterBtn, whatsappBtn);
                if (!email.isEmpty()) {
                    Button emailBtn = new Button("📧 Email Bhejo");
                    emailBtn.getStyleClass().add("btn-primary");
                    emailBtn.setOnAction(ev -> {
                        String html = PosterGenerator.generateOrderConfirmationEmail(order, selectedPkg);
                        boolean sent = EmailService.sendHtmlEmail(email, "Order Confirm - Mohsin Studio", html);
                        showAlert(sent ? "✅ Email bhej di gayi! " + email : "❌ Email send nahi ho saki.");
                    });
                    actionBtns.getChildren().add(emailBtn);
                }

                actionBtns.setAlignment(Pos.CENTER_LEFT);
                container.getChildren().removeIf(n -> n.getId() != null && n.getId().equals("orderActionBtns"));
                actionBtns.setId("orderActionBtns");
                container.getChildren().add(actionBtns);

                // Form clear
                customerNameField.clear();
                customerPhoneField.clear();
                customerEmailField.clear();
                customerAddressField.clear();
                orderTypeField.clear();
                amountField.clear();
                advanceField.clear();
                eventDatePicker.setValue(null);
                deliveryDatePicker.setValue(null);
                notesArea.clear();
                packageBox.setValue(null);
                balanceLabel.setText("Remaining Balance: Rs. 0");

            } else {
                messageLabel.getStyleClass().setAll("error-text");
                messageLabel.setText("Order book nahi ho saka. Dobara koshish karein.");
            }
        });

        container.getChildren().addAll(title, form, submitBtn, messageLabel);
        return container;
    }

    // ===== WhatsApp Message Banana =====
    private String buildWhatsAppMessage(Order order, Package pkg, LocalDate eventDate,
                                        LocalDate deliveryDate, double advance, double balance) {
        StringBuilder sb = new StringBuilder();
        sb.append("🎬 *Mohsin Movies & Photo Studio*\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━\n\n");
        sb.append("Assalam o Alaikum, *").append(order.getCustomerName()).append("* Ji!\n\n");
        sb.append("✅ Aapka order book ho gaya hai. Shukriya!\n\n");
        sb.append("📋 *Order Details:*\n");
        sb.append("• Order #: *").append(order.getId()).append("*\n");
        sb.append("• Order Type: *").append(order.getOrderType()).append("*\n");

        if (pkg != null) {
            sb.append("• Package: *").append(pkg.getPackageName()).append("*\n");
            java.util.List<String[]> svcs = pkg.getParsedServices();
            if (!svcs.isEmpty()) {
                sb.append("• Services: *");
                for (int i = 0; i < svcs.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(svcs.get(i)[0]).append(" (").append(svcs.get(i)[2]).append(" day)");
                }
                sb.append("*\n");
            }
        }

        if (eventDate != null) {
            sb.append("• Event Date: *").append(eventDate).append("*\n");
        }
        if (deliveryDate != null) {
            sb.append("• Delivery Date: *").append(deliveryDate).append("*\n");
        }

        sb.append("\n💰 *Payment Details:*\n");
        sb.append("• Total Amount: *Rs. ").append(String.format("%.0f", order.getAmount())).append("*\n");

        if (advance > 0) {
            sb.append("• Advance Paid: *Rs. ").append(String.format("%.0f", advance)).append("*\n");
            sb.append("• Remaining Balance: *Rs. ").append(String.format("%.0f", balance)).append("*\n");
        }

        if (order.getNotes() != null && !order.getNotes().isEmpty()) {
            sb.append("\n📝 *Notes:*\n").append(order.getNotes()).append("\n");
        }

        sb.append("\n━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("📍 Mohsin Studio — New Rana Market , Ada Kotli Bawa Faqir, Pasrur , Sialkot\n");
        sb.append("📸 Instagram: @mohsinphotography89\n");
        sb.append("🎵 TikTok: @mohsinphotography89\n\n");
        sb.append("Shukriya! 🙏");

        return sb.toString();
    }

    // ===== WhatsApp Open Karna =====
    private void sendWhatsApp(String phone, String message) {
        try {
            // Phone number format: 03001234567 -> 923001234567
            String formattedPhone = phone.startsWith("0") ? "92" + phone.substring(1) : phone;
            String encoded = java.net.URLEncoder.encode(message, "UTF-8");
            String url = "https://wa.me/" + formattedPhone + "?text=" + encoded;
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            showAlert("WhatsApp nahi khul saka: " + e.getMessage());
        }
    }

    private void showPosterPreview(Order order, Package pkg, String eventDate) {
        Stage posterStage = new Stage();
        posterStage.initModality(Modality.APPLICATION_MODAL);
        posterStage.setTitle("Order Poster Preview");

        String html = PosterGenerator.generateOrderConfirmationEmail(order, pkg);

        WebView webView = new WebView();
        webView.getEngine().loadContent(html);

        // Page load complete hone ke baad WebView ko content ki pure height jitna resize karte hain,
        // taake download (snapshot) lete waqt poster ka koi hissa cut na ho
        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                try {
                    Object result = webView.getEngine().executeScript("document.documentElement.scrollHeight");
                    if (result instanceof Number) {
                        double contentHeight = ((Number) result).doubleValue();
                        webView.setPrefHeight(Math.max(contentHeight, 400));
                    }
                } catch (Exception ignored) {
                    // agar height nahi mil saki to default size hi rahegi
                }
            }
        });

        ScrollPane scrollPane = new ScrollPane(webView);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Button downloadBtn = new Button("💾 Download Poster");
        downloadBtn.getStyleClass().add("btn-primary");
        downloadBtn.setOnAction(e -> downloadPoster(webView, order));

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("btn-secondary");
        closeBtn.setOnAction(e -> posterStage.close());

        HBox btnRow = new HBox(10, downloadBtn, closeBtn);
        btnRow.setPadding(new Insets(10, 15, 10, 15));
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox(scrollPane, btnRow);

        Scene scene = new Scene(layout, 520, 700);
        Utils.ThemeManager.applyTheme(scene);
        posterStage.setScene(scene);
        posterStage.show();
    }

    // WebView mein dikh rahe poster ka snapshot le kar PNG image ke taur pe save karta hai
    private void downloadPoster(WebView webView, Order order) {
        try {
            SnapshotParameters params = new SnapshotParameters();
            WritableImage fxImage = webView.snapshot(params, null);
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(fxImage, null);

            String homeDir = System.getProperty("user.home");
            File posterDir = new File(homeDir + "/MohsinStudioPosters");
            if (!posterDir.exists()) posterDir.mkdirs();

            String customerPart = order.getCustomerName() != null
                    ? order.getCustomerName().replaceAll("[^a-zA-Z0-9]", "_")
                    : "Customer";
            String fileName = "Poster_Order" + order.getId() + "_" + customerPart + ".png";
            File outFile = new File(posterDir, fileName);

            ImageIO.write(bufferedImage, "png", outFile);

            showAlert("✅ Poster download ho gaya!\nPath: " + outFile.getAbsolutePath());
            openFile(outFile.getAbsolutePath());

        } catch (Exception ex) {
            showAlert("❌ Poster download nahi ho saka: " + ex.getMessage());
        }
    }

    private void openFile(String filePath) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("nux") || os.contains("nix")) {
                new ProcessBuilder("xdg-open", filePath).start();
            } else {
                Desktop.getDesktop().open(new File(filePath));
            }
        } catch (Exception ex) {
            System.out.println("Could not open file: " + ex.getMessage());
        }
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
}