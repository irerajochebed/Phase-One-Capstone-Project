package com.igirepay.igirepaypaymentgateway.LAB2.dao;

import com.igirepay.igirepaypaymentgateway.LAB2.db.DatabaseConnection;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class NotificationDAO {

  
    public int addNotification(Notification notification) {
        String sql = "INSERT INTO notifications (customer_id, type, title, message, amount, fee, reference_id, " +
                     "sender_name, receiver_name, action_required, action_type, related_transaction_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, notification.getCustomerId());
            ps.setString(2, notification.getType());
            ps.setString(3, notification.getTitle());
            ps.setString(4, notification.getMessage());
            ps.setDouble(5, notification.getAmount());
            ps.setDouble(6, notification.getFee());
            ps.setString(7, notification.getReferenceId());
            ps.setString(8, notification.getSenderName());
            ps.setString(9, notification.getReceiverName());
            ps.setBoolean(10, notification.isActionRequired());
            ps.setString(11, notification.getActionType());
            if (notification.getRelatedTransactionId() != null) {
                ps.setInt(12, notification.getRelatedTransactionId());
            } else {
                ps.setNull(12, Types.INTEGER);
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int newId = rs.getInt("id");
                notification.setId(newId);
                System.out.println("[NotificationDAO] âœ“ Notification added with id=" + newId);
                return newId;
            }

        } catch (SQLException e) {
            System.err.println("[NotificationDAO] ERROR adding notification: " + e.getMessage());
        }
        return -1;
    }

    public List<Notification> findByCustomerId(int customerId) {
        String sql = "SELECT * FROM notifications WHERE customer_id = ? ORDER BY created_at DESC";
        List<Notification> list = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] ERROR finding notifications: " + e.getMessage());
        }
        return list;
    }

    public List<Notification> findUnreadByCustomerId(int customerId) {
        String sql = "SELECT * FROM notifications WHERE customer_id = ? AND is_read = false ORDER BY created_at DESC";
        List<Notification> list = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] ERROR finding unread notifications: " + e.getMessage());
        }
        return list;
    }

    public int countUnreadByCustomerId(int customerId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE customer_id = ? AND is_read = false";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] ERROR counting unread: " + e.getMessage());
        }
        return 0;
    }

    
    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = true WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] ERROR marking as read: " + e.getMessage());
        }
        return false;
    }

    public boolean markAllAsRead(int customerId) {
        String sql = "UPDATE notifications SET is_read = true WHERE customer_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            int rows = ps.executeUpdate();
            System.out.println("[NotificationDAO] âœ“ Marked " + rows + " notifications as read");
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] ERROR marking all as read: " + e.getMessage());
        }
        return false;
    }

    
    public boolean deleteNotification(int id) {
        String sql = "DELETE FROM notifications WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] ERROR deleting notification: " + e.getMessage());
        }
        return false;
    }

    
    private Notification mapRow(ResultSet rs) throws SQLException {
        Integer relatedTxId = rs.getObject("related_transaction_id", Integer.class);
        
        return new Notification(
                rs.getInt("id"),
                rs.getInt("customer_id"),
                rs.getString("type"),
                rs.getString("title"),
                rs.getString("message"),
                rs.getDouble("amount"),
                rs.getDouble("fee"),
                rs.getString("reference_id"),
                rs.getBoolean("is_read"),
                getStringOrNull(rs, "sender_name"),
                getStringOrNull(rs, "receiver_name"),
                getBoolOrDefault(rs, "action_required", false),
                getStringOrNull(rs, "action_type"),
                relatedTxId,
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
    
    private String getStringOrNull(ResultSet rs, String col) {
        try { return rs.getString(col); } 
        catch (SQLException e) { return null; }
    }
    
    private boolean getBoolOrDefault(ResultSet rs, String col, boolean def) {
        try { return rs.getBoolean(col); } 
        catch (SQLException e) { return def; }
    }
}
