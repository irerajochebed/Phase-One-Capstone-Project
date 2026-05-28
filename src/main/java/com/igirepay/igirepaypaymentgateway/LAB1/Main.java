package com.igirepay.igirepaypaymentgateway.LAB1;

import com.igirepay.igirepaypaymentgateway.LAB1.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB1.model.SavingsAccount;
import com.igirepay.igirepaypaymentgateway.LAB1.model.Transaction;
import com.igirepay.igirepaypaymentgateway.LAB1.model.WalletAccount;
import com.igirepay.igirepaypaymentgateway.LAB1.service.PaymentService;


public class Main {

    public static void main(String[] args) {

        System.out.println("--------------------------------------------");
        System.out.println("   IgirePay Payment Gateway  LAB 1       ");
        System.out.println("--------------------------------------------\n");

        Customer alice = new Customer("CUST001", "Alice Uwimana",
                "alice@igirepay.com", "0781234567", "1234");

        Customer bob = new Customer("CUST002", "Bob Nkurunziza",
                "bob@igirepay.com", "0789876543", "5678");

        WalletAccount aliceWallet = new WalletAccount(
                "ACC001", "078-001-001", 50_000.0, "RWF", "0781234567");

        SavingsAccount aliceSavings = new SavingsAccount(
                "ACC002", "SAV-001-001", 200_000.0, "RWF", 30_000.0, 1.5);

        WalletAccount bobWallet = new WalletAccount(
                "ACC003", "078-002-001", 80_000.0, "RWF", "0789876543");

        alice.addAccount(aliceWallet);
        alice.addAccount(aliceSavings);
        bob.addAccount(bobWallet);

        System.out.println("\n--- Customer Details ---");
        System.out.println(alice);
        System.out.println(bob);

        System.out.println("\n--- Account Details ---");
        System.out.println(aliceWallet);
        System.out.println(aliceSavings);
        System.out.println(bobWallet);

        System.out.println("\n--- PIN Validation ---");
        System.out.println("Alice PIN '1234' correct? " + alice.validatePin("1234"));
        System.out.println("Alice PIN '9999' correct? " + alice.validatePin("9999"));

        System.out.println("\n\n EXERCISE 1.2: Polymorphism ");

        System.out.println("\n[Direct calls on WalletAccount]");
        aliceWallet.deposit(10_000.0);
        aliceWallet.withdraw(5_000.0);

        System.out.println("\n[Direct calls on SavingsAccount]");
        aliceSavings.deposit(10_000.0);
        aliceSavings.withdraw(20_000.0);
        aliceSavings.withdraw(35_000.0);

        System.out.println("\n\n EXERCISE 1.3: Collections & Duplicate Detection ");

        PaymentService service = new PaymentService();

        service.registerCustomer(alice);
        service.registerCustomer(bob);

        Transaction t1 = new Transaction("TXN001", "REF-AAA-001",
                15_000.0, Transaction.TYPE_DEPOSIT, "Top-up from bank");

        Transaction t2 = new Transaction("TXN002", "REF-AAA-002",
                8_000.0, Transaction.TYPE_WITHDRAWAL, "Airtime purchase");

        Transaction t3 = new Transaction("TXN003", "REF-BBB-001",
                25_000.0, Transaction.TYPE_DEPOSIT, "Salary payment");

        service.processTransaction("CUST001", "ACC001", t1);
        service.processTransaction("CUST001", "ACC001", t2);
        service.processTransaction("CUST002", "ACC003", t3);

        System.out.println("\n[Simulating network retry  same referenceId sent again]");

        Transaction t1Retry = new Transaction("TXN004", "REF-AAA-001",
                15_000.0, Transaction.TYPE_DEPOSIT, "Top-up from bank (RETRY)");

        service.processTransaction("CUST001", "ACC001", t1Retry);

        Transaction t2Retry = new Transaction("TXN005", "REF-AAA-002",
                8_000.0, Transaction.TYPE_WITHDRAWAL, "Airtime (RETRY)");

        service.processTransaction("CUST001", "ACC001", t2Retry);

        Transaction t4 = new Transaction("TXN006", "REF-SAV-001",
                10_000.0, Transaction.TYPE_WITHDRAWAL, "Savings withdrawal");

        service.processTransaction("CUST001", "ACC002", t4);

        service.printTransactionHistory();
        service.printFailedTransactions();
        service.printProcessedReferenceIds();

        System.out.println("\n FINAL BALANCES ");
        System.out.println("Alice Wallet  : " + aliceWallet.getBalance() + " RWF");
        System.out.println("Alice Savings : " + aliceSavings.getBalance() + " RWF");
        System.out.println("Bob Wallet    : " + bobWallet.getBalance() + " RWF");

        System.out.println("\n LAB 1 complete.");
    }
}
