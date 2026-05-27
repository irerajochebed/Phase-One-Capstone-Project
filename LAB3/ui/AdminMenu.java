package com.igirepay.igirepaypaymentgateway.LAB3.ui;

import com.igirepay.igirepaypaymentgateway.LAB2.model.Account;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB2.service.PaymentService;
import com.igirepay.igirepaypaymentgateway.LAB3.exception.IgirePayException;
import com.igirepay.igirepaypaymentgateway.LAB3.service.AuthService;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final PaymentService paymentService;
    private final AuthService    authService;
    private final Scanner        scanner;

    public AdminMenu(PaymentService paymentService, AuthService authService, Scanner scanner) {
        this.paymentService = paymentService;
        this.authService    = authService;
        this.scanner        = scanner;
    }

    public void show() {
        if (!authService.isAdmin()) {
            System.out.println("âœ— Access denied. Admin role required.");
            return;
        }

        boolean back = false;
        while (!back) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> listAllCustomers();
                case "2" -> unlockAccount();
                case "3" -> changeRole();
                case "4" -> listAllAccounts();
                case "5" -> listAllTransactions();
                case "6" -> deleteCustomer();
                case "0" -> back = true;
                default  -> System.out.println("Invalid option. Please enter 0â€“6.");
            }
        }
    }

    private void printMenu() {
        System.out.println("        ADMIN PANEL               ");
        System.out.println("  1. View all customers           ");
        System.out.println("  2. Unlock a locked account      ");
        System.out.println("  3. Change customer role         ");
        System.out.println("  4. View all accounts            ");
        System.out.println("  5. View all transactions        ");
        System.out.println("  6. Delete a customer            ");
        System.out.println("  0. Back                         ");System.out.print("Choose: ");
    }
    private void listAllCustomers() {
        try {
            authService.requireAdmin();
            List<Customer> customers = paymentService.getAllCustomers();

             System.out.printf("%-5s %-20s %-25s %-15s %-7s %-8s %-6s%n",
                    "ID", "Name", "Email", "Phone", "Role", "Locked", "Fails");
            System.out.println("â”€".repeat(90));

            for (Customer c : customers) {
                System.out.printf("%-5d %-20s %-25s %-15s %-7s %-8s %-6d%n",
                        c.getId(),
                        c.getFullName(),
                        c.getEmail(),
                        c.getPhoneNumber(),
                        c.getRole(),
                        c.isLocked() ? "ðŸ”’ YES" : "NO",
                        c.getFailedPinAttempts());
            }
           
        } catch (IgirePayException e) {
            System.out.println("âœ— " + e.getMessage());
        }
    }
    private void unlockAccount() {
        try {
            authService.requireAdmin();

            System.out.print("Enter Customer ID to unlock: ");
            int customerId = AccountMenu.parseAccountId(scanner.nextLine().trim());

            authService.unlockAccount(customerId);

        } catch (IgirePayException e) {
            System.out.println(" " + e.getMessage());
        }
    }
    private void changeRole() {
        try {
            authService.requireAdmin();

            System.out.print("Enter Customer ID: ");
            int customerId = AccountMenu.parseAccountId(scanner.nextLine().trim());

            System.out.print("New role [ADMIN / USER]: ");
            String role = scanner.nextLine().trim().toUpperCase();

            boolean ok = authService.setRole(customerId, role);
            System.out.println(ok ? " Role updated to " + role : "âœ— Failed to update role.");

        } catch (IgirePayException e) {
            System.out.println("" + e.getMessage());
        }
    }
    private void listAllAccounts() {
        try {
            authService.requireAdmin();
            List<Customer> customers = paymentService.getAllCustomers();

            System.out.println("\n ALL ACCOUNTS ");
            System.out.printf("%-6s %-6s %-10s %15s  %-8s  %s%n",
                    "AccID", "CustID", "Type", "Balance", "Currency", "Owner");
            System.out.println("".repeat(65));

            for (Customer c : customers) {
                List<Account> accounts = paymentService.getAccountsByCustomer(c.getId());
                for (Account a : accounts) {
                    System.out.printf("%-6d %-6d %-10s %15.2f  %-8s  %s%n",
                            a.getId(), a.getCustomerId(), a.getAccountType(),
                            a.getBalance(), a.getCurrency(), c.getFullName());
                }
            }
            
        } catch (IgirePayException e) {
            System.out.println(" " + e.getMessage());
        }
    }
    private void listAllTransactions() {
        try {
            authService.requireAdmin();
            paymentService.printAllTransactions();
        } catch (IgirePayException e) {
            System.out.println("" + e.getMessage());
        }
    }
    private void deleteCustomer() {
        try {
            authService.requireAdmin();

            System.out.print("Enter Customer ID to delete: ");
            int customerId = AccountMenu.parseAccountId(scanner.nextLine().trim());

            System.out.print("Are you sure? This deletes all their accounts too. (yes/no): ");
            String confirm = scanner.nextLine().trim();
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("Deletion cancelled.");
                return;
            }

            boolean ok = paymentService.deleteCustomer(customerId);
            System.out.println(ok ? " Customer deleted." : " Deletion failed.");

        } catch (IgirePayException e) {
            System.out.println(" " + e.getMessage());
        }
    }
}
