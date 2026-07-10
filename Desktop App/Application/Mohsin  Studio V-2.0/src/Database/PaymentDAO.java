package Database;

import Models.Payment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    public static boolean addPayment(Payment payment) {
        String sql = "INSERT INTO payments (order_id, total_amount, advance_paid, payment_method, transaction_id, payment_date, recorded_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, payment.getOrderId());
            stmt.setDouble(2, payment.getTotalAmount());
            stmt.setDouble(3, payment.getAdvancePaid());
            stmt.setString(4, payment.getPaymentMethod());
            stmt.setString(5, payment.getTransactionId());
            stmt.setDate(6, payment.getPaymentDate());
            stmt.setInt(7, payment.getRecordedBy());

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Error adding payment: " + e.getMessage());
            return false;
        }
    }

    public static List<Payment> getPaymentsByOrder(int orderId) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE order_id = ? ORDER BY payment_date, id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                payments.add(mapRow(rs));
            }

        } catch (Exception e) {
            System.out.println("Error fetching payments: " + e.getMessage());
        }
        return payments;
    }

    public static double getTotalPaidForOrder(int orderId) {
        String sql = "SELECT COALESCE(SUM(advance_paid), 0) AS total_paid FROM payments WHERE order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total_paid");
            }

        } catch (Exception e) {
            System.out.println("Error calculating total paid: " + e.getMessage());
        }
        return 0;
    }

    private static Payment mapRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("id"));
        p.setOrderId(rs.getInt("order_id"));
        p.setTotalAmount(rs.getDouble("total_amount"));
        p.setAdvancePaid(rs.getDouble("advance_paid"));
        p.setBalance(rs.getDouble("balance"));
        p.setPaymentMethod(rs.getString("payment_method"));
        p.setTransactionId(rs.getString("transaction_id"));
        p.setPaymentDate(rs.getDate("payment_date"));
        p.setRecordedBy(rs.getInt("recorded_by"));
        return p;
    }

    // Galat/duplicate payment ko delete karta hai
    public static boolean deletePayment(int paymentId) {
        String sql = "DELETE FROM payments WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, paymentId);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error deleting payment: " + e.getMessage());
            return false;
        }
    }
}