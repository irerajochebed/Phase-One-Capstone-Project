package com.igirepay.igirepaypaymentgateway.LAB2.dao;

import com.igirepay.igirepaypaymentgateway.LAB2.db.DatabaseConnection;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class TransactionDAO {

   
    public int saveTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions " +
                     "(account_id, reference_id, transaction_type, amount, status, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, transaction.getAccountId());
            ps.setString(2, transaction.getReferenceId());
            ps.setString(3, transaction.getTransactionType());
            ps.setDouble(4, transaction.getAmount());
            ps.setString(5, transaction.getStatus());
            ps.setString(6, transaction.getDescription());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int newId = rs.getInt("id");
                transaction.setId(newId);
                System.out.println("[TransactionDAO] âœ“ Transaction saved with id=" + newId);
                return newId;
            }

        } catch (SQLException e) {
            System.err.println("[TransactionDAO] ERROR saving transaction: " + e.getMessage());
        }
        return -1;
    }

    
    public List<Transaction> findByAccountId(int accountId) {
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY created_at DESC";
        List<Transaction> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[TransactionDAO] ERROR fetching history: " + e.getMessage());
        }
        return list;
    }

    
    public List<Transaction> findAll() {
        String sql = "SELECT * FROM transactions ORDER BY created_at DESC";
        List<Transaction> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            System.err.println("[TransactionDAO] ERROR fetching all transactions: " + e.getMessage());
        }
        return list;
    }


    public Transaction findById(int transactionId) {
        String sql = "SELECT * FROM transactions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, transactionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (SQLException e) {
            System.err.println("[TransactionDAO] ERROR finding transaction: " + e.getMessage());
        }
        return null;
    }

    
    public List<Transaction> findByFilter(int accountId, String type,
                                          Double minAmount, Double maxAmount,
                                          String keyword) {
        
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM transactions WHERE account_id = ?");

        if (type != null && !type.isBlank())
            sql.append(" AND transaction_type = ?");
        if (minAmount != null)
            sql.append(" AND amount >= ?");
        if (maxAmount != null)
            sql.append(" AND amount <= ?");
        if (keyword != null && !keyword.isBlank())
            sql.append(" AND description ILIKE ?");

        sql.append(" ORDER BY created_at DESC");

        List<Transaction> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setInt(idx++, accountId);
            if (type != null && !type.isBlank())
                ps.setString(idx++, type);
            if (minAmount != null)
                ps.setDouble(idx++, minAmount);
            if (maxAmount != null)
                ps.setDouble(idx++, maxAmount);
            if (keyword != null && !keyword.isBlank())
                ps.setString(idx++, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("[TransactionDAO] ERROR filtering transactions: " + e.getMessage());
        }
        return list;
    }

    
    public boolean deleteTransaction(int id) {
        String sql = "DELETE FROM transactions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[TransactionDAO] ERROR deleting transaction: " + e.getMessage());
        }
        return false;
    }

   
    private Transaction mapRow(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getInt("id"),
                rs.getInt("account_id"),
                rs.getString("reference_id"),
                rs.getString("transaction_type"),
                rs.getDouble("amount"),
                rs.getString("status"),
                rs.getString("description"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
