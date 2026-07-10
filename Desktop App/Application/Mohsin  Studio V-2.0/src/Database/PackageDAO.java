package Database;

import Models.Package;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PackageDAO {

    public static List<Package> getAllPackages() {
        List<Package> packages = new ArrayList<>();
        String sql = "SELECT * FROM packages ORDER BY category, package_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                packages.add(mapRow(rs));
            }

        } catch (Exception e) {
            System.out.println("Error fetching packages: " + e.getMessage());
        }
        return packages;
    }

    public static Package getById(int id) {
        String sql = "SELECT * FROM packages WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (Exception e) {
            System.out.println("Error fetching package by id: " + e.getMessage());
        }
        return null;
    }

    public static boolean addPackage(Package pkg, int createdByUserId) {
        String sql = "INSERT INTO packages (package_name, description, price, category, services, discount, created_by) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pkg.getPackageName());
            stmt.setString(2, pkg.getDescription());
            stmt.setDouble(3, pkg.getPrice());
            stmt.setString(4, pkg.getCategory());
            stmt.setString(5, pkg.getServices());
            stmt.setDouble(6, pkg.getDiscount());
            stmt.setInt(7, createdByUserId);

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Error adding package: " + e.getMessage());
            return false;
        }
    }

    public static boolean updatePackage(Package pkg) {
        String sql = "UPDATE packages SET package_name=?, description=?, price=?, category=?, services=?, discount=?, is_active=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pkg.getPackageName());
            stmt.setString(2, pkg.getDescription());
            stmt.setDouble(3, pkg.getPrice());
            stmt.setString(4, pkg.getCategory());
            stmt.setString(5, pkg.getServices());
            stmt.setDouble(6, pkg.getDiscount());
            stmt.setBoolean(7, pkg.isActive());
            stmt.setInt(8, pkg.getId());

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Error updating package: " + e.getMessage());
            return false;
        }
    }

    public static boolean deletePackage(int packageId) {
        String sql = "DELETE FROM packages WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, packageId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error deleting package: " + e.getMessage());
            return false;
        }
    }

    private static Package mapRow(ResultSet rs) throws SQLException {
        Package pkg = new Package();
        pkg.setId(rs.getInt("id"));
        pkg.setPackageName(rs.getString("package_name"));
        pkg.setDescription(rs.getString("description"));
        pkg.setPrice(rs.getDouble("price"));
        pkg.setCategory(rs.getString("category"));
        pkg.setActive(rs.getBoolean("is_active"));

        // Naye fields - safely read karo
        try { pkg.setServices(rs.getString("services")); } catch (Exception ignored) {}
        try { pkg.setDiscount(rs.getDouble("discount")); } catch (Exception ignored) {}

        return pkg;
    }
}