package com.igirepay.igirepaypaymentgateway.LAB2.service;

import com.igirepay.igirepaypaymentgateway.LAB2.dao.AccountDAO;
import com.igirepay.igirepaypaymentgateway.LAB2.dao.CustomerDAO;
import com.igirepay.igirepaypaymentgateway.LAB2.dao.NotificationDAO;
import com.igirepay.igirepaypaymentgateway.LAB2.dao.ProcessedRequestDAO;
import com.igirepay.igirepaypaymentgateway.LAB2.dao.TransactionDAO;
import com.igirepay.igirepaypaymentgateway.LAB2.db.DatabaseConnection;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Account;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Notification;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Transaction;
import com.igirepay.igirepaypaymentgateway.LAB2.util.ValidationUtil;
import com.igirepay.igirepaypaymentgateway.LAB3.exception.IgirePayException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


public class PaymentService {

    private final CustomerDAO         customerDAO         = new CustomerDAO();
    private final AccountDAO          accountDAO          = new AccountDAO();
    private final TransactionDAO      transactionDAO      = new TransactionDAO();
    private final ProcessedRequestDAO processedRequestDAO = new ProcessedRequestDAO();
    private final NotificationDAO     notificationDAO     = new NotificationDAO();

   
    public Customer registerCustomer(String fullName, String email,
                                     String phoneNumber, String pin) throws IgirePayException {
        ValidationUtil.validatePhoneNumber(phoneNumber);
        ValidationUtil.validateEmail(email);
        
        Customer customer = new Customer(fullName, email, phoneNumber, pin);
        int id = customerDAO.addCustomer(customer);
        if (id == -1) {
            System.out.println("[PaymentService]  Failed to register customer.");
            throw new IgirePayException(
                IgirePayException.ErrorType.DATABASE_ERROR,
                "Failed to register customer. Email or phone may already be in use."
            );
        }
        return customer;
    }

    public Customer findCustomerByEmail(String email) { return customerDAO.findByEmail(email); }
    public boolean  updateCustomer(Customer customer)  { return customerDAO.updateCustomer(customer); }
    public List<Customer> getAllCustomers()             { return customerDAO.findAll(); }

  
    public Account createAccount(int customerId, String accountType,
                                 double initialBalance, String currency) {
        Account account = new Account(customerId, accountType, initialBalance, currency);
        int id = accountDAO.createAccount(account);
        if (id == -1) {
            System.out.println("[PaymentService]  Failed to create account.");
            return null;
        }
        return account;
    }

    public List<Account> getAccountsByCustomer(int customerId) {
        return accountDAO.findByCustomerId(customerId);
    }

    
    public boolean deleteAccount(int accountId) {
        
        AccountService accountService = new AccountService();
        return accountService.deleteAccountWithTransfer(accountId);
    }

    public boolean deleteCustomer(int customerId) {
        return customerDAO.deleteCustomer(customerId);
    }
    
    public boolean deposit(int accountId, String referenceId,
                           double amount, String description) {

       System.out.println("[PaymentService] DEPOSIT | Ref: " + referenceId);

         if (processedRequestDAO.existsByReferenceId(referenceId)) {
            System.out.println("[PaymentService]  DUPLICATE! Ref already processed: " + referenceId);
            return false;
        }

         try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); 
            try {
                
                Account account = findAccountInConn(conn, accountId);
                if (account == null) throw new SQLException("Account not found: " + accountId);

                
                updateBalanceInConn(conn, accountId, account.getBalance() + amount);

                
                saveTransactionInConn(conn, accountId, referenceId,
                        Transaction.TYPE_DEPOSIT, amount, Transaction.STATUS_SUCCESS, description);

                
                saveProcessedRefInConn(conn, referenceId);

                conn.commit(); 
                
                double newBalance = account.getBalance() + amount;
                System.out.println("[PaymentService]  Deposit committed. New balance: " + newBalance);
                
                
                sendDepositNotification(account.getCustomerId(), amount, newBalance, referenceId);
                
                return true;

            } catch (SQLException e) {
                conn.rollback(); 
                System.err.println("[PaymentService]  Deposit rolled back: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("[PaymentService]  DB connection error: " + e.getMessage());
            return false;
        }
    }
   
    public boolean withdraw(int accountId, String referenceId,
                            double amount, String description) {

         System.out.println("[PaymentService] WITHDRAWAL | Ref: " + referenceId);

        if (processedRequestDAO.existsByReferenceId(referenceId)) {
            System.out.println("[PaymentService]  DUPLICATE! Ref already processed: " + referenceId);
            return false;
        }

        
        double fee = ValidationUtil.calculateTransactionFee(amount);
        double totalDeduction = amount + fee;

        System.out.println("[PaymentService] Amount: " + amount + " RWF, Fee: " + fee + " RWF, Total: " + totalDeduction + " RWF");

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); 

