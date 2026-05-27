package com.igirepay.igirepaypaymentgateway.LAB3.service;

import java.util.Scanner;

import com.igirepay.igirepaypaymentgateway.LAB2.dao.CustomerDAO;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB3.exception.IgirePayException;
import com.igirepay.igirepaypaymentgateway.ui.AppState;


public class AuthService {

    private final CustomerDAO customerDAO = new CustomerDAO();
  
    public Customer login(String email, String pin) throws IgirePayException {
        Customer customer = customerDAO.findByEmail(email.trim());
        if (customer == null) {
            throw new IgirePayException(
                    IgirePayException.ErrorType.INVALID_CUSTOMER,
                    "No account found for email: " + email);
        }

        if (customer.isLocked()) {
            throw new IgirePayException(
                    IgirePayException.ErrorType.INVALID_PIN,
                    "Account is LOCKED after too many failed PIN attempts.\n" +
                    "Contact an admin to unlock your account.");
        }

        if (!customer.getPin().equals(pin.trim())) {
            int newFailedCount = customer.getFailedPinAttempts() + 1;
            boolean shouldLock = newFailedCount >= Customer.MAX_FAILED_ATTEMPTS;
            customerDAO.updateLockStatus(customer.getId(), newFailedCount, shouldLock);

            if (shouldLock) {
                throw new IgirePayException(
                        IgirePayException.ErrorType.INVALID_PIN,
                        "Incorrect PIN. Account is now LOCKED after "
                        + Customer.MAX_FAILED_ATTEMPTS + " failed attempts.");
            } else {
                int remaining = Customer.MAX_FAILED_ATTEMPTS - newFailedCount;
                throw new IgirePayException(
                        IgirePayException.ErrorType.INVALID_PIN,
                        "Incorrect PIN. " + remaining + " attempt(s) remaining before lockout.");
            }
        }

       if (customer.getFailedPinAttempts() > 0) {
            customerDAO.updateLockStatus(customer.getId(), 0, false);
        }
        customer.setFailedPinAttempts(0);
        customer.setLocked(false);
        
        AppState.getInstance().setCurrentCustomer(customer);
        
        System.out.println("[AuthService] âœ“ Login successful: " + customer.getFullName() + 
                         " (Role: " + customer.getRole() + ", Admin: " + customer.isAdmin() + ")");
        return customer;
    }
    
    public Customer login(Scanner scanner) throws IgirePayException {
        System.out.print("Enter your email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter your PIN: ");
        String pin = scanner.nextLine().trim();
        return login(email, pin);
    }

    public void logout() { 
        AppState.getInstance().setCurrentCustomer(null);
        AppState.getInstance().setSelectedAccount(null);
    }

    
    public Customer getLoggedInCustomer() { 
        return AppState.getInstance().getCurrentCustomer(); 
    }
    
    public boolean isLoggedIn() { 
        return AppState.getInstance().getCurrentCustomer() != null; 
    }
    
    public boolean isAdmin() { 
        Customer current = AppState.getInstance().getCurrentCustomer();
        return current != null && current.isAdmin(); 
    }

    public void requireAdmin() throws IgirePayException {
        Customer current = AppState.getInstance().getCurrentCustomer();
        
        if (current == null) {
            System.out.println("[AuthService] âœ— No user logged in");
            throw new IgirePayException(
                    IgirePayException.ErrorType.INVALID_CUSTOMER,
                    "Access denied. You must be logged in as an admin.");
        }
        
        if (!current.isAdmin()) {
            System.out.println("[AuthService] âœ— User " + current.getFullName() + 
                             " is not an admin. Role: " + current.getRole() + 
                             ", isAdmin(): " + current.isAdmin());
            throw new IgirePayException(
                    IgirePayException.ErrorType.INVALID_CUSTOMER,
                    "Access denied. Admin role required.");
        }
        
        System.out.println("[AuthService] âœ“ Admin check passed for: " + current.getFullName() + 
                         " (Role: " + current.getRole() + ")");
    }
   
    public void changePin(String currentPin, String newPin, String confirmPin)
            throws IgirePayException {
        if (!isLoggedIn()) throw new IgirePayException(
                IgirePayException.ErrorType.INVALID_CUSTOMER, "Not logged in.");

        Customer loggedInCustomer = getLoggedInCustomer();
        
        if (!loggedInCustomer.getPin().equals(currentPin))
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_PIN,
                    "Current PIN is incorrect.");

        if (!newPin.matches("\\d{4}"))
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_INPUT,
                    "PIN must be exactly 4 digits.");

        if (!newPin.equals(confirmPin))
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_INPUT,
                    "PINs do not match.");

        loggedInCustomer.setPin(newPin);
        customerDAO.updateCustomer(loggedInCustomer);
    }
   
    public void changePin(Scanner scanner) throws IgirePayException {
        System.out.print("Current PIN: ");  String cur  = scanner.nextLine().trim();
        System.out.print("New PIN: ");      String nw   = scanner.nextLine().trim();
        System.out.print("Confirm PIN: ");  String conf = scanner.nextLine().trim();
        changePin(cur, nw, conf);
    }

    public void requirePinConfirmation(String pin) throws IgirePayException {
        Customer loggedInCustomer = getLoggedInCustomer();
        if (loggedInCustomer == null || !loggedInCustomer.getPin().equals(pin))
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_PIN,
                    "Incorrect PIN. Transaction cancelled.");
    }

    public void requirePinConfirmation(Scanner scanner) throws IgirePayException {
        System.out.print("Confirm PIN: ");
        requirePinConfirmation(scanner.nextLine().trim());
    }

   
    public boolean unlockAccount(int customerId) throws IgirePayException {
        requireAdmin();
        boolean success = customerDAO.updateLockStatus(customerId, 0, false);
        if (success) {
            System.out.println("[AuthService] âœ“ Account unlocked for customer ID: " + customerId);
        }
        return success;
    }

    public boolean setRole(int customerId, String role) throws IgirePayException {
        requireAdmin();
        if (!Customer.ROLE_ADMIN.equals(role) && !Customer.ROLE_USER.equals(role))
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_INPUT,
                    "Role must be 'ADMIN' or 'USER'.");
        boolean success = customerDAO.updateRole(customerId, role);
        if (success) {
            System.out.println("[AuthService] âœ“ Role updated to " + role + " for customer ID: " + customerId);
        }
        return success;
    }

    public boolean setRoleForNewAdmin(int customerId, String role) throws IgirePayException {
       if (!Customer.ROLE_ADMIN.equals(role) && !Customer.ROLE_USER.equals(role))
            throw new IgirePayException(IgirePayException.ErrorType.INVALID_INPUT,
                    "Role must be 'ADMIN' or 'USER'.");
        return customerDAO.updateRole(customerId, role);
    }
}
