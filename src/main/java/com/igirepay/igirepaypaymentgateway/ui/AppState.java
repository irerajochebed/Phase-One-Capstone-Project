package com.igirepay.igirepaypaymentgateway.ui;

import com.igirepay.igirepaypaymentgateway.LAB2.model.Account;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB2.service.NotificationService;
import com.igirepay.igirepaypaymentgateway.LAB2.service.PaymentService;
import com.igirepay.igirepaypaymentgateway.LAB3.service.AdminService;
import com.igirepay.igirepaypaymentgateway.LAB3.service.AuthService;
import com.igirepay.igirepaypaymentgateway.LAB3.service.LoanService;


public class AppState {
    private static AppState instance;

    public static AppState getInstance() {
        if (instance == null) instance = new AppState();
        return instance;
    }

    private AppState() {
        
        this.adminService = new AdminService(authService);
    }
    private final PaymentService      paymentService      = new PaymentService();
    private final AuthService         authService         = new AuthService();
    private final NotificationService notificationService = new NotificationService();
    private final AdminService        adminService;
    private final LoanService         loanService         = new LoanService();
    private Customer      currentCustomer = null;  
    private Account       selectedAccount = null;  
    public PaymentService      getPaymentService()      {
         return paymentService;
          }
    public AuthService         getAuthService()         {
         return authService; 
         }
    public NotificationService getNotificationService() {
         return notificationService; 
         }
    public AdminService        getAdminService()        {
         return adminService; 
         }
    public LoanService         getLoanService()         {
         return loanService; 
         }

    public Customer getCurrentCustomer()                       { 
        return currentCustomer; 
    }
    public void     setCurrentCustomer(Customer c)             {
         this.currentCustomer = c; 
    }

    public Account  getSelectedAccount()                       {
         return selectedAccount;
     }
    public void     setSelectedAccount(Account a)              {
         this.selectedAccount = a;
          
    }
}
