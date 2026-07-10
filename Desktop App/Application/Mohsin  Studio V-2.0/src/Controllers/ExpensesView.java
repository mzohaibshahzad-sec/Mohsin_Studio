package Controllers;

import Database.ExpenseDAO;
import Models.Expense;
import Models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;



import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.*;
import java.sql.Date;
import java.time.LocalDate;

public class ExpensesView {

    private User currentUser;
    private TableView<Expense> table;
    private Label totalLabel;

    public ExpensesView(User currentUser) {
        this.currentUser = currentUser;
    }

    public VBox getView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        Label title = new Label("Expenses (Kharchay)");
        title.getStyleClass().add("page-title");

        // ===== Form =====
        TextField descField = new TextField();
        descField.setPromptText("Description (e.g. Shop Rent - June)");

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("RENT", "EQUIPMENT", "SALARY", "UTILITIES", "MAINTENANCE", "OTHER");
        categoryBox.setValue("OTHER");
        categoryBox.setMaxWidth(Double.MAX_VALUE);

        TextField amountField = new TextField();
        amountField.setPromptText("Amount (Rs.)");

        DatePicker dateField = new DatePicker(LocalDate.now());

        TextField notesField = new TextField();
        notesField.setPromptText("Notes (optional)");

        Label messageLabel = new Label();

        Button addBtn = new Button("Expense Add Karein");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> {
            String desc = descField.getText().trim();
            String amountText = amountField.getText().trim();
            LocalDate date = dateField.getValue();

            if (desc.isEmpty() || amountText.isEmpty() || date == null) {
                messageLabel.getStyleClass().setAll("error-text");
                messageLabel.setText("Description, Amount aur Date zaroori hain.");
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

            Expense expense = new Expense();
            expense.setDescription(desc);
            expense.setCategory(categoryBox.getValue());
            expense.setAmount(amount);
            expense.setExpenseDate(Date.valueOf(date));
            expense.setRecordedBy(currentUser.getId());
            expense.setNotes(notesField.getText().trim());

            boolean success = ExpenseDAO.addExpense(expense);

            if (success) {
                Database.AuditLogDAO.log(currentUser.getId(), "ADDED_EXPENSE", "expenses", null,
                        desc + " - Rs. " + amount + " (" + categoryBox.getValue() + ")");

                messageLabel.getStyleClass().setAll("success-text");
                messageLabel.setText("Expense add ho gaya!");
                descField.clear();
                amountField.clear();
                notesField.clear();
                categoryBox.setValue("OTHER");
                dateField.setValue(LocalDate.now());
                loadExpenses();
            } else {
                messageLabel.getStyleClass().setAll("error-text");
                messageLabel.setText("Expense add nahi ho saka.");
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("Description:"), 0, 0);
        form.add(descField, 1, 0);
        form.add(new Label("Category:"), 0, 1);
        form.add(categoryBox, 1, 1);
        form.add(new Label("Amount:"), 0, 2);
        form.add(amountField, 1, 2);
        form.add(new Label("Date:"), 0, 3);
        form.add(dateField, 1, 3);
        form.add(new Label("Notes:"), 0, 4);
        form.add(notesField, 1, 4);

        // ===== Table =====
        Label tableTitle = new Label("Sab Expenses");
        tableTitle.getStyleClass().add("section-title");

        table = new TableView<>();
        setupTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        totalLabel = new Label();
        totalLabel.getStyleClass().add("section-title");
        loadExpenses();

        Button deleteBtn = new Button("Selected Delete Karein");
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setOnAction(e -> {
            Expense selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                ExpenseDAO.deleteExpense(selected.getId());
                Database.AuditLogDAO.log(currentUser.getId(), "DELETED_EXPENSE", "expenses",
                        selected.getId(), selected.getDescription() + " - Rs. " + selected.getAmount());

                loadExpenses();
            }
        });

        // ===== Export Buttons =====
        Button exportExcelBtn = new Button("📊 Excel Export");
        exportExcelBtn.getStyleClass().add("btn-primary");
        exportExcelBtn.setOnAction(e -> exportToExcel());

        Button exportPdfBtn = new Button("📄 PDF Export");
        exportPdfBtn.getStyleClass().add("btn-primary");
        exportPdfBtn.setOnAction(e -> exportToPdf());

        HBox exportBox = new HBox(10, exportExcelBtn, exportPdfBtn);

        container.getChildren().addAll(title, form, addBtn, messageLabel, tableTitle, table, totalLabel, deleteBtn, exportBox);
        return container;
    }

