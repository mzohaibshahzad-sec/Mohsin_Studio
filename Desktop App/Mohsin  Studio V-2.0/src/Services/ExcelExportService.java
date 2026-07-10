package Services;

import Models.Customer;
import Models.Order;
import Models.SalesEntry;
import Models.SalesStatPoint;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.List;

public class ExcelExportService {

    // ===== Common base folder: ~/MohsinStudioBackups/<SubFolder>/ =====
    private static String getExportFolder(String subFolder) {
        String homeDir = System.getProperty("user.home");
        java.io.File dir = new java.io.File(homeDir + "/MohsinStudioBackups/" + subFolder);
        if (!dir.exists()) dir.mkdirs();
        return dir.getAbsolutePath();
    }

    // ===== Orders ko Excel mein export karna =====
    public static String exportOrders(List<Order> orders) {
        try {
            String filePath = getExportFolder("Orders") + "/orders_export_" + System.currentTimeMillis() + ".xlsx";

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Orders");

            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {"Order #", "Customer", "Phone", "Order Type", "Amount", "Order Date",
                    "Event Date", "Delivery Date", "Status", "Booked By"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Order o : orders) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(o.getId());
                row.createCell(1).setCellValue(o.getCustomerName());
                row.createCell(2).setCellValue(o.getCustomerPhone());
                row.createCell(3).setCellValue(o.getOrderType());
                row.createCell(4).setCellValue(o.getAmount());
                row.createCell(5).setCellValue(o.getOrderDate() != null ? o.getOrderDate().toString() : "");
                row.createCell(6).setCellValue(o.getEventDate() != null ? o.getEventDate().toString() : "");
                row.createCell(7).setCellValue(o.getDeliveryDate() != null ? o.getDeliveryDate().toString() : "");
                row.createCell(8).setCellValue(o.getStatus());
                row.createCell(9).setCellValue(o.getCreatedByName());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
            workbook.close();

            return filePath;

        } catch (Exception e) {
            System.out.println("Error exporting orders to Excel: " + e.getMessage());
            return null;
        }
    }

