package Services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WhatsAppHelper {

    // Order confirmation ke liye WhatsApp message link banata hai
    public static String buildOrderConfirmationLink(String customerPhone, String customerName, String orderType,
                                                    double amount, String eventDate, String deliveryDate) {

        String message = "Assalam o Alaikum " + customerName + ",\n\n" +
                "Aapka order confirm ho gaya hai - Mohsin Movies and Photo Studio\n\n" +
                "Order Type: " + orderType + "\n" +
                "Amount: Rs. " + amount + "\n" +
                "Event Date: " + (eventDate != null ? eventDate : "N/A") + "\n" +
                "Delivery Date: " + (deliveryDate != null ? deliveryDate : "N/A") + "\n\n" +
                "Shukriya!";

        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
        String cleanPhone = formatPhoneForWhatsApp(customerPhone);

        return "https://wa.me/" + cleanPhone + "?text=" + encodedMessage;
    }

    // Pakistani phone numbers ko WhatsApp ke format mein convert karta hai
    // Example: 03001234567 -> 923001234567
    private static String formatPhoneForWhatsApp(String phone) {
        String cleaned = phone.replaceAll("[^0-9]", ""); // sirf digits rakhega

        if (cleaned.startsWith("0")) {
            cleaned = "92" + cleaned.substring(1); // 0300... -> 92300...
        } else if (!cleaned.startsWith("92")) {
            cleaned = "92" + cleaned; // agar country code missing hai
        }

        return cleaned;
    }
}