package Services;

import Models.Order;
import Models.Package;

import java.util.List;

public class PosterGenerator {

    private static String iconFor(String service) {
        if (service == null) return "✦";
        String s = service.trim().toLowerCase();
        if (s.contains("photo") && s.contains("album")) return "📸";
        if (s.contains("photo")) return "📷";
        if (s.contains("video") && s.contains("short")) return "🎬";
        if (s.contains("video")) return "🎥";
        if (s.contains("drone")) return "🚁";
        if (s.contains("sound")) return "🎵";
        if (s.contains("reel")) return "🎞️";
        if (s.contains("edit")) return "✂️";
        if (s.contains("print")) return "🖨️";
        if (s.contains("frame")) return "🖼️";
        return "✦";
    }

    private static String socialFooter() {
        return "<div style='text-align:center; margin-top:18px; padding-top:14px; border-top:1px solid rgba(212,165,116,0.2);'>"
                + "<div style='color:#d4a574; font-size:11px; font-weight:600; letter-spacing:1px; margin-bottom:10px;'>Regards — Mohsin Movies &amp; Photo Studio</div>"
                + "<div style='display:flex; justify-content:center; gap:18px; flex-wrap:wrap;'>"
                + "  <a href='https://www.instagram.com/mohsinphotography89' style='color:#d4a574; text-decoration:none; font-size:11px; display:flex; align-items:center; gap:5px;'>"
                + "    <span style='background:#d4a574; color:#0d0d1a; font-size:9px; font-weight:900; padding:2px 5px; border-radius:3px;'>IG</span>"
                + "    mohsinphotography89"
                + "  </a>"
                + "  <a href='https://www.tiktok.com/@mohsinphotography89' style='color:#d4a574; text-decoration:none; font-size:11px; display:flex; align-items:center; gap:5px;'>"
                + "    <span style='background:#d4a574; color:#0d0d1a; font-size:9px; font-weight:900; padding:2px 5px; border-radius:3px;'>TT</span>"
                + "    mohsinphotography89"
                + "  </a>"
                + "</div>"
                + "<div style='color:#505070; font-size:9px; letter-spacing:2px; text-transform:uppercase; margin-top:10px;'>Mohsin Movies &amp; Photo Studio — New Rana Market Kotli Bawa Faqir , Pasrur , Sialkot</div>"
                + "</div>";
    }

