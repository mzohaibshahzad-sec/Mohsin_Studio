import Database.DBConnection;
import Security.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class CreateCEO {

    public static void main(String[] args) {

        // ===== Apni CEO details yahan likhein =====
        String fullName = "Bilal Ahmed";              // <-- apna naam likhein
        String username = "bilalahmed";           // <-- login username
        String plainPassword = "Bilal@978";       // <-- apna password (mazboot rakhein)
        String email = "mosinmoviesandphotostudio@gmail.com";     // <-- apna email
        String phone = "03456751380";              // <-- apna phone number
        // =============================================

        String hashedPassword = PasswordUtil.hashPassword(plainPassword);

        String sql = "INSERT INTO users (full_name, username, password_hash, role, email, phone, is_active) " +
                "VALUES (?, ?, ?, 'CEO', ?, ?, TRUE)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fullName);
            stmt.setString(2, username);
            stmt.setString(3, hashedPassword);
            stmt.setString(4, email);
            stmt.setString(5, phone);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("CEO account created successfully!");
                System.out.println("Username: " + username);
                System.out.println("Password: " + plainPassword + " (yaad rakhein, ye plain text yahan sirf reference ke liye hai)");
            }

        } catch (Exception e) {
            System.out.println("Error creating CEO account: " + e.getMessage());
        }
    }
}