package com.igirepay.igirepaypaymentgateway.ui.controller;

import com.igirepay.igirepaypaymentgateway.LAB2.model.Account;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Dispute;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Transaction;
import com.igirepay.igirepaypaymentgateway.LAB3.exception.IgirePayException;
import com.igirepay.igirepaypaymentgateway.ui.AppState;
import com.igirepay.igirepaypaymentgateway.ui.IgirePayApp;
import com.igirepay.igirepaypaymentgateway.ui.SceneHelper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * AdminPanelController â€” Comprehensive admin dashboard for managing users, accounts, and disputes
 */
public class AdminPanelController implements Initializable {

    @FXML private Label adminNameLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label lockedAccountsLabel;
    @FXML private Label adminCountLabel;
    @FXML private Label pendingDisputesLabel;
    @FXML private Label inactiveAccountsLabel;
    @FXML private TextField searchField;
    @FXML private VBox customerListBox;
    @FXML private TabPane mainTabPane;

    private final AppState state = AppState.getInstance();
    private List<Customer> allCustomers;
    private String currentFilter = "ALL";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Customer admin = state.getCurrentCustomer();
        if (admin == null || !admin.isAdmin()) {
            showErrorDialog("Access Denied", 
                "Admin privileges required", 
                "You must be an administrator to access this panel.");
            go("dashboard");
            return;
        }

