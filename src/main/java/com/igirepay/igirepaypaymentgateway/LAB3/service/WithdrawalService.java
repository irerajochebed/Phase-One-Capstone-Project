package com.igirepay.igirepaypaymentgateway.LAB3.service;

import com.igirepay.igirepaypaymentgateway.LAB2.dao.AccountDAO;
import com.igirepay.igirepaypaymentgateway.LAB2.dao.NotificationDAO;
import com.igirepay.igirepaypaymentgateway.LAB2.dao.WithdrawalRequestDAO;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Account;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Notification;
import com.igirepay.igirepaypaymentgateway.LAB2.model.WithdrawalRequest;
import com.igirepay.igirepaypaymentgateway.LAB2.service.PaymentService;
import com.igirepay.igirepaypaymentgateway.LAB3.exception.IgirePayException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;


public class WithdrawalService {

    private final AccountDAO accountDAO = new AccountDAO();
    private final WithdrawalRequestDAO withdrawalRequestDAO = new WithdrawalRequestDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final PaymentService paymentService = new PaymentService();

    private static final int ACCOUNT_MATURITY_DAYS = 30;  // 1 month
    private static final int WAITING_PERIOD_HOURS = 48;   // 48 hours

    public WithdrawalRequest requestWithdrawal(int accountId, int customerId, double amount) 
            throws IgirePayException {
        
        
        Account account = accountDAO.findById(accountId);
        if (account == null) {
            throw new IgirePayException(
                IgirePayException.ErrorType.INVALID_ACCOUNT,
                "Account not found"
            );
        }

        if (account.getCustomerId() != customerId) {
            throw new IgirePayException(
                IgirePayException.ErrorType.INVALID_CUSTOMER,
                "Account does not belong to this customer"
            );
        }

               boolean isSavingsAccount = account.getAccountType().equalsIgnoreCase("SAVINGS");

        LocalDateTime availableDate;
        String message;

        if (!isSavingsAccount) {
            availableDate = LocalDateTime.now();
            message = "Withdrawal available immediately (Wallet account)";
        } else {
            LocalDateTime accountCreationDate = account.getCreatedAt();
            long daysSinceCreation = ChronoUnit.DAYS.between(accountCreationDate, LocalDateTime.now());

            if (daysSinceCreation >= ACCOUNT_MATURITY_DAYS) {
                availableDate = LocalDateTime.now();
                message = "Withdrawal available immediately (Account is mature)";
            } else {
               availableDate = LocalDateTime.now().plusHours(WAITING_PERIOD_HOURS);
                message = "Withdrawal will be available after 48-hour waiting period";
            }
        }

         String referenceId = "WR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        WithdrawalRequest request = new WithdrawalRequest(
            accountId, customerId, amount, availableDate, referenceId
        );

        int requestId = withdrawalRequestDAO.addWithdrawalRequest(request);
        if (requestId == -1) {
            throw new IgirePayException(
                IgirePayException.ErrorType.DATABASE_ERROR,
                "Failed to create withdrawal request"
            );
        }

       
        sendWithdrawalRequestNotification(customerId, amount, availableDate, referenceId);

