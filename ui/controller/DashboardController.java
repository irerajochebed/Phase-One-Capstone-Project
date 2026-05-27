package com.igirepay.igirepaypaymentgateway.ui.controller;

import com.igirepay.igirepaypaymentgateway.LAB2.model.Account;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Transaction;
import com.igirepay.igirepaypaymentgateway.ui.AppState;
import com.igirepay.igirepaypaymentgateway.ui.IgirePayApp;
import com.igirepay.igirepaypaymentgateway.ui.SceneHelper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label phoneLabel;
    @FXML private Label currencyLabel;
    @FXML private Label balanceLabel;
    @FXML private VBox  recentTxBox;
    @FXML private ImageView headerLogoView;
    @FXML private VBox adminPanelSection;

    private final AppState state = AppState.getInstance();
    private boolean balanceVisible = true;
    private double  totalBalance   = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•");
        System.out.println("[Dashboard] INITIALIZE CALLED");
        System.out.println("â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•");
        try {
            File logoFile = new File("C:\\Users\\HP\\Downloads\\Igire_Rwanda_Logo.png");
            if (logoFile.exists()) {
                Image logoImage = new Image(logoFile.toURI().toString());
                headerLogoView.setImage(logoImage);
                System.out.println("[Dashboard] âœ“ Logo loaded");
            }
        } catch (Exception e) {
            System.err.println("[Dashboard] Could not load logo: " + e.getMessage());
        }

        Customer customer = state.getCurrentCustomer();
        if (customer == null) { 
            System.err.println("[Dashboard] ERROR: No customer in session!");
            go("login"); 
            return; 
        }

        System.out.println("[Dashboard] âœ“ Customer loaded: " + customer.getFullName());
        System.out.println("[Dashboard]   - ID: " + customer.getId());
        System.out.println("[Dashboard]   - Email: " + customer.getEmail());
        System.out.println("[Dashboard]   - Role: " + customer.getRole());
        System.out.println("[Dashboard]   - isAdmin(): " + customer.isAdmin());

        // Show phone number on balance card
        phoneLabel.setText(customer.getPhoneNumber());
        
        // Show admin panel section if user is admin
        System.out.println("[Dashboard] Checking admin panel visibility...");
        System.out.println("[Dashboard]   - customer.isAdmin() = " + customer.isAdmin());
        System.out.println("[Dashboard]   - adminPanelSection = " + adminPanelSection);
        
        if (customer.isAdmin()) {
            if (adminPanelSection != null) {
                adminPanelSection.setManaged(true);
                adminPanelSection.setVisible(true);
                System.out.println("[Dashboard] âœ“ Admin panel section ENABLED");
                System.out.println("[Dashboard]   - setManaged(true) called");
                System.out.println("[Dashboard]   - setVisible(true) called");
            } else {
                System.err.println("[Dashboard] âœ— ERROR: adminPanelSection is NULL!");
            }
        } else {
            System.out.println("[Dashboard] â„¹ User is not admin, panel hidden");
        }

        // Sum all account balances
        List<Account> accounts = state.getPaymentService()
                .getAccountsByCustomer(customer.getId());
        totalBalance = accounts.stream().mapToDouble(Account::getBalance).sum();
        updateBalanceDisplay();
        loadRecentTransactions(accounts);
        
        System.out.println("[Dashboard] âœ“ Initialize complete");
        System.out.println("â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•");
    }

    private void updateBalanceDisplay() {
        if (balanceVisible) {
            balanceLabel.setText(String.format("%,.0f", totalBalance));
        } else {
            balanceLabel.setText("â€¢â€¢â€¢â€¢â€¢â€¢");
        }
    }

    @FXML
    private void onToggleBalance() {
        balanceVisible = !balanceVisible;
        updateBalanceDisplay();
    }

    private void loadRecentTransactions(List<Account> accounts) {
        recentTxBox.getChildren().clear();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy | HH:mm");

        List<Transaction> all = new ArrayList<>();
        for (Account acc : accounts) {
            all.addAll(state.getPaymentService()
                    .searchTransactions(acc.getId(), null, null, null, null));
        }

        // Sort newest first, take top 5
        all.sort((a, b) -> {
            if (a.getCreatedAt() == null) return 1;
            if (b.getCreatedAt() == null) return -1;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });
        List<Transaction> recent = all.subList(0, Math.min(5, all.size()));

        if (recent.isEmpty()) {
            Label empty = new Label("No transactions yet.");
            empty.getStyleClass().add("tagline-label");
            recentTxBox.getChildren().add(empty);
            return;
        }

        for (Transaction t : recent) {
            recentTxBox.getChildren().add(buildTxCard(t, fmt));
        }
    }

    /** Build one transaction card matching the MoMo list style. */
    private VBox buildTxCard(Transaction t, DateTimeFormatter fmt) {
        VBox card = new VBox(4);
        card.getStyleClass().add("tx-card");

        // Date + amount row
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        String dateStr = t.getCreatedAt() != null ? t.getCreatedAt().format(fmt) : "-";
        Label dateLabel = new Label(dateStr);
        dateLabel.getStyleClass().add("tx-date");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        boolean isOut = Transaction.TYPE_WITHDRAWAL.equals(t.getTransactionType())
                || Transaction.TYPE_TRANSFER.equals(t.getTransactionType());
        Label amtLabel = new Label((isOut ? "-" : "+")
                + String.format("%,.2f", t.getAmount()));
        amtLabel.getStyleClass().add(isOut ? "tx-amount-out" : "tx-amount-in");

        topRow.getChildren().addAll(dateLabel, spacer, amtLabel);

        // Description / name row
        Label nameLabel = new Label(
                t.getDescription() != null && !t.getDescription().isBlank()
                        ? t.getDescription() : t.getReferenceId());
        nameLabel.getStyleClass().add("tx-name");

        // Type row
        Label typeLabel = new Label("Transfer type: " + t.getTransactionType());
        typeLabel.getStyleClass().add("tx-type");

        card.getChildren().addAll(topRow, nameLabel, typeLabel);
        return card;
    }
    @FXML private void onHome()         { go("dashboard"); }
    @FXML private void onSendMoney()    { go("send-money"); }
    @FXML private void onDeposit()      { go("deposit"); }
    @FXML private void onWithdraw()     { go("withdraw"); }
    @FXML private void onTransactions() { go("transactions"); }
    @FXML private void onAccounts()     { go("accounts"); }
    @FXML private void onTabServices()  { /* already on services */ }
    @FXML private void onNotifications() { /* future feature */ }

    @FXML
    private void onShowAccount() { go("account-panel"); }
    
    @FXML
    private void onAdminPanel() {
        Customer customer = state.getCurrentCustomer();
        if (customer != null && customer.isAdmin()) {
            go("admin-panel");
        }
    }

    private void go(String s) {
        try { SceneHelper.switchTo(IgirePayApp.getPrimaryStage(), s); }
        catch (Exception e) { e.printStackTrace(); }
    }
}
