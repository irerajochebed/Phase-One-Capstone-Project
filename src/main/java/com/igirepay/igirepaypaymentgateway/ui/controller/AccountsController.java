package com.igirepay.igirepaypaymentgateway.ui.controller;

import com.igirepay.igirepaypaymentgateway.LAB2.model.Account;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.ui.AppState;
import com.igirepay.igirepaypaymentgateway.ui.IgirePayApp;
import com.igirepay.igirepaypaymentgateway.ui.SceneHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AccountsController implements Initializable {

    @FXML private TableView<AccRow>        accountsTable;
    @FXML private TableColumn<AccRow, String> colId;
    @FXML private TableColumn<AccRow, String> colType;
    @FXML private TableColumn<AccRow, String> colBalance;
    @FXML private TableColumn<AccRow, String> colCurrency;
    @FXML private TableColumn<AccRow, String> colCreated;
    @FXML private Label                    deleteStatusLabel;
    @FXML private ComboBox<String>         accountTypeCombo;
    @FXML private TextField                initialBalanceField;
    @FXML private Label                    createStatusLabel;

    private final AppState state = AppState.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Customer customer = state.getCurrentCustomer();
        if (customer == null) return;

        // Wire table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        colCurrency.setCellValueFactory(new PropertyValueFactory<>("currency"));
        colCreated.setCellValueFactory(new PropertyValueFactory<>("created"));

        // Account type options
        accountTypeCombo.setItems(FXCollections.observableArrayList("WALLET", "SAVINGS"));
        accountTypeCombo.getSelectionModel().selectFirst();

        loadAccounts();
    }

    private void loadAccounts() {
        Customer customer = state.getCurrentCustomer();
        List<Account> accounts = state.getPaymentService()
                .getAccountsByCustomer(customer.getId());

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        ObservableList<AccRow> rows = FXCollections.observableArrayList();
        for (Account a : accounts) {
            rows.add(new AccRow(
                    String.valueOf(a.getId()),
                    a.getAccountType(),
                    String.format("%,.2f", a.getBalance()),
                    a.getCurrency(),
                    a.getCreatedAt() != null ? a.getCreatedAt().format(fmt) : "-"
            ));
        }
        accountsTable.setItems(rows);
    }

    @FXML
    private void onCreateAccount() {
        try {
            String type = accountTypeCombo.getValue();
            String balText = initialBalanceField.getText().trim();
            double balance = balText.isEmpty() ? 0.0 : Double.parseDouble(balText);

            if (balance < 0) { showCreateError("Initial balance cannot be negative."); return; }

            Customer customer = state.getCurrentCustomer();
            Account account = state.getPaymentService()
                    .createAccount(customer.getId(), type, balance, "RWF");

            if (account != null) {
                showCreateSuccess(" " + type + " account created! ID: " + account.getId());
                initialBalanceField.clear();
                loadAccounts(); // refresh the table
            } else {
                showCreateError("Failed to create account.");
            }
        } catch (NumberFormatException e) {
            showCreateError("Enter a valid number for initial balance.");
        } catch (Exception e) {
            showCreateError("Error: " + e.getMessage());
        }
    }

    @FXML
    private void onDeleteAccount() {
        AccRow selected = accountsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            deleteStatusLabel.setText("Select an account to delete.");
            return;
        }

        int accountId = Integer.parseInt(selected.getId());

        // Confirm dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Account");
        alert.setHeaderText("Delete account #" + accountId + "?");
        alert.setContentText("This will permanently delete the account and all its transactions.");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                boolean ok = state.getPaymentService().deleteAccount(accountId);
                if (ok) {
                    deleteStatusLabel.setText(" Account deleted.");
                    loadAccounts();
                } else {
                    deleteStatusLabel.setText(" Could not delete account.");
                }
            }
        });
    }

    private void showCreateError(String m) {
        createStatusLabel.setText(m);
        createStatusLabel.getStyleClass().removeAll("msg-success");
        if (!createStatusLabel.getStyleClass().contains("msg-error"))
            createStatusLabel.getStyleClass().add("msg-error");
    }

    private void showCreateSuccess(String m) {
        createStatusLabel.setText(m);
        createStatusLabel.getStyleClass().removeAll("msg-error");
        if (!createStatusLabel.getStyleClass().contains("msg-success"))
            createStatusLabel.getStyleClass().add("msg-success");
    }
    @FXML private void onDashboard()    { nav("dashboard"); }
    @FXML private void onSendMoney()    { nav("send-money"); }
    @FXML private void onDeposit()      { nav("deposit"); }
    @FXML private void onWithdraw()     { nav("withdraw"); }
    @FXML private void onTransactions() { nav("transactions"); }
    @FXML private void onAccounts()     { nav("accounts"); }
    @FXML private void onShowAccount()  { nav("account-panel"); }
    @FXML private void onLogout() {
        state.getAuthService().logout();
        state.setCurrentCustomer(null);
        nav("login");
    }
    private void nav(String s) {
        try { SceneHelper.switchTo(IgirePayApp.getPrimaryStage(), s); }
        catch (Exception e) { e.printStackTrace(); }
    }
    public static class AccRow {
        private final String id, type, balance, currency, created;

        public AccRow(String id, String type, String balance,
                      String currency, String created) {
            this.id = id; this.type = type; this.balance = balance;
            this.currency = currency; this.created = created;
        }

        public String getId()       { return id; }
        public String getType()     { return type; }
        public String getBalance()  { return balance; }
        public String getCurrency() { return currency; }
        public String getCreated()  { return created; }
    }
}
