package com.igirepay.igirepaypaymentgateway.LAB3.ui;

import com.igirepay.igirepaypaymentgateway.LAB2.model.Account;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Transaction;
import com.igirepay.igirepaypaymentgateway.LAB2.service.PaymentService;
import com.igirepay.igirepaypaymentgateway.LAB3.exception.IgirePayException;
import com.igirepay.igirepaypaymentgateway.LAB3.service.AuthService;
import com.igirepay.igirepaypaymentgateway.LAB3.service.ReportService;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;


public class TransactionMenu {

    private final PaymentService paymentService;
    private final AuthService    authService;
    private final ReportService  reportService;
    private final Scanner        scanner;

    public TransactionMenu(PaymentService paymentService, AuthService authService,
                           ReportService reportService, Scanner scanner) {
        this.paymentService = paymentService;
        this.authService    = authService;
        this.reportService  = reportService;
        this.scanner        = scanner;
    }

    public void show() {
        boolean back = false;
        while (!back) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> deposit();
                case "2" -> withdraw();
                case "3" -> transfer();
                case "4" -> viewHistory();
                case "5" -> searchTransactions();
                case "6" -> exportCsv();
                case "7" -> dailySummary();
                case "8" -> customerStatement();
                case "0" -> back = true;
                default  -> System.out.println("Invalid option. Please enter 08.");
            }
        }
    }

    private void printMenu() {
        System.out.println("     TRANSACTION MANAGEMENT       ");
        System.out.println("  1. Deposit money                ");
        System.out.println("  2. Withdraw money               ");
        System.out.println("  3. Transfer money               ");
        System.out.println("  4. View transaction history     ");
        System.out.println("  5. Search & filter transactions ");
        System.out.println("  6. Export history to CSV        ");
        System.out.println("  7. Daily summary                ");
        System.out.println("  8. Customer statement           ");
        System.out.println("  0. Back                         ");
        System.out.print("Choose: ");
    }
    private void deposit() {
        System.out.println("\n Deposit Money ");
        if (!requireLogin()) return;

        try {
            int    accountId = selectMyAccount("deposit into");
            double amount    = promptAmount();
            String refId     = generateReferenceId();
            String desc      = promptDescription();

            System.out.println("Reference ID: " + refId + " (save this if you need to track the transaction)");

            boolean ok = paymentService.deposit(accountId, refId, amount, desc);
            if (!ok) {
                throw new IgirePayException(
                        IgirePayException.ErrorType.DUPLICATE_TRANSACTION,
                        "Deposit was rejected. It may be a duplicate.");
            }
            System.out.println(" Deposit of " + amount + " RWF completed.");

        } catch (IgirePayException e) {
            System.out.println(" " + e.getMessage());
        }
    }
    private void withdraw() {
        System.out.println("\n Withdraw Money ");
        if (!requireLogin()) return;

        try {
            int    accountId = selectMyAccount("withdraw from");
            double amount    = promptAmount();

            // PIN confirmation required before withdrawals
            authService.requirePinConfirmation(scanner);

            String refId = generateReferenceId();
            String desc  = promptDescription();

            boolean ok = paymentService.withdraw(accountId, refId, amount, desc);
            if (!ok) {
                throw new IgirePayException(
                        IgirePayException.ErrorType.INSUFFICIENT_BALANCE,
                        "Withdrawal failed. Check your balance or duplicate reference.");
            }
            System.out.println(" Withdrawal of " + amount + " RWF completed.");

        } catch (IgirePayException e) {
            System.out.println("" + e.getMessage());
        }
    }
    private void transfer() {
        System.out.println("\n Transfer Money ");
        if (!requireLogin()) return;

        try {
            int fromAccountId = selectMyAccount("transfer FROM");

            System.out.print("Enter destination Account ID: ");
            int toAccountId = AccountMenu.parseAccountId(scanner.nextLine().trim());

            if (fromAccountId == toAccountId) {
                throw new IgirePayException(
                        IgirePayException.ErrorType.INVALID_ACCOUNT,
                        "Cannot transfer to the same account.");
            }

            double amount = promptAmount();

            authService.requirePinConfirmation(scanner);

            String refId = generateReferenceId();
            String desc  = promptDescription();

            boolean ok = paymentService.transfer(fromAccountId, toAccountId, refId, amount, desc);
            if (!ok) {
                throw new IgirePayException(
                        IgirePayException.ErrorType.INSUFFICIENT_BALANCE,
                        "Transfer failed. Check balance or account IDs.");
            }
            System.out.println(" Transfer of " + amount + " RWF completed.");

        } catch (IgirePayException e) {
            System.out.println(" " + e.getMessage());
        }
    }
    private void viewHistory() {
        System.out.println("\n Transaction History ");
        if (!requireLogin()) return;

        try {
            int accountId = selectMyAccount("view history for");
            paymentService.printTransactionHistory(accountId);

        } catch (IgirePayException e) {
            System.out.println(" " + e.getMessage());
        }
    }
    private void searchTransactions() {
        System.out.println("\n Search & Filter Transactions ");
        if (!requireLogin()) return;

        try {
            int accountId = selectMyAccount("search transactions for");

            System.out.println("(Press Enter to skip any filter)");

            System.out.print("Filter by type [DEPOSIT / WITHDRAWAL / TRANSFER]: ");
            String typeInput = scanner.nextLine().trim().toUpperCase();
            String type = typeInput.isEmpty() ? null : typeInput;

            System.out.print("Minimum amount (e.g. 1000): ");
            String minInput = scanner.nextLine().trim();
            Double minAmount = minInput.isEmpty() ? null : Double.parseDouble(minInput);

            System.out.print("Maximum amount (e.g. 50000): ");
            String maxInput = scanner.nextLine().trim();
            Double maxAmount = maxInput.isEmpty() ? null : Double.parseDouble(maxInput);

            System.out.print("Keyword in description (e.g. airtime): ");
            String keyword = scanner.nextLine().trim();
            if (keyword.isEmpty()) keyword = null;
            List<Transaction> results = paymentService.searchTransactions(
                    accountId, type, minAmount, maxAmount, keyword);

            System.out.println("\n SEARCH RESULTS ");
            if (results.isEmpty()) {
                System.out.println("  No transactions match your filters.");
            } else {
                System.out.printf("  Found %d transaction(s):%n", results.size());
                System.out.printf("  %-6s %-14s %-12s %10s %-10s%n",
                        "ID", "Reference", "Type", "Amount", "Status");
                System.out.println("  " + "".repeat(58));
                for (Transaction t : results) {
                    System.out.printf("  %-6d %-14s %-12s %10.2f %-10s%n",
                            t.getId(), t.getReferenceId(), t.getTransactionType(),
                            t.getAmount(), t.getStatus());
                }
            }
           
        } catch (IgirePayException e) {
            System.out.println(" " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println(" Invalid amount entered. Please use numbers only.");
        }
    }
    private void exportCsv() {
        System.out.println("\n Export to CSV ");
        if (!requireLogin()) return;

        try {
            int accountId = selectMyAccount("export transactions for");
            reportService.exportToCsv(accountId);

        } catch (IgirePayException e) {
            System.out.println(" " + e.getMessage());
        }
    }
    private void dailySummary() {
        System.out.println("\n Daily Summary ");
        if (!requireLogin()) return;

        try {
            int accountId = selectMyAccount("view daily summary for");
            reportService.printDailySummary(accountId);

        } catch (IgirePayException e) {
            System.out.println(" " + e.getMessage());
        }
    }
    private void customerStatement() {
        if (!requireLogin()) return;
        Customer me = authService.getLoggedInCustomer();
        reportService.printCustomerStatement(me);
    }

    private boolean requireLogin() {
        if (!authService.isLoggedIn()) {
            System.out.println(" You must be logged in to perform transactions.");
            return false;
        }
        return true;
    }

    private int selectMyAccount(String action) throws IgirePayException {
        Customer me = authService.getLoggedInCustomer();
        List<Account> accounts = paymentService.getAccountsByCustomer(me.getId());

        if (accounts.isEmpty()) {
            throw new IgirePayException(
                    IgirePayException.ErrorType.INVALID_ACCOUNT,
                    "You have no accounts. Create one first in Account Management.");
        }

        System.out.println("Select account to " + action + ":");
        for (Account acc : accounts) {
            System.out.printf("  [%d] %-8s | Balance: %.2f %s%n",
                    acc.getId(), acc.getAccountType(), acc.getBalance(), acc.getCurrency());
        }
        System.out.print("Account ID: ");
        int accountId = AccountMenu.parseAccountId(scanner.nextLine().trim());

        boolean owned = accounts.stream().anyMatch(a -> a.getId() == accountId);
        if (!owned) {
            throw new IgirePayException(
                    IgirePayException.ErrorType.INVALID_ACCOUNT,
                    "Account ID " + accountId + " does not belong to you.");
        }
        return accountId;
    }

    private double promptAmount() throws IgirePayException {
        System.out.print("Amount (RWF): ");
        return AccountMenu.parseAmount(scanner.nextLine().trim());
    }

    private String promptDescription() {
        System.out.print("Description (optional, press Enter to skip): ");
        String desc = scanner.nextLine().trim();
        return desc.isEmpty() ? "N/A" : desc;
    }

    private String generateReferenceId() {
        return "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
