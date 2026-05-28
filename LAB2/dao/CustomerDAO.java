package com.igirepay.igirepaypaymentgateway.LAB2.dao;

import com.igirepay.igirepaypaymentgateway.LAB2.db.DatabaseConnection;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class CustomerDAO {

    
    public int addCustomer(Customer customer) {
        String sql = "INSERT INTO customers (full_name, email, phone_number, pin, role) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhoneNumber());
            ps.setString(4, customer.getPin());
            ps.setString(5, customer.getRole());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int newId = rs.getInt("id");
                customer.setId(newId);
                System.out.println("[CustomerDAO] âœ“ Customer added with id=" + newId);
                return newId;
            }

        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR adding customer: " + e.getMessage());
        }
        return -1;
    }

    
    public Customer findById(int id) {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR finding customer: " + e.getMessage());
        }
        return null;
    }

    public Customer findByEmail(String email) {
        String sql = "SELECT * FROM customers WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR finding customer by email: " + e.getMessage());
        }
        return null;
    }

    public List<Customer> findAll() {
        String sql = "SELECT * FROM customers ORDER BY id";
        List<Customer> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR listing customers: " + e.getMessage());
        }
        return list;
    }

   
    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET full_name=?, email=?, phone_number=?, pin=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhoneNumber());
            ps.setString(4, customer.getPin());
            ps.setInt(5, customer.getId());
            int rows = ps.executeUpdate();
            System.out.println("[CustomerDAO] âœ“ Customer updated. Rows: " + rows);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR updating customer: " + e.getMessage());
        }
        return false;
    }

    
    public boolean updateLockStatus(int customerId, int failedAttempts, boolean isLocked) {
        String sql = "UPDATE customers SET failed_pin_attempts=?, is_locked=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, failedAttempts);
            ps.setBoolean(2, isLocked);
            ps.setInt(3, customerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR updating lock status: " + e.getMessage());
        }
        return false;
    }

        public boolean updateRole(int customerId, String role) {
        String sql = "UPDATE customers SET role=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setInt(2, customerId);
            int rows = ps.executeUpdate();
            System.out.println("[CustomerDAO] âœ“ Role updated to " + role + " for customer id=" + customerId);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR updating role: " + e.getMessage());
        }
        return false;
    }

    
    public boolean deleteCustomer(int id) {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            System.out.println("[CustomerDAO] âœ“ Customer deleted. Rows: " + rows);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR deleting customer: " + e.getMessage());
        }
        return false;
    }

    
    private Customer mapRow(ResultSet rs) throws SQLException {
        String  role           = getStringOrDefault(rs, "role",               Customer.ROLE_USER);
        int     failedAttempts = getIntOrDefault(rs,    "failed_pin_attempts", 0);
        boolean isLocked       = getBoolOrDefault2(rs,  "is_locked",           false);

        return new Customer(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone_number"),
                rs.getString("pin"),
                role,
                failedAttempts,
                isLocked,
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

   

    private String getStringOrDefault(ResultSet rs, String col, String def) {
        try {
             String v = rs.getString(col);
         return v != null ? v : def; 
        }
        
        catch (SQLException e) {
             return def; 
        }
    }
    private int getIntOrDefault(ResultSet rs, String col, int def) {
        try { 
            return rs.getInt(col); 
        } 
        catch (SQLException e) {
             return def; 
        }
    }
    private boolean getBoolOrDefault2(ResultSet rs, String col, boolean def) {
        try { 
            return rs.getBoolean(col);
         } 
        catch (SQLException e) {
             return def; 
        }
    }
}
