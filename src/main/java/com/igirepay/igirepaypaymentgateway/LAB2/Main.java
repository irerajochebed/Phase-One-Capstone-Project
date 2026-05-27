package com.igirepay.igirepaypaymentgateway.LAB2;

import com.igirepay.igirepaypaymentgateway.LAB2.db.SchemaSetup;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Account;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB2.service.PaymentService;

import java.util.List;


public class  Main {

    public static void main(String[] args) {

         System.out.println("  IgirePay Payment Gateway  LAB 2       ");
        SchemaSetup.createTables();

        PaymentService service = new PaymentService();
        Customer alice = null;
        try {
            alice = service.registerCustomer(
                    "Alice Uwimana", "alice@igirepay.com", "0781234567", "1234");
        } catch (Exception e) {
            alice = service.findCustomerByEmail("alice@igirepay.com");
            System.out.println("[Main] Alice already exists, loaded from DB: " + alice);
        }

        Customer bob = null;
        try {
            bob = service.registerCustomer(
                    "Bob Nkurunziza", "bob@igirepay.com", "0789876543", "5678");
        } catch (Exception e) {
             bob = service.findCustomerByEmail("bob@igirepay.com");
            System.out.println("[Main] Bob already exists, loaded from DB: " + bob);
        }

        if (alice == null || bob == null) {
            System.out.println("ERROR: Could not load customers. Check DB connection.");
            return;
        }

        System.out.println("\nCreated: " + alice);
        System.out.println("Created: " + bob);
        List<Account> aliceAccounts = service.getAccountsByCustomer(alice.getId());
        List<Account> bobAccounts   = service.getAccountsByCustomer(bob.getId());

        Account aliceWallet, aliceSavings, bobWallet;

        if (aliceAccounts.isEmpty()) {
            
            aliceWallet  = service.createAccount(alice.getId(), "WALLET",  50_000.0, "RWF");
            aliceSavings = service.createAccount(alice.getId(), "SAVINGS", 200_000.0, "RWF");
        } else {
            
            aliceWallet  = aliceAccounts.get(0);
            aliceSavings = aliceAccounts.size() > 1 ? aliceAccounts.get(1) : aliceAccounts.get(0);
            System.out.println("[Main] Alice's accounts loaded from DB.");
        }

        if (bobAccounts.isEmpty()) {
            bobWallet = service.createAccount(bob.getId(), "WALLET", 80_000.0, "RWF");
        } else {
            bobWallet = bobAccounts.get(0);
            System.out.println("[Main] Bob's account loaded from DB.");
        }

        System.out.println("\nCreated: " + aliceWallet);
        System.out.println("Created: " + aliceSavings);
        System.out.println("Created: " + bobWallet);
        System.out.println("\nUpdating Alice's phone number ");
        alice.setPhoneNumber("0781111111");
        service.updateCustomer(alice);
        System.out.println("\n All customers in DB ");
        List<Customer> allCustomers = service.getAllCustomers();
        allCustomers.forEach(c -> System.out.println("  " + c));
        
        service.deposit(aliceWallet.getId(), "REF-DEP-001", 10_000.0, "Top-up from bank");
        service.deposit(bobWallet.getId(),   "REF-DEP-002", 20_000.0, "Salary payment");
        service.withdraw(aliceWallet.getId(), "REF-WIT-001", 5_000.0, "Airtime purchase");
        
        service.transfer(aliceWallet.getId(), bobWallet.getId(),
                "REF-TRF-001", 8_000.0, "Payment for services");
        service.printTransactionHistory(aliceWallet.getId());
        service.printTransactionHistory(bobWallet.getId());
        
        System.out.println("[Simulating network retry â€” same referenceId sent again]");

        service.deposit(aliceWallet.getId(), "REF-DEP-001", 10_000.0, "Top-up (RETRY)");

        service.withdraw(aliceWallet.getId(), "REF-WIT-001", 5_000.0, "Airtime (RETRY)");

        service.transfer(aliceWallet.getId(), bobWallet.getId(),
                "REF-TRF-001", 8_000.0, "Payment (RETRY)");
        System.out.println("\n All transactions in the system ");
        service.printAllTransactions();

        System.out.println("\n LAB 2 complete. Check pgAdmin to see the data in your tables.");
    }
}
