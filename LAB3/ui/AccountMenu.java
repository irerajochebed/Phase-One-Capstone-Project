package com.igirepay.igirepaypaymentgateway.LAB3.ui;

import com.igirepay.igirepaypaymentgateway.LAB2.model.Account;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB2.service.PaymentService;
import com.igirepay.igirepaypaymentgateway.LAB3.exception.IgirePayException;
import com.igirepay.igirepaypaymentgateway.LAB3.service.AuthService;

import java.util.List;
import java.util.Scanner;


public class AccountMenu {

    private final PaymentService paymentService;
    private final AuthService    authService;
    private final Scanner        scanner;

    public AccountMenu(PaymentService paymentService, AuthService authService, Scanner scanner) {
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
                case "1" -> createAccount("WALLET");
                case "2" -> createAccount("SAVINGS");
                case "3" -> viewBalance();
                case "4" -> deleteAccount();
                case "0" -> back = true;
                default  -> System.out.println("Invalid option. Please enter 0â€“4.");
            }
        }
    }

    private void printMenu() {
       System.out.println("       ACCOUNT MANAGEMENT       ");
        System.out.println("  1. Create wallet account        ");
        System.out.println("  2. Create savings account       ");
        System.out.println("  3. View account balance         ");
        System.out.println("  4. Delete an account           ");
        System.out.println("  0. Back                         ");
        System.out.print("Choose: ");
    }
    private void createAccount(String type) {
        System.out.println("\n Create " + type + " Account ");

        if (!authService.isLoggedIn()) {
            System.out.println("You must be logged in.");
            return;
        }

        try {
            Customer me = authService.getLoggedInCustomer();

            System.out.print("Initial deposit amount (RWF): ");
            double initialBalance = parseAmount(scanner.nextLine().trim());

            Account account = paymentService.createAccount(
                    me.getId(), type, initialBalance, "RWF");

            if (account != null) {
                System.out.println("âœ“ " + type + " account created!");
                System.out.printf("  Account ID : %d%n", account.getId());
                System.out.printf("  Balance    : %.2f RWF%n", account.getBalance());
            } else {
                System.out.println(" Failed to create account.");
            }

        } catch (IgirePayException e) {
            System.out.println("" + e.getMessage());
        }
    }
    private void viewBalance() {
        System.out.println("\n View Account Balance ");

        if (!authService.isLoggedIn()) {
            System.out.println("You must be logged in.");
            return;
        }

        try {
            Customer me = authService.getLoggedInCustomer();
            List<Account> accounts = paymentService.getAccountsByCustomer(me.getId());

            if (accounts.isEmpty()) {
                System.out.println("You have no accounts.");
                return;
            }

            // Show all accounts and their balances
            System.out.printf("%-6s %-10s %15s  %s%n", "ID", "Type", "Balance", "Currency");
            System.out.println("â”€".repeat(42));
            for (Account acc : accounts) {
                System.out.printf("%-6d %-10s %15.2f  %s%n",
                        acc.getId(), acc.getAccountType(), acc.getBalance(), acc.getCurrency());
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    private void deleteAccount() {
        System.out.println("\n Delete Account ");

        if (!authService.isLoggedIn()) {
            System.out.println(" You must be logged in.");
            return;
        }

        try {
            Customer me = authService.getLoggedInCustomer();
            List<Account> accounts = paymentService.getAccountsByCustomer(me.getId());

            if (accounts.isEmpty()) {
                System.out.println("You have no accounts to delete.");
                return;
            }

            System.out.println("Your accounts:");
            for (Account acc : accounts) {
                System.out.printf("  ID: %-4d | Type: %-8s | Balance: %.2f RWF%n",
                        acc.getId(), acc.getAccountType(), acc.getBalance());
            }

            System.out.print("Enter Account ID to delete: ");
            int accountId = parseAccountId(scanner.nextLine().trim());

           boolean owned = accounts.stream().anyMatch(a -> a.getId() == accountId);
            if (!owned) {
                throw new IgirePayException(
                        IgirePayException.ErrorType.INVALID_ACCOUNT,
                        "Account ID " + accountId + " does not belong to you.");
            }

            System.out.print("Are you sure you want to delete account " + accountId + "? (yes/no): ");
            String confirm = scanner.nextLine().trim();
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("Deletion cancelled.");
                return;
            }

            boolean deleted = paymentService.deleteAccount(accountId);
            System.out.println(deleted ? "âœ“ Account deleted." : "âœ— Deletion failed.");

        } catch (IgirePayException e) {
            System.out.println(" " + e.getMessage());
        }
    }

    static double parseAmount(String input) throws IgirePayException {
        try {
            double amount = Double.parseDouble(input);
            if (amount <= 0) {
                throw new IgirePayException(
                        IgirePayException.ErrorType.INVALID_AMOUNT,
                        "Amount must be greater than zero. You entered: " + input);
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new IgirePayException(
                    IgirePayException.ErrorType.INVALID_AMOUNT,
                    "'" + input + "' is not a valid number. Please enter digits only (e.g. 5000).");
        }
    }

        static int parseAccountId(String input) throws IgirePayException {
        try {
            int id = Integer.parseInt(input);
            if (id <= 0) {
                throw new IgirePayException(
                        IgirePayException.ErrorType.INVALID_ACCOUNT,
                        "Account ID must be a positive number.");
            }
            return id;
        } catch (NumberFormatException e) {
            throw new IgirePayException(
                    IgirePayException.ErrorType.INVALID_ACCOUNT,
                    "'" + input + "' is not a valid account ID.");
        }
    }
}
