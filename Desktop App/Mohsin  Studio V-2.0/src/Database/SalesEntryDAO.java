package Database;

import Models.SalesEntry;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SalesEntryDAO {

    // Nayi sale entry add karta hai
    public static boolean addEntry(SalesEntry entry) {
        String sql = "INSERT INTO sales_entries (entry_type, description, amount, entered_by, entry_date, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entry.getEntryType());
            stmt.setString(2, entry.getDescription());
            stmt.setDouble(3, entry.getAmount());
            stmt.setInt(4, entry.getEnteredBy());
            stmt.setDate(5, entry.getEntryDate());
            stmt.setString(6, entry.getNotes());

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Error adding sales entry: " + e.getMessage());
            return false;
        }
    }

    // Aaj ki entries (sirf logged-in user ki)
    public static List<SalesEntry> getTodayEntriesByUser(int userId) {
        List<SalesEntry> entries = new ArrayList<>();
        String sql = "SELECT se.*, u.full_name AS entered_by_name " +
                "FROM sales_entries se " +
                "JOIN users u ON se.entered_by = u.id " +
                "WHERE se.entered_by = ? AND se.entry_date = ? " +
                "ORDER BY se.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(LocalDate.now()));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                entries.add(mapRow(rs));
            }

        } catch (Exception e) {
            System.out.println("Error fetching today's entries: " + e.getMessage());
        }
        return entries;
    }

    // Ek specific date ki sab entries
    public static List<SalesEntry> getEntriesByDate(LocalDate date) {
        List<SalesEntry> entries = new ArrayList<>();
        String sql = "SELECT se.*, u.full_name AS entered_by_name " +
                "FROM sales_entries se " +
                "JOIN users u ON se.entered_by = u.id " +
                "WHERE se.entry_date = ? " +
                "ORDER BY se.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                entries.add(mapRow(rs));
            }

        } catch (Exception e) {
            System.out.println("Error fetching entries by date: " + e.getMessage());
        }
        return entries;
    }

    // ===== NAYA: Saari entries export ke liye =====
    public static List<SalesEntry> getAllEntries() {
        List<SalesEntry> entries = new ArrayList<>();
        String sql = "SELECT se.*, u.full_name AS entered_by_name " +
                "FROM sales_entries se " +
                "JOIN users u ON se.entered_by = u.id " +
                "ORDER BY se.entry_date DESC, se.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                entries.add(mapRow(rs));
            }

        } catch (Exception e) {
            System.out.println("Error fetching all entries: " + e.getMessage());
        }
        return entries;
    }

    // ===== NAYA: Date range se entries =====
    public static List<SalesEntry> getEntriesByRange(LocalDate from, LocalDate to) {
        List<SalesEntry> entries = new ArrayList<>();
        String sql = "SELECT se.*, u.full_name AS entered_by_name " +
                "FROM sales_entries se " +
                "JOIN users u ON se.entered_by = u.id " +
                "WHERE se.entry_date BETWEEN ? AND ? " +
                "ORDER BY se.entry_date DESC, se.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(from));
            stmt.setDate(2, Date.valueOf(to));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                entries.add(mapRow(rs));
            }

        } catch (Exception e) {
            System.out.println("Error fetching entries by range: " + e.getMessage());
        }
        return entries;
    }

    // Total sales ek date ke liye
    public static double getTotalSalesByDate(LocalDate date) {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM sales_entries WHERE entry_date = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("total");

        } catch (Exception e) {
            System.out.println("Error calculating total sales: " + e.getMessage());
        }
        return 0;
    }

    // Sab clerks ki aaj ki entries (CEO ke liye)
    public static List<SalesEntry> getAllTodayEntries() {
        List<SalesEntry> entries = new ArrayList<>();
        String sql = "SELECT se.*, u.full_name AS entered_by_name " +
                "FROM sales_entries se " +
                "JOIN users u ON se.entered_by = u.id " +
                "WHERE se.entry_date = CURDATE() " +
                "ORDER BY u.full_name, se.created_at";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                entries.add(mapRow(rs));
            }

        } catch (Exception e) {
            System.out.println("Error fetching all today entries: " + e.getMessage());
        }
        return entries;
    }

    private static SalesEntry mapRow(ResultSet rs) throws SQLException {
        SalesEntry entry = new SalesEntry();
        entry.setId(rs.getInt("id"));
        entry.setEntryType(rs.getString("entry_type"));
        entry.setDescription(rs.getString("description"));
        entry.setAmount(rs.getDouble("amount"));
        entry.setEnteredBy(rs.getInt("entered_by"));
        entry.setEnteredByName(rs.getString("entered_by_name"));
        entry.setEntryDate(rs.getDate("entry_date"));
        entry.setNotes(rs.getString("notes"));
        entry.setCreatedAt(rs.getTimestamp("created_at"));
        return entry;
    }
}