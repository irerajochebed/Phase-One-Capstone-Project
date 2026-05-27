package com.igirepay.igirepaypaymentgateway.LAB2.dao;

import com.igirepay.igirepaypaymentgateway.LAB2.db.DatabaseConnection;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Loan;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {

    
    public int createLoan(Loan loan) {
        String sql = "INSERT INTO loans (customer_id, account_id, principal_amount, interest_rate, " +
                     "total_amount, remaining_balance, duration_months, purpose, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, loan.getCustomerId());
            ps.setInt(2, loan.getAccountId());
            ps.setDouble(3, loan.getPrincipalAmount());
            ps.setDouble(4, loan.getInterestRate());
            ps.setDouble(5, loan.getTotalAmount());
            ps.setDouble(6, loan.getRemainingBalance());
            ps.setInt(7, loan.getDurationMonths());
            ps.setString(8, loan.getPurpose());
            ps.setString(9, loan.getStatus());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int newId = rs.getInt("id");
                loan.setId(newId);
                System.out.println("[LoanDAO] âœ“ Loan application created with id=" + newId);
                return newId;
            }

        } catch (SQLException e) {
            System.err.println("[LoanDAO] ERROR creating loan: " + e.getMessage());
        }
        return -1;
    }

    
    public Loan findById(int id) {
        String sql = "SELECT * FROM loans WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[LoanDAO] ERROR finding loan: " + e.getMessage());
        }
        return null;
    }

    public List<Loan> findByCustomerId(int customerId) {
        String sql = "SELECT * FROM loans WHERE customer_id = ? ORDER BY application_date DESC";
        List<Loan> list = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[LoanDAO] ERROR finding loans: " + e.getMessage());
        }
        return list;
    }

    public List<Loan> findByStatus(String status) {
        String sql = "SELECT * FROM loans WHERE status = ? ORDER BY application_date DESC";
        List<Loan> list = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[LoanDAO] ERROR finding loans by status: " + e.getMessage());
        }
        return list;
    }

    public List<Loan> findAll() {
        String sql = "SELECT * FROM loans ORDER BY application_date DESC";
        List<Loan> list = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[LoanDAO] ERROR finding all loans: " + e.getMessage());
        }
        return list;
    }

    public List<Loan> findOverdueLoans() {
        String sql = "SELECT * FROM loans WHERE status IN ('ACTIVE', 'OVERDUE') " +
                     "AND due_date < CURRENT_TIMESTAMP AND remaining_balance > 0 " +
                     "ORDER BY due_date ASC";
        List<Loan> list = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[LoanDAO] ERROR finding overdue loans: " + e.getMessage());
        }
        return list;
    }

    public List<Loan> findLoansDueSoon(int days) {
        String sql = "SELECT * FROM loans WHERE status = 'ACTIVE' " +
                     "AND due_date BETWEEN CURRENT_TIMESTAMP AND CURRENT_TIMESTAMP + INTERVAL '" + days + " days' " +
                     "AND remaining_balance > 0 ORDER BY due_date ASC";
        List<Loan> list = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[LoanDAO] ERROR finding loans due soon: " + e.getMessage());
        }
        return list;
    }

   
    public boolean approveLoan(int loanId, int adminId) {
        String sql = "UPDATE loans SET status = ?, approval_date = CURRENT_TIMESTAMP, " +
                     "approved_by = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Loan.STATUS_ACTIVE);
            ps.setInt(2, adminId);
            ps.setInt(3, loanId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[LoanDAO] ERROR approving loan: " + e.getMessage());
        }
        return false;
    }

    public boolean disburseLoan(int loanId, LocalDateTime dueDate) {
        String sql = "UPDATE loans SET disbursement_date = CURRENT_TIMESTAMP, due_date = ?, " +
                     "updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(dueDate));
            ps.setInt(2, loanId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[LoanDAO] ERROR disbursing loan: " + e.getMessage());
        }
        return false;
    }

    public boolean rejectLoan(int loanId, String reason) {
        String sql = "UPDATE loans SET status = ?, rejection_reason = ?, " +
                     "updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Loan.STATUS_REJECTED);
            ps.setString(2, reason);
            ps.setInt(3, loanId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[LoanDAO] ERROR rejecting loan: " + e.getMessage());
        }
        return false;
    }

    public boolean recordPayment(int loanId, double amount) {
        String sql = "UPDATE loans SET amount_paid = amount_paid + ?, " +
                     "remaining_balance = remaining_balance - ?, " +
                     "last_payment_date = CURRENT_TIMESTAMP, " +
                     "status = CASE WHEN remaining_balance - ? <= 0 THEN ? ELSE status END, " +
                     "updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setDouble(2, amount);
            ps.setDouble(3, amount);
            ps.setString(4, Loan.STATUS_PAID);
            ps.setInt(5, loanId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[LoanDAO] ERROR recording payment: " + e.getMessage());
        }
        return false;
    }

    public boolean updateStatus(int loanId, String status) {
        String sql = "UPDATE loans SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, loanId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[LoanDAO] ERROR updating status: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteLoan(int id) {
        String sql = "DELETE FROM loans WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[LoanDAO] ERROR deleting loan: " + e.getMessage());
        }
        return false;
    }

    
    private Loan mapRow(ResultSet rs) throws SQLException {
        return new Loan(
                rs.getInt("id"),
                rs.getInt("customer_id"),
                rs.getInt("account_id"),
                rs.getDouble("principal_amount"),
                rs.getDouble("interest_rate"),
                rs.getDouble("total_amount"),
                rs.getDouble("amount_paid"),
                rs.getDouble("remaining_balance"),
                rs.getInt("duration_months"),
                getTimestampOrNull(rs, "application_date"),
                getTimestampOrNull(rs, "approval_date"),
                getTimestampOrNull(rs, "disbursement_date"),
                getTimestampOrNull(rs, "due_date"),
                getTimestampOrNull(rs, "last_payment_date"),
                rs.getString("status"),
                rs.getString("purpose"),
                getIntOrNull(rs, "approved_by"),
                rs.getString("rejection_reason")
        );
    }

    private LocalDateTime getTimestampOrNull(ResultSet rs, String col) {
        try {
            Timestamp ts = rs.getTimestamp(col);
            return ts != null ? ts.toLocalDateTime() : null;
        } catch (SQLException e) {
            return null;
        }
    }

    private Integer getIntOrNull(ResultSet rs, String col) {
        try {
            int val = rs.getInt(col);
            return rs.wasNull() ? null : val;
        } catch (SQLException e) {
            return null;
        }
    }
}
