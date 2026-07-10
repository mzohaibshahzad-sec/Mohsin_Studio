package Database;

import java.sql.*;
import java.util.Random;

public class OtpDAO {

    // 6-digit random OTP generate karta hai
    public static String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 100000 to 999999
        return String.valueOf(otp);
    }

    // OTP database mein save karta hai (5 minute expiry ke sath)
    public static boolean saveOtp(int userId, String otpCode) {
        invalidateOldOtps(userId);

        String sql = "INSERT INTO login_otps (user_id, otp_code, expires_at) VALUES (?, ?, DATE_ADD(NOW(), INTERVAL 5 MINUTE))";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, otpCode);

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Error saving OTP: " + e.getMessage());
            return false;
        }
    }

    // OTP verify karta hai - sahi, expired nahi, aur already used nahi hona chahiye
    public static boolean verifyOtp(int userId, String enteredOtp) {
        String sql = "SELECT id FROM login_otps WHERE user_id = ? AND otp_code = ? " +
                "AND used = FALSE AND expires_at > NOW() ORDER BY created_at DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, enteredOtp);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int otpId = rs.getInt("id");
                markAsUsed(otpId);
                return true;
            }

        } catch (Exception e) {
            System.out.println("Error verifying OTP: " + e.getMessage());
        }
        return false;
    }

    private static void markAsUsed(int otpId) {
        String sql = "UPDATE login_otps SET used = TRUE WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, otpId);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error marking OTP used: " + e.getMessage());
        }
    }

    private static void invalidateOldOtps(int userId) {
        String sql = "UPDATE login_otps SET used = TRUE WHERE user_id = ? AND used = FALSE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error invalidating old OTPs: " + e.getMessage());
        }
    }
}