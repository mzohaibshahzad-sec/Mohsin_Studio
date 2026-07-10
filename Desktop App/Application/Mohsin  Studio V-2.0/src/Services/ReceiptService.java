package Services;

import Models.Order;
import Models.Package;
import Database.PackageDAO;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.util.List;

public class ReceiptService {

    public static String generateReceipt(Order order, double alreadyPaid, java.util.List<Models.Payment> payments) {
        try {
            String fileName = "receipt_order_" + order.getId() + "_" + System.currentTimeMillis() + ".pdf";
            String filePath = System.getProperty("java.io.tmpdir") + "/" + fileName;

            Document document = new Document(PageSize.A4, 25, 25, 15, 15);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Package fetch karo agar order mein package hai
            Package pkg = null;
            if (order.getPackageId() != null) {
                pkg = PackageDAO.getById(order.getPackageId());
            }

            addReceiptCopy(document, order, alreadyPaid, payments, pkg, "CUSTOMER COPY");
            addCutLine(document);
            addReceiptCopy(document, order, alreadyPaid, payments, pkg, "CEO COPY (OFFICE RECORD)");

            document.close();
            return filePath;

        } catch (Exception e) {
            System.out.println("Error generating receipt: " + e.getMessage());
            return null;
        }
    }

    private static void addReceiptCopy(Document document, Order order, double alreadyPaid,
                                       java.util.List<Models.Payment> payments, Package pkg, String copyLabel) throws DocumentException {

        Font titleFont  = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(43, 40, 37));
        Font subFont    = new Font(Font.FontFamily.HELVETICA, 9,  Font.NORMAL, BaseColor.GRAY);
        Font labelFont  = new Font(Font.FontFamily.HELVETICA, 9,  Font.BOLD);
        Font valueFont  = new Font(Font.FontFamily.HELVETICA, 9,  Font.NORMAL);
        Font totalFont  = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Font badgeFont  = new Font(Font.FontFamily.HELVETICA, 8,  Font.BOLD, BaseColor.WHITE);
        Font footerFont = new Font(Font.FontFamily.HELVETICA, 8,  Font.ITALIC, BaseColor.GRAY);
        Font svcTitleFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, new BaseColor(43, 40, 37));
        Font svcFont    = new Font(Font.FontFamily.HELVETICA, 8,  Font.NORMAL);
        Font svcHeaderFont = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.WHITE);

        // ===== Header =====
        Paragraph title = new Paragraph("Mohsin Movies and Photo Studio", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(1);
        document.add(title);

        Paragraph sub = new Paragraph("Order Receipt", subFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(3);
        document.add(sub);

        // Copy badge
        PdfPTable badgeTable = new PdfPTable(1);
        badgeTable.setWidthPercentage(35);
        badgeTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell badgeCell = new PdfPCell(new Phrase(copyLabel, badgeFont));
        badgeCell.setBackgroundColor(new BaseColor(201, 138, 68));
        badgeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        badgeCell.setPadding(3);
        badgeCell.setBorder(Rectangle.NO_BORDER);
        badgeTable.addCell(badgeCell);
        badgeTable.setSpacingBefore(2);
        badgeTable.setSpacingAfter(4);
        document.add(badgeTable);

        addDivider(document);

        // ===== Info Table =====
        PdfPTable infoTable = new PdfPTable(4);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1.3f, 2.2f, 1.3f, 2.2f});
        infoTable.setSpacingBefore(2);
        infoTable.setSpacingAfter(3);

        addInfoCell(infoTable, "Receipt No:", labelFont);
        addInfoCell(infoTable, "ORD-" + order.getId(), valueFont);
        addInfoCell(infoTable, "Order Date:", labelFont);
        addInfoCell(infoTable, order.getOrderDate() != null ? order.getOrderDate().toString() : "-", valueFont);

        addInfoCell(infoTable, "Customer:", labelFont);
        addInfoCell(infoTable, order.getCustomerName(), valueFont);
        addInfoCell(infoTable, "Phone:", labelFont);
        addInfoCell(infoTable, order.getCustomerPhone() != null ? order.getCustomerPhone() : "-", valueFont);

        addInfoCell(infoTable, "Order Type:", labelFont);
        addInfoCell(infoTable, order.getOrderType(), valueFont);
        addInfoCell(infoTable, "Status:", labelFont);
        addInfoCell(infoTable, order.getStatus(), valueFont);

        addInfoCell(infoTable, "Event Date:", labelFont);
        addInfoCell(infoTable, order.getEventDate() != null ? order.getEventDate().toString() : "-", valueFont);
        addInfoCell(infoTable, "Delivery Date:", labelFont);
        addInfoCell(infoTable, order.getDeliveryDate() != null ? order.getDeliveryDate().toString() : "-", valueFont);

        // Package name agar ho
        if (pkg != null) {
            addInfoCell(infoTable, "Package:", labelFont);
            addInfoCell(infoTable, pkg.getPackageName(), valueFont);
            addInfoCell(infoTable, "", labelFont);
            addInfoCell(infoTable, "", valueFont);
        }

        document.add(infoTable);

        // ===== SERVICES SECTION (naya!) =====
        if (pkg != null) {
            List<String[]> services = pkg.getParsedServices();
            if (!services.isEmpty()) {
                addDivider(document);

                Paragraph svcTitle = new Paragraph("Package Services", svcTitleFont);
                svcTitle.setSpacingAfter(3);
                document.add(svcTitle);

                PdfPTable svcTable = new PdfPTable(1);
                svcTable.setWidthPercentage(100);
                svcTable.setSpacingAfter(4);

                // Header row
                PdfPCell sh1 = new PdfPCell(new Phrase("Service", svcHeaderFont));
                sh1.setBackgroundColor(new BaseColor(201, 138, 68));
                sh1.setPadding(4);
                svcTable.addCell(sh1);

                // Service rows
                for (String[] svc : services) {
                    String svcName = svc[0];
                    String svcDays = svc.length > 2 && !svc[2].isEmpty() ? svc[2] + " day" : "";
                    String display = svcDays.isEmpty() ? svcName : svcName + " (" + svcDays + ")";

                    PdfPCell nc = new PdfPCell(new Phrase(display, svcFont));
                    nc.setPadding(4);
                    nc.setBorderColor(new BaseColor(220, 220, 220));
                    svcTable.addCell(nc);
                }

                document.add(svcTable);
            }
        }

        addDivider(document);

        // ===== Amount Summary =====
        double advancePaid = order.getAdvancePaid();
        double totalPaid   = alreadyPaid + advancePaid;
        double balance     = order.getAmount() - totalPaid;

        PdfPTable amountTable = new PdfPTable(2);
        amountTable.setWidthPercentage(55);
        amountTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        amountTable.setWidths(new float[]{1.5f, 1.3f});
        amountTable.setSpacingAfter(3);

        addInfoCell(amountTable, "Total Amount:", labelFont);
        addInfoCell(amountTable, "Rs. " + (int) order.getAmount(), totalFont);

        if (advancePaid > 0) {
            addInfoCell(amountTable, "Advance Paid:", labelFont);
            addInfoCell(amountTable, "Rs. " + (int) advancePaid, valueFont);
        }

        addInfoCell(amountTable, "Payments Paid:", labelFont);
        addInfoCell(amountTable, "Rs. " + (int) alreadyPaid, valueFont);

        addInfoCell(amountTable, "Balance Due:", labelFont);
        Font balFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,
                balance <= 0 ? new BaseColor(39, 174, 96) : new BaseColor(231, 76, 60));
        PdfPCell balCell = new PdfPCell(new Phrase("Rs. " + (int) balance, balFont));
        balCell.setBorder(Rectangle.NO_BORDER);
        balCell.setPadding(3);
        amountTable.addCell(balCell);

        document.add(amountTable);

        // ===== Payment History =====
        if (payments != null && !payments.isEmpty()) {
            addDivider(document);

            Paragraph histTitle = new Paragraph("Payment History", svcTitleFont);
            histTitle.setSpacingAfter(2);
            document.add(histTitle);

            PdfPTable histTable = new PdfPTable(4);
            histTable.setWidthPercentage(100);
            histTable.setWidths(new float[]{1.5f, 1.3f, 1.5f, 2.2f});
            histTable.setSpacingAfter(3);

            Font histHeaderFont = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.WHITE);
            for (String h : new String[]{"Date", "Amount", "Method", "Transaction ID"}) {
                PdfPCell hc = new PdfPCell(new Phrase(h, histHeaderFont));
                hc.setBackgroundColor(new BaseColor(201, 138, 68));
                hc.setPadding(3);
                histTable.addCell(hc);
            }

            Font histCellFont = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
            for (Models.Payment p : payments) {
                PdfPCell c1 = new PdfPCell(new Phrase(p.getPaymentDate() != null ? p.getPaymentDate().toString() : "-", histCellFont));
                c1.setPadding(3); histTable.addCell(c1);
                PdfPCell c2 = new PdfPCell(new Phrase("Rs. " + (int) p.getAdvancePaid(), histCellFont));
                c2.setPadding(3); histTable.addCell(c2);
                PdfPCell c3 = new PdfPCell(new Phrase(p.getPaymentMethod() != null ? p.getPaymentMethod() : "-", histCellFont));
                c3.setPadding(3); histTable.addCell(c3);
                PdfPCell c4 = new PdfPCell(new Phrase(
                        (p.getTransactionId() != null && !p.getTransactionId().trim().isEmpty())
                                ? p.getTransactionId() : "-", histCellFont));
                c4.setPadding(3); histTable.addCell(c4);
            }
            document.add(histTable);
        }

        // ===== Notes =====
        if (order.getNotes() != null && !order.getNotes().isEmpty()) {
            Font notesFont = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY);
            Paragraph notes = new Paragraph("Notes: " + order.getNotes(), notesFont);
            notes.setSpacingBefore(3);
            document.add(notes);
        }

        // ===== Footer =====
        Paragraph footer = new Paragraph("Thanks! - Mohsin Movies and Photo Studio", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(4);
        document.add(footer);
    }

    private static void addDivider(Document document) throws DocumentException {
        Paragraph divider = new Paragraph("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        divider.setAlignment(Element.ALIGN_CENTER);
        divider.setSpacingBefore(2);
        divider.setSpacingAfter(3);
        document.add(divider);
    }

    private static void addCutLine(Document document) throws DocumentException {
        Font cutFont = new Font(Font.FontFamily.HELVETICA, 7, Font.ITALIC, BaseColor.GRAY);
        Paragraph cutLine = new Paragraph(
                "✂ - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ✂",
                cutFont);
        cutLine.setAlignment(Element.ALIGN_CENTER);
        cutLine.setSpacingBefore(5);
        cutLine.setSpacingAfter(5);
        document.add(cutLine);
    }

    private static void addInfoCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(2);
        table.addCell(cell);
    }
}