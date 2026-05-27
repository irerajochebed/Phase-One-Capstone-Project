package com.igirepay.igirepaypaymentgateway.LAB3.ui;

import java.util.Scanner;

import com.igirepay.igirepaypaymentgateway.LAB2.db.SchemaSetup;
import com.igirepay.igirepaypaymentgateway.LAB2.service.PaymentService;
import com.igirepay.igirepaypaymentgateway.LAB3.exception.IgirePayException;
import com.igirepay.igirepaypaymentgateway.LAB3.service.AuthService;
import com.igirepay.igirepaypaymentgateway.LAB3.service.ReportService;
import com.igirepay.igirepaypaymentgateway.LAB3.util.AdminSetupUtil;


public class ConsoleApp {

    private final PaymentService  paymentService  = new PaymentService();
    private final AuthService     authService     = new AuthService();
    private final ReportService   reportService   = new ReportService();
    private final Scanner         scanner         = new Scanner(System.in);

    private final CustomerMenu    customerMenu;
    private final AccountMenu     accountMenu;
    private final TransactionMenu transactionMenu;
    private final AdminMenu       adminMenu;

    public ConsoleApp() {
        this.customerMenu    = new CustomerMenu(paymentService, authService, scanner);
        this.accountMenu     = new AccountMenu(paymentService, authService, scanner);
        this.transactionMenu = new TransactionMenu(paymentService, authService, reportService, scanner);
        this.adminMenu       = new AdminMenu(paymentService, authService, scanner);
    }

    public void start() {
        printBanner();
        SchemaSetup.createTables();
        if (!hasAdminUser()) {
            System.out.println("\n No admin account found. Let's create one!\n");
            createFirstAdminInteractively();
        }

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> handleLogin();
                case "2" -> handleLogout();
                case "3" -> customerMenu.show();
                case "4" -> accountMenu.show();
                case "5" -> transactionMenu.show();
                case "6" -> adminMenu.show();   
                case "0" -> {
                    System.out.println("\nThank you for using IgirePay. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option. Please enter 0â€“6.");
            }
        }
        scanner.close();
    }

    private void printMainMenu() {
       
        if (authService.isLoggedIn()) {
           
            String badge = authService.isAdmin() ? " [ADMIN]" : " [USER]";
            System.out.printf("  %-38sâ•‘%n",
                    authService.getLoggedInCustomer().getFullName() + badge);
        } else {
            System.out.println("  Status: Not logged in              ");
        }

        System.out.println("         MAIN MENU                    ");System.out.println("  1. Login                            â•‘");
        System.out.println("  2. Logout                           ");
        System.out.println("  3. Customer Management              ");
        System.out.println("  4. Account Management               ");
        System.out.println("  5. Transaction Management           ");

        
        if (authService.isAdmin()) {
            System.out.println("  6.  Admin Panel                    ");
        } else {
            System.out.println(" 6. Admin Panel (admin only)         ");
        }

        System.out.println(" 0. Exit                             ");
        System.out.print("Choose: ");
    }

    private void handleLogin() {
        if (authService.isLoggedIn()) {
            System.out.println("Already logged in as "
                    + authService.getLoggedInCustomer().getFullName()
                    + ". Logout first.");
            return;
        }

        try {
            authService.login(scanner);
        } catch (IgirePayException e) {
            System.out.println(" " + e.getMessage());
        }
    }

    private void handleLogout() {
        if (!authService.isLoggedIn()) {
            System.out.println("You are not logged in.");
            return;
        }
        authService.logout();
    }

    private void printBanner() {
        System.out.println("                                            ");
        System.out.println("        IgirePay Payment Gateway              ");
        System.out.println("        Console Application â€” LAB 3           ");
        System.out.println("                                              ");
        System.out.println("  Powered by Java + JDBC + PostgreSQL         ");}

    private boolean hasAdminUser() {
        return AdminSetupUtil.adminExists();
    }

    
    private void createFirstAdminInteractively() {
        System.out.println("     FIRST ADMIN ACCOUNT SETUP                ");
        System.out.println("   (One-time setup â€” happens only once)     ");
        
        try {
            System.out.print("Enter admin full name: ");
            String fullName = scanner.nextLine().trim();
            System.out.print("Enter admin email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Enter admin phone (10 digits, starts with 078 or 079): ");
            String phone = scanner.nextLine().trim();
            System.out.print("Create admin PIN (4 digits, e.g., 0000): ");
            String pin = scanner.nextLine().trim();
            var newAdmin = AdminSetupUtil.createAdmin(fullName, email, phone, pin);

            System.out.println("\nâœ“ SUCCESS! Admin account created:\n");
            System.out.println("   Admin Details ");
            System.out.printf("   Name  : %-25sâ”‚%n", fullName);
            System.out.printf("   Email : %-25sâ”‚%n", email);
            System.out.printf("   Phone : %-25sâ”‚%n", phone);
            System.out.printf("   PIN   : %-25sâ”‚%n", pin);
            System.out.println("  Role  : ADMIN                   â”‚");
            System.out.println("\n  You can now login to unlock accounts!");
            System.out.println("  Press Enter to continue...\n");
            scanner.nextLine();

        } catch (IgirePayException e) {
            System.out.println("\n ERROR: " + e.getMessage());
            System.out.println("Please try again.\n");
           
            createFirstAdminInteractively();
        }
    }
}
