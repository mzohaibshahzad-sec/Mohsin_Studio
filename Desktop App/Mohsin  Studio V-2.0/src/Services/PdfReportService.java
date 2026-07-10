package Services;

import Models.SalesEntry;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfReportService {

    // Daily sales report PDF banata hai, file path return karta hai
    public static String generateDailySalesReport(List<SalesEntry> entries, String clerkName, LocalDate date) {
        try {
            String fileName = "daily_report_" + date.toString() + "_" + System.currentTimeMillis() + ".pdf";
            String filePath = System.getProperty("java.io.tmpdir") + "/" + fileName;

            Document document = new Document(PageSize.A4, 36, 36, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // ===== Header =====
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Mohsin Movies and Photo Studio", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.GRAY);
            Paragraph subtitle = new Paragraph("Daily Sales Report", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            document.add(new Paragraph(" "));

            Font normalFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            document.add(new Paragraph("Date: " + date.format(formatter), normalFont));
            document.add(new Paragraph("Clerk: " + clerkName, normalFont));
            document.add(new Paragraph(" "));

            // ===== Table =====
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 4f, 2f, 3f});

            Font headerFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
            String[] headers = {"Type", "Description", "Amount (Rs.)", "Notes"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new BaseColor(44, 62, 80));
                cell.setPadding(8);
                table.addCell(cell);
            }

            double total = 0;
            for (SalesEntry entry : entries) {
                table.addCell(new PdfPCell(new Phrase(entry.getEntryType(), normalFont)) {{ setPadding(6); }});
                table.addCell(new PdfPCell(new Phrase(entry.getDescription() != null ? entry.getDescription() : "-", normalFont)) {{ setPadding(6); }});
                table.addCell(new PdfPCell(new Phrase(String.valueOf(entry.getAmount()), normalFont)) {{ setPadding(6); }});
                table.addCell(new PdfPCell(new Phrase(entry.getNotes() != null ? entry.getNotes() : "-", normalFont)) {{ setPadding(6); }});
                total += entry.getAmount();
            }

            document.add(table);
            document.add(new Paragraph(" "));

            Font totalFont = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD);
            Paragraph totalPara = new Paragraph("Total Entries: " + entries.size() + "      Total Sales: Rs. " + total, totalFont);
            totalPara.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalPara);

            document.close();
            return filePath;

        } catch (Exception e) {
            System.out.println("Error generating PDF: " + e.getMessage());
            return null;
        }
    }
}