            try {
                Account account = findAccountInConn(conn, accountId);
                if (account == null) throw new SQLException("Account not found: " + accountId);

                if (totalDeduction > account.getBalance())
                    throw new SQLException("Insufficient funds. Balance="
                            + account.getBalance() + ", Required (amount + fee)=" + totalDeduction);

                updateBalanceInConn(conn, accountId, account.getBalance() - totalDeduction);
                saveTransactionInConn(conn, accountId, referenceId,
                        Transaction.TYPE_WITHDRAWAL, totalDeduction, Transaction.STATUS_SUCCESS, 
                        description + " (Fee: " + fee + " RWF)");
                
                
                if (fee > 0) {
                    saveTransactionInConn(conn, accountId, referenceId + "-FEE",
                            "FEE", fee, Transaction.STATUS_SUCCESS,
                            "Withdrawal fee");
                }
                
                saveProcessedRefInConn(conn, referenceId);

                conn.commit(); 
                
                double newBalance = account.getBalance() - totalDeduction;
                System.out.println("[PaymentService]  Withdrawal committed. Amount: " + amount 
                        + " RWF, Fee: " + fee + " RWF, Total deducted: " + totalDeduction + " RWF");
                
                
                sendWithdrawalNotification(account.getCustomerId(), amount, fee, newBalance, referenceId);
                
                return true;

            } catch (SQLException e) {
                conn.rollback(); 
                System.err.println("[PaymentService]  Withdrawal rolled back: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("[PaymentService]  DB connection error: " + e.getMessage());
            return false;
        }
    }
    
    public boolean transfer(int fromAccountId, int toAccountId,
                            String referenceId, double amount, String description) {

        System.out.println("[PaymentService] TRANSFER | Ref: " + referenceId);

        if (processedRequestDAO.existsByReferenceId(referenceId)) {
            System.out.println("[PaymentService]  DUPLICATE! Ref already processed: " + referenceId);
            return false;
        }

        
        double fee = ValidationUtil.calculateTransactionFee(amount);
        double totalDeduction = amount + fee;

        System.out.println("[PaymentService] Amount: " + amount + " RWF, Fee: " + fee + " RWF, Total: " + totalDeduction + " RWF");

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); 

            try {
                Account from = findAccountInConn(conn, fromAccountId);
                Account to   = findAccountInConn(conn, toAccountId);

                if (from == null) throw new SQLException("Source account not found: " + fromAccountId);
                if (to   == null) throw new SQLException("Destination account not found: " + toAccountId);
                if (totalDeduction > from.getBalance())
                    throw new SQLException("Insufficient funds. Balance="
                            + from.getBalance() + ", Required (amount + fee)=" + totalDeduction);

                
                updateBalanceInConn(conn, fromAccountId, from.getBalance() - totalDeduction);
                saveTransactionInConn(conn, fromAccountId, referenceId,
                        Transaction.TYPE_TRANSFER, totalDeduction, Transaction.STATUS_SUCCESS,
                        "Transfer out: " + description + " (Fee: " + fee + " RWF)");

                
                updateBalanceInConn(conn, toAccountId, to.getBalance() + amount);
                saveTransactionInConn(conn, toAccountId, referenceId + "-CREDIT",
                        Transaction.TYPE_TRANSFER, amount, Transaction.STATUS_SUCCESS,
                        "Transfer in: " + description);

               if (fee > 0) {
                    saveTransactionInConn(conn, fromAccountId, referenceId + "-FEE",
                            "FEE", fee, Transaction.STATUS_SUCCESS,
                            "Transaction fee for transfer");
                }

                saveProcessedRefInConn(conn, referenceId);

                conn.commit(); 
                
                double senderNewBalance = from.getBalance() - totalDeduction;
                double receiverNewBalance = to.getBalance() + amount;
                
                System.out.println("[PaymentService]  Transfer committed. "
                        + amount + " RWF moved from " + fromAccountId + " to " + toAccountId 
                        + " (Fee: " + fee + " RWF)");
                
                sendTransferNotifications(from.getCustomerId(), to.getCustomerId(), 
                                        amount, fee, senderNewBalance, receiverNewBalance, referenceId);
                
                return true;

            } catch (SQLException e) {
                conn.rollback(); 
                System.err.println("[PaymentService]  Transfer rolled back: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("[PaymentService]  DB connection error: " + e.getMessage());
            return false;
        }
    }
   
    public boolean transferInternal(int fromAccountId, int toAccountId,
                                    String referenceId, double amount, String description) {

        System.out.println("[PaymentService] INTERNAL TRANSFER | Ref: " + referenceId);

        if (processedRequestDAO.existsByReferenceId(referenceId)) {
            System.out.println("[PaymentService] DUPLICATE! Ref already processed: " + referenceId);
            return false;
        }

        System.out.println("[PaymentService] Amount: " + amount + " RWF (No fee for internal transfer)");

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); 