        System.out.println("[WithdrawalService]  Withdrawal request created: " + message);
        return request;
    }

 
    public boolean processWithdrawalRequest(int requestId, String description) 
            throws IgirePayException {
        
        WithdrawalRequest request = withdrawalRequestDAO.findById(requestId);
        if (request == null) {
            throw new IgirePayException(
                IgirePayException.ErrorType.INVALID_INPUT,
                "Withdrawal request not found"
            );
        }

        if (!request.getStatus().equals(WithdrawalRequest.STATUS_PENDING)) {
            throw new IgirePayException(
                IgirePayException.ErrorType.INVALID_INPUT,
                "Withdrawal request already " + request.getStatus().toLowerCase()
            );
        }

        if (!request.isAvailable()) {
            long hoursRemaining = request.getHoursRemaining();
            throw new IgirePayException(
                IgirePayException.ErrorType.INVALID_INPUT,
                "Withdrawal not yet available. Please wait " + hoursRemaining + " more hours."
            );
        }

         boolean success = paymentService.withdraw(
            request.getAccountId(),
            request.getReferenceId(),
            request.getAmount(),
            description != null ? description : "Savings withdrawal"
        );

        if (!success) {
            throw new IgirePayException(
                IgirePayException.ErrorType.DATABASE_ERROR,
                "Withdrawal transaction failed"
            );
        }

        withdrawalRequestDAO.updateStatus(requestId, WithdrawalRequest.STATUS_PROCESSED);

        System.out.println("[WithdrawalService]  Withdrawal request processed successfully");
        return true;
    }

        public boolean cancelWithdrawalRequest(int requestId, int customerId) throws IgirePayException {
        WithdrawalRequest request = withdrawalRequestDAO.findById(requestId);
        
        if (request == null) {
            throw new IgirePayException(
                IgirePayException.ErrorType.INVALID_INPUT,
                "Withdrawal request not found"
            );
        }

        if (request.getCustomerId() != customerId) {
            throw new IgirePayException(
                IgirePayException.ErrorType.INVALID_CUSTOMER,
                "Not authorized to cancel this request"
            );
        }

        if (!request.getStatus().equals(WithdrawalRequest.STATUS_PENDING)) {
            throw new IgirePayException(
                IgirePayException.ErrorType.INVALID_INPUT,
                "Cannot cancel a " + request.getStatus().toLowerCase() + " request"
            );
        }

        boolean success = withdrawalRequestDAO.cancelRequest(requestId);
        
        if (success) {
            sendCancellationNotification(customerId, request.getAmount(), request.getReferenceId());
        }

        return success;
    }

        public List<WithdrawalRequest> getCustomerWithdrawalRequests(int customerId) {
        return withdrawalRequestDAO.findByCustomerId(customerId);
    }

        public List<WithdrawalRequest> getPendingWithdrawalRequests(int customerId) {
        return withdrawalRequestDAO.findPendingByCustomerId(customerId);
    }

    
    public List<WithdrawalRequest> getAvailableWithdrawalRequests(int customerId) {
        return withdrawalRequestDAO.findAvailableByCustomerId(customerId);
    }

    
    public boolean requiresWaitingPeriod(int accountId) {
        Account account = accountDAO.findById(accountId);
        if (account == null) return false;

        if (!account.getAccountType().equalsIgnoreCase("SAVINGS")) {
            return false;
        }
        LocalDateTime accountCreationDate = account.getCreatedAt();
        long daysSinceCreation = ChronoUnit.DAYS.between(accountCreationDate, LocalDateTime.now());

        return daysSinceCreation < ACCOUNT_MATURITY_DAYS;
    }

    
    public LocalDateTime getAccountMaturityDate(int accountId) {
        Account account = accountDAO.findById(accountId);
        if (account == null) return null;

        return account.getCreatedAt().plusDays(ACCOUNT_MATURITY_DAYS);
    }

    private void sendWithdrawalRequestNotification(int customerId, double amount, 
                                                   LocalDateTime availableDate, String referenceId) {
        try {
            boolean isImmediate = LocalDateTime.now().isAfter(availableDate) || 
                                 LocalDateTime.now().isEqual(availableDate);

            String title = isImmediate ? "Withdrawal Request - Ready" : "Withdrawal Request - Pending";
            String message;

            if (isImmediate) {
                message = String.format(
                    "Your withdrawal request for %.2f RWF is ready to process. Reference: %s",
                    amount, referenceId
                );
            } else {
                long hoursToWait = ChronoUnit.HOURS.between(LocalDateTime.now(), availableDate);
                message = String.format(
                    "Your withdrawal request for %.2f RWF has been created. " +
                    "It will be available in %d hours (48-hour waiting period for new savings accounts). " +
                    "Reference: %s",
                    amount, hoursToWait, referenceId
                );
            }

            Notification notification = new Notification(
                customerId,
                "WITHDRAWAL_REQUEST",
                title,
                message,
                amount,
                0.0,
                referenceId,
                null,
                null,
                false,
                null,
                null
            );

            notificationDAO.addNotification(notification);
        } catch (Exception e) {
            System.err.println("[WithdrawalService] Failed to send notification: " + e.getMessage());
        }
    }

    private void sendCancellationNotification(int customerId, double amount, String referenceId) {
        try {
            Notification notification = new Notification(
                customerId,
                "WITHDRAWAL_CANCELLED",
                "Withdrawal Request Cancelled",
                String.format("Your withdrawal request for %.2f RWF has been cancelled. Reference: %s",
                             amount, referenceId),
                amount,
                0.0,
                referenceId,
                null,
                null,
                false,
                null,
                null
            );

            notificationDAO.addNotification(notification);
        } catch (Exception e) {
            System.err.println("[WithdrawalService] Failed to send cancellation notification: " + e.getMessage());
        }
    }
}
