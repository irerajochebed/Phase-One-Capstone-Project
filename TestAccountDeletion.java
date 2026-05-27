import com.igirepay.igirepaypaymentgateway.LAB2.dao.AccountDAO;
import com.igirepay.igirepaypaymentgateway.LAB2.dao.CustomerDAO;
import com.igirepay.igirepaypaymentgateway.LAB2.db.SchemaSetup;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Account;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB2.service.PaymentService;

import java.util.List;

/**
 * Quick test to verify account deletion with money transfer works correctly
 */
public class TestAccountDeletion {
    
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  TESTING ACCOUNT DELETION WITH MONEY TRANSFER");
        System.out.println("═══════════════════════════════════════════════════\n");
        
        // Setup
        SchemaSetup.createTables();
        CustomerDAO customerDAO = new CustomerDAO();
        AccountDAO accountDAO = new AccountDAO();
        PaymentService paymentService = new PaymentService();
        
        // Create customer
        System.out.println("[1] Creating customer...");
        Customer customer = new Customer("Test User", "test@example.com", "+250788999888", "1234");
        int customerId = customerDAO.addCustomer(customer);
        System.out.println("✓ Customer created: ID = " + customerId);
        
        // Create wallet with 50,000 RWF
        System.out.println("\n[2] Creating wallet with 50,000 RWF...");
        Account wallet = paymentService.createAccount(customerId, "WALLET", 50000.0, "RWF");
        System.out.println("✓ Wallet created: ID = " + wallet.getId() + ", Balance = " + wallet.getBalance());
        
        // Create savings with 30,000 RWF
        System.out.println("\n[3] Creating savings with 30,000 RWF...");
        Account savings = paymentService.createAccount(customerId, "SAVINGS", 30000.0, "RWF");
        System.out.println("✓ Savings created: ID = " + savings.getId() + ", Balance = " + savings.getBalance());
        
        // Show current accounts
        System.out.println("\n[4] Current accounts BEFORE deletion:");
        List<Account> accounts = accountDAO.findByCustomerId(customerId);
        for (Account acc : accounts) {
            System.out.println("  - " + acc.getAccountType() + " (ID: " + acc.getId() + ") = " + acc.getBalance() + " RWF");
        }
        
        // Delete wallet (50,000 should go to savings)
        System.out.println("\n[5] Deleting WALLET account...");
        System.out.println("Expected: 50,000 RWF will transfer to savings (30,000 + 50,000 = 80,000)");
        boolean deleted = paymentService.deleteAccount(wallet.getId());
        System.out.println(deleted ? "✓ Wallet deleted successfully" : "✗ Failed to delete wallet");
        
        // Show accounts after deletion
        System.out.println("\n[6] Accounts AFTER deletion:");
        accounts = accountDAO.findByCustomerId(customerId);
        for (Account acc : accounts) {
            System.out.println("  - " + acc.getAccountType() + " (ID: " + acc.getId() + ") = " + acc.getBalance() + " RWF");
        }
        
        // Verify the balance
        System.out.println("\n[7] Verification:");
        Account updatedSavings = accountDAO.findById(savings.getId());
        if (updatedSavings != null) {
            double expectedBalance = 80000.0;
            double actualBalance = updatedSavings.getBalance();
            
            if (actualBalance == expectedBalance) {
                System.out.println("✓ SUCCESS! Savings balance is correct: " + actualBalance + " RWF");
            } else {
                System.out.println("✗ FAILED! Expected " + expectedBalance + " but got " + actualBalance);
            }
        }
        
        System.out.println("\n═══════════════════════════════════════════════════");
        System.out.println("  TEST COMPLETED");
        System.out.println("═══════════════════════════════════════════════════");
    }
}
