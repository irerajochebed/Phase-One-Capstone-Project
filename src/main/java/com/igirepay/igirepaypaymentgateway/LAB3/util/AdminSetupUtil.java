package com.igirepay.igirepaypaymentgateway.LAB3.util;

import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB2.service.PaymentService;
import com.igirepay.igirepaypaymentgateway.LAB3.exception.IgirePayException;
import com.igirepay.igirepaypaymentgateway.LAB3.service.AuthService;


public class AdminSetupUtil {

    private static final PaymentService paymentService = new PaymentService();
    private static final AuthService authService = new AuthService();

    public static boolean adminExists() {
        try {
            var allCustomers = paymentService.getAllCustomers();
            return allCustomers.stream().anyMatch(c -> "ADMIN".equals(c.getRole()));
        } catch (Exception e) {
            System.err.println("[AdminSetupUtil] Error checking admin: " + e.getMessage());
            return true; 
        }
    }

    
    public static Customer createAdmin(String fullName, String email, 
                                       String phone, String pin) throws IgirePayException {
        Customer newAdmin = paymentService.registerCustomer(fullName, email, phone, pin);
        
       authService.setRoleForNewAdmin(newAdmin.getId(), "ADMIN");
        
        return newAdmin;
    }

    public static PaymentService getPaymentService() {
        return paymentService;
    }

    
    public static AuthService getAuthService() {
        return authService;
    }
}
