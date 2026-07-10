package Database;

import Models.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    // Type = "PACKAGE" ya "SALES" - sirf active categories list mein milengi
    public static List<Category> getAllByType(String type) {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE type = ? AND is_active = TRUE ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, type);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            System.out.println("Error fetching categories: " + e.getMessage());
        }
        return list;
    }

    // Sirf naam (String) chahiye ho to - ComboBox mein direct use karne ke liye
    public static List<String> getNamesByType(String type) {
        List<String> names = new ArrayList<>();
        for (Category c : getAllByType(type)) names.add(c.getName());
        return names;
    }

    public static boolean addCategory(String name, String type) {
        if (name == null || name.trim().isEmpty()) return false;
        String sql = "INSERT INTO categories (name, type, is_active) VALUES (?, ?, TRUE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name.trim());
            stmt.setString(2, type);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error adding category: " + e.getMessage());
            return false;
        }
    }

    public static boolean renameCategory(int id, String newName) {
        String sql = "UPDATE categories SET name=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newName.trim());
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error renaming category: " + e.getMessage());
            return false;
        }
    }

    // Hard delete na karke deactivate karte hain - taake purane Packages/Sales records kharab na hon
    public static boolean deactivateCategory(int id) {
        String sql = "UPDATE categories SET is_active = FALSE WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error deactivating category: " + e.getMessage());
            return false;
        }
    }

    public static boolean categoryExists(String name, String type) {
        String sql = "SELECT id FROM categories WHERE LOWER(name) = LOWER(?) AND type = ? AND is_active = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name.trim());
            stmt.setString(2, type);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            System.out.println("Error checking category: " + e.getMessage());
            return false;
        }
    }

    private static Category mapRow(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setType(rs.getString("type"));
        c.setActive(rs.getBoolean("is_active"));
        return c;
    }
}