package Database;

import Models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    // Username se user dhoondhta hai database mein
    public static User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFullName(rs.getString("full_name"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setRole(rs.getString("role"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setProfilePicturePath(rs.getString("profile_picture_path"));
                user.setActive(rs.getBoolean("is_active"));
                user.setLocked(rs.getBoolean("is_locked"));
                user.setLoginAttempts(rs.getInt("login_attempts"));
                return user;
            }

        } catch (Exception e) {
            System.out.println("Error fetching user: " + e.getMessage());
        }

        return null; // user nahi mila
    }

    // Login attempts ginne ke liye (galat password baar baar try hone par)
    public static void incrementLoginAttempts(int userId) {
        String sql = "UPDATE users SET login_attempts = login_attempts + 1 WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error updating login attempts: " + e.getMessage());
        }
    }

    // Successful login ke baad attempts reset karta hai
    public static void resetLoginAttempts(int userId) {
        String sql = "UPDATE users SET login_attempts = 0 WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error resetting login attempts: " + e.getMessage());
        }
    }

    // Account lock karta hai 5 se zyada galat attempts ke baad
    public static void lockAccount(int userId) {
        String sql = "UPDATE users SET is_locked = TRUE WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error locking account: " + e.getMessage());
        }
    }

    // ===== Naye methods - User Management (CEO only) ke liye =====

    // Naya user (clerk/co-founder/CEO) banata hai
    public static boolean createUser(String fullName, String username, String hashedPassword,
                                     String role, String email, String phone) {
        String sql = "INSERT INTO users (full_name, username, password_hash, role, email, phone, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, TRUE)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fullName);
            stmt.setString(2, username);
            stmt.setString(3, hashedPassword);
            stmt.setString(4, role);
            stmt.setString(5, email);
            stmt.setString(6, phone);

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Error creating user: " + e.getMessage());
            return false;
        }
    }

    // Sab users ki list (CEO ke liye)
    public static java.util.List<Models.User> getAllUsers() {
        java.util.List<Models.User> users = new java.util.ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY role, full_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Models.User user = new Models.User();
                user.setId(rs.getInt("id"));
                user.setFullName(rs.getString("full_name"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setProfilePicturePath(rs.getString("profile_picture_path"));
                user.setActive(rs.getBoolean("is_active"));
                user.setLocked(rs.getBoolean("is_locked"));
                users.add(user);
            }

        } catch (Exception e) {
            System.out.println("Error fetching users: " + e.getMessage());
        }
        return users;
    }

    // User ko activate/deactivate karta hai
    public static boolean setUserActive(int userId, boolean active) {
        String sql = "UPDATE users SET is_active = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error updating user status: " + e.getMessage());
            return false;
        }
    }

    // Locked account ko unlock karta hai (attempts bhi reset)
    public static boolean unlockAccount(int userId) {
        String sql = "UPDATE users SET is_locked = FALSE, login_attempts = 0 WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error unlocking account: " + e.getMessage());
            return false;
        }
    }

    // Username pehle se exist karta hai ya nahi check karta hai
    public static boolean usernameExists(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            System.out.println("Error checking username: " + e.getMessage());
            return false;
        }
    }

    // CEO ka email nikalta hai (daily report bhejne ke liye)
    public static String getCeoEmail() {
        String sql = "SELECT email FROM users WHERE role = 'CEO' LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (Exception e) {
            System.out.println("Error fetching CEO email: " + e.getMessage());
        }
        return null;
    }

    // Profile data update karta hai (naam, email, phone)
    public static boolean updateProfile(int userId, String fullName, String email, String phone) {
        String sql = "UPDATE users SET full_name=?, email=?, phone=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setInt(4, userId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error updating profile: " + e.getMessage());
            return false;
        }
    }

    // Password update karta hai (already hashed password expect karta hai)
    public static boolean updatePassword(int userId, String newHashedPassword) {
        String sql = "UPDATE users SET password_hash=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newHashedPassword);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error updating password: " + e.getMessage());
            return false;
        }
    }

    // Profile picture ka path save karta hai
    public static boolean updateProfilePicture(int userId, String picturePath) {
        String sql = "UPDATE users SET profile_picture_path=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, picturePath);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error updating profile picture: " + e.getMessage());
            return false;
        }
    }

    // User ID se fresh data fetch karta hai (profile refresh ke liye)
    public static Models.User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Models.User user = new Models.User();
                user.setId(rs.getInt("id"));
                user.setFullName(rs.getString("full_name"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setRole(rs.getString("role"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setProfilePicturePath(rs.getString("profile_picture_path"));
                user.setActive(rs.getBoolean("is_active"));
                user.setLocked(rs.getBoolean("is_locked"));
                user.setLoginAttempts(rs.getInt("login_attempts"));
                return user;
            }
        } catch (Exception e) {
            System.out.println("Error fetching user by id: " + e.getMessage());
        }
        return null;
    }

}