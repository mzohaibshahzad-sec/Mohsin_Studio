import Database.DBConnection;
import Security.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class CreateAdmin {

    public static void main(String[] args) {

        // ===== Apni Admin details yahan likhein =====
        String fullName = "Zohaib Mughal";                          // <-- apna naam
        String username = "zohaib";                       // <-- login username (yaad rakhein)
        String plainPassword = "zohaib@978";     // <-- mazboot password
        String email = "mzohaibshahzad.sec@gmail.com";      // <-- apna email (OTP yahan aayega)
        String phone = "03472324993";                        // <-- apna number
        // =================================================

        String hashedPassword = PasswordUtil.hashPassword(plainPassword);

        String sql = "INSERT INTO users (full_name, username, password_hash, role, email, phone, is_active) " +
                "VALUES (?, ?, ?, 'ADMIN', ?, ?, TRUE)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fullName);
            stmt.setString(2, username);
            stmt.setString(3, hashedPassword);
            stmt.setString(4, email);
            stmt.setString(5, phone);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("Admin account created successfully!");
                System.out.println("Username: " + username);
                System.out.println("Password: " + plainPassword + " (yaad rakhein)");
            }

        } catch (Exception e) {
            System.out.println("Error creating Admin account: " + e.getMessage());
        }
    }
}