    // ===== Sales Entries ko Excel mein export karna =====
    public static String exportSalesEntries(List<SalesEntry> entries, String title) {
        try {
            String filePath = getExportFolder("Sales") + "/sales_export_" + System.currentTimeMillis() + ".xlsx";

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Sales Entries");

            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {"Type", "Description", "Amount", "Entered By", "Date", "Notes"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            double total = 0;
            for (SalesEntry entry : entries) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getEntryType());
                row.createCell(1).setCellValue(entry.getDescription() != null ? entry.getDescription() : "");
                row.createCell(2).setCellValue(entry.getAmount());
                row.createCell(3).setCellValue(entry.getEnteredByName());
                row.createCell(4).setCellValue(entry.getEntryDate() != null ? entry.getEntryDate().toString() : "");
                row.createCell(5).setCellValue(entry.getNotes() != null ? entry.getNotes() : "");
                total += entry.getAmount();
            }

            // Total row
            Row totalRow = sheet.createRow(rowNum + 1);
            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            Cell totalLabelCell = totalRow.createCell(1);
            totalLabelCell.setCellValue("Total:");
            totalLabelCell.setCellStyle(boldStyle);

            Cell totalValueCell = totalRow.createCell(2);
            totalValueCell.setCellValue(total);
            totalValueCell.setCellStyle(boldStyle);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
            workbook.close();

            return filePath;

        } catch (Exception e) {
            System.out.println("Error exporting sales to Excel: " + e.getMessage());
            return null;
        }
    }

    // ===== Customers ko Excel mein export karna (CEO backup) =====
    public static String exportCustomers(List<Customer> customers) {
        try {
            // Backup folder: ~/MohsinStudioBackups/Customers/
            String fileName = "customers_backup_" + java.time.LocalDate.now() + "_" + System.currentTimeMillis() + ".xlsx";
            String filePath = getExportFolder("Customers") + "/" + fileName;

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Customers");

            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {"ID", "Naam", "Phone", "Email", "Address"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Customer c : customers) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(c.getId());
                row.createCell(1).setCellValue(c.getName() != null ? c.getName() : "");
                row.createCell(2).setCellValue(c.getPhone() != null ? c.getPhone() : "");
                row.createCell(3).setCellValue(c.getEmail() != null ? c.getEmail() : "");
                row.createCell(4).setCellValue(c.getAddress() != null ? c.getAddress() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
            workbook.close();

            System.out.println("Customer backup saved: " + filePath);
            return filePath;

        } catch (Exception e) {
            System.out.println("Error exporting customers to Excel: " + e.getMessage());
            return null;
        }
    }

    // ===== Stats exports: Monthly aur Yearly - Sale alag, Orders alag =====

    public static String exportMonthlySales(List<SalesStatPoint> points) {
        return exportStatsColumn(points, "Month", "Shop Sale (Rs.)", true, "monthly_sales");
    }

    public static String exportMonthlyOrders(List<SalesStatPoint> points) {
        return exportStatsColumn(points, "Month", "Orders (Rs.)", false, "monthly_orders");
    }

    public static String exportYearlySales(List<SalesStatPoint> points) {
        return exportStatsColumn(points, "Year", "Shop Sale (Rs.)", true, "yearly_sales");
    }

    public static String exportYearlyOrders(List<SalesStatPoint> points) {
        return exportStatsColumn(points, "Year", "Orders (Rs.)", false, "yearly_orders");
    }

    // Common helper - period (Month/Year) ke against sirf ek value column (Sale ya Orders) export karta hai
    private static String exportStatsColumn(List<SalesStatPoint> points, String periodLabel,
                                            String valueLabel, boolean useShopSales, String fileNamePrefix) {
        try {
            String filePath = getExportFolder("Stats") + "/" + fileNamePrefix + "_" + System.currentTimeMillis() + ".xlsx";

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet(periodLabel + " Stats");

            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {periodLabel, valueLabel};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            double total = 0;
            for (SalesStatPoint p : points) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getLabel());
                double value = useShopSales ? p.getShopSales() : p.getOrdersRevenue();
                row.createCell(1).setCellValue(value);
                total += value;
            }

            // Total row
            Row totalRow = sheet.createRow(rowNum + 1);
            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("Total:");
            totalLabelCell.setCellStyle(boldStyle);

            Cell totalValueCell = totalRow.createCell(1);
            totalValueCell.setCellValue(total);
            totalValueCell.setCellStyle(boldStyle);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
            workbook.close();

            return filePath;

        } catch (Exception e) {
            System.out.println("Error exporting stats to Excel: " + e.getMessage());
            return null;
        }
    }

    // ===== Audit Logs ko Excel mein export karna =====
    public static String exportAuditLogs(List<Models.AuditLog> logs) {
        try {
            String filePath = getExportFolder("AuditLogs") + "/audit_logs_export_" + System.currentTimeMillis() + ".xlsx";

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Audit Logs");

            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {"Time", "User", "Action", "Table", "Target ID", "Details"};

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Models.AuditLog log : logs) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(log.getTimestamp() != null ? log.getTimestamp().toString() : "");
                row.createCell(1).setCellValue(log.getUserName() != null ? log.getUserName() : "");
                row.createCell(2).setCellValue(log.getAction() != null ? log.getAction() : "");
                row.createCell(3).setCellValue(log.getTargetTable() != null ? log.getTargetTable() : "");
                row.createCell(4).setCellValue(log.getTargetId() != null ? log.getTargetId() : 0);
                row.createCell(5).setCellValue(log.getDetails() != null ? log.getDetails() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
            workbook.close();

            return filePath;

        } catch (Exception e) {
            System.out.println("Error exporting audit logs to Excel: " + e.getMessage());
            return null;
        }
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}