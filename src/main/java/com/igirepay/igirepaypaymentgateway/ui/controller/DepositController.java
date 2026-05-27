package com.igirepay.igirepaypaymentgateway.ui.controller;

import com.igirepay.igirepaypaymentgateway.LAB2.model.Account;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.ui.AppState;
import com.igirepay.igirepaypaymentgateway.ui.IgirePayApp;
import com.igirepay.igirepaypaymentgateway.ui.SceneHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class DepositController implements Initializable {

    @FXML private ComboBox<String> accountCombo;
    @FXML private TextField        amountField;
    @FXML private TextField        descField;
    @FXML private Label            statusLabel;

    private final AppState state = AppState.getInstance();
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
    private void onDoDeposit() {
        try {
            int idx = accountCombo.getSelectionModel().getSelectedIndex();
            if (idx < 0) { showError("Select an account."); return; }

            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) { showError("Amount must be greater than zero."); return; }

            String desc  = descField.getText().trim();
            String refId = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            boolean ok = state.getPaymentService().deposit(
                    accounts.get(idx).getId(), refId, amount,
                    desc.isEmpty() ? "Deposit" : desc);

            if (ok) {
                showSuccess("âœ“ Deposited " + String.format("%,.2f", amount) + " RWF.  Ref: " + refId);
                amountField.clear();
                descField.clear();
            } else {
                showError("Deposit failed. Please try again.");
            }
        } catch (NumberFormatException e) {
            showError("Enter a valid amount (numbers only).");
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
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
