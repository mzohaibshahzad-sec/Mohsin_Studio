package Services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.util.Properties;

public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SENDER_EMAIL = "mohsinmoviesandphotostudio@gmail.com";
    private static final String SENDER_APP_PASSWORD = "psdkmkvxbvfdmmot";

    private static Session getSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PASSWORD);
            }
        });
    }

    // Plain text email
    public static boolean sendEmail(String toEmail, String subject, String body) {
        try {
            Session session = getSession();
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, "Mohsin Movies and Photo Studio"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            System.out.println("Email sent to " + toEmail);
            return true;
        } catch (Exception e) {
            System.out.println("Error sending email: " + e.getMessage());
            return false;
        }
    }

    // HTML email - order confirmation ke liye (poster style)
    public static boolean sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            Session session = getSession();
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, "Mohsin Movies and Photo Studio"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            // HTML + plain text dono bhejo (compatibility ke liye)
            MimeMultipart multipart = new MimeMultipart("alternative");

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText("Aapka order confirm ho gaya hai. HTML email dekhen.", "utf-8");

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html; charset=utf-8");

            multipart.addBodyPart(textPart);
            multipart.addBodyPart(htmlPart);

            message.setContent(multipart);
            Transport.send(message);
            System.out.println("HTML Email sent to " + toEmail);
            return true;
        } catch (Exception e) {
            System.out.println("Error sending HTML email: " + e.getMessage());
            return false;
        }
    }

    // Attachment ke saath email
    public static boolean sendEmailWithAttachment(String toEmail, String subject, String body, String attachmentPath) {
        try {
            java.io.File file = new java.io.File(attachmentPath);
            if (!file.exists()) { System.out.println("Attachment not found: " + attachmentPath); return false; }
            if (file.length() == 0) { System.out.println("Attachment empty: " + attachmentPath); return false; }

            Session session = getSession();
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, "Mohsin Movies and Photo Studio"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body);

            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(file);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);
            Transport.send(message);
            System.out.println("Email with attachment sent to " + toEmail);
            return true;
        } catch (Exception e) {
            System.out.println("Error sending email with attachment: " + e.getMessage());
            return false;
        }
    }

    // Order confirmation (plain text - purana method, backward compatibility)
    public static boolean sendOrderConfirmation(String customerEmail, String customerName, String orderType,
                                                double amount, String eventDate, String deliveryDate) {
        String subject = "Order Confirmed - Mohsin Movies and Photo Studio";
        String body = "Assalam o Alaikum " + customerName + ",\n\n"
                + "Congragulations! Your Order Have Been Confirmed.\n\n"
                + "Order Type: " + orderType + "\n"
                + "Amount: Rs. " + amount + "\n"
                + "Event Date: " + (eventDate != null ? eventDate : "N/A") + "\n"
                + "Delivery Date: " + (deliveryDate != null ? deliveryDate : "N/A") + "\n\n"
                + "Shukriya,\nMohsin Movies and Photo Studio";
        return sendEmail(customerEmail, subject, body);
    }

    // Daily report
    public static boolean sendDailyReport(String ceoEmail, String clerkName, String reportDate,
                                          double totalSales, int totalEntries) {
        String subject = "Daily Sales Report - " + reportDate;
        String body = "Daily Sales Report\n\nClerk: " + clerkName + "\nDate: " + reportDate
                + "\nTotal Entries: " + totalEntries + "\nTotal Sales: Rs. " + totalSales
                + "\n\nMohsin Movies and Photo Studio - Automated Report";
        return sendEmail(ceoEmail, subject, body);
    }
}