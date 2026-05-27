package com.igirepay.igirepaypaymentgateway.LAB3.service;

import com.igirepay.igirepaypaymentgateway.LAB2.dao.AccountDAO;
import com.igirepay.igirepaypaymentgateway.LAB2.dao.LoanDAO;
import com.igirepay.igirepaypaymentgateway.LAB2.dao.NotificationDAO;
import com.igirepay.igirepaypaymentgateway.LAB2.dao.TransactionDAO;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Account;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Loan;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Notification;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Transaction;
import com.igirepay.igirepaypaymentgateway.LAB3.exception.IgirePayException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * LoanService â€” Business logic for loan management
 * - 1-month loans with 10% interest
 * - Admin approval required
 * - Automatic reminders for due loans
 */
public class LoanService {

    private final LoanDAO loanDAO = new LoanDAO();
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    // Loan constants
    public static final double DEFAULT_INTEREST_RATE = 10.0; // 10%
    public static final int DEFAULT_DURATION_MONTHS = 1;     // 1 month
    public static final int REMINDER_DAYS_BEFORE_DUE = 7;    // Remind 7 days before due
    public static final int OVERDUE_GRACE_DAYS = 7;          // Grace period before default

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // LOAN APPLICATION (Customer)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    /**
     * Apply for a loan
     */
    public Loan applyForLoan(int customerId, int accountId, double amount, String purpose) 
            throws IgirePayException {
        if (amount <= 0) {
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_AMOUNT,
                    "Loan amount must be positive");
        }
        
