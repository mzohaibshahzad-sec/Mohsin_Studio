package Database;

import Models.SalesStatPoint;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StatsDAO {

    // Orders ke wo statuses jo "real revenue" maane jaate hain (rejected/pending exclude)
    private static final String CONFIRMED_STATUSES = "'CONFIRMED','IN_PROGRESS','READY','DELIVERED'";

    // ===== Ek mahine ke har din ka breakdown: Dukan Sale vs Orders =====
    public static List<SalesStatPoint> getDailyBreakdown(int year, int month) {
        Map<String, double[]> dayMap = new LinkedHashMap<>(); // key = date string, value = [shopSale, orders]

        String shopSql = "SELECT entry_date, COALESCE(SUM(amount),0) AS total FROM sales_entries " +
                "WHERE YEAR(entry_date)=? AND MONTH(entry_date)=? GROUP BY entry_date";
        String orderSql = "SELECT order_date, COALESCE(SUM(amount),0) AS total FROM orders " +
                "WHERE status IN (" + CONFIRMED_STATUSES + ") AND YEAR(order_date)=? AND MONTH(order_date)=? " +
                "GROUP BY order_date";

        try (Connection conn = DBConnection.getConnection()) {

            try (PreparedStatement stmt = conn.prepareStatement(shopSql)) {
                stmt.setInt(1, year);
                stmt.setInt(2, month);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String dateStr = rs.getDate("entry_date").toString();
                    double[] arr = dayMap.computeIfAbsent(dateStr, k -> new double[2]);
                    arr[0] = rs.getDouble("total");
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(orderSql)) {
                stmt.setInt(1, year);
                stmt.setInt(2, month);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String dateStr = rs.getDate("order_date").toString();
                    double[] arr = dayMap.computeIfAbsent(dateStr, k -> new double[2]);
                    arr[1] = rs.getDouble("total");
                }
            }

        } catch (Exception e) {
            System.out.println("Error fetching daily breakdown: " + e.getMessage());
        }

        return toSortedList(dayMap);
    }

    // ===== Last N mahino ka breakdown: Dukan Sale vs Orders (month-wise) =====
    public static List<SalesStatPoint> getMonthlyTrend(int monthsBack) {
        Map<String, double[]> monthMap = new LinkedHashMap<>();

        String shopSql = "SELECT DATE_FORMAT(entry_date, '%Y-%m') AS ym, COALESCE(SUM(amount),0) AS total " +
                "FROM sales_entries WHERE entry_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) GROUP BY ym";
        String orderSql = "SELECT DATE_FORMAT(order_date, '%Y-%m') AS ym, COALESCE(SUM(amount),0) AS total " +
                "FROM orders WHERE status IN (" + CONFIRMED_STATUSES + ") " +
                "AND order_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) GROUP BY ym";

        try (Connection conn = DBConnection.getConnection()) {

            try (PreparedStatement stmt = conn.prepareStatement(shopSql)) {
                stmt.setInt(1, monthsBack);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String ym = rs.getString("ym");
                    double[] arr = monthMap.computeIfAbsent(ym, k -> new double[2]);
                    arr[0] = rs.getDouble("total");
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(orderSql)) {
                stmt.setInt(1, monthsBack);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String ym = rs.getString("ym");
                    double[] arr = monthMap.computeIfAbsent(ym, k -> new double[2]);
                    arr[1] = rs.getDouble("total");
                }
            }

        } catch (Exception e) {
            System.out.println("Error fetching monthly trend: " + e.getMessage());
        }

        return toSortedList(monthMap);
    }

    // ===== Last N saalon ka breakdown: Dukan Sale vs Orders (year-wise) =====
    public static List<SalesStatPoint> getYearlyBreakdown(int yearsBack) {
        Map<String, double[]> yearMap = new LinkedHashMap<>();

        String shopSql = "SELECT YEAR(entry_date) AS yr, COALESCE(SUM(amount),0) AS total " +
                "FROM sales_entries WHERE entry_date >= DATE_SUB(CURDATE(), INTERVAL ? YEAR) GROUP BY yr";
        String orderSql = "SELECT YEAR(order_date) AS yr, COALESCE(SUM(amount),0) AS total " +
                "FROM orders WHERE status IN (" + CONFIRMED_STATUSES + ") " +
                "AND order_date >= DATE_SUB(CURDATE(), INTERVAL ? YEAR) GROUP BY yr";

        try (Connection conn = DBConnection.getConnection()) {

            try (PreparedStatement stmt = conn.prepareStatement(shopSql)) {
                stmt.setInt(1, yearsBack);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String yr = String.valueOf(rs.getInt("yr"));
                    double[] arr = yearMap.computeIfAbsent(yr, k -> new double[2]);
                    arr[0] = rs.getDouble("total");
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(orderSql)) {
                stmt.setInt(1, yearsBack);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String yr = String.valueOf(rs.getInt("yr"));
                    double[] arr = yearMap.computeIfAbsent(yr, k -> new double[2]);
                    arr[1] = rs.getDouble("total");
                }
            }

        } catch (Exception e) {
            System.out.println("Error fetching yearly breakdown: " + e.getMessage());
        }

        return toSortedList(yearMap);
    }

    // Map ko SalesStatPoint list mein convert karta hai, date/month/year (key) ke hisaab se sorted
    private static List<SalesStatPoint> toSortedList(Map<String, double[]> map) {
        List<SalesStatPoint> result = new ArrayList<>();
        map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    SalesStatPoint p = new SalesStatPoint();
                    p.setLabel(entry.getKey());
                    p.setShopSales(entry.getValue()[0]);
                    p.setOrdersRevenue(entry.getValue()[1]);
                    result.add(p);
                });
        return result;
    }
}