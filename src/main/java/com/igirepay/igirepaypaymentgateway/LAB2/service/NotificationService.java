package com.igirepay.igirepaypaymentgateway.LAB2.service;

import com.igirepay.igirepaypaymentgateway.LAB2.dao.NotificationDAO;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Notification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationService {

    private final NotificationDAO notificationDAO = new NotificationDAO();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

   
    public void notifyMoneySent(int customerId, String recipientName, double amount, 
                               double fee, String referenceId) {
        String title = "ðŸ’¸ Money Sent Successfully";
        String message = String.format(
            "You sent %.2f RWF to %s\n" +
            "Fee: %.2f RWF\n" +
            "Total deducted: %.2f RWF\n" +
            "Reference: %s\n" +
            "Time: %s",
            amount, recipientName, fee, (amount + fee), referenceId,
            LocalDateTime.now().format(TIME_FORMAT)
        );

        Notification notification = new Notification(
            customerId, Notification.TYPE_SENT, title, message,
            amount, fee, referenceId
        );
        notificationDAO.addNotification(notification);
    }

   
    public void notifyMoneyReceived(int customerId, String senderName, double amount, 
                                   double newBalance, String referenceId) {
        String title = "ðŸ’° Money Received";
        String message = String.format(
            "You received %.2f RWF from %s\n" +
            "New balance: %.2f RWF\n" +
            "Reference: %s\n" +
            "Time: %s",
            amount, senderName, newBalance, referenceId,
            LocalDateTime.now().format(TIME_FORMAT)
        );

        Notification notification = new Notification(
            customerId, Notification.TYPE_RECEIVED, title, message,
            amount, 0.0, referenceId
        );
        notificationDAO.addNotification(notification);
    }

    
    public void notifyDeposit(int customerId, double amount, String referenceId) {
        String title = "âœ… Deposit Successful";
        String message = String.format(
            "Deposit of %.2f RWF completed\n" +
            "Reference: %s\n" +
            "Time: %s",
            amount, referenceId,
            LocalDateTime.now().format(TIME_FORMAT)
        );

        Notification notification = new Notification(
            customerId, Notification.TYPE_DEPOSIT, title, message,
            amount, 0.0, referenceId
        );
        notificationDAO.addNotification(notification);
    }

   
    public void notifyWithdrawal(int customerId, double amount, String referenceId) {
        String title = "ðŸ’µ Withdrawal Successful";
        String message = String.format(
            "Withdrawal of %.2f RWF completed\n" +
            "Reference: %s\n" +
            "Time: %s",
            amount, referenceId,
            LocalDateTime.now().format(TIME_FORMAT)
        );

        Notification notification = new Notification(
            customerId, Notification.TYPE_WITHDRAWAL, title, message,
            amount, 0.0, referenceId
        );
        notificationDAO.addNotification(notification);
    }

    
    public List<Notification> getNotifications(int customerId) {
        return notificationDAO.findByCustomerId(customerId);
    }

    
    public List<Notification> getUnreadNotifications(int customerId) {
        return notificationDAO.findUnreadByCustomerId(customerId);
    }

   
    public int getUnreadCount(int customerId) {
        return notificationDAO.countUnreadByCustomerId(customerId);
    }

   
    public boolean markAsRead(int notificationId) {
        return notificationDAO.markAsRead(notificationId);
    }

    
    public boolean markAllAsRead(int customerId) {
        return notificationDAO.markAllAsRead(customerId);
    }

    
    public boolean deleteNotification(int notificationId) {
        return notificationDAO.deleteNotification(notificationId);
    }

    
    public void sendAdminMessage(int customerId, String title, String message, 
                                boolean actionRequired, String actionType) {
        Notification notification = new Notification(
            customerId, Notification.TYPE_ADMIN_MESSAGE, title, message,
            0.0, 0.0, null, "Admin", null, actionRequired, actionType, null
        );
        notificationDAO.addNotification(notification);
    }

   
    public void notifyPasswordReset(int customerId) {
        String title = "ðŸ” Account Unlocked - Password Reset Required";
        String message = String.format(
            "Your account has been unlocked by an administrator.\n\n" +
            "For security reasons, please change your PIN immediately.\n\n" +
            "Time: %s",
            LocalDateTime.now().format(TIME_FORMAT)
        );

        Notification notification = new Notification(
            customerId, Notification.TYPE_PASSWORD_RESET, title, message,
            0.0, 0.0, null, "Admin", null, true, "RESET_PASSWORD", null
        );
        notificationDAO.addNotification(notification);
    }

   
    public void notifyAccountUnlocked(int customerId) {
        String title = "âœ… Account Unlocked";
        String message = String.format(
            "Your account has been unlocked by an administrator.\n" +
            "You can now login and use all services.\n\n" +
            "Time: %s",
            LocalDateTime.now().format(TIME_FORMAT)
        );

        Notification notification = new Notification(
            customerId, Notification.TYPE_ACCOUNT_UNLOCKED, title, message,
            0.0, 0.0, null
        );
        notificationDAO.addNotification(notification);
    }

   
    public void notifyDisputeUpdate(int customerId, String disputeStatus, String adminNotes) {
        String title = "ðŸ“‹ Dispute Update";
        String message = String.format(
            "Your dispute has been updated.\n\n" +
            "Status: %s\n" +
            "Admin Notes: %s\n\n" +
            "Time: %s",
            disputeStatus, adminNotes,
            LocalDateTime.now().format(TIME_FORMAT)
        );

        Notification notification = new Notification(
            customerId, Notification.TYPE_DISPUTE_UPDATE, title, message,
            0.0, 0.0, null
        );
        notificationDAO.addNotification(notification);
    }

    
    public void notifyFrozenFunds(int customerId, double amount, String reason, 
                                 int transactionId, boolean isReceiver) {
        String title = isReceiver ? " Funds Frozen - Action Required" : "Transaction Under Review";
        String message;
        
        if (isReceiver) {
            message = String.format(
                "The amount of %.2f RWF you received has been frozen due to a dispute.\n\n" +
                "Reason: %s\n\n" +
                "Please contact the sender or return the funds if this was sent in error.\n\n" +
                "Time: %s",
                amount, reason,
                LocalDateTime.now().format(TIME_FORMAT)
            );
        } else {
            message = String.format(
                "Your transaction of %.2f RWF is under review.\n\n" +
                "Reason: %s\n\n" +
                "We will notify you once this is resolved.\n\n" +
                "Time: %s",
                amount, reason,
                LocalDateTime.now().format(TIME_FORMAT)
            );
        }

        Notification notification = new Notification(
            customerId, Notification.TYPE_FROZEN_FUNDS, title, message,
            amount, 0.0, null, null, null, isReceiver, "RETURN_FUNDS", transactionId
        );
        notificationDAO.addNotification(notification);
    }

    
    public void requestFundReturn(int receiverCustomerId, String senderName, 
                                 double amount, int transactionId, String referenceId) {
        String title = "ðŸ”„ Fund Return Requested";
        String message = String.format(
            "%s has reported that %.2f RWF was sent to you by mistake.\n\n" +
            "Please return this amount to the sender.\n\n" +
            "Reference: %s\n" +
            "Time: %s",
            senderName, amount, referenceId,
            LocalDateTime.now().format(TIME_FORMAT)
        );

        Notification notification = new Notification(
            receiverCustomerId, Notification.TYPE_ADMIN_MESSAGE, title, message,
            amount, 0.0, referenceId, senderName, null, true, "RETURN_FUNDS", transactionId
        );
        notificationDAO.addNotification(notification);
    }
}
