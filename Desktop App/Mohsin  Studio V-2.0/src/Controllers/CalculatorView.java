package Controllers;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CalculatorView {

    private TextField display;
    private Label expressionLabel;
    private String currentInput = "";
    private String operator = "";
    private double firstNumber = 0;
    private boolean newInput = false;
    private String fullExpression = "";
    private List<String> history = new ArrayList<>();
    private VBox historyBox;
    private ScrollPane historyScroll;

    // Permanent history file path
    private static final String HISTORY_FILE = System.getProperty("user.home") + "/MohsinStudioBackups/calc_history.txt";

    public HBox getView() {
        HBox root = new HBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);

        // ===== LEFT: Calculator =====
        VBox calcPane = new VBox(14);
        calcPane.setAlignment(Pos.TOP_CENTER);
        calcPane.setPrefWidth(360);

        Label title = new Label("🧮 Calculator");
        title.getStyleClass().add("page-title");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        expressionLabel = new Label("");
        expressionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #8080a0; -fx-padding: 0 6 0 6;");
        expressionLabel.setMaxWidth(340);
        expressionLabel.setAlignment(Pos.CENTER_RIGHT);

        display = new TextField("0");
        display.setEditable(false);
        display.setAlignment(Pos.CENTER_RIGHT);
        display.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-padding: 14 18 14 18; " +
                "-fx-background-radius: 10; -fx-background-color: rgba(255,255,255,0.08); " +
                "-fx-text-fill: #d4a574; -fx-border-color: rgba(212,165,116,0.3); " +
                "-fx-border-radius: 10; -fx-border-width: 1;");
        display.setPrefHeight(70);
        display.setMaxWidth(340);

        VBox displayBox = new VBox(4, expressionLabel, display);
        displayBox.setAlignment(Pos.CENTER_RIGHT);

        // Keyboard hint label
        Label keyHint = new Label("Calculator");
        keyHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #505070; -fx-font-style: italic;");

        // Buttons grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        grid.setMaxWidth(340);

        String[][] buttons = {
                {"C", "±", "%", "÷"},
                {"7", "8", "9", "×"},
                {"4", "5", "6", "−"},
                {"1", "2", "3", "+"},
                {"00", "0", ".", "="}
        };

        for (int row = 0; row < buttons.length; row++) {
            for (int col = 0; col < buttons[row].length; col++) {
                String label = buttons[row][col];
                Button btn = createButton(label);
                grid.add(btn, col, row);
            }
        }

        calcPane.getChildren().addAll(title, displayBox, keyHint, grid);

        // ===== RIGHT: History =====
        VBox historyPane = new VBox(10);
        historyPane.setPrefWidth(290);
        historyPane.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 12; -fx-padding: 16;");

        HBox histHeader = new HBox(10);
        histHeader.setAlignment(Pos.CENTER_LEFT);

        Label histTitle = new Label("📋 History");
        histTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #d4a574;");

        Region histSpacer = new Region();
        HBox.setHgrow(histSpacer, Priority.ALWAYS);

        Button clearHistBtn = new Button("🗑 Clear");
        clearHistBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5 12 5 12; " +
                "-fx-background-radius: 6; -fx-cursor: hand;");
        clearHistBtn.setOnAction(e -> clearHistory());

        histHeader.getChildren().addAll(histTitle, histSpacer, clearHistBtn);

        Separator sep = new Separator();

        historyBox = new VBox(6);
        historyBox.setAlignment(Pos.TOP_LEFT);

        historyScroll = new ScrollPane(historyBox);
        historyScroll.setFitToWidth(true);
        historyScroll.setStyle("-fx-background-color: transparent;");
        historyScroll.setPrefHeight(450);
        VBox.setVgrow(historyScroll, Priority.ALWAYS);

        historyPane.getChildren().addAll(histHeader, sep, historyScroll);

        root.getChildren().addAll(calcPane, historyPane);
        HBox.setHgrow(historyPane, Priority.ALWAYS);

        // ===== Keyboard Support =====
        root.setFocusTraversable(true);
        root.setOnKeyPressed(e -> handleKeyboard(e));

        // History file se load karo
        loadHistoryFromFile();

        // Focus lene ke liye
        Platform.runLater(() -> root.requestFocus());

        return root;
    }

    // ===== KEYBOARD HANDLER =====
    private void handleKeyboard(KeyEvent e) {
        switch (e.getCode()) {
            case DIGIT0: case NUMPAD0: handleButton("0"); break;
            case DIGIT1: case NUMPAD1: handleButton("1"); break;
            case DIGIT2: case NUMPAD2: handleButton("2"); break;
            case DIGIT3: case NUMPAD3: handleButton("3"); break;
            case DIGIT4: case NUMPAD4: handleButton("4"); break;
            case DIGIT5: case NUMPAD5:
                if (e.isShiftDown()) handleButton("%");
                else handleButton("5");
                break;
            case DIGIT6: case NUMPAD6: handleButton("6"); break;
            case DIGIT7: case NUMPAD7: handleButton("7"); break;
            case DIGIT8: case NUMPAD8:
                if (e.isShiftDown()) handleButton("×");
                else handleButton("8");
                break;
            case DIGIT9: case NUMPAD9: handleButton("9"); break;
            case PLUS:   case ADD:      handleButton("+"); break;
            case MINUS:  case SUBTRACT: handleButton("−"); break;
            case SLASH:  case DIVIDE:   handleButton("÷"); break;
            case MULTIPLY:              handleButton("×"); break;
            case PERIOD: case DECIMAL:  handleButton("."); break;
            case ENTER:  case EQUALS:   handleButton("="); break;
            case BACK_SPACE:            handleBackspace(); break;
            case ESCAPE:                handleButton("C"); break;
            default: break;
        }
    }

    private void handleBackspace() {
        if (currentInput.length() > 0) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
            display.setText(currentInput.isEmpty() ? "0" : currentInput);
        }
    }

    private Button createButton(String label) {
        Button btn = new Button(label);
        btn.setPrefWidth(75);
        btn.setPrefHeight(65);
        btn.setStyle(getButtonStyle(label));
        btn.setFocusTraversable(false);
        btn.setOnAction(e -> handleButton(label));

        String base = getButtonStyle(label);
        btn.setOnMouseEntered(e -> btn.setStyle(base + "-fx-opacity: 0.8;"));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    private String getButtonStyle(String label) {
        String base = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 12;";
        switch (label) {
            case "=":  return base + "-fx-background-color: #d4a574; -fx-text-fill: #0d0d1a;";
            case "C":  return base + "-fx-background-color: #e74c3c; -fx-text-fill: white;";
            case "÷": case "×": case "−": case "+":
                return base + "-fx-background-color: #2980b9; -fx-text-fill: white;";
            case "±": case "%":
                return base + "-fx-background-color: rgba(255,255,255,0.12); -fx-text-fill: #d4a574;";
            default:   return base + "-fx-background-color: rgba(255,255,255,0.07); -fx-text-fill: #eee;";
        }
    }

    private void handleButton(String label) {
        switch (label) {
            case "C":
                currentInput = ""; firstNumber = 0; operator = "";
                newInput = false; fullExpression = "";
                display.setText("0"); expressionLabel.setText("");
                break;
            case "±":
                try {
                    double val = -Double.parseDouble(display.getText());
                    display.setText(formatResult(val));
                    currentInput = display.getText();
                } catch (Exception ignored) {}
                break;
            case "%":
                try {
                    double val = Double.parseDouble(display.getText()) / 100;
                    display.setText(formatResult(val));
                    currentInput = display.getText();
                } catch (Exception ignored) {}
                break;
            case "+": case "−": case "×": case "÷":
                try { firstNumber = Double.parseDouble(display.getText()); } catch (Exception ignored) {}
                operator = label;
                fullExpression = display.getText() + " " + label;
                expressionLabel.setText(fullExpression);
                newInput = true;
                break;
            case "=":
                calculate();
                operator = ""; newInput = false;
                break;
            case ".":
                if (newInput) { currentInput = "0"; newInput = false; }
                if (!currentInput.contains(".")) {
                    currentInput = currentInput.isEmpty() ? "0." : currentInput + ".";
                    display.setText(currentInput);
                }
                break;
            default:
                if (newInput) { currentInput = ""; newInput = false; }
                currentInput += label;
                display.setText(currentInput);
                if (!operator.isEmpty()) expressionLabel.setText(fullExpression + " " + currentInput);
                break;
        }
    }

    private void calculate() {
        try {
            double second = Double.parseDouble(display.getText());
            double result = 0;
            boolean valid = true;

            switch (operator) {
                case "+": result = firstNumber + second; break;
                case "−": result = firstNumber - second; break;
                case "×": result = firstNumber * second; break;
                case "÷":
                    if (second == 0) {
                        display.setText("Error");
                        expressionLabel.setText("Zero se divide nahi ho sakta!");
                        return;
                    }
                    result = firstNumber / second; break;
                default: valid = false; break;
            }

            if (!valid) return;

            String resultStr = formatResult(result);
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
            String expr = formatResult(firstNumber) + " " + operator + " " + formatResult(second) + " = " + resultStr;
            String histEntry = time + " | " + expr;

            addToHistory(histEntry, expr);
            saveHistoryToFile(histEntry);

            display.setText(resultStr);
            expressionLabel.setText(expr);
            currentInput = resultStr;
            firstNumber  = result;
            fullExpression = "";

        } catch (Exception ignored) {}
    }

    // ===== HISTORY UI =====
    private void addToHistory(String histEntry, String expr) {
        history.add(0, histEntry);
        refreshHistoryUI();
    }

    private void refreshHistoryUI() {
        historyBox.getChildren().clear();

        if (history.isEmpty()) {
            Label empty = new Label("Koi history nahi.");
            empty.setStyle("-fx-text-fill: #606080; -fx-font-size: 12px; -fx-font-style: italic;");
            historyBox.getChildren().add(empty);
            return;
        }

        for (String h : history) {
            // Format: "dd/MM HH:mm | expr"
            String[] parts = h.split(" \\| ", 2);
            String time = parts.length > 1 ? parts[0] : "";
            String expr = parts.length > 1 ? parts[1] : h;

            VBox card = new VBox(3);
            card.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-padding: 8 12 8 12; -fx-cursor: hand;");

            Label exprLbl = new Label(expr);
            exprLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #d4a574;");
            exprLbl.setWrapText(true);

            Label timeLbl = new Label(time);
            timeLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #505070;");

            card.getChildren().addAll(exprLbl, timeLbl);

            // Click = result display mein
            String result = expr.contains("=") ? expr.substring(expr.lastIndexOf("=") + 1).trim() : expr;
            card.setOnMouseClicked(e -> {
                display.setText(result);
                currentInput = result;
                try { firstNumber = Double.parseDouble(result); } catch (Exception ignored) {}
            });
            card.setOnMouseEntered(ev -> card.setStyle("-fx-background-color: rgba(212,165,116,0.12); -fx-background-radius: 8; -fx-padding: 8 12 8 12; -fx-cursor: hand;"));
            card.setOnMouseExited(ev -> card.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-padding: 8 12 8 12; -fx-cursor: hand;"));

            historyBox.getChildren().add(card);
        }

        Platform.runLater(() -> historyScroll.setVvalue(0));
    }

    private void clearHistory() {
        history.clear();
        // File bhi clear karo
        try {
            Files.deleteIfExists(Paths.get(HISTORY_FILE));
        } catch (Exception ignored) {}
        refreshHistoryUI();
    }

    // ===== PERMANENT FILE SAVE/LOAD =====
    private void saveHistoryToFile(String entry) {
        try {
            File dir = new File(System.getProperty("user.home") + "/MohsinStudioBackups");
            if (!dir.exists()) dir.mkdirs();

            try (FileWriter fw = new FileWriter(HISTORY_FILE, true)) {
                fw.write(entry + "\n");
            }
        } catch (Exception e) {
            System.out.println("History save error: " + e.getMessage());
        }
    }

    private void loadHistoryFromFile() {
        try {
            File file = new File(HISTORY_FILE);
            if (!file.exists()) {
                refreshHistoryUI();
                return;
            }
            List<String> lines = Files.readAllLines(Paths.get(HISTORY_FILE));
            history.clear();
            // Latest upar dikhao
            for (int i = lines.size() - 1; i >= 0; i--) {
                String line = lines.get(i).trim();
                if (!line.isEmpty()) history.add(line);
            }
            refreshHistoryUI();
        } catch (Exception e) {
            System.out.println("History load error: " + e.getMessage());
            refreshHistoryUI();
        }
    }

    private String formatResult(double val) {
        if (val == (long) val) return String.valueOf((long) val);
        String s = String.format("%.8f", val);
        s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        return s;
    }
}