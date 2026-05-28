package com.igirepay.igirepaypaymentgateway.LAB3.service;

import com.igirepay.igirepaypaymentgateway.LAB2.dao.AccountDAO;
import com.igirepay.igirepaypaymentgateway.LAB2.dao.TransactionDAO;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Account;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Transaction;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


public class ReportService {

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final AccountDAO     accountDAO     = new AccountDAO();

    
    public void exportToCsv(int accountId) {
        List<Transaction> transactions = transactionDAO.findByAccountId(accountId);

        if (transactions.isEmpty()) {
            System.out.println("No transactions found for account " + accountId + ".");
            return;
        }

        String date     = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String filename = "statement_account_" + accountId + "_" + date + ".csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {

             writer.println("ID,Account ID,Reference ID,Type,Amount,Status,Description,Date");

            for (Transaction t : transactions) {
                writer.printf("%d,%d,%s,%s,%.2f,%s,\"%s\",%s%n",
                        t.getId(),
                        t.getAccountId(),
                        t.getReferenceId(),
                        t.getTransactionType(),
                        t.getAmount(),
                        t.getStatus(),
                        t.getDescription() != null ? t.getDescription() : "",
                        t.getCreatedAt() != null ? t.getCreatedAt().toString() : ""
                );
            }

            System.out.println(" CSV exported successfully " + filename);
            System.out.println("  You can open this file in Excel or any text editor.");

        } catch (IOException e) {
            System.err.println(" Failed to write CSV: " + e.getMessage());
        }
    }

    
    public boolean exportTransactionsToCsv(List<Transaction> transactions, String filepath) {
        if (transactions == null || transactions.isEmpty()) {
            System.out.println("No transactions to export.");
            return false;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(filepath))) {

            writer.println("ID,Account ID,Reference ID,Type,Amount,Status,Description,Date");

           for (Transaction t : transactions) {
                writer.printf("%d,%d,%s,%s,%.2f,%s,\"%s\",%s%n",
                        t.getId(),
                        t.getAccountId(),
                        t.getReferenceId(),
                        t.getTransactionType(),
                        t.getAmount(),
                        t.getStatus(),
                        t.getDescription() != null ? t.getDescription().replace("\"", "\"\"") : "",
                        t.getCreatedAt() != null ? t.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : ""
                );
            }

            System.out.println(" CSV exported successfully  " + filepath);
            return true;

        } catch (IOException e) {
            System.err.println("Failed to write CSV: " + e.getMessage());
            return false;
        }
    }

    
    public void printDailySummary(int accountId) {
        List<Transaction> all = transactionDAO.findByAccountId(accountId);

        LocalDate today = LocalDate.now();
        List<Transaction> todayTxns = all.stream()
                .filter(t -> t.getCreatedAt() != null &&
                             t.getCreatedAt().toLocalDate().equals(today))
                .collect(Collectors.toList());

        System.out.println("Account ID: " + accountId);
        System.out.println("Total transactions today: " + todayTxns.size());

        if (todayTxns.isEmpty()) {
            System.out.println("No transactions today.");
            return;
        }

        double totalDeposits    = 0, totalWithdrawals = 0, totalTransfers = 0;
        int    countDeposits    = 0, countWithdrawals = 0, countTransfers = 0;

        for (Transaction t : todayTxns) {
            switch (t.getTransactionType()) {
                case Transaction.TYPE_DEPOSIT    -> { totalDeposits    += t.getAmount(); countDeposits++; }
                case Transaction.TYPE_WITHDRAWAL -> { totalWithdrawals += t.getAmount(); countWithdrawals++; }
                case Transaction.TYPE_TRANSFER   -> { totalTransfers   += t.getAmount(); countTransfers++; }
            }
        }

        System.out.printf("  Deposits    : %d transaction(s) | Total: %.2f RWF%n",
                countDeposits, totalDeposits);
        System.out.printf("  Withdrawals : %d transaction(s) | Total: %.2f RWF%n",
                countWithdrawals, totalWithdrawals);
        System.out.printf("  Transfers   : %d transaction(s) | Total: %.2f RWF%n",
                countTransfers, totalTransfers);
        }

   
    public void printCustomerStatement(Customer customer) {
        System.out.println("       CUSTOMER STATEMENT                   ");
        System.out.println("Customer : " + customer.getFullName());
        System.out.println("Email    : " + customer.getEmail());
        System.out.println("Phone    : " + customer.getPhoneNumber());
        
        List<Account> accounts = accountDAO.findByCustomerId(customer.getId());

        if (accounts.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }

        for (Account account : accounts) {
            System.out.printf("%nAccount #%d | Type: %-8s | Balance: %.2f %s%n",
                    account.getId(),
                    account.getAccountType(),
                    account.getBalance(),
                    account.getCurrency());
            System.out.println("  Transactions:");

            List<Transaction> txns = transactionDAO.findByAccountId(account.getId());
            if (txns.isEmpty()) {
                System.out.println("    (no transactions yet)");
            } else {
                System.out.printf("  %-6s %-14s %-12s %10s %-10s  %s%n",
                        "ID", "Reference", "Type", "Amount", "Status", "Date");
                System.out.println("  " + "".repeat(72));
                for (Transaction t : txns) {
                    System.out.printf("  %-6d %-14s %-12s %10.2f %-10s  %s%n",
                            t.getId(),
                            t.getReferenceId(),
                            t.getTransactionType(),
                            t.getAmount(),
                            t.getStatus(),
                            t.getCreatedAt() != null
                                    ? t.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                                    : "N/A"
                    );
                }
            }
        }
        }
}

