package Database;

import Models.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    // Naya customer add karta hai, aur uska generated ID return karta hai
    // Auto-backup bhi hoti hai background mein
    public static int addCustomer(Customer customer) {
        String sql = "INSERT INTO customers (name, phone, email, address) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getPhone());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getAddress());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    int newId = keys.getInt(1);

                    // Auto-backup - background thread mein taake UI slow na ho
                    new Thread(() -> {
                        try {
                            List<Customer> allCustomers = getAllCustomers();
                            Services.ExcelExportService.exportCustomers(allCustomers);
                            System.out.println("Customer auto-backup complete. Total customers: " + allCustomers.size());
                        } catch (Exception e) {
                            System.out.println("Customer auto-backup error: " + e.getMessage());
                        }
                    }).start();

                    return newId;
                }
            }

        } catch (Exception e) {
            System.out.println("Error adding customer: " + e.getMessage());
        }
        return -1;
    }

    // Phone number se customer dhoondhta hai (taake dubara same customer na bane)
    public static Customer findByPhone(String phone) {
        String sql = "SELECT * FROM customers WHERE phone = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (Exception e) {
            System.out.println("Error finding customer: " + e.getMessage());
        }
        return null;
    }

    // Naam se search karta hai (partial match, jaise type karte hue suggestions)
    public static List<Customer> searchByName(String namePart) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE name LIKE ? ORDER BY name LIMIT 20";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + namePart + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                customers.add(mapRow(rs));
            }

        } catch (Exception e) {
            System.out.println("Error searching customers: " + e.getMessage());
        }
        return customers;
    }

    // Sab customers list karta hai
    public static List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                customers.add(mapRow(rs));
            }

        } catch (Exception e) {
            System.out.println("Error fetching customers: " + e.getMessage());
        }
        return customers;
    }

    private static Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setAddress(rs.getString("address"));
        return c;
    }

    public static boolean updateCustomer(Customer c) {
        String sql = "UPDATE customers SET name=?, phone=?, email=?, address=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getAddress());
            ps.setInt(5, c.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public static List<Customer> searchByNameOrPhone(String query) {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE name LIKE ? OR phone LIKE ? ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            ps.setString(2, "%" + query + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // Sab customers ka Excel backup banane ke liye (CEO use karega)
    public static List<Customer> getAllCustomersForBackup() {
        return getAllCustomers();
    }
}