            try {
                Account from = findAccountInConn(conn, fromAccountId);
                Account to   = findAccountInConn(conn, toAccountId);

                if (from == null) throw new SQLException("Source account not found: " + fromAccountId);
                if (to   == null) throw new SQLException("Destination account not found: " + toAccountId);
                if (amount > from.getBalance())
                    throw new SQLException("Insufficient funds. Balance="
                            + from.getBalance() + ", Required=" + amount);

                
                if (from.getCustomerId() != to.getCustomerId()) {
                    throw new SQLException("Internal transfer only allowed between accounts of the same customer");
                }

                
                updateBalanceInConn(conn, fromAccountId, from.getBalance() - amount);
                saveTransactionInConn(conn, fromAccountId, referenceId,
                        Transaction.TYPE_TRANSFER, amount, Transaction.STATUS_SUCCESS,
                        "Internal transfer out: " + description);

                
                updateBalanceInConn(conn, toAccountId, to.getBalance() + amount);
                saveTransactionInConn(conn, toAccountId, referenceId + "-CREDIT",
                        Transaction.TYPE_TRANSFER, amount, Transaction.STATUS_SUCCESS,
                        "Internal transfer in: " + description);

                
                saveProcessedRefInConn(conn, referenceId);

                conn.commit(); 
                System.out.println("[PaymentService]  Internal transfer committed. "
                        + amount + " RWF moved from " + fromAccountId + " to " + toAccountId 
                        + " (No fee)");
                
                
                sendInternalTransferNotification(from.getCustomerId(), 
                                                from.getAccountType(), 
                                                to.getAccountType(), 
                                                amount, 
                                                referenceId);
                
                return true;

            } catch (SQLException e) {
                conn.rollback(); 
                System.err.println("[PaymentService]  Internal transfer rolled back: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("[PaymentService]  DB connection error: " + e.getMessage());
            return false;
        }
    }

    
    public List<Transaction> searchTransactions(int accountId, String type,
                                                Double minAmount, Double maxAmount,
                                                String keyword) {
        return transactionDAO.findByFilter(accountId, type, minAmount, maxAmount, keyword);
    }

    
    public void printTransactionHistory(int accountId) {
        List<Transaction> history = transactionDAO.findByAccountId(accountId);
        System.out.println("\n TRANSACTION HISTORY (account " + accountId + ") ");
        if (history.isEmpty()) System.out.println("  (no transactions yet)");
        for (Transaction t : history) System.out.println("  " + t);
       }

    public void printAllTransactions() {
        List<Transaction> all = transactionDAO.findAll();
        System.out.println("\n ALL TRANSACTIONS ");
        for (Transaction t : all) System.out.println("  " + t);
       
        }
   
    private Account findAccountInConn(Connection conn, int accountId) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                boolean isActive = true;
                try { isActive = rs.getBoolean("is_active"); } catch (Exception e) {}
                
