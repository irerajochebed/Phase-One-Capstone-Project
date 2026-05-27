package com.igirepay.igirepaypaymentgateway.LAB2.dao;

import com.igirepay.igirepaypaymentgateway.LAB2.db.DatabaseConnection;
import com.igirepay.igirepaypaymentgateway.LAB2.model.WithdrawalRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class WithdrawalRequestDAO {

   
    public int addWithdrawalRequest(WithdrawalRequest request) {
        String sql = "INSERT INTO withdrawal_requests (account_id, customer_id, amount, " +
                     "request_date, available_date, status, reference_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, request.getAccountId());
            ps.setInt(2, request.getCustomerId());
            ps.setDouble(3, request.getAmount());
            ps.setTimestamp(4, Timestamp.valueOf(request.getRequestDate()));
            ps.setTimestamp(5, Timestamp.valueOf(request.getAvailableDate()));
            ps.setString(6, request.getStatus());
            ps.setString(7, request.getReferenceId());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int newId = rs.getInt("id");
                request.setId(newId);
                System.out.println("[WithdrawalRequestDAO] âœ“ Withdrawal request added with id=" + newId);
                return newId;
            }

        } catch (SQLException e) {
            System.err.println("[WithdrawalRequestDAO] ERROR adding request: " + e.getMessage());
        }
        return -1;
    }

   
    public WithdrawalRequest findById(int id) {
        String sql = "SELECT * FROM withdrawal_requests WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[WithdrawalRequestDAO] ERROR finding request: " + e.getMessage());
        }
        return null;
    }

    public List<WithdrawalRequest> findByCustomerId(int customerId) {
        String sql = "SELECT * FROM withdrawal_requests WHERE customer_id = ? ORDER BY request_date DESC";
        List<WithdrawalRequest> list = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[WithdrawalRequestDAO] ERROR finding requests: " + e.getMessage());
        }
        return list;
    }

    public List<WithdrawalRequest> findPendingByCustomerId(int customerId) {
        String sql = "SELECT * FROM withdrawal_requests WHERE customer_id = ? AND status = 'PENDING' " +
                     "ORDER BY request_date DESC";
        List<WithdrawalRequest> list = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[WithdrawalRequestDAO] ERROR finding pending requests: " + e.getMessage());
        }
        return list;
    }

    public List<WithdrawalRequest> findAvailableByCustomerId(int customerId) {
        String sql = "SELECT * FROM withdrawal_requests WHERE customer_id = ? AND status = 'PENDING' " +
                     "AND available_date <= CURRENT_TIMESTAMP ORDER BY request_date DESC";
        List<WithdrawalRequest> list = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[WithdrawalRequestDAO] ERROR finding available requests: " + e.getMessage());
        }
        return list;
    }

    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE withdrawal_requests SET status = ?, processed_date = CURRENT_TIMESTAMP " +
                     "WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[WithdrawalRequestDAO] ERROR updating status: " + e.getMessage());
        }
        return false;
    }

    public boolean cancelRequest(int id) {
        return updateStatus(id, WithdrawalRequest.STATUS_CANCELLED);
    }

   
    public boolean deleteRequest(int id) {
        String sql = "DELETE FROM withdrawal_requests WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[WithdrawalRequestDAO] ERROR deleting request: " + e.getMessage());
        }
        return false;
    }

    
    private WithdrawalRequest mapRow(ResultSet rs) throws SQLException {
        Timestamp processedTs = rs.getTimestamp("processed_date");
        
        return new WithdrawalRequest(
                rs.getInt("id"),
                rs.getInt("account_id"),
                rs.getInt("customer_id"),
                rs.getDouble("amount"),
                rs.getTimestamp("request_date").toLocalDateTime(),
                rs.getTimestamp("available_date").toLocalDateTime(),
                rs.getString("status"),
                processedTs != null ? processedTs.toLocalDateTime() : null,
                rs.getString("reference_id"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
