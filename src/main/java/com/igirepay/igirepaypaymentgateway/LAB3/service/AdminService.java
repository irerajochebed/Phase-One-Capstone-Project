package com.igirepay.igirepaypaymentgateway.LAB3.service;

import com.igirepay.igirepaypaymentgateway.LAB2.dao.*;
import com.igirepay.igirepaypaymentgateway.LAB2.model.*;
import com.igirepay.igirepaypaymentgateway.LAB2.service.NotificationService;
import com.igirepay.igirepaypaymentgateway.LAB3.exception.IgirePayException;

import java.util.List;


public class AdminService {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AccountDAO accountDAO = new AccountDAO();
    private final DisputeDAO disputeDAO = new DisputeDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final NotificationService notificationService = new NotificationService();
    private final AuthService authService;

    public AdminService(AuthService authService) {
        this.authService = authService;
    }

  
    public List<Account> getInactiveAccounts() throws IgirePayException {
        authService.requireAdmin();
        return accountDAO.getInactiveAccounts();
    }

    
    public List<Account> getStaleAccounts(int days) throws IgirePayException {
        authService.requireAdmin();
        return accountDAO.getStaleAccounts(days);
    }

    public boolean deleteInactiveAccount(int accountId) throws IgirePayException {
        authService.requireAdmin();
        
        Account account = accountDAO.findById(accountId);
        if (account == null) {
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_ACCOUNT,
                    "Account not found");
        }

        if (account.getBalance() > 0) {
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_INPUT,
                    "Cannot delete account with balance. Balance must be 0.");
        }

        return accountDAO.deleteAccount(accountId);
    }

  
    public boolean markAccountInactive(int accountId) throws IgirePayException {
        authService.requireAdmin();
        return accountDAO.markAsInactive(accountId);
    }

    
    public boolean unlockCustomerAccount(int customerId) throws IgirePayException {
        authService.requireAdmin();
        
        boolean success = customerDAO.updateLockStatus(customerId, 0, false);
        if (success) {
            notificationService.notifyAccountUnlocked(customerId);
            notificationService.notifyPasswordReset(customerId);
            System.out.println("[AdminService]  Customer " + customerId + " unlocked and notified");
        }
        return success;
    }

    public int createDispute(int customerId, int transactionId, String disputeType, 
                            String description) {
        Dispute dispute = new Dispute(customerId, transactionId, disputeType, description);
        int disputeId = disputeDAO.createDispute(dispute);
        
        if (disputeId > 0) {
            List<Customer> admins = customerDAO.findAll().stream()
                    .filter(Customer::isAdmin)
                    .toList();
            
            for (Customer admin : admins) {
                notificationService.sendAdminMessage(
                    admin.getId(),
                    " New Dispute Reported",
                    "A customer has reported a dispute.\n\n" +
                    "Dispute ID: " + disputeId + "\n" +
                    "Type: " + disputeType + "\n" +
                    "Description: " + description,
                    true,
                    "REVIEW_DISPUTE"
                );
            }
        }
        
        return disputeId;
    }

    
    public List<Dispute> getPendingDisputes() throws IgirePayException {
        authService.requireAdmin();
        return disputeDAO.getPendingDisputes();
    }

    
    public List<Dispute> getAllDisputes() throws IgirePayException {
        authService.requireAdmin();
        return disputeDAO.getAllDisputes();
    }

    public List<Dispute> getCustomerDisputes(int customerId) {
        return disputeDAO.getDisputesByCustomer(customerId);
    }

    
    public boolean resolveDispute(int disputeId, String status, String adminNotes, 
                                 int adminId) throws IgirePayException {
        authService.requireAdmin();
        
        Dispute dispute = disputeDAO.getDisputeById(disputeId);
        if (dispute == null) {
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_INPUT,
                    "Dispute not found");
        }

        boolean success = disputeDAO.updateDisputeStatus(disputeId, status, adminNotes, adminId);
        
        if (success) {
             notificationService.notifyDisputeUpdate(dispute.getCustomerId(), status, adminNotes);
        }
        
        return success;
    }

    
    public boolean freezeFundsAndRequestReturn(int disputeId) throws IgirePayException {
        authService.requireAdmin();
        
        Dispute dispute = disputeDAO.getDisputeById(disputeId);
        if (dispute == null) {
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_INPUT,
                    "Dispute not found");
        }

        Transaction transaction = transactionDAO.findById(dispute.getTransactionId());
        if (transaction == null) {
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_INPUT,
                    "Transaction not found");
        }
        Account senderAccount = accountDAO.findById(transaction.getAccountId());
        Customer sender = customerDAO.findById(senderAccount.getCustomerId());
       disputeDAO.updateDisputeStatus(disputeId, Dispute.STATUS_INVESTIGATING, 
                "Funds frozen, return requested", authService.getLoggedInCustomer().getId());

        notificationService.notifyFrozenFunds(
            sender.getId(),
            transaction.getAmount(),
            "Dispute reported - under investigation",
            transaction.getId(),
            false
        );

        
        System.out.println("[AdminService]  Funds frozen for dispute " + disputeId);
        return true;
    }

    
    public void sendMessageToCustomer(int customerId, String title, String message, 
                                     boolean actionRequired, String actionType) throws IgirePayException {
        authService.requireAdmin();
        notificationService.sendAdminMessage(customerId, title, message, actionRequired, actionType);
    }

    
    public void requestFundReturn(int receiverCustomerId, String senderName, 
                                 double amount, int transactionId, String referenceId) throws IgirePayException {
        authService.requireAdmin();
        notificationService.requestFundReturn(receiverCustomerId, senderName, amount, 
                                            transactionId, referenceId);
    }

  
    public int getPendingDisputeCount() throws IgirePayException {
        authService.requireAdmin();
        return disputeDAO.getPendingDisputes().size();
    }

    
    public int getInactiveAccountCount() throws IgirePayException {
        authService.requireAdmin();
        return accountDAO.getInactiveAccounts().size();
    }
}