        adminNameLabel.setText(admin.getFullName());
        loadCustomers();
        loadStatistics();
    }

    private void loadCustomers() {
        allCustomers = state.getPaymentService().getAllCustomers();
        displayCustomers(allCustomers);
    }

    private void loadStatistics() {
        int total = allCustomers.size();
        long locked = allCustomers.stream().filter(Customer::isLocked).count();
        long admins = allCustomers.stream().filter(Customer::isAdmin).count();
        
        totalUsersLabel.setText(String.valueOf(total));
        lockedAccountsLabel.setText(String.valueOf(locked));
        adminCountLabel.setText(String.valueOf(admins));
        
        try {
            int pendingDisputes = state.getAdminService().getPendingDisputeCount();
            int inactiveAccounts = state.getAdminService().getInactiveAccountCount();
            
            if (pendingDisputesLabel != null) {
                pendingDisputesLabel.setText(String.valueOf(pendingDisputes));
            }
            if (inactiveAccountsLabel != null) {
                inactiveAccountsLabel.setText(String.valueOf(inactiveAccounts));
            }
        } catch (IgirePayException e) {
            System.err.println("[AdminPanel] Error loading statistics: " + e.getMessage());
        }
    }

    private void displayCustomers(List<Customer> customers) {
        customerListBox.getChildren().clear();

        if (customers.isEmpty()) {
            Label empty = new Label("No customers found");
            empty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px;");
            customerListBox.getChildren().add(empty);
            return;
        }

        for (Customer customer : customers) {
            customerListBox.getChildren().add(createCustomerCard(customer));
        }
    }

    private VBox createCustomerCard(Customer customer) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-width: 1; " +
                     "-fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 16; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        // Header row: Name + Role badge
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(customer.getFullName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        Label roleBadge = new Label(customer.getRole());
        roleBadge.setStyle(customer.isAdmin() 
            ? "-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;"
            : "-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        if (customer.isLocked()) {
            Label lockedBadge = new Label("ðŸ”’ LOCKED");
            lockedBadge.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");
            headerRow.getChildren().addAll(nameLabel, roleBadge, lockedBadge);
        } else {
            headerRow.getChildren().addAll(nameLabel, roleBadge);
        }

        // Info row
        VBox infoBox = new VBox(6);
        Label emailLabel = new Label("ðŸ“§ " + customer.getEmail());
        emailLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");
        
        Label phoneLabel = new Label("ðŸ“± " + customer.getPhoneNumber());
        phoneLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px;");

        Label idLabel = new Label("ID: " + customer.getId());
        idLabel.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");

        infoBox.getChildren().addAll(emailLabel, phoneLabel, idLabel);

        // Account balances and status
        List<Account> accounts = state.getPaymentService().getAccountsByCustomer(customer.getId());
        if (!accounts.isEmpty()) {
            double totalBalance = accounts.stream().mapToDouble(Account::getBalance).sum();
            long activeAccounts = accounts.stream().filter(Account::isActive).count();
            long inactiveAccounts = accounts.size() - activeAccounts;
            
            Label balanceLabel = new Label("ðŸ’° Total Balance: " + String.format("%,.2f RWF", totalBalance));
            balanceLabel.setStyle("-fx-text-fill: #059669; -fx-font-size: 14px; -fx-font-weight: bold;");
            infoBox.getChildren().add(balanceLabel);
            
            Label accountsLabel = new Label("ðŸ“Š Accounts: " + activeAccounts + " active, " + inactiveAccounts + " inactive");
            accountsLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");
            infoBox.getChildren().add(accountsLabel);
        }

        // Failed attempts warning
        if (customer.getFailedPinAttempts() > 0) {
            Label attemptsLabel = new Label("âš ï¸ Failed PIN attempts: " + customer.getFailedPinAttempts() + "/3");
            attemptsLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 12px; -fx-font-weight: bold;");
            infoBox.getChildren().add(attemptsLabel);
        }

        // Action buttons
        HBox actionBox = new HBox(8);
        actionBox.setAlignment(Pos.CENTER_LEFT);

        if (customer.isLocked()) {
            Button unlockBtn = new Button("ðŸ”“ Unlock & Reset PIN");
            unlockBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
            unlockBtn.setOnAction(e -> onUnlockCustomer(customer));
            actionBox.getChildren().add(unlockBtn);
        }

        Button viewAccountsBtn = new Button("ðŸ’³ View Accounts");
        viewAccountsBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
        viewAccountsBtn.setOnAction(e -> onViewCustomerAccounts(customer));
        actionBox.getChildren().add(viewAccountsBtn);

        Button sendMessageBtn = new Button("âœ‰ï¸ Send Message");
        sendMessageBtn.setStyle("-fx-background-color: #8B5CF6; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
        sendMessageBtn.setOnAction(e -> onSendMessageToCustomer(customer));
        actionBox.getChildren().add(sendMessageBtn);

        if (!customer.isAdmin()) {
            Button promoteBtn = new Button("ðŸ‘‘ Make Admin");
            promoteBtn.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
            promoteBtn.setOnAction(e -> onPromoteToAdmin(customer));
            actionBox.getChildren().add(promoteBtn);
        } else if (customer.getId() != state.getCurrentCustomer().getId()) {
            Button demoteBtn = new Button("ðŸ‘¤ Remove Admin");
            demoteBtn.setStyle("-fx-background-color: #6B7280; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
            demoteBtn.setOnAction(e -> onDemoteToUser(customer));
            actionBox.getChildren().add(demoteBtn);
        }

        if (customer.getId() != state.getCurrentCustomer().getId()) {
            Button deleteBtn = new Button("ðŸ—‘ Delete");
            deleteBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
            deleteBtn.setOnAction(e -> onDeleteCustomer(customer));
            actionBox.getChildren().add(deleteBtn);
        }

        card.getChildren().addAll(headerRow, new Separator(), infoBox, actionBox);
        return card;
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // ADMIN ACTIONS
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    private void onUnlockCustomer(Customer customer) {
        boolean confirmed = showConfirmationDialog(
            "Unlock Account & Reset PIN",
            "Unlock " + customer.getFullName() + "'s account?",
            "This will:\n" +
            "â€¢ Reset their failed PIN attempts to 0\n" +
            "â€¢ Unlock their account for login\n" +
            "â€¢ Send them a notification to change their PIN\n\n" +
            "Customer: " + customer.getFullName() + "\n" +
            "Email: " + customer.getEmail() + "\n" +
            "Phone: " + customer.getPhoneNumber() + "\n\n" +
            "The customer will receive a notification with instructions."
        );

        if (!confirmed) return;

        try {
            System.out.println("[AdminPanel] Attempting to unlock customer ID: " + customer.getId());
            
            boolean success = state.getAdminService().unlockCustomerAccount(customer.getId());
            
            if (success) {
                showSuccessDialog("Account Unlocked", 
                    "Success!", 
                    customer.getFullName() + "'s account has been unlocked.\n\n" +
                    "âœ… Account unlocked\n" +
                    "âœ… Failed attempts reset to 0\n" +
                    "âœ… Notification sent to customer\n\n" +
                    "The customer can now login and will be prompted to change their PIN.");
                loadCustomers();
                loadStatistics();
            } else {
                showErrorDialog("Unlock Failed", 
                    "Could not unlock account", 
                    "The database operation failed. Please try again.");
            }
        } catch (IgirePayException e) {
            System.err.println("[AdminPanel] IgirePayException: " + e.getMessage());
            showErrorDialog("Unlock Failed", "Could not unlock account", e.getMessage());
        } catch (Exception e) {
            System.err.println("[AdminPanel] Unexpected exception: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Unlock Failed", "Unexpected error", e.getMessage());
        }
    }

    private void onPromoteToAdmin(Customer customer) {
        boolean confirmed = showConfirmationDialog(
            "Promote to Admin",
            "Make " + customer.getFullName() + " an administrator?",
            "This will give them full admin privileges including:\n" +
            "â€¢ Access to admin panel\n" +
            "â€¢ Ability to unlock accounts\n" +
            "â€¢ Ability to manage other users\n" +
            "â€¢ View all customer data\n\n" +
            "Customer: " + customer.getFullName() + "\n" +
            "Email: " + customer.getEmail()
        );

        if (!confirmed) return;

        try {
            state.getAuthService().setRole(customer.getId(), Customer.ROLE_ADMIN);
            showSuccessDialog("Promoted to Admin", 
                "Success!", 
                customer.getFullName() + " is now an administrator.");
            loadCustomers();
        } catch (IgirePayException e) {
            showErrorDialog("Promotion Failed", "Could not promote user", e.getMessage());
        }
    }

    private void onDemoteToUser(Customer customer) {
        boolean confirmed = showConfirmationDialog(
            "Remove Admin Privileges",
            "Remove admin privileges from " + customer.getFullName() + "?",
            "This will revoke their admin access.\n" +
            "They will become a regular user.\n\n" +
            "Customer: " + customer.getFullName() + "\n" +
            "Email: " + customer.getEmail()
        );

        if (!confirmed) return;

        try {
            state.getAuthService().setRole(customer.getId(), Customer.ROLE_USER);
            showSuccessDialog("Admin Removed", 
                "Success!", 
                customer.getFullName() + " is now a regular user.");
            loadCustomers();
        } catch (IgirePayException e) {
            showErrorDialog("Demotion Failed", "Could not change role", e.getMessage());
        }
    }

    private void onViewCustomerDetails(Customer customer) {
        List<Account> accounts = state.getPaymentService().getAccountsByCustomer(customer.getId());
        
        StringBuilder details = new StringBuilder();
        details.append("CUSTOMER INFORMATION\n");
        details.append("â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”\n\n");
        details.append("Name: ").append(customer.getFullName()).append("\n");
        details.append("Email: ").append(customer.getEmail()).append("\n");
        details.append("Phone: ").append(customer.getPhoneNumber()).append("\n");
        details.append("Role: ").append(customer.getRole()).append("\n");
        details.append("Status: ").append(customer.isLocked() ? "ðŸ”’ LOCKED" : "âœ… Active").append("\n");
        details.append("Failed Attempts: ").append(customer.getFailedPinAttempts()).append("/3\n");
        details.append("Customer ID: ").append(customer.getId()).append("\n\n");
        
        details.append("ACCOUNTS\n");
        details.append("â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”\n\n");
        
        if (accounts.isEmpty()) {
            details.append("No accounts yet\n");
        } else {
            double total = 0;
            for (Account acc : accounts) {
                String status = acc.isActive() ? "âœ… Active" : "âŒ Inactive";
                details.append(acc.getAccountType()).append(" Account (").append(status).append(")\n");
                details.append("  ID: ").append(acc.getId()).append("\n");
                details.append("  Balance: ").append(String.format("%,.2f", acc.getBalance())).append(" ").append(acc.getCurrency()).append("\n");
                if (acc.getLastTransactionDate() != null) {
                    details.append("  Last Transaction: ").append(acc.getLastTransactionDate().format(DATE_FORMAT)).append("\n");
                }
                details.append("  Created: ").append(acc.getCreatedAt().format(DATE_FORMAT)).append("\n\n");
                total += acc.getBalance();
            }
            details.append("Total Balance: ").append(String.format("%,.2f RWF", total)).append("\n");
        }

        showInfoDialog("Customer Details", customer.getFullName(), details.toString());
    }

    /**
     * View customer accounts with management options
     */
    private void onViewCustomerAccounts(Customer customer) {
        List<Account> accounts = state.getPaymentService().getAccountsByCustomer(customer.getId());
        
        if (accounts.isEmpty()) {
            showInfoDialog("No Accounts", customer.getFullName(), 
                "This customer has no accounts yet.");
            return;
        }
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Manage Accounts - " + customer.getFullName());
        dialog.setHeaderText("Customer Accounts");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        for (Account account : accounts) {
            VBox accountCard = new VBox(8);
            accountCard.setStyle("-fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 8; " +
                               "-fx-background-radius: 8; -fx-padding: 12; -fx-background-color: white;");

            String statusIcon = account.isActive() ? "âœ…" : "âŒ";
            String statusText = account.isActive() ? "Active" : "Inactive";
            
            Label typeLabel = new Label(statusIcon + " " + account.getAccountType() + " Account - " + statusText);
            typeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

            Label balanceLabel = new Label("Balance: " + String.format("%,.2f %s", account.getBalance(), account.getCurrency()));
            balanceLabel.setStyle("-fx-font-size: 13px;");

            Label idLabel = new Label("Account ID: " + account.getId());
            idLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");

            if (account.getLastTransactionDate() != null) {
                Label lastTxLabel = new Label("Last Transaction: " + account.getLastTransactionDate().format(DATE_FORMAT));
                lastTxLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
                accountCard.getChildren().add(lastTxLabel);
            }

            HBox buttonBox = new HBox(8);
            
            if (!account.isActive() && account.getBalance() == 0) {
                Button deleteBtn = new Button("ðŸ—‘ Delete Inactive Account");
                deleteBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white;");
                deleteBtn.setOnAction(e -> {
                    if (onDeleteInactiveAccount(account)) {
                        dialog.close();
                        loadCustomers();
                        loadStatistics();
                    }
                });
                buttonBox.getChildren().add(deleteBtn);
            } else if (!account.isActive()) {
                Label warningLabel = new Label("âš ï¸ Cannot delete: Account has balance");
                warningLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 11px;");
                buttonBox.getChildren().add(warningLabel);
            }

            accountCard.getChildren().addAll(typeLabel, balanceLabel, idLabel, buttonBox);
            content.getChildren().add(accountCard);
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    /**
     * Delete an inactive account
     */
    private boolean onDeleteInactiveAccount(Account account) {
        boolean confirmed = showConfirmationDialog(
            "Delete Inactive Account",
            "Delete this account permanently?",
            "Account Type: " + account.getAccountType() + "\n" +
            "Account ID: " + account.getId() + "\n" +
            "Balance: " + String.format("%,.2f %s", account.getBalance(), account.getCurrency()) + "\n\n" +
            "This action cannot be undone!"
        );

        if (!confirmed) return false;

        try {
            boolean success = state.getAdminService().deleteInactiveAccount(account.getId());
            if (success) {
                showSuccessDialog("Account Deleted", 
                    "Success!", 
                    "The inactive account has been permanently deleted.");
                return true;
            } else {
                showErrorDialog("Delete Failed", 
                    "Could not delete account", 
                    "The operation failed. Please try again.");
                return false;
            }
        } catch (IgirePayException e) {
            showErrorDialog("Delete Failed", "Cannot delete account", e.getMessage());
            return false;
        }
    }

    /**
     * Send a message to a customer
     */
    private void onSendMessageToCustomer(Customer customer) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Send Message to " + customer.getFullName());
        dialog.setHeaderText("Compose Admin Message");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Message Title:");
        TextField titleField = new TextField();
        titleField.setPromptText("Enter message title...");

        Label messageLabel = new Label("Message:");
        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Enter your message...");
        messageArea.setPrefRowCount(6);

        CheckBox actionRequiredCheck = new CheckBox("Requires Action");
        
        ComboBox<String> actionTypeCombo = new ComboBox<>();
        actionTypeCombo.getItems().addAll("RESET_PASSWORD", "RETURN_FUNDS", "CONFIRM_RECEIPT", "CONTACT_SUPPORT");
        actionTypeCombo.setPromptText("Select action type...");
        actionTypeCombo.setDisable(true);

        actionRequiredCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            actionTypeCombo.setDisable(!newVal);
        });

        content.getChildren().addAll(
            titleLabel, titleField,
            messageLabel, messageArea,
            actionRequiredCheck, actionTypeCombo
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String title = titleField.getText().trim();
            String message = messageArea.getText().trim();
            boolean actionRequired = actionRequiredCheck.isSelected();
            String actionType = actionRequired ? actionTypeCombo.getValue() : null;

            if (title.isEmpty() || message.isEmpty()) {
                showErrorDialog("Invalid Input", "Missing Information", 
                    "Please provide both title and message.");
                return;
            }

            if (actionRequired && actionType == null) {
                showErrorDialog("Invalid Input", "Missing Action Type", 
                    "Please select an action type when 'Requires Action' is checked.");
                return;
            }

            try {
                state.getAdminService().sendMessageToCustomer(
                    customer.getId(), title, message, actionRequired, actionType
                );
                showSuccessDialog("Message Sent", 
                    "Success!", 
                    "Your message has been sent to " + customer.getFullName() + ".\n" +
                    "They will see it in their notifications.");
            } catch (IgirePayException e) {
                showErrorDialog("Send Failed", "Could not send message", e.getMessage());
            }
        }
    }

    private void onDeleteCustomer(Customer customer) {
        boolean confirmed = showConfirmationDialog(
            "âš ï¸ Delete Customer",
            "Permanently delete " + customer.getFullName() + "?",
            "WARNING: This action cannot be undone!\n\n" +
            "This will delete:\n" +
            "â€¢ Customer account\n" +
            "â€¢ All their accounts (wallet, savings)\n" +
            "â€¢ All transaction history\n\n" +
            "Customer: " + customer.getFullName() + "\n" +
            "Email: " + customer.getEmail() + "\n" +
            "Phone: " + customer.getPhoneNumber() + "\n\n" +
            "Are you absolutely sure?"
        );

        if (!confirmed) return;

        boolean success = state.getPaymentService().deleteCustomer(customer.getId());
        
        if (success) {
            showSuccessDialog("Customer Deleted", 
                "Success!", 
                customer.getFullName() + " has been permanently deleted.");
            loadCustomers();
        } else {
            showErrorDialog("Deletion Failed", 
                "Could not delete customer", 
                "An error occurred while trying to delete the customer.");
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // SEARCH & FILTER
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    @FXML
    private void onSearch() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            displayCustomers(allCustomers);
            return;
        }

        List<Customer> filtered = allCustomers.stream()
            .filter(c -> c.getFullName().toLowerCase().contains(query) ||
                        c.getEmail().toLowerCase().contains(query) ||
                        c.getPhoneNumber().contains(query))
            .collect(Collectors.toList());

        displayCustomers(filtered);
    }

    @FXML
    private void onShowAll() {
        searchField.clear();
        currentFilter = "ALL";
        displayCustomers(allCustomers);
    }

    @FXML
    private void onFilterAll() {
        currentFilter = "ALL";
        displayCustomers(allCustomers);
    }

    @FXML
    private void onFilterLocked() {
        currentFilter = "LOCKED";
        List<Customer> locked = allCustomers.stream()
            .filter(Customer::isLocked)
            .collect(Collectors.toList());
        displayCustomers(locked);
    }

    @FXML
    private void onFilterAdmins() {
        currentFilter = "ADMINS";
        List<Customer> admins = allCustomers.stream()
            .filter(Customer::isAdmin)
            .collect(Collectors.toList());
        displayCustomers(admins);
    }

    @FXML
    private void onRefresh() {
        loadCustomers();
        loadStatistics();
        showSuccess("Refreshed!");
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // DIALOG HELPERS
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    private boolean showConfirmationDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void showSuccessDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfoDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        // Make the dialog resizable for long content
        alert.setResizable(true);
        alert.getDialogPane().setPrefWidth(600);
        
        alert.showAndWait();
    }

    private void showSuccess(String msg) {
        // Could add a toast notification here
        System.out.println("[AdminPanel] " + msg);
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // NAVIGATION
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    @FXML private void onBack()         { 
        System.out.println("[AdminPanel] Back button clicked - navigating to dashboard");
        go("dashboard"); 
    }
    @FXML private void onManageLoans()  {
        System.out.println("[AdminPanel] Manage Loans button clicked");
        go("admin-loans");
    }
    @FXML private void onHome()         { 
        System.out.println("[AdminPanel] Home button clicked - navigating to dashboard");
        go("dashboard"); 
    }
    @FXML private void onTransactions() { 
        System.out.println("[AdminPanel] Transactions button clicked");
        go("transactions"); 
    }
    @FXML private void onAdminPanel()   { 
        System.out.println("[AdminPanel] Admin panel button clicked - already here");
        /* already here */ 
    }
    @FXML private void onAccounts()     { 
        System.out.println("[AdminPanel] Accounts button clicked");
        go("accounts"); 
    }
    @FXML private void onShowAccount()  { 
        System.out.println("[AdminPanel] Show account button clicked");
        go("account-panel"); 
    }

    private void go(String scene) {
        try {
            System.out.println("[AdminPanel] Navigating to: " + scene);
            SceneHelper.switchTo(IgirePayApp.getPrimaryStage(), scene);
            System.out.println("[AdminPanel] Navigation successful");
        } catch (Exception e) {
            System.err.println("[AdminPanel] Navigation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
