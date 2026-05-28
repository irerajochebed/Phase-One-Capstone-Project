package com.igirepay.igirepaypaymentgateway.ui.controller;

import com.igirepay.igirepaypaymentgateway.LAB2.model.Account;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Transaction;
import com.igirepay.igirepaypaymentgateway.ui.AppState;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardContentController implements Initializable {

    @FXML private Label phoneLabel;
    @FXML private Label balanceLabel;
    @FXML private VBox  recentTxBox;
    @FXML private VBox  adminPanelSection;

    private final AppState state = AppState.getInstance();
    private double  totalBalance  = 0;
    private boolean balanceVisible = true;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("");
        System.out.println("[DashboardContent] INITIALIZE CALLED");
        System.out.println("");
        
        Customer customer = state.getCurrentCustomer();
        if (customer == null) {
            System.err.println("[DashboardContent] ERROR: No customer in session!");
            return;
        }

        System.out.println("[DashboardContent]  Customer loaded: " + customer.getFullName());
        System.out.println("[DashboardContent]   - ID: " + customer.getId());
        System.out.println("[DashboardContent]   - Email: " + customer.getEmail());
        System.out.println("[DashboardContent]   - Role: " + customer.getRole());
        System.out.println("[DashboardContent]   - isAdmin(): " + customer.isAdmin());

        phoneLabel.setText(customer.getPhoneNumber());

        // Show admin panel section if user is admin
        System.out.println("[DashboardContent] Checking admin panel visibility...");
        System.out.println("[DashboardContent]   - customer.isAdmin() = " + customer.isAdmin());
        System.out.println("[DashboardContent]   - adminPanelSection = " + adminPanelSection);
        
        if (customer.isAdmin()) {
            if (adminPanelSection != null) {
                adminPanelSection.setManaged(true);
                adminPanelSection.setVisible(true);
                System.out.println("[DashboardContent]  Admin panel section ENABLED");
                System.out.println("[DashboardContent]   - setManaged(true) called");
                System.out.println("[DashboardContent]   - setVisible(true) called");
            } else {
                System.err.println("[DashboardContent]  ERROR: adminPanelSection is NULL!");
            }
        } else {
            System.out.println("[DashboardContent]  User is not admin, panel hidden");
        }

        List<Account> accounts = state.getPaymentService()
                .getAccountsByCustomer(customer.getId());
        totalBalance = accounts.stream().mapToDouble(Account::getBalance).sum();
        updateBalance();
        loadRecent(accounts);
        
        System.out.println("[DashboardContent]  Initialize complete");
        System.out.println("");
    }

    private void updateBalance() {
        balanceLabel.setText(balanceVisible
                ? String.format("%,.0f", totalBalance) : "");
    }

    @FXML private void onToggleBalance() {
        balanceVisible = !balanceVisible;
        updateBalance();
    }

    private void loadRecent(List<Account> accounts) {
        recentTxBox.getChildren().clear();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy | HH:mm");

        List<Transaction> all = new ArrayList<>();
        for (Account acc : accounts)
            all.addAll(state.getPaymentService()
                    .searchTransactions(acc.getId(), null, null, null, null));

        all.sort((a, b) -> {
            if (a.getCreatedAt() == null) return 1;
            if (b.getCreatedAt() == null) return -1;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });

        List<Transaction> recent = all.subList(0, Math.min(5, all.size()));
        if (recent.isEmpty()) {
            Label e = new Label("No transactions yet.");
            e.setStyle("-fx-text-fill:#9CA3AF;");
            recentTxBox.getChildren().add(e);
            return;
        }

        for (Transaction t : recent) {
            VBox card = new VBox(4);
            card.getStyleClass().add("tx-card");

            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);

            Label date = new Label(t.getCreatedAt() != null
                    ? t.getCreatedAt().format(fmt) : "-");
            date.getStyleClass().add("tx-date");

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            boolean out = !Transaction.TYPE_DEPOSIT.equals(t.getTransactionType());
            Label amt = new Label((out ? "-" : "+")
                    + String.format("%,.2f", t.getAmount()));
            amt.getStyleClass().add(out ? "tx-amount-out" : "tx-amount-in");

            row.getChildren().addAll(date, sp, amt);

            Label desc = new Label(t.getDescription() != null
                    && !t.getDescription().isBlank()
                    ? t.getDescription() : t.getReferenceId());
            desc.getStyleClass().add("tx-name");

            Label type = new Label("Type: " + t.getTransactionType());
            type.getStyleClass().add("tx-type");

            card.getChildren().addAll(row, desc, type);
            recentTxBox.getChildren().add(card);
        }
    }
    @FXML private void onSendMoney()    { nav("send-money"); }
    @FXML private void onDeposit()      { nav("deposit"); }
    @FXML private void onWithdraw()     { nav("withdraw"); }
    @FXML private void onTransactions() { nav("transactions"); }
    @FXML private void onAccounts()     { nav("accounts"); }
    @FXML private void onLoans()        { nav("loans"); }
    @FXML private void onSettings()     { nav("account-panel"); }
    @FXML private void onAdminPanel()   { nav("admin-panel"); }

    private void nav(String screen) {
        try {
            com.igirepay.igirepaypaymentgateway.ui.SceneHelper
                    .switchTo(com.igirepay.igirepaypaymentgateway.ui.IgirePayApp
                            .getPrimaryStage(), screen);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
