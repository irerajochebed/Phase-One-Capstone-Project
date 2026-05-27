package com.igirepay.igirepaypaymentgateway.LAB2.dao;

import com.igirepay.igirepaypaymentgateway.LAB2.db.DatabaseConnection;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Dispute;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DisputeDAO {
    public int createDispute(Dispute dispute) {
        String sql = "INSERT INTO disputes (customer_id, transaction_id, dispute_type, description, status) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, dispute.getCustomerId());
            ps.setInt(2, dispute.getTransactionId());
            ps.setString(3, dispute.getDisputeType());
            ps.setString(4, dispute.getDescription());
            ps.setString(5, dispute.getStatus());
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                dispute.setId(id);
                System.out.println("[DisputeDAO] âœ“ Dispute created with id=" + id);
                return id;
            }
        } catch (SQLException e) {
            System.err.println("[DisputeDAO] ERROR creating dispute: " + e.getMessage());
        }
        return -1;
    }
    public List<Dispute> getAllDisputes() {
        String sql = "SELECT * FROM disputes ORDER BY created_at DESC";
        List<Dispute> disputes = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                disputes.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DisputeDAO] ERROR getting disputes: " + e.getMessage());
        }
        return disputes;
    }
    public List<Dispute> getDisputesByCustomer(int customerId) {
        String sql = "SELECT * FROM disputes WHERE customer_id = ? ORDER BY created_at DESC";
        List<Dispute> disputes = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                disputes.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DisputeDAO] ERROR getting customer disputes: " + e.getMessage());
        }
        return disputes;
    }
    public List<Dispute> getPendingDisputes() {
        String sql = "SELECT * FROM disputes WHERE status = ? ORDER BY created_at ASC";
        List<Dispute> disputes = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, Dispute.STATUS_PENDING);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                disputes.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DisputeDAO] ERROR getting pending disputes: " + e.getMessage());
        }
        return disputes;
    }
    public boolean updateDisputeStatus(int disputeId, String status, String adminNotes, int resolvedBy) {
        String sql = "UPDATE disputes SET status = ?, admin_notes = ?, resolved_by = ?, resolved_at = CURRENT_TIMESTAMP " +
                     "WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status);
            ps.setString(2, adminNotes);
            ps.setInt(3, resolvedBy);
            ps.setInt(4, disputeId);
            
            int rows = ps.executeUpdate();
            System.out.println("[DisputeDAO] âœ“ Dispute " + disputeId + " updated to " + status);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[DisputeDAO] ERROR updating dispute: " + e.getMessage());
        }
        return false;
    }
    public Dispute getDisputeById(int id) {
        String sql = "SELECT * FROM disputes WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[DisputeDAO] ERROR getting dispute: " + e.getMessage());
        }
        return null;
    }

    private Dispute mapRow(ResultSet rs) throws SQLException {
        Integer resolvedBy = rs.getObject("resolved_by", Integer.class);
        Timestamp resolvedAtTs = rs.getTimestamp("resolved_at");
        
        return new Dispute(
            rs.getInt("id"),
            rs.getInt("customer_id"),
            rs.getInt("transaction_id"),
            rs.getString("dispute_type"),
            rs.getString("description"),
            rs.getString("status"),
            rs.getString("admin_notes"),
            resolvedBy,
            rs.getTimestamp("created_at").toLocalDateTime(),
            resolvedAtTs != null ? resolvedAtTs.toLocalDateTime() : null
        );
    }
}