        if (amount < 10000) {
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_AMOUNT,
                    "Minimum loan amount is 10,000 RWF");
        }
        
        if (amount > 5000000) {
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_AMOUNT,
                    "Maximum loan amount is 5,000,000 RWF");
        }
        Account account = accountDAO.findById(accountId);
        if (account == null) {
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_ACCOUNT,
                    "Account not found");
        }

        if (account.getCustomerId() != customerId) {
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_ACCOUNT,
                    "Account does not belong to customer");
        }
        List<Loan> activeLoans = loanDAO.findByCustomerId(customerId).stream()
                .filter(l -> Loan.STATUS_ACTIVE.equals(l.getStatus()) || 
                            Loan.STATUS_PENDING.equals(l.getStatus()) ||
                            Loan.STATUS_OVERDUE.equals(l.getStatus()))
                .toList();

        if (!activeLoans.isEmpty()) {
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_INPUT,
                    "You already have an active loan. Please repay it before applying for a new one.");
        }
        Loan loan = new Loan(customerId, accountId, amount, 
                           DEFAULT_INTEREST_RATE, DEFAULT_DURATION_MONTHS, purpose);

        int loanId = loanDAO.createLoan(loan);
        if (loanId <= 0) {
            throw new IgirePayException(IgirePayException.ErrorType.DATABASE_ERROR,
                    "Failed to create loan application");
        }

        // Send notification to customer
        Notification notification = new Notification(
                customerId,
                "LOAN_APPLICATION",
                "Loan Application Submitted",
                String.format("Your loan application for %,.2f RWF has been submitted and is pending admin approval. " +
                             "You will be notified once it's reviewed.", amount),
                amount,
                0,
                "LOAN-" + loanId,
                null, null, false, null, null
        );
        notificationDAO.addNotification(notification);

        System.out.println("[LoanService] âœ“ Loan application created: " + loanId);
        return loanDAO.findById(loanId);
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // LOAN APPROVAL (Admin)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    /**
     * Approve loan and disburse funds
     */
    public boolean approveLoan(int loanId, int adminId) throws IgirePayException {
        Loan loan = loanDAO.findById(loanId);
        if (loan == null) {
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_INPUT,
                    "Loan not found");
        }

        if (!Loan.STATUS_PENDING.equals(loan.getStatus())) {
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_INPUT,
                    "Loan is not pending approval");
        }

        // Approve the loan
        boolean approved = loanDAO.approveLoan(loanId, adminId);
        if (!approved) {
            throw new IgirePayException(IgirePayException.ErrorType.DATABASE_ERROR,
                    "Failed to approve loan");
        }

        // Calculate due date (1 month from now)
        LocalDateTime dueDate = LocalDateTime.now().plusMonths(DEFAULT_DURATION_MONTHS);
        
        // Disburse funds to customer's account
        Account account = accountDAO.findById(loan.getAccountId());
        if (account == null) {
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_ACCOUNT,
                    "Account not found");
        }

        // Credit the account
        boolean credited = accountDAO.updateBalance(account.getId(), 
                                                   account.getBalance() + loan.getPrincipalAmount());
        if (!credited) {
            throw new IgirePayException(IgirePayException.ErrorType.DATABASE_ERROR,
                    "Failed to credit account");
        }

        // Record disbursement
        boolean disbursed = loanDAO.disburseLoan(loanId, dueDate);
        if (!disbursed) {
            throw new IgirePayException(IgirePayException.ErrorType.DATABASE_ERROR,
                    "Failed to record disbursement");
        }
        Transaction transaction = new Transaction(
                account.getId(),
                "LOAN-DISBURSEMENT-" + loanId,
                Transaction.TYPE_DEPOSIT,
                loan.getPrincipalAmount(),
                "Loan disbursement - Loan ID: " + loanId
        );
        transactionDAO.saveTransaction(transaction);

        // Send notification to customer
        Notification notification = new Notification(
                loan.getCustomerId(),
                "LOAN_APPROVED",
                "âœ… Loan Approved!",
                String.format("Congratulations! Your loan of %,.2f RWF has been approved and disbursed to your account.\n\n" +
                             "Loan Details:\n" +
                             "â€¢ Principal: %,.2f RWF\n" +
                             "â€¢ Interest Rate: %.1f%%\n" +
                             "â€¢ Total to Repay: %,.2f RWF\n" +
                             "â€¢ Due Date: %s\n\n" +
                             "Please ensure you repay the full amount before the due date to avoid penalties.",
                             loan.getPrincipalAmount(),
                             loan.getPrincipalAmount(),
                             loan.getInterestRate(),
                             loan.getTotalAmount(),
                             dueDate.toLocalDate()),
                loan.getPrincipalAmount(),
                0,
                "LOAN-" + loanId,
                null, null, true, "REPAY_LOAN", null
        );
        notificationDAO.addNotification(notification);

        System.out.println("[LoanService] âœ“ Loan approved and disbursed: " + loanId);
        return true;
    }

    /**
     * Reject loan application
     */
    public boolean rejectLoan(int loanId, String reason) throws IgirePayException {
        Loan loan = loanDAO.findById(loanId);
        if (loan == null) {
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_INPUT,
                    "Loan not found");
        }

        if (!Loan.STATUS_PENDING.equals(loan.getStatus())) {
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_INPUT,
                    "Loan is not pending approval");
        }

        boolean rejected = loanDAO.rejectLoan(loanId, reason);
        if (!rejected) {
            throw new IgirePayException(IgirePayException.ErrorType.DATABASE_ERROR,
                    "Failed to reject loan");
        }

        // Send notification to customer
        Notification notification = new Notification(
                loan.getCustomerId(),
                "LOAN_REJECTED",
                "âŒ Loan Application Rejected",
                String.format("Unfortunately, your loan application for %,.2f RWF has been rejected.\n\n" +
                             "Reason: %s\n\n" +
                             "You may apply again after addressing the concerns.",
                             loan.getPrincipalAmount(), reason),
                loan.getPrincipalAmount(),
                0,
                "LOAN-" + loanId,
                null, null, false, null, null
        );
        notificationDAO.addNotification(notification);

        System.out.println("[LoanService] âœ“ Loan rejected: " + loanId);
        return true;
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // LOAN REPAYMENT (Customer)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    /**
     * Make a loan payment
     */
    public boolean repayLoan(int loanId, int accountId, double amount) throws IgirePayException {
        Loan loan = loanDAO.findById(loanId);
        if (loan == null) {
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_INPUT,
                    "Loan not found");
        }

        if (!Loan.STATUS_ACTIVE.equals(loan.getStatus()) && 
            !Loan.STATUS_OVERDUE.equals(loan.getStatus())) {
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_INPUT,
                    "Loan is not active");
        }

        if (amount <= 0) {
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_AMOUNT,
                    "Payment amount must be positive");
        }

        if (amount > loan.getRemainingBalance()) {
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_AMOUNT,
                    String.format("Payment amount (%.2f) exceeds remaining balance (%.2f)", 
                                 amount, loan.getRemainingBalance()));
        }

        // Verify account
        Account account = accountDAO.findById(accountId);
        if (account == null) {
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_ACCOUNT,
                    "Account not found");
        }

        if (account.getCustomerId() != loan.getCustomerId()) {
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_ACCOUNT,
                    "Account does not belong to loan holder");
        }

        if (account.getBalance() < amount) {
            throw new IgirePayException(IgirePayException.ErrorType.INSUFFICIENT_BALANCE,
                    "Insufficient balance");
        }

        // Debit the account
        boolean debited = accountDAO.updateBalance(accountId, account.getBalance() - amount);
        if (!debited) {
            throw new IgirePayException(IgirePayException.ErrorType.DATABASE_ERROR,
                    "Failed to debit account");
        }

        // Record payment
        boolean recorded = loanDAO.recordPayment(loanId, amount);
        if (!recorded) {
            // Rollback account debit
            accountDAO.updateBalance(accountId, account.getBalance());
            throw new IgirePayException(IgirePayException.ErrorType.DATABASE_ERROR,
                    "Failed to record payment");
        }
        Transaction transaction = new Transaction(
                accountId,
                "LOAN-PAYMENT-" + loanId + "-" + UUID.randomUUID().toString().substring(0, 8),
                Transaction.TYPE_WITHDRAWAL,
                amount,
                "Loan repayment - Loan ID: " + loanId
        );
        transactionDAO.saveTransaction(transaction);

        // Reload loan to get updated balance
        loan = loanDAO.findById(loanId);

        // Send notification
        String notifType = loan.getRemainingBalance() <= 0 ? "LOAN_PAID" : "LOAN_PAYMENT";
        String title = loan.getRemainingBalance() <= 0 ? "ðŸŽ‰ Loan Fully Repaid!" : "âœ… Loan Payment Received";
        String message = loan.getRemainingBalance() <= 0 
            ? String.format("Congratulations! You have fully repaid your loan of %,.2f RWF.\n\n" +
                           "Payment: %,.2f RWF\n" +
                           "Your loan is now closed. Thank you for your timely repayment!",
                           loan.getTotalAmount(), amount)
            : String.format("Your loan payment of %,.2f RWF has been received.\n\n" +
                           "Remaining Balance: %,.2f RWF\n" +
                           "Due Date: %s",
                           amount, loan.getRemainingBalance(), 
                           loan.getDueDate() != null ? loan.getDueDate().toLocalDate() : "N/A");

        Notification notification = new Notification(
                loan.getCustomerId(),
                notifType,
                title,
                message,
                amount,
                0,
                "LOAN-" + loanId,
                null, null, false, null, null
        );
        notificationDAO.addNotification(notification);

        System.out.println("[LoanService] âœ“ Loan payment recorded: " + amount);
        return true;
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // ADMIN REMINDERS
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    /**
     * Send reminders to borrowers with loans due soon (within 7 days)
     * Called by admin
     */
    public int sendPaymentReminders() {
        List<Loan> loansDueSoon = loanDAO.findLoansDueSoon(REMINDER_DAYS_BEFORE_DUE);
        int remindersSent = 0;

        for (Loan loan : loansDueSoon) {
            long daysUntilDue = loan.daysUntilDue();
            
            String message = String.format(
                "âš ï¸ PAYMENT REMINDER\n\n" +
                "Your loan payment is due in %d day%s!\n\n" +
                "Loan Details:\n" +
                "â€¢ Loan ID: %d\n" +
                "â€¢ Amount Due: %,.2f RWF\n" +
                "â€¢ Due Date: %s\n\n" +
                "Please ensure you have sufficient funds in your account to make the payment. " +
                "Late payments may affect your credit standing.",
                daysUntilDue,
                daysUntilDue == 1 ? "" : "s",
                loan.getId(),
                loan.getRemainingBalance(),
                loan.getDueDate().toLocalDate()
            );

            Notification notification = new Notification(
                    loan.getCustomerId(),
                    "LOAN_REMINDER",
                    "â° Loan Payment Due Soon",
                    message,
                    loan.getRemainingBalance(),
                    0,
                    "LOAN-" + loan.getId(),
                    null, null, true, "REPAY_LOAN", null
            );

            int notifId = notificationDAO.addNotification(notification);
            if (notifId > 0) {
                remindersSent++;
            }
        }

        System.out.println("[LoanService] âœ“ Sent " + remindersSent + " payment reminders");
        return remindersSent;
    }

    /**
     * Send overdue notices to borrowers with overdue loans
     * Called by admin
     */
    public int sendOverdueNotices() {
        List<Loan> overdueLoans = loanDAO.findOverdueLoans();
        int noticesSent = 0;

        for (Loan loan : overdueLoans) {
            long daysOverdue = loan.daysOverdue();
            if (Loan.STATUS_ACTIVE.equals(loan.getStatus())) {
                loanDAO.updateStatus(loan.getId(), Loan.STATUS_OVERDUE);
            }
            
            // Mark as DEFAULTED if severely overdue
            if (daysOverdue > OVERDUE_GRACE_DAYS && !Loan.STATUS_DEFAULTED.equals(loan.getStatus())) {
                loanDAO.updateStatus(loan.getId(), Loan.STATUS_DEFAULTED);
            }

            String severity = daysOverdue > OVERDUE_GRACE_DAYS ? "ðŸš¨ URGENT" : "âš ï¸ OVERDUE";
            String message = String.format(
                "%s - LOAN PAYMENT OVERDUE\n\n" +
                "Your loan payment is %d day%s overdue!\n\n" +
                "Loan Details:\n" +
                "â€¢ Loan ID: %d\n" +
                "â€¢ Amount Due: %,.2f RWF\n" +
                "â€¢ Original Due Date: %s\n" +
                "â€¢ Days Overdue: %d\n\n" +
                "%s\n\n" +
                "Please contact support immediately to arrange payment.",
                severity,
                daysOverdue,
                daysOverdue == 1 ? "" : "s",
                loan.getId(),
                loan.getRemainingBalance(),
                loan.getDueDate().toLocalDate(),
                daysOverdue,
                daysOverdue > OVERDUE_GRACE_DAYS 
                    ? "âš ï¸ Your loan has been marked as DEFAULTED. This may affect your ability to borrow in the future."
                    : "Please make payment as soon as possible to avoid further penalties."
            );

            Notification notification = new Notification(
                    loan.getCustomerId(),
                    "LOAN_OVERDUE",
                    severity + " - Loan Payment Overdue",
                    message,
                    loan.getRemainingBalance(),
                    0,
                    "LOAN-" + loan.getId(),
                    null, null, true, "CONTACT_SUPPORT", null
            );

            int notifId = notificationDAO.addNotification(notification);
            if (notifId > 0) {
                noticesSent++;
            }
        }

        System.out.println("[LoanService] âœ“ Sent " + noticesSent + " overdue notices");
        return noticesSent;
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // QUERIES
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    public List<Loan> getCustomerLoans(int customerId) {
        return loanDAO.findByCustomerId(customerId);
    }

    public List<Loan> getPendingLoans() {
        return loanDAO.findByStatus(Loan.STATUS_PENDING);
    }

    public List<Loan> getActiveLoans() {
        return loanDAO.findByStatus(Loan.STATUS_ACTIVE);
    }

    public List<Loan> getOverdueLoans() {
        return loanDAO.findOverdueLoans();
    }

    public List<Loan> getAllLoans() {
        return loanDAO.findAll();
    }

    public Loan getLoanById(int loanId) {
        return loanDAO.findById(loanId);
    }
}