    public static String generatePackagePoster(Package pkg, String customerName, String eventDate, int days) {
        List<String[]> parsedServices = pkg.getParsedServices();

        StringBuilder serviceRows = new StringBuilder();
        double computedTotal = 0;
        boolean hasPrices = false;

        for (String[] svc : parsedServices) {
            String name = svc[0];
            String priceStr = svc[1];
            String icon = iconFor(name);

            if (!priceStr.isEmpty()) {
                hasPrices = true;
                try {
                    double price = Double.parseDouble(priceStr);
                    computedTotal += price;
                } catch (Exception ignored) {}
            }
            serviceRows.append("<tr>")
                    .append("<td class='svc-icon'>").append(icon).append("</td>")
                    .append("<td class='svc-name' colspan='2'>").append(escHtml(name)).append("</td>")
                    .append("</tr>");
        }

        double baseTotal = hasPrices ? computedTotal : pkg.getPrice();
        double discount = pkg.getDiscount();
        double savedAmount = baseTotal * discount / 100.0;
        double finalPrice = baseTotal - savedAmount;

        String discountRow = "";
        if (discount > 0) {
            discountRow = "<div class='discount-badge'>🎉 SPECIAL DISCOUNT: " + String.format("%.0f", discount)
                    + "% OFF — Rs. " + String.format("%.0f", savedAmount) + " Ki Bachat!</div>";
        }

        String description = pkg.getDescription() != null ? pkg.getDescription() : "";
        String categoryDisplay = pkg.getCategory() != null ? pkg.getCategory().replace("_", " ") : "";

        String tableHeader = "<tr class='th-row'><th></th><th colspan='2'>Service</th></tr>";

        String css = "<style>"
                + "* { margin:0; padding:0; box-sizing:border-box; }"
                + "body { font-family: 'Segoe UI', Tahoma, Arial, sans-serif; background:#0d0d1a; color:#eee; }"
                + ".poster { max-width:500px; margin:0 auto; background:linear-gradient(160deg,#16213e 0%,#0f3460 55%,#1a1a2e 100%); min-height:100vh; padding:32px 28px; }"
                + ".header { text-align:center; margin-bottom:22px; }"
                + ".header-line { width:60px; height:2px; background:linear-gradient(90deg,transparent,#d4a574,transparent); margin:0 auto 12px; }"
                + ".studio-name { font-size:26px; font-weight:900; letter-spacing:4px; color:#d4a574; text-transform:uppercase; }"
                + ".studio-sub { font-size:9px; letter-spacing:6px; color:#8080a0; text-transform:uppercase; margin-top:5px; }"
                + ".studio-tagline { font-size:11px; color:#a0a0c0; margin-top:6px; font-style:italic; }"
                + ".divider { border:none; border-top:1px solid rgba(212,165,116,0.25); margin:18px 0; }"
                + ".pkg-name { font-size:21px; font-weight:800; color:#fff; text-align:center; margin-bottom:4px; line-height:1.3; }"
                + ".pkg-category { font-size:9px; letter-spacing:4px; color:#d4a574; text-transform:uppercase; text-align:center; margin-bottom:14px; }"
                + ".description-box { background:rgba(255,255,255,0.04); border-left:3px solid #d4a574; padding:10px 14px; border-radius:0 8px 8px 0; margin-bottom:16px; font-size:12px; color:#b0b0cc; line-height:1.6; }"
                + ".section-title { font-size:9px; letter-spacing:3px; color:#d4a574; text-transform:uppercase; margin-bottom:10px; font-weight:700; }"
                + ".services-table { width:100%; border-collapse:collapse; margin-bottom:16px; }"
                + ".th-row th { font-size:10px; letter-spacing:1px; color:#8080a0; text-transform:uppercase; padding:6px 4px; border-bottom:1px solid rgba(212,165,116,0.2); text-align:left; }"
                + ".services-table tr { border-bottom:1px solid rgba(255,255,255,0.05); }"
                + ".services-table tr:last-child { border-bottom:none; }"
                + ".svc-icon { padding:10px 6px; font-size:15px; color:#d4a574; width:30px; }"
                + ".svc-name { padding:10px 8px; font-size:13px; color:#e0e0f0; font-weight:500; }"
                + ".svc-total { padding:10px 4px; font-size:13px; color:#d4a574; font-weight:600; width:90px; text-align:right; }"
                + ".discount-badge { background:linear-gradient(135deg,rgba(39,174,96,0.2),rgba(39,174,96,0.08)); border:1px solid rgba(39,174,96,0.35); color:#2ecc71; padding:9px 14px; border-radius:8px; font-size:12px; font-weight:700; margin-bottom:14px; text-align:center; }"
                + ".total-box { background:linear-gradient(135deg,rgba(212,165,116,0.18),rgba(212,165,116,0.04)); border:1px solid rgba(212,165,116,0.45); border-radius:14px; padding:18px 22px; display:flex; justify-content:space-between; align-items:center; margin-bottom:22px; }"
                + ".total-label { font-size:10px; color:#8080a0; letter-spacing:2px; text-transform:uppercase; }"
                + ".total-amount { font-size:30px; font-weight:900; color:#d4a574; }"
                + ".star-row { text-align:center; font-size:13px; color:rgba(212,165,116,0.4); letter-spacing:8px; margin:10px 0; }"
                + "</style>";

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>" + css + "</head><body>"
                + "<div class='poster'>"
                + "<div class='header'>"
                + "  <div class='header-line'></div>"
                + "  <div class='studio-name'>Mohsin Studio</div>"
                + "  <div class='studio-sub'>Movies &amp; Photo Studio</div>"
                + "  <div class='studio-tagline'>Professional Photography &amp; Videography</div>"
                + "  <div class='header-line' style='margin-top:12px;'></div>"
                + "</div>"
                + "<div class='pkg-name'>" + escHtml(pkg.getPackageName()) + "</div>"
                + "<div class='pkg-category'>" + escHtml(categoryDisplay) + "</div>"
                + (description.isEmpty() ? "" : "<div class='description-box'>" + escHtml(description) + "</div>")
                + "<hr class='divider'>"
                + "<div class='section-title'>✦ Package Services</div>"
                + "<table class='services-table'>"
                + tableHeader
                + serviceRows
                + "</table>"
                + discountRow
                + "<div class='total-box'>"
                + "  <div class='total-label'>TOTAL AMOUNT</div>"
                + "  <div class='total-amount'>Rs. " + String.format("%.0f", finalPrice) + "</div>"
                + "</div>"
                + "<div class='star-row'>* * *</div>"
                + socialFooter()
                + "</div></body></html>";
    }

