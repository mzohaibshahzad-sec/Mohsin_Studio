package Services;

import Models.SalesEntry;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.List;

public class BackupService {

    // Backup folder: user ke home directory mein "MohsinStudioBackups"
    private static String getBackupFolderPath() {
        String userHome = System.getProperty("user.home");
        String folderPath = userHome + File.separator + "MohsinStudioBackups";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folderPath;
    }

    // Aaj ki sab clerks ki combined sales ka backup Excel banata hai
    public static String backupTodaySales(List<SalesEntry> allTodayEntries) {
        try {
            LocalDate today = LocalDate.now();
            String folderPath = getBackupFolderPath();
            String fileName = "sales_backup_" + today + ".xlsx";
            String filePath = folderPath + File.separator + fileName;

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Daily Sales - " + today);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = {"Clerk/Staff Name", "Entry Type", "Description", "Amount (Rs.)", "Notes"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            double grandTotal = 0;
            for (SalesEntry entry : allTodayEntries) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.getEnteredByName());
                row.createCell(1).setCellValue(entry.getEntryType());
                row.createCell(2).setCellValue(entry.getDescription() != null ? entry.getDescription() : "");
                row.createCell(3).setCellValue(entry.getAmount());
                row.createCell(4).setCellValue(entry.getNotes() != null ? entry.getNotes() : "");
                grandTotal += entry.getAmount();
            }

            // Grand total row
            Row totalRow = sheet.createRow(rowNum + 1);
            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            Cell totalLabelCell = totalRow.createCell(2);
            totalLabelCell.setCellValue("Grand Total:");
            totalLabelCell.setCellStyle(boldStyle);

            Cell totalValueCell = totalRow.createCell(3);
            totalValueCell.setCellValue(grandTotal);
            totalValueCell.setCellStyle(boldStyle);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
            workbook.close();

            System.out.println("Daily backup saved: " + filePath);
            return filePath;

        } catch (Exception e) {
            System.out.println("Error creating daily backup: " + e.getMessage());
            return null;
        }
    }
}