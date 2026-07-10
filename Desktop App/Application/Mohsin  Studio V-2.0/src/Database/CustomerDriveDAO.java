package Database;

import Models.CustomerDrive;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDriveDAO {

    // Naya drive record add karta hai, generated ID return karta hai
    public static int addDrive(CustomerDrive drive) {
        String sql = "INSERT INTO customer_drives (customer_id, drive_name, location, notes) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, drive.getCustomerId());
            stmt.setString(2, drive.getDriveName());
            stmt.setString(3, drive.getLocation());
            stmt.setString(4, drive.getNotes());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

        } catch (Exception e) {
            System.out.println("Error adding customer drive: " + e.getMessage());
        }
        return -1;
    }

    // Ek customer ke sab drives list karta hai
    public static List<CustomerDrive> getDrivesForCustomer(int customerId) {
        List<CustomerDrive> drives = new ArrayList<>();
        String sql = "SELECT * FROM customer_drives WHERE customer_id = ? ORDER BY id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                drives.add(mapRow(rs));
            }

        } catch (Exception e) {
            System.out.println("Error fetching customer drives: " + e.getMessage());
        }
        return drives;
    }

    public static boolean updateDrive(CustomerDrive drive) {
        String sql = "UPDATE customer_drives SET drive_name=?, location=?, notes=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, drive.getDriveName());
            ps.setString(2, drive.getLocation());
            ps.setString(3, drive.getNotes());
            ps.setInt(4, drive.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error updating customer drive: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteDrive(int driveId) {
        String sql = "DELETE FROM customer_drives WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, driveId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error deleting customer drive: " + e.getMessage());
            return false;
        }
    }

    private static CustomerDrive mapRow(ResultSet rs) throws SQLException {
        CustomerDrive d = new CustomerDrive();
        d.setId(rs.getInt("id"));
        d.setCustomerId(rs.getInt("customer_id"));
        d.setDriveName(rs.getString("drive_name"));
        d.setLocation(rs.getString("location"));
        d.setNotes(rs.getString("notes"));
        return d;
    }
}
