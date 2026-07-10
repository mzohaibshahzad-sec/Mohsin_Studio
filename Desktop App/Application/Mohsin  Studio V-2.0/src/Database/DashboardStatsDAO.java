package Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DashboardStatsDAO {

    // Aaj ki total sales entries ka amount
    public static double getTodaySalesTotal() {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM sales_entries WHERE entry_date = CURDATE()";
        return runSingleDoubleQuery(sql);
    }

    // Pending approval orders ka count
    public static int getPendingOrdersCount() {
        String sql = "SELECT COUNT(*) AS cnt FROM orders WHERE status = 'PENDING_APPROVAL'";
        return runSingleIntQuery(sql);
    }

    // Is mahine ki total earning
    public static double getThisMonthEarning() {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM orders " +
                "WHERE status IN ('CONFIRMED','IN_PROGRESS','READY','DELIVERED') " +
                "AND MONTH(order_date) = MONTH(CURDATE()) AND YEAR(order_date) = YEAR(CURDATE())";
        return runSingleDoubleQuery(sql);
    }

    // Aaj confirm hue orders ka count
    public static int getTodayConfirmedOrdersCount() {
        String sql = "SELECT COUNT(*) AS cnt FROM orders WHERE DATE(approved_at) = CURDATE() AND status != 'REJECTED'";
        return runSingleIntQuery(sql);
    }

    // Total customers count
    public static int getTotalCustomersCount() {
        String sql = "SELECT COUNT(*) AS cnt FROM customers";
        return runSingleIntQuery(sql);
    }

    // Active orders count
    public static int getActiveOrdersCount() {
        String sql = "SELECT COUNT(*) AS cnt FROM orders WHERE status IN ('CONFIRMED','IN_PROGRESS','READY')";
        return runSingleIntQuery(sql);
    }

    // Is hafte ki total earning
    public static double getThisWeekEarning() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM orders " +
                "WHERE status IN ('CONFIRMED','IN_PROGRESS','READY','DELIVERED') " +
                "AND YEARWEEK(order_date, 1) = YEARWEEK(CURDATE(), 1)";
        return runSingleDoubleQuery(sql);
    }

    // Aaj ki combined earning (Orders + Sales)
    public static double getTodayCombinedEarning() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM orders " +
                "WHERE status IN ('CONFIRMED','IN_PROGRESS','READY','DELIVERED') " +
                "AND DATE(order_date) = CURDATE() " +
                "UNION ALL " +
                "SELECT COALESCE(SUM(amount), 0) FROM sales_entries " +
                "WHERE DATE(entry_date) = CURDATE()";
        double total = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) total += rs.getDouble(1);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return total;
    }

    // Aaj sirf Orders amount
    public static double getTodayOrdersTotal() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM orders " +
                "WHERE status IN ('CONFIRMED','IN_PROGRESS','READY','DELIVERED') " +
                "AND DATE(order_date) = CURDATE()";
        return runSingleDoubleQuery(sql);
    }

    // Is mahine combined earning (Orders + Sales)
    public static double getThisMonthCombinedEarning() {
        double total = 0;
        String sql1 = "SELECT COALESCE(SUM(amount), 0) FROM orders " +
                "WHERE status IN ('CONFIRMED','IN_PROGRESS','READY','DELIVERED') " +
                "AND MONTH(order_date) = MONTH(CURDATE()) AND YEAR(order_date) = YEAR(CURDATE())";
        String sql2 = "SELECT COALESCE(SUM(amount), 0) FROM sales_entries " +
                "WHERE MONTH(entry_date) = MONTH(CURDATE()) AND YEAR(entry_date) = YEAR(CURDATE())";
        total += runSingleDoubleQuery(sql1);
        total += runSingleDoubleQuery(sql2);
        return total;
    }

    // Is mahine sirf Sales amount
    public static double getThisMonthSalesTotal() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM sales_entries " +
                "WHERE MONTH(entry_date) = MONTH(CURDATE()) AND YEAR(entry_date) = YEAR(CURDATE())";
        return runSingleDoubleQuery(sql);
    }

    // Is mahine sirf Orders amount
    public static double getThisMonthOrdersTotal() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM orders " +
                "WHERE status IN ('CONFIRMED','IN_PROGRESS','READY','DELIVERED') " +
                "AND MONTH(order_date) = MONTH(CURDATE()) AND YEAR(order_date) = YEAR(CURDATE())";
        return runSingleDoubleQuery(sql);
    }

    // Is saal ki combined earning
    public static double getThisYearCombinedEarning() {
        double total = 0;
        String sql1 = "SELECT COALESCE(SUM(amount), 0) FROM orders " +
                "WHERE status IN ('CONFIRMED','IN_PROGRESS','READY','DELIVERED') " +
                "AND YEAR(order_date) = YEAR(CURDATE())";
        String sql2 = "SELECT COALESCE(SUM(amount), 0) FROM sales_entries " +
                "WHERE YEAR(entry_date) = YEAR(CURDATE())";
        total += runSingleDoubleQuery(sql1);
        total += runSingleDoubleQuery(sql2);
        return total;
    }

    // Is saal sirf Orders
    public static double getThisYearOrdersTotal() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM orders " +
                "WHERE status IN ('CONFIRMED','IN_PROGRESS','READY','DELIVERED') " +
                "AND YEAR(order_date) = YEAR(CURDATE())";
        return runSingleDoubleQuery(sql);
    }

    // Is saal sirf Sales
    public static double getThisYearSalesTotal() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM sales_entries " +
                "WHERE YEAR(entry_date) = YEAR(CURDATE())";
        return runSingleDoubleQuery(sql);
    }




    // Aaj ke delivered orders
    public static int getTodayDeliveredCount() {
        String sql = "SELECT COUNT(*) FROM orders WHERE status = 'DELIVERED' AND DATE(order_date) = CURDATE()";
        return runSingleIntQuery(sql);
    }

    // Recent 5 orders (customer name + status + amount)
    public static List<String[]> getRecentOrders(int limit) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT o.id, c.name, o.event_date, o.amount, o.status " +
                "FROM orders o JOIN customers c ON o.customer_id = c.id " +
                "ORDER BY o.id DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("name"),
                        rs.getString("event_date") != null ? rs.getString("event_date") : "-",
                        "Rs. " + (int) rs.getDouble("amount"),
                        rs.getString("status")
                });
            }
        } catch (Exception e) {
            System.out.println("Error fetching recent orders: " + e.getMessage());
        }
        return list;
    }

    // Recent 3 packages
    public static List<String[]> getRecentPackages(int limit) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT package_name, price, discount FROM packages ORDER BY id DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                double price = rs.getDouble("price");
                double discount = rs.getDouble("discount");
                double final_price = price - (price * discount / 100);
                list.add(new String[]{
                        rs.getString("package_name"),
                        "Rs. " + (int) final_price,
                        discount > 0 ? (int) discount + "% off" : ""
                });
            }
        } catch (Exception e) {
            System.out.println("Error fetching recent packages: " + e.getMessage());
        }
        return list;
    }

    private static double runSingleDoubleQuery(String sql) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) {
            System.out.println("Error in dashboard stats: " + e.getMessage());
        }
        return 0;
    }

    private static int runSingleIntQuery(String sql) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.out.println("Error in dashboard stats: " + e.getMessage());
        }
        return 0;
    }
}