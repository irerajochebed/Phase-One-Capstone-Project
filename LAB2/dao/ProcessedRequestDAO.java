package com.igirepay.igirepaypaymentgateway.LAB2.dao;

import com.igirepay.igirepaypaymentgateway.LAB2.db.DatabaseConnection;
import com.igirepay.igirepaypaymentgateway.LAB2.model.ProcessedRequest;

import java.sql.*;

public class ProcessedRequestDAO {

   
    public boolean save(String referenceId) {
        String sql = "INSERT INTO processed_requests (reference_id) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, referenceId);
            ps.executeUpdate();
            System.out.println("[ProcessedRequestDAO] âœ“ Reference ID saved: " + referenceId);
            return true;

        } catch (SQLException e) {
            
            System.err.println("[ProcessedRequestDAO] ERROR saving ref: " + e.getMessage());
        }
        return false;
    }

    
    public boolean existsByReferenceId(String referenceId) {
        String sql = "SELECT 1 FROM processed_requests WHERE reference_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, referenceId);
            ResultSet rs = ps.executeQuery();
            return rs.next(); 

        } catch (SQLException e) {
            System.err.println("[ProcessedRequestDAO] ERROR checking ref: " + e.getMessage());
        }
        return false;
    }

 
    public ProcessedRequest findByReferenceId(String referenceId) {
        String sql = "SELECT * FROM processed_requests WHERE reference_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, referenceId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new ProcessedRequest(
                        rs.getInt("id"),
                        rs.getString("reference_id"),
                        rs.getTimestamp("processed_at").toLocalDateTime()
                );
            }

        } catch (SQLException e) {
            System.err.println("[ProcessedRequestDAO] ERROR finding ref: " + e.getMessage());
        }
        return null;
    }

    
    public boolean delete(String referenceId) {
        String sql = "DELETE FROM processed_requests WHERE reference_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, referenceId);
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[ProcessedRequestDAO] ERROR deleting ref: " + e.getMessage());
        }
        return false;
    }
}