                java.sql.Timestamp lastTxTs = null;
                try { lastTxTs = rs.getTimestamp("last_transaction_date"); } catch (Exception e) {}
                
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
        }
        return null;
    }

   
    private void updateBalanceInConn(Connection conn, int accountId,
                                     double newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        }
    }

   
    private void saveTransactionInConn(Connection conn, int accountId,
                                       String referenceId, String type,
                                       double amount, String status,
                                       String description) throws SQLException {
        String sql = "INSERT INTO transactions " +
                     "(account_id, reference_id, transaction_type, amount, status, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setString(2, referenceId);
            ps.setString(3, type);
            ps.setDouble(4, amount);
            ps.setString(5, status);
            ps.setString(6, description);
            ps.executeUpdate();
        }
    }

   private void saveProcessedRefInConn(Connection conn,
                                        String referenceId) throws SQLException {
        String sql = "INSERT INTO processed_requests (reference_id) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, referenceId);
            ps.executeUpdate();
        }
    }
    
        private void sendDepositNotification(int customerId, double amount, double newBalance, String referenceId) {
        try {
            Customer customer = customerDAO.findById(customerId);
            if (customer == null) return;

            String title = "Deposit Successful";
            String message = String.format("You have deposited %.2f RWF. New balance: %.2f RWF", 
                                         amount, newBalance);

            Notification notification = new Notification(
                customerId,
                Notification.TYPE_DEPOSIT,
                title,
                message,
                amount,
                0.0, 
                referenceId,
                null, 
                customer.getFullName(), 
                false, 
                null,
                null
            );

            notificationDAO.addNotification(notification);
            System.out.println("[PaymentService]  Deposit notification sent to customer " + customerId);

        } catch (Exception e) {
            System.err.println("[PaymentService]  Failed to send deposit notification: " + e.getMessage());
        }
    }

        private void sendWithdrawalNotification(int customerId, double amount, double fee, double newBalance, String referenceId) {
        try {
            Customer customer = customerDAO.findById(customerId);
            if (customer == null) return;

            String title = "Withdrawal Successful";
            String message = String.format("You have withdrawn %.2f RWF (Fee: %.2f RWF). Remaining balance: %.2f RWF", 
                                         amount, fee, newBalance);

            Notification notification = new Notification(
                customerId,
                Notification.TYPE_WITHDRAWAL,
                title,
                message,
                amount,
                fee,
                referenceId,
                customer.getFullName(), 
                null, 
                false, 
                null,
                null
            );

            notificationDAO.addNotification(notification);
            System.out.println("[PaymentService]  Withdrawal notification sent to customer " + customerId);

        } catch (Exception e) {
            System.err.println("[PaymentService]  Failed to send withdrawal notification: " + e.getMessage());
        }
    }

    /**
     * Send notifications to both sender and receiver after successful transfer.
     * Sender sees: receiver phone (masked), receiver name, remaining balance, fee
     * Receiver sees: sender phone (masked), sender name, new balance
     */
    private void sendTransferNotifications(int fromCustomerId, int toCustomerId, 
                                          double amount, double fee, 
                                          double senderNewBalance, double receiverNewBalance,
                                          String referenceId) {
        try {
            Customer sender = customerDAO.findById(fromCustomerId);
            Customer receiver = customerDAO.findById(toCustomerId);

            if (sender == null || receiver == null) {
                System.err.println("[PaymentService]  Cannot send transfer notifications: customer not found");
                return;
            }

            // Mask phone numbers (show first 3 digits + ...)
            String maskedReceiverPhone = maskPhoneNumber(receiver.getPhoneNumber());
            String maskedSenderPhone = maskPhoneNumber(sender.getPhoneNumber());

            // 1. Notification to SENDER
            String senderTitle = "Payment Sent";
            String senderMessage = String.format(
                "You made payment to %s (%s). Amount: %.2f RWF, Fee: %.2f RWF. Remaining balance: %.2f RWF",
                maskedReceiverPhone,
                receiver.getFullName(),
                amount,
                fee,
                senderNewBalance
            );

            Notification senderNotification = new Notification(
                fromCustomerId,
                Notification.TYPE_SENT,
                senderTitle,
                senderMessage,
                amount,
                fee,
                referenceId,
                sender.getFullName(),
                receiver.getFullName(),
                false,
                null,
                null
            );

            notificationDAO.addNotification(senderNotification);
            System.out.println("[PaymentService]  Transfer notification sent to sender " + fromCustomerId);

            // 2. Notification to RECEIVER
            String receiverTitle = "Payment Received";
            String receiverMessage = String.format(
                "You have received %.2f RWF from %s (%s). New balance: %.2f RWF",
                amount,
                maskedSenderPhone,
                sender.getFullName(),
                receiverNewBalance
            );

            Notification receiverNotification = new Notification(
                toCustomerId,
                Notification.TYPE_RECEIVED,
                receiverTitle,
                receiverMessage,
                amount,
                0.0, // Receiver doesn't pay the fee
                referenceId,
                sender.getFullName(),
                receiver.getFullName(),
                false,
                null,
                null
            );

            notificationDAO.addNotification(receiverNotification);
            System.out.println("[PaymentService]  Transfer notification sent to receiver " + toCustomerId);

        } catch (Exception e) {
            System.err.println("[PaymentService]  Failed to send transfer notifications: " + e.getMessage());
        }
    }

    /**
     * Send notification to customer after successful internal transfer (wallet  savings).
     */
    private void sendInternalTransferNotification(int customerId, String fromAccountType, 
                                                 String toAccountType, double amount, 
                                                 String referenceId) {
        try {
            Customer customer = customerDAO.findById(customerId);
            if (customer == null) return;

            String title = "Internal Transfer Successful";
            String message = String.format(
                "You transferred %.2f RWF from %s to %s. No fee charged for internal transfers.",
                amount,
                fromAccountType,
                toAccountType
            );

            Notification notification = new Notification(
                customerId,
                Notification.TYPE_SENT,
                title,
                message,
                amount,
                0.0, // No fee for internal transfers
                referenceId,
                customer.getFullName(),
                customer.getFullName(),
                false,
                null,
                null
            );

            notificationDAO.addNotification(notification);
            System.out.println("[PaymentService]  Internal transfer notification sent to customer " + customerId);

        } catch (Exception e) {
            System.err.println("[PaymentService]  Failed to send internal transfer notification: " + e.getMessage());
        }
    }

    /**
     * Mask phone number for privacy (show first 3 digits + ...).
     * Example: 0781234567  078...
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 3) {
            return "***";
        }
        return phoneNumber.substring(0, 3) + "...";
    }
}
