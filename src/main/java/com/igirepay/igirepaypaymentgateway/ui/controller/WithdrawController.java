package com.igirepay.igirepaypaymentgateway.ui.controller;

import com.igirepay.igirepaypaymentgateway.LAB2.model.Account;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB2.model.WithdrawalRequest;
import com.igirepay.igirepaypaymentgateway.LAB2.util.ValidationUtil;
import com.igirepay.igirepaypaymentgateway.LAB3.exception.IgirePayException;
import com.igirepay.igirepaypaymentgateway.LAB3.service.WithdrawalService;
import com.igirepay.igirepaypaymentgateway.ui.AppState;
import com.igirepay.igirepaypaymentgateway.ui.IgirePayApp;
import com.igirepay.igirepaypaymentgateway.ui.SceneHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

public class WithdrawController implements Initializable {

    @FXML private ComboBox<String> accountCombo;
    @FXML private TextField        amountField;
    @FXML private TextField        descField;
    @FXML private PasswordField    pinField;
    @FXML private Label            statusLabel;

    private final AppState state = AppState.getInstance();
    private final WithdrawalService withdrawalService = new WithdrawalService();
    private List<Account>  accounts;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Customer customer = state.getCurrentCustomer();
        if (customer == null) return;

        accounts = state.getPaymentService().getAccountsByCustomer(customer.getId());
        var items = FXCollections.<String>observableArrayList();
        for (Account acc : accounts)
            items.add("[" + acc.getId() + "] " + acc.getAccountType()
                    + "  â€”  " + String.format("%,.2f", acc.getBalance()) + " RWF");
        accountCombo.setItems(items);
        if (!items.isEmpty()) accountCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void onDoWithdraw() {
        try {
            int idx = accountCombo.getSelectionModel().getSelectedIndex();
            if (idx < 0) { showError("Select an account."); return; }

            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) { showError("Amount must be greater than zero."); return; }

            String pin = pinField.getText().trim();
            if (pin.isEmpty()) { showError("Enter your PIN to confirm."); return; }

            state.getAuthService().requirePinConfirmation(pin);

            Account selectedAccount = accounts.get(idx);
            Customer customer = state.getCurrentCustomer();
            boolean isSavingsAccount = selectedAccount.getAccountType().equalsIgnoreCase("SAVINGS");
            boolean requiresWaiting = withdrawalService.requiresWaitingPeriod(selectedAccount.getId());

            if (isSavingsAccount && requiresWaiting) {
                // Savings account younger than 1 month - create withdrawal request
                handleSavingsWithdrawalRequest(selectedAccount, customer, amount);
            } else {
                // Immediate withdrawal (wallet or mature savings account)
                handleImmediateWithdrawal(selectedAccount, amount);
            }

        } catch (IgirePayException e) {
            showErrorDialog("Authentication Error", "PIN verification failed", e.getMessage());
        } catch (NumberFormatException e) {
            showError("Enter a valid amount.");
        } catch (Exception e) {
            showErrorDialog("Unexpected Error", "An error occurred", e.getMessage());
        }
    }

    private void handleSavingsWithdrawalRequest(Account account, Customer customer, double amount) {
        try {
            // Show information about waiting period
            LocalDateTime maturityDate = withdrawalService.getAccountMaturityDate(account.getId());
            long daysUntilMaturity = ChronoUnit.DAYS.between(LocalDateTime.now(), maturityDate);
            
            Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
            infoAlert.setTitle("Savings Account Withdrawal");
            infoAlert.setHeaderText("48-Hour Waiting Period Required");
            infoAlert.setContentText(
                "Your savings account is less than 1 month old.\n\n" +
                "Account created: " + account.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n" +
                "Matures on: " + maturityDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + 
                " (" + daysUntilMaturity + " days remaining)\n\n" +
                "For withdrawals before maturity, you must wait 48 hours after requesting.\n\n" +
                "Would you like to create a withdrawal request?"
            );
            
            ButtonType createRequest = new ButtonType("Create Request", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            infoAlert.getButtonTypes().setAll(createRequest, cancel);
            
            Optional<ButtonType> result = infoAlert.showAndWait();
            if (result.isPresent() && result.get() == createRequest) {
                WithdrawalRequest request = withdrawalService.requestWithdrawal(
                    account.getId(), customer.getId(), amount
                );
                
                long hoursToWait = request.getHoursRemaining();
                String availableTime = request.getAvailableDate().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                );
                
                showSuccessDialog(
                    "Withdrawal Request Created",
                    "Your request has been submitted",
                    "Amount: " + String.format("%,.2f", amount) + " RWF\n" +
                    "Reference: " + request.getReferenceId() + "\n" +
                    "Available in: " + hoursToWait + " hours\n" +
                    "Available on: " + availableTime + "\n\n" +
                    "You will receive a notification when your withdrawal is ready.\n" +
                    "You can view pending requests in the Accounts section."
                );
                
                amountField.clear();
                descField.clear();
                pinField.clear();
            }
            
        } catch (IgirePayException e) {
            showErrorDialog("Request Failed", "Could not create withdrawal request", e.getMessage());
        }
    }

    private void handleImmediateWithdrawal(Account account, double amount) {
        try {
            // Calculate fee
            double fee = ValidationUtil.calculateTransactionFee(amount);
            double totalDeduction = amount + fee;
            
            // Show confirmation dialog
            boolean confirmed = showConfirmationDialog(
                "Confirm Withdrawal",
                "Withdraw " + String.format("%,.2f", amount) + " RWF",
                "From: " + account.getAccountType() + " Account\n" +
                "Amount: " + String.format("%,.2f", amount) + " RWF\n" +
                "Fee: " + String.format("%,.2f", fee) + " RWF\n" +
                "Total: " + String.format("%,.2f", totalDeduction) + " RWF\n\n" +
                "Do you want to proceed?"
            );

            if (!confirmed) {
                showError("Withdrawal cancelled.");
                return;
            }

            String desc  = descField.getText().trim();
            String refId = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            boolean ok = state.getPaymentService().withdraw(
                    account.getId(), refId, amount,
                    desc.isEmpty() ? "Withdrawal" : desc);

            if (ok) {
                showSuccessDialog("Withdrawal Successful!",
                    "Money withdrawn successfully",
                    "Amount: " + String.format("%,.2f", amount) + " RWF\n" +
                    "Fee: " + String.format("%,.2f", fee) + " RWF\n" +
                    "Total deducted: " + String.format("%,.2f", totalDeduction) + " RWF\n" +
                    "Reference: " + refId);
                amountField.clear(); descField.clear(); pinField.clear();
            } else {
                showErrorDialog("Withdrawal Failed",
                    "The withdrawal could not be completed",
                    "Possible reasons:\n" +
                    "â€¢ Insufficient funds\n" +
                    "â€¢ Network error\n\n" +
                    "Your balance was NOT deducted. You can try again.");
            }
        } catch (Exception e) {
            showErrorDialog("Withdrawal Error", "An error occurred", e.getMessage());
        }
    }
    private void showError(String msg) {
        statusLabel.setText(msg);
        statusLabel.getStyleClass().removeAll("msg-success");
        if (!statusLabel.getStyleClass().contains("msg-error"))
            statusLabel.getStyleClass().add("msg-error");
    }

    private void showSuccess(String msg) {
        statusLabel.setText(msg);
        statusLabel.getStyleClass().removeAll("msg-error");
        if (!statusLabel.getStyleClass().contains("msg-success"))
            statusLabel.getStyleClass().add("msg-success");
    }

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
    @FXML private void onDashboard()    { nav("dashboard"); }
    @FXML private void onSendMoney()    { nav("send-money"); }
    @FXML private void onDeposit()      { nav("deposit"); }
    @FXML private void onWithdraw()     { nav("withdraw"); }
    @FXML private void onTransactions() { nav("transactions"); }
    @FXML private void onAccounts()     { nav("accounts"); }
    @FXML private void onShowAccount()  { nav("account-panel"); }
    @FXML private void onLogout()       { state.getAuthService().logout(); state.setCurrentCustomer(null); nav("login"); }

    private void nav(String s) {
        try { SceneHelper.switchTo(IgirePayApp.getPrimaryStage(), s); }
        catch (Exception e) { e.printStackTrace(); }
    }
}
