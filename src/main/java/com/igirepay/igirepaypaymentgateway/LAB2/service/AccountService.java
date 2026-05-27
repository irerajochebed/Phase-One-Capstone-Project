package com.igirepay.igirepaypaymentgateway.LAB2.service;

import com.igirepay.igirepaypaymentgateway.LAB2.dao.AccountDAO;
import com.igirepay.igirepaypaymentgateway.LAB2.dao.TransactionDAO;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Account;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.igirepay.igirepaypaymentgateway.LAB2.db.DatabaseConnection;


public class AccountService {

    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;

    public AccountService() {
        this.accountDAO = new AccountDAO();
        this.transactionDAO = new TransactionDAO();
    }

   
    public int createAccount(int customerId, String accountType, double initialBalance, String currency) {
        if (accountDAO.hasAccountOfType(customerId, accountType)) {
            System.err.println("[AccountService]  Customer " + customerId + 
                             " already has a " + accountType + " account.");
            return -1;
        }
        Account newAccount = new Account(customerId, accountType, initialBalance, currency);
        int accountId = accountDAO.createAccount(newAccount);

        if (accountId > 0) {
            System.out.println("[AccountService]  Created " + accountType + 
                             " account for customer " + customerId);
        }

        return accountId;
    }

        public int createWalletAccount(int customerId) {
        return createAccount(customerId, "WALLET", 0.0, "RWF");
    }

        public int createSavingsAccount(int customerId) {
        return createAccount(customerId, "SAVINGS", 0.0, "RWF");
    }

 
    public boolean deleteAccountWithTransfer(int accountId) {
        Account accountToDelete = accountDAO.findById(accountId);
        
        if (accountToDelete == null) {
            System.err.println("[AccountService] âœ— Account " + accountId + " not found.");
            return false;
        }

        int customerId = accountToDelete.getCustomerId();
        String accountType = accountToDelete.getAccountType();
        double balance = accountToDelete.getBalance();
        String otherAccountType = accountType.equals("WALLET") ? "SAVINGS" : "WALLET";
        Account remainingAccount = accountDAO.findByCustomerIdAndType(customerId, otherAccountType);

        if (remainingAccount == null) {
            System.err.println("[AccountService] âœ— Cannot delete the only account. " +
                             "Customer must have at least one account.");
            return false;
        }

        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); 

            if (balance > 0) {
                String getBalanceSql = "SELECT balance FROM accounts WHERE id = ?";
                double currentBalance = 0;
                try (PreparedStatement ps = conn.prepareStatement(getBalanceSql)) {
                    ps.setInt(1, remainingAccount.getId());
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        currentBalance = rs.getDouble("balance");
                    }
                }
                
                double newBalance = currentBalance + balance;
                String updateBalanceSql = "UPDATE accounts SET balance = ?, last_transaction_date = CURRENT_TIMESTAMP WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateBalanceSql)) {
                    ps.setDouble(1, newBalance);
                    ps.setInt(2, remainingAccount.getId());
                    int rows = ps.executeUpdate();
                    
                    if (rows == 0) {
                        conn.rollback();
                        System.err.println("[AccountService] âœ— Failed to transfer funds.");
                        return false;
                    }
                }

                
                String referenceId = "ACCT_TRANSFER_" + UUID.randomUUID().toString();
                
                
                String insertTxSql = "INSERT INTO transactions (account_id, reference_id, transaction_type, amount, status, description) VALUES (?, ?, ?, ?, ?, ?)";
                
                try (PreparedStatement ps = conn.prepareStatement(insertTxSql)) {
                    ps.setInt(1, accountToDelete.getId());
                    ps.setString(2, referenceId);
                    ps.setString(3, "TRANSFER");
                    ps.setDouble(4, balance);
                    ps.setString(5, "SUCCESS");
                    ps.setString(6, "Account closure - funds transferred to " + otherAccountType + " account");
                    ps.executeUpdate();
                }

                
                try (PreparedStatement ps = conn.prepareStatement(insertTxSql)) {
                    ps.setInt(1, remainingAccount.getId());
                    ps.setString(2, referenceId);
                    ps.setString(3, "TRANSFER");
                    ps.setDouble(4, balance);
                    ps.setString(5, "SUCCESS");
                    ps.setString(6, "Received from closed " + accountType + " account");
                    ps.executeUpdate();
                }

                System.out.println("[AccountService] âœ“ Transferred " + balance + 
                                 " from " + accountType + " to " + otherAccountType);
                System.out.println("[AccountService]   Old balance: " + currentBalance + 
                                 " â†’ New balance: " + newBalance);
            }
            
            String deleteTransactionsSql = "DELETE FROM transactions WHERE account_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteTransactionsSql)) {
                ps.setInt(1, accountId);
                int txCount = ps.executeUpdate();
                System.out.println("[AccountService] âœ“ Deleted " + txCount + " transaction records");
            }
            
            
            String deleteAccountSql = "DELETE FROM accounts WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteAccountSql)) {
                ps.setInt(1, accountId);
                int rows = ps.executeUpdate();
                
                if (rows == 0) {
                    conn.rollback();
                    System.err.println("[AccountService] âœ— Failed to delete account.");
                    return false;
                }
            }

            conn.commit(); 
            System.out.println("[AccountService] âœ“ Account " + accountId + " deleted successfully.");
            return true;

        } catch (SQLException e) {
            System.err.println("[AccountService] âœ— Error during account deletion: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("[AccountService] âœ— Rollback failed: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); 
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("[AccountService] âœ— Error closing connection: " + e.getMessage());
                }
            }
        }
    }

        public Account getWalletAccount(int customerId) {
        return accountDAO.findByCustomerIdAndType(customerId, "WALLET");
    }

        public Account getSavingsAccount(int customerId) {
        return accountDAO.findByCustomerIdAndType(customerId, "SAVINGS");
    }

        public List<Account> getCustomerAccounts(int customerId) {
        return accountDAO.findByCustomerId(customerId);
    }

        public boolean hasWalletAccount(int customerId) {
        return accountDAO.hasAccountOfType(customerId, "WALLET");
    }

        public boolean hasSavingsAccount(int customerId) {
        return accountDAO.hasAccountOfType(customerId, "SAVINGS");
    }

        public Account getAccountById(int accountId) {
        return accountDAO.findById(accountId);
    }

    
    public String validateAccountCreation(int customerId, String accountType) {
        if (!accountType.equals("WALLET") && !accountType.equals("SAVINGS")) {
            return "Invalid account type. Must be WALLET or SAVINGS.";
        }

        if (accountDAO.hasAccountOfType(customerId, accountType)) {
            return "You already have a " + accountType + " account. Each customer can have only one account of each type.";
        }

        return null; 
    }

   
    public String validateAccountDeletion(int accountId) {
        Account account = accountDAO.findById(accountId);
        
        if (account == null) {
            return "Account not found.";
        }

        int customerId = account.getCustomerId();
        List<Account> customerAccounts = accountDAO.findByCustomerId(customerId);

        if (customerAccounts.size() <= 1) {
            return "Cannot delete your only account. You must have at least one account.";
        }

        return null; 
    }
}
