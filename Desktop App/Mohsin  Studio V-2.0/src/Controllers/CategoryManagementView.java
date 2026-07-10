package Controllers;

import Database.CategoryDAO;
import Models.Category;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * CEO yahan se Package categories aur Sales Entry categories
 * khud add/rename/delete kar sakta hai - koi code change nahi karna parta.
 */
public class CategoryManagementView {

    public VBox getView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        Label title = new Label("Manage Categories");
        title.getStyleClass().add("page-title");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Tab pkgTab = new Tab("Package Categories");
        pkgTab.setContent(buildCategoryTab("PACKAGE"));
        tabPane.getTabs().add(pkgTab);

        Tab salesTab = new Tab("Sales Entry Categories");
        salesTab.setContent(buildCategoryTab("SALES"));
        tabPane.getTabs().add(salesTab);

        container.getChildren().addAll(title, tabPane);
        return container;
    }

    private VBox buildCategoryTab(String type) {
        VBox box = new VBox(12);
        box.setPadding(new Insets(15));

        ListView<Category> listView = new ListView<>();
        VBox.setVgrow(listView, Priority.ALWAYS);
        refreshList(listView, type);

        TextField nameField = new TextField();
        nameField.setPromptText("Nayi category ka naam");
        nameField.setPrefWidth(220);

        Label msgLabel = new Label();
        msgLabel.setWrapText(true);

        Button addBtn = new Button("+ Add Category");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                msgLabel.getStyleClass().setAll("error-text");
                msgLabel.setText("Category ka naam likhein.");
                return;
            }
            if (CategoryDAO.categoryExists(name, type)) {
                msgLabel.getStyleClass().setAll("error-text");
                msgLabel.setText("Yeh category pehle se mojood hai.");
                return;
            }
            if (CategoryDAO.addCategory(name, type)) {
                msgLabel.getStyleClass().setAll("success-text");
                msgLabel.setText("Category add ho gayi!");
                nameField.clear();
                refreshList(listView, type);
            } else {
                msgLabel.getStyleClass().setAll("error-text");
                msgLabel.setText("Add nahi ho saki.");
            }
        });

        Button renameBtn = new Button("✏ Rename");
        renameBtn.getStyleClass().add("btn-secondary");
        renameBtn.setOnAction(e -> {
            Category selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                msgLabel.getStyleClass().setAll("error-text");
                msgLabel.setText("Pehle koi category select karein.");
                return;
            }
            TextInputDialog dialog = new TextInputDialog(selected.getName());
            dialog.setTitle("Category Rename Karein");
            dialog.setHeaderText(null);
            dialog.setContentText("Naya naam:");
            dialog.showAndWait().ifPresent(newName -> {
                if (!newName.trim().isEmpty() && CategoryDAO.renameCategory(selected.getId(), newName.trim())) {
                    msgLabel.getStyleClass().setAll("success-text");
                    msgLabel.setText("Category rename ho gayi!");
                    refreshList(listView, type);
                }
            });
        });

        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setOnAction(e -> {
            Category selected = listView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                msgLabel.getStyleClass().setAll("error-text");
                msgLabel.setText("Pehle koi category select karein.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Kya '" + selected.getName() + "' delete karna chahte hain?\n(Purane Packages/Entries is naam ke sath theek rahenge, sirf nayi list se hat jayegi)");
            confirm.showAndWait().ifPresent(resp -> {
                if (resp == ButtonType.OK) {
                    CategoryDAO.deactivateCategory(selected.getId());
                    msgLabel.getStyleClass().setAll("success-text");
                    msgLabel.setText("Category remove ho gayi.");
                    refreshList(listView, type);
                }
            });
        });

        HBox addRow = new HBox(10, nameField, addBtn);
        addRow.setAlignment(Pos.CENTER_LEFT);

        HBox actionRow = new HBox(10, renameBtn, deleteBtn);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(listView, addRow, actionRow, msgLabel);
        return box;
    }

    private void refreshList(ListView<Category> listView, String type) {
        List<Category> categories = CategoryDAO.getAllByType(type);
        listView.setItems(FXCollections.observableArrayList(categories));
    }
}