package Database;

import Models.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // Naya order create karta hai (status hamesha PENDING_APPROVAL se shuru hota hai)
    public static int createOrder(Order order) {
        String sql = "INSERT INTO orders (customer_id, package_id, order_type, amount, order_date, event_date, delivery_date, notes, created_by, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING_APPROVAL')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, order.getCustomerId());
            if (order.getPackageId() != null) {
                stmt.setInt(2, order.getPackageId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, order.getOrderType());
            stmt.setDouble(4, order.getAmount());
            stmt.setDate(5, order.getOrderDate());
            stmt.setDate(6, order.getEventDate());
            stmt.setDate(7, order.getDeliveryDate());
            stmt.setString(8, order.getNotes());
            stmt.setInt(9, order.getCreatedBy());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

        } catch (Exception e) {
            System.out.println("Error creating order: " + e.getMessage());
        }
        return -1;
    }

    // CEO order ko approve karta hai
    public static boolean approveOrder(int orderId, int ceoUserId) {
        String sql = "UPDATE orders SET status='CONFIRMED', approved_by=?, approved_at=NOW() WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ceoUserId);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Error approving order: " + e.getMessage());
            return false;
        }
    }

    // CEO order ko reject karta hai
    public static boolean rejectOrder(int orderId, int ceoUserId) {
        String sql = "UPDATE orders SET status='REJECTED', approved_by=?, approved_at=NOW() WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ceoUserId);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Error rejecting order: " + e.getMessage());
            return false;
        }
    }

    // Order status update karta hai (In Progress / Ready / Delivered)
    public static boolean updateStatus(int orderId, String newStatus) {
        String sql = "UPDATE orders SET status=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Error updating order status: " + e.getMessage());
            return false;
        }
    }

    // Sab orders list karta hai (customer aur creator ke naam ke saath)
    public static List<Order> getAllOrders() {
        return getOrdersByStatus(null);
    }

    // Status ke hisab se filter karta hai (null = sab orders)
    public static List<Order> getOrdersByStatus(String status) {
        List<Order> orders = new ArrayList<>();

        String sql = "SELECT o.*, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, " +
                "u.full_name AS created_by_name, " +
                "COALESCE((SELECT SUM(p.advance_paid) FROM payments p WHERE p.order_id = o.id), 0) AS total_paid " +
                "FROM orders o " +
                "JOIN customers c ON o.customer_id = c.id " +
                "JOIN users u ON o.created_by = u.id ";

        if (status != null) {
            sql += "WHERE o.status = ? ";
        }
        sql += "ORDER BY o.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (status != null) {
                stmt.setString(1, status);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(mapRow(rs));
            }

        } catch (Exception e) {
            System.out.println("Error fetching orders: " + e.getMessage());
        }
        return orders;
    }

    private static Order mapRow(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setCustomerId(rs.getInt("customer_id"));
        o.setCustomerName(rs.getString("customer_name"));
        o.setCustomerPhone(rs.getString("customer_phone"));
        o.setCustomerEmail(rs.getString("customer_email"));

        int pkgId = rs.getInt("package_id");
        o.setPackageId(rs.wasNull() ? null : pkgId);

        o.setOrderType(rs.getString("order_type"));
        o.setAmount(rs.getDouble("amount"));
        o.setOrderDate(rs.getDate("order_date"));
        o.setEventDate(rs.getDate("event_date"));
        o.setDeliveryDate(rs.getDate("delivery_date"));
        o.setStatus(rs.getString("status"));
        o.setNotes(rs.getString("notes"));
        o.setCreatedBy(rs.getInt("created_by"));
        o.setCreatedByName(rs.getString("created_by_name"));

        int approvedBy = rs.getInt("approved_by");

        o.setApprovedBy(rs.wasNull() ? null : approvedBy);

        o.setApprovedAt(rs.getTimestamp("approved_at"));

        try {
            o.setTotalPaid(rs.getDouble("total_paid"));
        } catch (SQLException ignored) {
            // total_paid column kuch queries mein nahi hoti (jaise getOrdersByCustomerId), to ignore karein
        }

        return o;
    }
    // Ek specific customer ke sab orders (order history ke liye)
    public static java.util.List<Order> getOrdersByCustomerId(int customerId) {
        java.util.List<Order> orders = new java.util.ArrayList<>();

        String sql = "SELECT o.*, c.name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, " +
                "u.full_name AS created_by_name " +
                "FROM orders o " +
                "JOIN customers c ON o.customer_id = c.id " +
                "JOIN users u ON o.created_by = u.id " +
                "WHERE o.customer_id = ? " +
                "ORDER BY o.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(mapRow(rs));
            }

        } catch (Exception e) {
            System.out.println("Error fetching customer orders: " + e.getMessage());
        }
        return orders;
    }
}