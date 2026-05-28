package com.igirepay.igirepaypaymentgateway.LAB3.ui;

import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB2.service.PaymentService;
import com.igirepay.igirepaypaymentgateway.LAB3.exception.IgirePayException;
import com.igirepay.igirepaypaymentgateway.LAB3.service.AuthService;

import java.util.List;
import java.util.Scanner;


public class CustomerMenu {

    private final PaymentService paymentService;
    private final AuthService    authService;
    private final Scanner        scanner;

    public CustomerMenu(PaymentService paymentService, AuthService authService, Scanner scanner) {
        this.paymentService = paymentService;
        this.authService    = authService;
        this.scanner        = scanner;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> registerCustomer();
                case "2" -> updateCustomer();
                case "3" -> viewAccounts();
                case "4" -> changePin();
                case "0" -> back = true;
                default  -> System.out.println("Invalid option. Please enter 04.");
            }
        }
    }

    private void printMenu() {
        System.out.println("     CUSTOMER MANAGEMENT         ");
         System.out.println("  1. Register new customer      ");
        System.out.println("  2. Update my information       ");
        System.out.println("  3. View my accounts            ");
        System.out.println("  4. Change PIN                  ");
        System.out.println("  0. Back                        ");
        System.out.print("Choose: ");
    }
    private void registerCustomer() {
        System.out.println("\nRegister New Customer ");
        try {
            System.out.print("Full name    : ");
            String name = scanner.nextLine().trim();
            validateNotEmpty(name, "Full name");

            System.out.print("Email        : ");
            String email = scanner.nextLine().trim();
            validateNotEmpty(email, "Email");

            System.out.print("Phone number : ");
            String phone = scanner.nextLine().trim();
            validateNotEmpty(phone, "Phone number");

            System.out.print("Create a 4-digit PIN: ");
            String pin = scanner.nextLine().trim();
            if (!pin.matches("\\d{4}")) {
                throw new IgirePayException(
                        IgirePayException.ErrorType.INVALID_INPUT,
                        "PIN must be exactly 4 digits.");
            }

            Customer customer = paymentService.registerCustomer(name, email, phone, pin);
            if (customer != null) {
                System.out.println(" Customer registered! Your ID is: " + customer.getId());
                System.out.println("  Use your email and PIN to log in.");
            } else {
                System.out.println("Registration failed. Email or phone may already be in use.");
            }

        } catch (IgirePayException e) {
            System.out.println(" " + e.getMessage());
        }
    }
    private void updateCustomer() {
        System.out.println("\n Update My Information ");
        if (!authService.isLoggedIn()) {
            System.out.println(" You must be logged in to update your information.");
            return;
        }

        try {
            Customer me = authService.getLoggedInCustomer();
            System.out.println("Current name  : " + me.getFullName());
            System.out.println("Current email : " + me.getEmail());
            System.out.println("Current phone : " + me.getPhoneNumber());
            System.out.println("(Press Enter to keep the current value)");

            System.out.print("New full name  [" + me.getFullName() + "]: ");
            String name = scanner.nextLine().trim();
            if (!name.isEmpty()) me.setFullName(name);

            System.out.print("New email      [" + me.getEmail() + "]: ");
            String email = scanner.nextLine().trim();
            if (!email.isEmpty()) me.setEmail(email);

            System.out.print("New phone      [" + me.getPhoneNumber() + "]: ");
            String phone = scanner.nextLine().trim();
            if (!phone.isEmpty()) me.setPhoneNumber(phone);

            boolean updated = paymentService.updateCustomer(me);
            System.out.println(updated ? " Information updated." : " Update failed.");

        } catch (Exception e) {
            System.out.println(" Error: " + e.getMessage());
        }
    }
    private void viewAccounts() {
        System.out.println("\n My Accounts ");
        if (!authService.isLoggedIn()) {
            System.out.println("You must be logged in.");
            return;
        }

        Customer me = authService.getLoggedInCustomer();
        List<com.igirepay.igirepaypaymentgateway.LAB2.model.Account> accounts =
                paymentService.getAccountsByCustomer(me.getId());

        if (accounts.isEmpty()) {
            System.out.println("You have no accounts yet. Go to Account Management to create one.");
            return;
        }

        System.out.printf("%-6s %-10s %15s  %s%n", "ID", "Type", "Balance", "Currency");
        System.out.println("".repeat(42));
        for (var acc : accounts) {
            System.out.printf("%-6d %-10s %15.2f  %s%n",
                    acc.getId(), acc.getAccountType(), acc.getBalance(), acc.getCurrency());
        }
    }
    private void changePin() {
        try {
            authService.changePin(scanner);
        } catch (IgirePayException e) {
            System.out.println(" " + e.getMessage());
        }
    }
    private void validateNotEmpty(String value, String fieldName) throws IgirePayException {
        if (value == null || value.isEmpty()) {
            throw new IgirePayException(
                    IgirePayException.ErrorType.INVALID_INPUT,
                    fieldName + " cannot be empty.");
        }
    }
}
