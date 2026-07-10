package Database;

import Models.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    // Ek nayi log entry banata hai - har important action ke baad call karenge
    public static void log(int userId, String action, String targetTable, Integer targetId, String details) {
        String sql = "INSERT INTO audit_logs (user_id, action, target_table, target_id, details) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, action);
            stmt.setString(3, targetTable);
            if (targetId != null) {
                stmt.setInt(4, targetId);
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setString(5, details);

            stmt.executeUpdate();

        } catch (Exception e) {
            System.out.println("Error writing audit log: " + e.getMessage());
        }
    }

    // CEO/Admin ke liye - sab logs dekhna (most recent pehle)
    public static List<AuditLog> getAllLogs() {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT al.*, u.full_name AS user_name " +
                "FROM audit_logs al " +
                "JOIN users u ON al.user_id = u.id " +
                "ORDER BY al.timestamp DESC LIMIT 500";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                logs.add(mapRow(rs));
            }

        } catch (Exception e) {
            System.out.println("Error fetching audit logs: " + e.getMessage());
        }
        return logs;
    }

    // Action type se filter karke logs dekhna (e.g. sirf "LOGIN_SUCCESS" wale)
    public static List<AuditLog> getLogsByAction(String actionFilter) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT al.*, u.full_name AS user_name " +
                "FROM audit_logs al " +
                "JOIN users u ON al.user_id = u.id " +
                "WHERE al.action = ? " +
                "ORDER BY al.timestamp DESC LIMIT 500";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, actionFilter);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                logs.add(mapRow(rs));
            }

        } catch (Exception e) {
            System.out.println("Error fetching filtered audit logs: " + e.getMessage());
        }
        return logs;
    }

    private static AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(rs.getInt("id"));
        log.setUserId(rs.getInt("user_id"));
        log.setUserName(rs.getString("user_name"));
        log.setAction(rs.getString("action"));
        log.setTargetTable(rs.getString("target_table"));

        int targetId = rs.getInt("target_id");
        log.setTargetId(rs.wasNull() ? null : targetId);

        log.setDetails(rs.getString("details"));
        log.setTimestamp(rs.getTimestamp("timestamp"));
        return log;
    }
}