package com.igirepay.igirepaypaymentgateway.ui.controller;

import com.igirepay.igirepaypaymentgateway.LAB2.model.Account;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Transaction;
import com.igirepay.igirepaypaymentgateway.LAB2.util.ValidationUtil;
import com.igirepay.igirepaypaymentgateway.LAB3.exception.IgirePayException;
import com.igirepay.igirepaypaymentgateway.ui.AppState;
import com.igirepay.igirepaypaymentgateway.ui.IgirePayApp;
import com.igirepay.igirepaypaymentgateway.ui.SceneHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * SendMoneyController â€” send money by PHONE NUMBER.
 *
 * Flow:
 *   1. User types recipient's phone number
 *   2. App looks up the customer by phone â†’ shows their name as confirmation
 *   3. User enters amount + PIN â†’ transfer executes
 *   4. If account not found â†’ JDBC rollback â†’ balance unchanged â†’ user can retry
 */
public class SendMoneyController implements Initializable {

    @FXML private VBox             iroUserCard;
    @FXML private VBox             bankCard;
    @FXML private ComboBox<String> fromAccountCombo;
    @FXML private Label            toLabel;
    @FXML private TextField        toAccountField;   // phone number input
    @FXML private Label            recipientLabel;   // shows resolved name
    @FXML private TextField        amountField;
    @FXML private Label            feeLabel;         // shows transaction fee
    @FXML private TextField        descField;
    @FXML private PasswordField    pinField;
    @FXML private Label            statusLabel;
    @FXML private VBox             recentTransfersBox;

    private final AppState state = AppState.getInstance();
    private List<Account>  myAccounts;
    private String         transferMode = "IRO";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Customer customer = state.getCurrentCustomer();
        if (customer == null) return;

        // Populate from-account dropdown
        myAccounts = state.getPaymentService().getAccountsByCustomer(customer.getId());
        var items = FXCollections.<String>observableArrayList();
        for (Account acc : myAccounts)
            items.add("[" + acc.getId() + "] " + acc.getAccountType()
                    + "  â€”  " + String.format("%,.2f", acc.getBalance()) + " RWF");
        fromAccountCombo.setItems(items);
        if (!items.isEmpty()) fromAccountCombo.getSelectionModel().selectFirst();

        highlightCard(true);

        // Live phone lookup â€” as user types, resolve the recipient name
        toAccountField.textProperty().addListener((obs, oldVal, newVal) -> {
            String phone = newVal.trim();
            if (phone.length() >= 9) {
                lookupRecipient(phone);
            } else {
                recipientLabel.setText("");
            }
        });

