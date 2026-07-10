package Database;

import Models.Expense;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {

    public static boolean addExpense(Expense expense) {
        String sql = "INSERT INTO expenses (description, category, amount, expense_date, recorded_by, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, expense.getDescription());
            stmt.setString(2, expense.getCategory());
            stmt.setDouble(3, expense.getAmount());
            stmt.setDate(4, expense.getExpenseDate());
            stmt.setInt(5, expense.getRecordedBy());
            stmt.setString(6, expense.getNotes());

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("Error adding expense: " + e.getMessage());
            return false;
        }
    }

    public static List<Expense> getAllExpenses() {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT e.*, u.full_name AS recorded_by_name " +
                "FROM expenses e JOIN users u ON e.recorded_by = u.id " +
                "ORDER BY e.expense_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                expenses.add(mapRow(rs));
            }

        } catch (Exception e) {
            System.out.println("Error fetching expenses: " + e.getMessage());
        }
        return expenses;
    }

    // Is mahine ki total expense
    public static double getThisMonthTotal() {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM expenses " +
                "WHERE MONTH(expense_date) = MONTH(CURDATE()) AND YEAR(expense_date) = YEAR(CURDATE())";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (Exception e) {
            System.out.println("Error calculating monthly expenses: " + e.getMessage());
        }
        return 0;
    }

    public static boolean deleteExpense(int id) {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error deleting expense: " + e.getMessage());
            return false;
        }
    }

    private static Expense mapRow(ResultSet rs) throws SQLException {
        Expense expense = new Expense();
        expense.setId(rs.getInt("id"));
        expense.setDescription(rs.getString("description"));
        expense.setCategory(rs.getString("category"));
        expense.setAmount(rs.getDouble("amount"));
        expense.setExpenseDate(rs.getDate("expense_date"));
        expense.setRecordedBy(rs.getInt("recorded_by"));
        expense.setRecordedByName(rs.getString("recorded_by_name"));
        expense.setNotes(rs.getString("notes"));
        return expense;
    }
}