    private void exportToExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Excel File Save Karein");
        fileChooser.setInitialFileName("Expenses.xlsx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(null);
        if (file == null) return;

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Expenses");

            // Header row
            Row header = sheet.createRow(0);
            String[] cols = {"Description", "Category", "Amount (Rs.)", "Date", "Recorded By", "Notes"};
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
            }

            // Data rows
            ObservableList<Expense> data = table.getItems();
            double total = 0;
            for (int i = 0; i < data.size(); i++) {
                Expense exp = data.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(exp.getDescription());
                row.createCell(1).setCellValue(exp.getCategory());
                row.createCell(2).setCellValue(exp.getAmount());
                row.createCell(3).setCellValue(exp.getExpenseDate() != null ? exp.getExpenseDate().toString() : "");
                row.createCell(4).setCellValue(exp.getRecordedByName() != null ? exp.getRecordedByName() : "");
                row.createCell(5).setCellValue(exp.getNotes() != null ? exp.getNotes() : "");
                total += exp.getAmount();
            }

            // Total row
            Row totalRow = sheet.createRow(data.size() + 1);
            totalRow.createCell(0).setCellValue("TOTAL");
            totalRow.createCell(2).setCellValue(total);

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Successful");
            alert.setContentText("Excel file save ho gayi:\n" + file.getAbsolutePath());
            alert.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Excel export mein error: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    private void exportToPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("PDF File Save Karein");
        fileChooser.setInitialFileName("Expenses.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);
        if (file == null) return;

        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("Mohsin Studio - Expenses Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("Date: " + LocalDate.now().toString()));
            document.add(Chunk.NEWLINE);

            // Table
            PdfPTable pdfTable = new PdfPTable(6);
            pdfTable.setWidthPercentage(100);
            pdfTable.setWidths(new float[]{3f, 2f, 2f, 2f, 2f, 3f});

            // Headers
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
            String[] headers = {"Description", "Category", "Amount (Rs.)", "Date", "Recorded By", "Notes"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(BaseColor.DARK_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6);
                pdfTable.addCell(cell);
            }

            // Data
            ObservableList<Expense> data = table.getItems();
            double total = 0;
            for (Expense exp : data) {
                pdfTable.addCell(exp.getDescription() != null ? exp.getDescription() : "");
                pdfTable.addCell(exp.getCategory() != null ? exp.getCategory() : "");
                pdfTable.addCell("Rs. " + String.format("%.0f", exp.getAmount()));
                pdfTable.addCell(exp.getExpenseDate() != null ? exp.getExpenseDate().toString() : "");
                pdfTable.addCell(exp.getRecordedByName() != null ? exp.getRecordedByName() : "");
                pdfTable.addCell(exp.getNotes() != null ? exp.getNotes() : "");
                total += exp.getAmount();
            }

            // Total row
            PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL", headerFont));
            totalLabelCell.setColspan(2);
            totalLabelCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            pdfTable.addCell(totalLabelCell);

            PdfPCell totalValueCell = new PdfPCell(new Phrase("Rs. " + String.format("%.0f", total), headerFont));
            totalValueCell.setColspan(4);
            totalValueCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            pdfTable.addCell(totalValueCell);

            document.add(pdfTable);
            document.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export Successful");
            alert.setContentText("PDF file save ho gayi:\n" + file.getAbsolutePath());
            alert.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("PDF export mein error: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        TableColumn<Expense, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(200);

        TableColumn<Expense, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(120);

        TableColumn<Expense, Double> amountCol = new TableColumn<>("Amount (Rs.)");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setPrefWidth(110);

        TableColumn<Expense, Date> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("expenseDate"));
        dateCol.setPrefWidth(110);

        TableColumn<Expense, String> byCol = new TableColumn<>("Recorded By");
        byCol.setCellValueFactory(new PropertyValueFactory<>("recordedByName"));
        byCol.setPrefWidth(130);

        table.getColumns().addAll(descCol, categoryCol, amountCol, dateCol, byCol);
    }

    private void loadExpenses() {
        ObservableList<Expense> data = FXCollections.observableArrayList(ExpenseDAO.getAllExpenses());
        table.setItems(data);

        double total = 0;
        for (Expense e : data) total += e.getAmount();
        totalLabel.setText("Total Expenses: Rs. " + total);
    }
}