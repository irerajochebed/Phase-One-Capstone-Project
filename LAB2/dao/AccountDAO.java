package com.igirepay.igirepaypaymentgateway.LAB2.dao;

import com.igirepay.igirepaypaymentgateway.LAB2.db.DatabaseConnection;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Account;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class AccountDAO {

    
    public int createAccount(Account account) {
        String sql = "INSERT INTO accounts (customer_id, account_type, balance, currency) " +
                     "VALUES (?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, account.getCustomerId());
            ps.setString(2, account.getAccountType());
            ps.setDouble(3, account.getBalance());
            ps.setString(4, account.getCurrency());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int newId = rs.getInt("id");
                account.setId(newId);
                System.out.println("[AccountDAO] âœ“ Account created with id=" + newId);
                return newId;
            }

        } catch (SQLException e) {
            System.err.println("[AccountDAO] ERROR creating account: " + e.getMessage());
        }
        return -1;
    }

    
    public Account findById(int id) {
        String sql = "SELECT * FROM accounts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            System.err.println("[AccountDAO] ERROR finding account: " + e.getMessage());
        }
        return null;
    }

    
    public List<Account> findByCustomerId(int customerId) {
        String sql = "SELECT * FROM accounts WHERE customer_id = ? ORDER BY id";
        List<Account> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[AccountDAO] ERROR listing accounts: " + e.getMessage());
        }
        return list;
    }

    
    public Account findByCustomerIdAndType(int customerId, String accountType) {
        String sql = "SELECT * FROM accounts WHERE customer_id = ? AND account_type = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            ps.setString(2, accountType);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            System.err.println("[AccountDAO] ERROR finding account by type: " + e.getMessage());
        }
        return null;
    }

    
    public boolean hasAccountOfType(int customerId, String accountType) {
        return findByCustomerIdAndType(customerId, accountType) != null;
    }

    
    public boolean updateBalance(int accountId, double newBalance) {
        String sql = "UPDATE accounts SET balance = ?, last_transaction_date = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, newBalance);
            ps.setInt(2, accountId);

            int rows = ps.executeUpdate();
            System.out.println("[AccountDAO] âœ“ Balance updated for account id=" + accountId
                    + " â†’ " + newBalance);
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[AccountDAO] ERROR updating balance: " + e.getMessage());
        }
        return false;
    }

    
    public boolean markAsInactive(int accountId) {
        String sql = "UPDATE accounts SET is_active = false WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, accountId);
            int rows = ps.executeUpdate();
            System.out.println("[AccountDAO] âœ“ Account " + accountId + " marked as inactive");
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[AccountDAO] ERROR marking account inactive: " + e.getMessage());
        }
        return false;
    }

    
    public List<Account> getInactiveAccounts() {
        String sql = "SELECT * FROM accounts WHERE is_active = false ORDER BY last_transaction_date DESC";
        List<Account> list = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[AccountDAO] ERROR getting inactive accounts: " + e.getMessage());
        }
        return list;
    }

    
    public List<Account> getStaleAccounts(int days) {
        String sql = "SELECT * FROM accounts WHERE last_transaction_date < (CURRENT_TIMESTAMP - INTERVAL '" + days + " days') " +
                     "OR last_transaction_date IS NULL ORDER BY last_transaction_date ASC";
        List<Account> list = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[AccountDAO] ERROR getting stale accounts: " + e.getMessage());
        }
        return list;
    }

    
    public boolean deleteAccount(int accountId) {
        String sql = "DELETE FROM accounts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            int rows = ps.executeUpdate();
            System.out.println("[AccountDAO] âœ“ Account deleted. id=" + accountId);
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[AccountDAO] ERROR deleting account: " + e.getMessage());
        }
        return false;
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        boolean isActive = getBoolOrDefault(rs, "is_active", true);
        Timestamp lastTxTs = getTimestampOrNull(rs, "last_transaction_date");
        
        return new Account(
                rs.getInt("id"),
                rs.getInt("customer_id"),
                rs.getString("account_type"),
                rs.getDouble("balance"),
                rs.getString("currency"),
                isActive,
                lastTxTs != null ? lastTxTs.toLocalDateTime() : null,
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
    
    private boolean getBoolOrDefault(ResultSet rs, String col, boolean def) {
        try { return rs.getBoolean(col); } 
        catch (SQLException e) { return def; }
    }
    
    private Timestamp getTimestampOrNull(ResultSet rs, String col) {
        try { return rs.getTimestamp(col); } 
        catch (SQLException e) { return null; }
    }
}