        // Live fee calculation â€” as user types amount, show the fee
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                if (feeLabel != null) { // Null check to prevent crashes
                    double amount = Double.parseDouble(newVal.trim());
                    if (amount > 0) {
                        double fee = ValidationUtil.calculateTransactionFee(amount);
                        double total = amount + fee;
                        feeLabel.setText(String.format("Fee: %.2f RWF | Total: %.2f RWF", fee, total));
                        feeLabel.setStyle("-fx-text-fill:#F5A623; -fx-font-size:13px; -fx-font-weight:bold;");
                    } else {
                        feeLabel.setText("");
                    }
                }
            } catch (NumberFormatException e) {
                if (feeLabel != null) {
                    feeLabel.setText("");
                }
            }
        });

        loadRecentTransfers();
    }

    /** Look up a customer by phone number and show their name. */
    private void lookupRecipient(String phone) {
        try {
            List<Customer> all = state.getPaymentService().getAllCustomers();
            Customer found = all.stream()
                    .filter(c -> c.getPhoneNumber().replaceAll("\\s+", "")
                            .equals(phone.replaceAll("\\s+", "")))
                    .findFirst()
                    .orElse(null);

            if (found != null) {
                List<Account> accs = state.getPaymentService()
                        .getAccountsByCustomer(found.getId());
                String extra = accs.isEmpty() ? "  (wallet will be created)" : "";
                recipientLabel.setText("âœ“  " + found.getFullName() + extra);
                recipientLabel.setStyle(
                        "-fx-text-fill:#10B981; -fx-font-size:13px; -fx-font-weight:bold;");
            } else {
                recipientLabel.setText("âœ—  No IRO account found for this number");
                recipientLabel.setStyle("-fx-text-fill:#EF4444; -fx-font-size:12px;");
            }
        } catch (Exception e) {
            recipientLabel.setText("");
        }
    }

    @FXML private void onSelectIROUser() {
        transferMode = "IRO";
        highlightCard(true);
        toLabel.setText("RECIPIENT PHONE NUMBER");
        toAccountField.setPromptText("e.g. 0781234567");
    }

    @FXML private void onSelectBank() {
        transferMode = "BANK";
        highlightCard(false);
        toLabel.setText("BANK ACCOUNT ID");
        toAccountField.setPromptText("Enter bank account ID");
        recipientLabel.setText("");
    }

    private void highlightCard(boolean iroSelected) {
        String active   = "-fx-border-color:#F5A623; -fx-border-width:2; -fx-border-radius:12;";
        String inactive = "-fx-border-color:transparent;";
        iroUserCard.setStyle(iroSelected ? active : inactive);
        bankCard.setStyle(iroSelected ? inactive : active);
    }

    @FXML
    private void onSend() {
        statusLabel.setText("");

        try {
            int idx = fromAccountCombo.getSelectionModel().getSelectedIndex();
            if (idx < 0) { showError("Select a source account."); return; }

            String toInput = toAccountField.getText().trim();
            if (toInput.isEmpty()) { showError("Enter recipient phone number."); return; }

            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) { showError("Amount must be greater than zero."); return; }

            String pin = pinField.getText().trim();
            if (pin.isEmpty()) { showError("Enter your PIN to confirm."); return; }

            // PIN confirmation
            state.getAuthService().requirePinConfirmation(pin);

            int fromId = myAccounts.get(idx).getId();
            Account fromAccount = myAccounts.get(idx);
            int toId;
            boolean isInternalTransfer = false; // wallet â†’ savings for same customer
            String transferType = "";

            if ("IRO".equals(transferMode)) {
                // Look up recipient by phone number
                List<Customer> all = state.getPaymentService().getAllCustomers();
                Customer recipient = all.stream()
                        .filter(c -> c.getPhoneNumber().replaceAll("\\s+", "")
                                .equals(toInput.replaceAll("\\s+", "")))
                        .findFirst().orElse(null);

                if (recipient == null) {
                    showErrorDialog("Recipient Not Found",
                            "No IRO account found for phone: " + toInput,
                            "Your balance was NOT deducted. Please check the phone number and try again.");
                    return;
                }
                Customer currentCustomer = state.getCurrentCustomer();
                if (recipient.getId() == currentCustomer.getId()) {
                    // Same customer - check if it's wallet â†’ savings
                    List<Account> myAllAccounts = state.getPaymentService()
                            .getAccountsByCustomer(currentCustomer.getId());
                    Account targetAccount = null;
                    for (Account acc : myAllAccounts) {
                        if (acc.getId() != fromId) {
                            targetAccount = acc;
                            break;
                        }
                    }

                    if (targetAccount == null) {
                        showErrorDialog("Invalid Transfer",
                                "Cannot send money to the same account",
                                "You can only transfer between your Wallet and Savings accounts.");
                        return;
                    }

                    // Show confirmation dialog for internal transfer
                    transferType = fromAccount.getAccountType() + " â†’ " + targetAccount.getAccountType();
                    boolean confirmed = showConfirmationDialog(
                            "Internal Transfer",
                            "Transfer " + String.format("%,.2f", amount) + " RWF",
                            "From: " + fromAccount.getAccountType() + " Account\n" +
                            "To: " + targetAccount.getAccountType() + " Account\n" +
                            "Amount: " + String.format("%,.2f", amount) + " RWF\n" +
                            "Fee: FREE (Internal transfer)\n\n" +
                            "Do you want to proceed?"
                    );

                    if (!confirmed) {
                        showError("Transfer cancelled.");
                        return;
                    }

                    // This is an internal transfer (wallet â†” savings) - no fee
                    toId = targetAccount.getId();
                    isInternalTransfer = true;
                } else {
                    // Different customer - proceed normally
                    List<Account> recipientAccounts = state.getPaymentService()
                            .getAccountsByCustomer(recipient.getId());

                    if (recipientAccounts.isEmpty()) {
                        // Auto-create a WALLET account with 0 balance for the recipient
                        Account newAcc = state.getPaymentService()
                                .createAccount(recipient.getId(), "WALLET", 0.0, "RWF");
                        if (newAcc == null) {
                            showErrorDialog("Account Creation Failed",
                                    "Could not create wallet for recipient",
                                    "Your balance was NOT deducted. Please try again later.");
                            return;
                        }
                        toId = newAcc.getId();
                        System.out.println("[SendMoney] Auto-created wallet for recipient id="
                                + recipient.getId() + " â†’ account id=" + toId);
                    } else {
                        // Use their first WALLET account, or fall back to first account
                        toId = recipientAccounts.stream()
                                .filter(a -> "WALLET".equals(a.getAccountType()))
                                .findFirst()
                                .orElse(recipientAccounts.get(0))
                                .getId();
                    }

                    // Show confirmation dialog for external transfer
                    double fee = ValidationUtil.calculateTransactionFee(amount);
                    double totalDeduction = amount + fee;
                    
                    boolean confirmed = showConfirmationDialog(
                            "Confirm Transfer",
                            "Send " + String.format("%,.2f", amount) + " RWF to " + recipient.getFullName(),
                            "Recipient: " + recipient.getFullName() + "\n" +
                            "Phone: " + recipient.getPhoneNumber() + "\n" +
                            "Amount: " + String.format("%,.2f", amount) + " RWF\n" +
                            "Fee: " + String.format("%,.2f", fee) + " RWF\n" +
                            "Total: " + String.format("%,.2f", totalDeduction) + " RWF\n\n" +
                            "Do you want to proceed?"
                    );

                    if (!confirmed) {
                        showError("Transfer cancelled.");
                        return;
                    }
                }

            } else {
                // Bank mode â€” use numeric ID directly
                toId = Integer.parseInt(toInput);
                
                double fee = ValidationUtil.calculateTransactionFee(amount);
                double totalDeduction = amount + fee;
                
                boolean confirmed = showConfirmationDialog(
                        "Confirm Bank Transfer",
                        "Send " + String.format("%,.2f", amount) + " RWF",
                        "To Bank Account: " + toId + "\n" +
                        "Amount: " + String.format("%,.2f", amount) + " RWF\n" +
                        "Fee: " + String.format("%,.2f", fee) + " RWF\n" +
                        "Total: " + String.format("%,.2f", totalDeduction) + " RWF\n\n" +
                        "Do you want to proceed?"
                );

                if (!confirmed) {
                    showError("Transfer cancelled.");
                    return;
                }
            }

            String refId = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String desc  = descField.getText().trim();

            // Transfer with or without fee based on transfer type
            boolean ok;
            if (isInternalTransfer) {
                // Internal transfer - use transferInternal (no fee)
                ok = state.getPaymentService().transferInternal(
                        fromId, toId, refId, amount,
                        desc.isEmpty() ? "Internal Transfer" : desc);
            } else {
                // External transfer - use regular transfer (with fee)
                ok = state.getPaymentService().transfer(
                        fromId, toId, refId, amount,
                        desc.isEmpty() ? "IROPay Transfer" : desc);
            }

            if (ok) {
                if (isInternalTransfer) {
                    showSuccessDialog("Transfer Successful!",
                            "Internal transfer completed",
                            "Amount: " + String.format("%,.2f", amount) + " RWF\n" +
                            "Type: " + transferType + "\n" +
                            "Fee: FREE\n" +
                            "Reference: " + refId);
                } else {
                    double fee = ValidationUtil.calculateTransactionFee(amount);
                    double totalDeduction = amount + fee;
                    showSuccessDialog("Transfer Successful!",
                            "Money sent successfully",
                            "Amount sent: " + String.format("%,.2f", amount) + " RWF\n" +
                            "Fee: " + String.format("%,.2f", fee) + " RWF\n" +
                            "Total deducted: " + String.format("%,.2f", totalDeduction) + " RWF\n" +
                            "Reference: " + refId);
                }
                clearForm();
                loadRecentTransfers();
            } else {
                showErrorDialog("Transfer Failed",
                        "The transfer could not be completed",
                        "Possible reasons:\n" +
                        "â€¢ Insufficient funds\n" +
                        "â€¢ Account not found\n" +
                        "â€¢ Network error\n\n" +
                        "Your balance was NOT deducted. You can try again.");
            }

        } catch (IgirePayException e) {
            showErrorDialog("Authentication Error", "PIN verification failed", e.getMessage());
        } catch (NumberFormatException e) {
            showError("Invalid amount. Use numbers only (e.g. 5000).");
        } catch (Exception e) {
            showErrorDialog("Unexpected Error", "An error occurred", e.getMessage());
        }
    }

    private void loadRecentTransfers() {
        recentTransfersBox.getChildren().clear();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy | HH:mm");

        List<Transaction> all = new ArrayList<>();
        for (Account acc : myAccounts) {
            all.addAll(state.getPaymentService()
                    .searchTransactions(acc.getId(), Transaction.TYPE_TRANSFER,
                            null, null, null));
        }
        all.sort((a, b) -> {
            if (a.getCreatedAt() == null) return 1;
            if (b.getCreatedAt() == null) return -1;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });

        List<Transaction> recent = all.subList(0, Math.min(5, all.size()));
        if (recent.isEmpty()) {
            Label empty = new Label("No recent transfers.");
            empty.setStyle("-fx-text-fill:#9CA3AF; -fx-font-size:13px;");
            recentTransfersBox.getChildren().add(empty);
            return;
        }

        for (Transaction t : recent) {
            VBox card = new VBox(4);
            card.getStyleClass().add("tx-card");

            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);

            String dateStr = t.getCreatedAt() != null ? t.getCreatedAt().format(fmt) : "-";
            Label dateLabel = new Label(dateStr);
            dateLabel.getStyleClass().add("tx-date");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label amtLabel = new Label("-" + String.format("%,.2f", t.getAmount()));
            amtLabel.getStyleClass().add("tx-amount-out");

            row.getChildren().addAll(dateLabel, spacer, amtLabel);

            Label descLabel = new Label(
                    t.getDescription() != null && !t.getDescription().isBlank()
                            ? t.getDescription() : t.getReferenceId());
            descLabel.getStyleClass().add("tx-name");

            Label typeLabel = new Label("Transfer type: " + transferMode);
            typeLabel.getStyleClass().add("tx-type");

            card.getChildren().addAll(row, descLabel, typeLabel);
            recentTransfersBox.getChildren().add(card);
        }
    }

    private void clearForm() {
        toAccountField.clear();
        amountField.clear();
        descField.clear();
        pinField.clear();
        recipientLabel.setText("");
        if (feeLabel != null) { // Null check to prevent crashes
            feeLabel.setText("");
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

    /**
     * Show a confirmation dialog and return true if user clicks OK
     */
    private boolean showConfirmationDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Show a success dialog
     */
    private void showSuccessDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Show an error dialog
     */
    private void showErrorDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML private void onBack()         { go("dashboard"); }
    @FXML private void onHome()         { go("dashboard"); }
    @FXML private void onSendMoney()    { /* already here */ }
    @FXML private void onTransactions() { go("transactions"); }
    @FXML private void onShowAccount()  { go("account-panel"); }

    private void go(String s) {
        try { SceneHelper.switchTo(IgirePayApp.getPrimaryStage(), s); }
        catch (Exception e) { e.printStackTrace(); }
    }
}