    // ===== ORDER CONFIRMATION RECEIPT - Clean White Invoice Style (Print-Friendly) =====
    public static String generateOrderConfirmationEmail(Order order, Package pkg) {

        // ===== Extra order detail rows (Package / Event Date / Delivery Date) =====
        String orderDetailsRows = "";
        if (pkg != null) {
            orderDetailsRows += "<tr><td style='padding:7px 0;font-size:12px;color:#6b7280;'>Package</td>"
                    + "<td style='padding:7px 0;font-size:13px;color:#1f2937;font-weight:600;text-align:right;'>"
                    + escHtml(pkg.getPackageName()) + "</td></tr>";
        }
        if (order.getEventDate() != null) {
            orderDetailsRows += "<tr><td style='padding:7px 0;font-size:12px;color:#6b7280;'>Event Date</td>"
                    + "<td style='padding:7px 0;font-size:13px;color:#1f2937;font-weight:600;text-align:right;'>"
                    + order.getEventDate() + "</td></tr>";
        }
        if (order.getDeliveryDate() != null) {
            orderDetailsRows += "<tr><td style='padding:7px 0;font-size:12px;color:#6b7280;'>Delivery Date</td>"
                    + "<td style='padding:7px 0;font-size:13px;color:#1f2937;font-weight:600;text-align:right;'>"
                    + order.getDeliveryDate() + "</td></tr>";
        }

        // ===== Package services table =====
        String servicesBlock = "";
        if (pkg != null) {
            List<String[]> parsed = pkg.getParsedServices();
            if (!parsed.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append("<table style='width:100%;border-collapse:collapse;margin-bottom:18px;'>");
                sb.append("<tr style='background:#1f2937;'>")
                        .append("<th style='text-align:left;padding:9px 12px;font-size:10px;letter-spacing:1px;color:#ffffff;text-transform:uppercase;font-weight:600;'>Service</th>")
                        .append("</tr>");

                for (int i = 0; i < parsed.size(); i++) {
                    String[] s = parsed.get(i);
                    String rowBg = (i % 2 == 0) ? "#ffffff" : "#f9fafb";
                    sb.append("<tr style='background:").append(rowBg).append(";'>")
                            .append("<td style='padding:9px 12px;font-size:13px;color:#1f2937;border-bottom:1px solid #e5e7eb;'>")
                            .append(escHtml(s[0])).append("</td>")
                            .append("</tr>");
                }
                sb.append("</table>");

                servicesBlock = "<div style='font-size:9px;letter-spacing:2px;color:#a9762f;text-transform:uppercase;font-weight:700;margin-bottom:8px;'>Services Included</div>"
                        + sb;
            }
        }

        String descriptionBlock = "";
        if (pkg != null && pkg.getDescription() != null && !pkg.getDescription().isEmpty()) {
            descriptionBlock = "<div style='background:#f9fafb;border-left:3px solid #a9762f;"
                    + "padding:10px 14px;border-radius:0 4px 4px 0;margin-bottom:16px;font-size:12px;color:#4b5563;line-height:1.6;'>"
                    + escHtml(pkg.getDescription()) + "</div>";
        }

        String discountBlock = "";
        if (pkg != null && pkg.getDiscount() > 0) {
            double saved = pkg.getPrice() * pkg.getDiscount() / 100.0;
            discountBlock = "<div style='text-align:right;font-size:12px;color:#15803d;font-weight:600;margin-bottom:16px;'>"
                    + "Discount Applied: " + (int) pkg.getDiscount() + "% &nbsp;(Rs. " + String.format("%.0f", saved) + " saved)</div>";
        }

        // ===== Payment summary =====
        double advance = order.getAdvancePaid();
        double balance = order.getAmount() - advance;

        String paymentRows = "<div style='display:flex;justify-content:space-between;padding:5px 0;'>"
                + "<span style='font-size:12px;color:#6b7280;'>Total Amount</span>"
                + "<span style='font-size:14px;font-weight:700;color:#1f2937;'>Rs. " + String.format("%.0f", order.getAmount()) + "</span>"
                + "</div>";

        if (advance > 0) {
            paymentRows += "<div style='display:flex;justify-content:space-between;padding:5px 0;'>"
                    + "<span style='font-size:12px;color:#6b7280;'>Advance Paid</span>"
                    + "<span style='font-size:13px;font-weight:600;color:#15803d;'>Rs. " + String.format("%.0f", advance) + "</span>"
                    + "</div>"
                    + "<div style='display:flex;justify-content:space-between;padding:8px 0 0;border-top:1px solid #e5e7eb;margin-top:4px;'>"
                    + "<span style='font-size:13px;font-weight:700;color:#b45309;'>Balance Due</span>"
                    + "<span style='font-size:16px;font-weight:800;color:#b45309;'>Rs. " + String.format("%.0f", balance) + "</span>"
                    + "</div>";
        }

        String notesBlock = "";
        if (order.getNotes() != null && !order.getNotes().isEmpty()) {
            notesBlock = "<div style='margin-top:16px;font-size:12px;color:#4b5563;background:#f9fafb;"
                    + "border:1px solid #e5e7eb;padding:12px 14px;border-radius:4px;line-height:1.6;'>"
                    + "<div style='font-size:9px;letter-spacing:2px;color:#a9762f;text-transform:uppercase;font-weight:700;margin-bottom:6px;'>Notes</div>"
                    + escHtml(order.getNotes()) + "</div>";
        }

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
                + "<body style='font-family:Georgia,\"Times New Roman\",serif;background:#f3f4f6;margin:0;padding:24px;'>"
                + "<div style='max-width:560px;margin:0 auto;background:#ffffff;border:1px solid #e5e7eb;border-radius:4px;overflow:hidden;'>"

                // Top accent bar
                + "<div style='height:6px;background:linear-gradient(90deg,#a9762f,#d4a574,#a9762f);'></div>"

                + "<div style='padding:36px 40px;'>"

                // Header
                + "<div style='text-align:center;margin-bottom:26px;'>"
                + "  <div style='font-size:26px;font-weight:700;letter-spacing:3px;color:#1f2937;text-transform:uppercase;'>Mohsin Studio</div>"
                + "  <div style='font-size:10px;letter-spacing:4px;color:#a9762f;text-transform:uppercase;margin-top:5px;font-weight:600;'>Movies &amp; Photo Studio</div>"
                + "  <div style='width:40px;height:2px;background:#a9762f;margin:14px auto 0;'></div>"
                + "</div>"

                // Title row: Order Confirmation + Order # + Status
                + "<div style='display:flex;justify-content:space-between;align-items:flex-end;border-bottom:2px solid #1f2937;padding-bottom:12px;margin-bottom:22px;'>"
                + "  <div>"
                + "    <div style='font-size:18px;font-weight:700;color:#1f2937;'>Order Confirmation</div>"
                + "    <div style='font-size:11px;color:#6b7280;margin-top:3px;'>Order # <strong style=\"color:#1f2937;\">" + order.getId() + "</strong></div>"
                + "  </div>"
                + "  <div style='text-align:right;'>"
                + "    <div style='font-size:10px;color:#6b7280;letter-spacing:1px;text-transform:uppercase;'>Status</div>"
                + "    <div style='font-size:13px;font-weight:700;color:#15803d;'>Confirmed</div>"
                + "  </div>"
                + "</div>"

                // Customer block
                + "<div style='margin-bottom:20px;'>"
                + "  <div style='font-size:9px;letter-spacing:2px;color:#a9762f;text-transform:uppercase;font-weight:700;margin-bottom:6px;'>Billed To</div>"
                + "  <div style='font-size:17px;font-weight:600;color:#1f2937;'>" + escHtml(order.getCustomerName()) + "</div>"
                + "</div>"

                // Order details table
                + "<table style='width:100%;border-collapse:collapse;margin-bottom:22px;'>"
                + "  <tr><td style='padding:7px 0;font-size:12px;color:#6b7280;'>Order Type</td>"
                + "      <td style='padding:7px 0;font-size:13px;color:#1f2937;font-weight:600;text-align:right;'>" + escHtml(order.getOrderType()) + "</td></tr>"
                + orderDetailsRows
                + "</table>"

                // Package description, services, discount
                + descriptionBlock + servicesBlock + discountBlock

                // Payment summary box
                + "<div style='background:#f9fafb;border:1px solid #e5e7eb;border-radius:4px;padding:16px 18px;margin-bottom:6px;'>"
                + paymentRows
                + "</div>"

                // Notes
                + notesBlock

                // Footer
                + "<div style='text-align:center;margin-top:30px;padding-top:18px;border-top:1px solid #e5e7eb;'>"
                + "  <div style='font-size:11px;color:#6b7280;margin-bottom:8px;'>Thank you for choosing Mohsin Studio</div>"
                + "  <div style='font-size:10px;color:#a9762f;font-weight:600;'>Instagram: @mohsinphotography89 &nbsp;|&nbsp; TikTok: @mohsinphotography89</div>"
                + "  <div style='font-size:9px;color:#9ca3af;margin-top:10px;letter-spacing:0.5px;'>New Rana Market Kotli Bawa Faqir, Pasrur, Sialkot</div>"
                + "</div>"

                + "</div></div></body></html>";
    }

    private static String escHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}