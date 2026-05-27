package com.igirepay.igirepaypaymentgateway.ui.controller;

import com.igirepay.igirepaypaymentgateway.LAB2.model.Account;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Loan;
import com.igirepay.igirepaypaymentgateway.LAB3.exception.IgirePayException;
import com.igirepay.igirepaypaymentgateway.ui.AppState;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * LoansContentController â€” Customer loan management interface
 */
public class LoansContentController implements Initializable {

    @FXML private VBox loansBox;
    @FXML private Button filterAllBtn;
    @FXML private Button filterActiveBtn;
    @FXML private Button filterPendingBtn;
    @FXML private Button filterPaidBtn;

    private final AppState state = AppState.getInstance();
    private List<Loan> allLoans;
    private String currentFilter = "ALL";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("[Loans] INITIALIZE CALLED");
        
        Customer customer = state.getCurrentCustomer();
        if (customer == null) {
            showError("Please login first");
            return;
        }

        System.out.println("[Loans] âœ“ Customer loaded: " + customer.getFullName());
        loadLoans();
    }

    private void loadLoans() {
        Customer customer = state.getCurrentCustomer();
        if (customer == null) return;

        allLoans = state.getLoanService().getCustomerLoans(customer.getId());
        System.out.println("[Loans] Loaded " + allLoans.size() + " loans");
        applyFilter();
    }

    private void applyFilter() {
        List<Loan> filtered;
        
        switch (currentFilter) {
            case "ACTIVE":
                filtered = allLoans.stream()
                    .filter(l -> Loan.STATUS_ACTIVE.equals(l.getStatus()) || 
                                Loan.STATUS_OVERDUE.equals(l.getStatus()))
                    .collect(Collectors.toList());
                break;
            case "PENDING":
                filtered = allLoans.stream()
                    .filter(l -> Loan.STATUS_PENDING.equals(l.getStatus()))
                    .collect(Collectors.toList());
                break;
            case "PAID":
                filtered = allLoans.stream()
                    .filter(l -> Loan.STATUS_PAID.equals(l.getStatus()))
                    .collect(Collectors.toList());
                break;
            default: // ALL
                filtered = allLoans;
        }

        displayLoans(filtered);
        updateFilterButtons();
    }

    private void displayLoans(List<Loan> loans) {
        loansBox.getChildren().clear();

        if (loans.isEmpty()) {
            Label empty = new Label(getEmptyMessage());
            empty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 16px; -fx-padding: 40;");
            loansBox.getChildren().add(empty);
            return;
        }

        for (Loan loan : loans) {
            loansBox.getChildren().add(createLoanCard(loan));
        }
    }

    private String getEmptyMessage() {
        return switch (currentFilter) {
            case "ACTIVE" -> "ðŸ“­ No active loans";
            case "PENDING" -> "ðŸ“­ No pending loan applications";
            case "PAID" -> "ðŸ“­ No paid loans";
            default -> "ðŸ“­ No loans yet. Apply for your first loan!";
        };
    }

    private VBox createLoanCard(Loan loan) {
        VBox card = new VBox(12);
        
        // Style based on status
        String bgColor = getStatusColor(loan.getStatus());
        String borderColor = getStatusBorderColor(loan.getStatus());
        
        card.setStyle("-fx-background-color: " + bgColor + "; " +
                     "-fx-border-color: " + borderColor + "; " +
                     "-fx-border-width: 2; " +
                     "-fx-border-radius: 12; -fx-background-radius: 12; " +
                     "-fx-padding: 16; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");

        // Header: Status badge + Loan ID
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label statusBadge = new Label(getStatusIcon(loan.getStatus()) + " " + loan.getStatus());
        statusBadge.setStyle(getStatusBadgeStyle(loan.getStatus()));

        Label idLabel = new Label("Loan #" + loan.getId());
        idLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px;");

        headerRow.getChildren().addAll(statusBadge, idLabel);

        // Amount info
        VBox amountBox = new VBox(6);
        Label principalLabel = new Label(String.format("Principal: %,.2f RWF", loan.getPrincipalAmount()));
        principalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        Label interestLabel = new Label(String.format("Interest: %.1f%% | Total: %,.2f RWF", 
                                                      loan.getInterestRate(), loan.getTotalAmount()));
        interestLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");

        amountBox.getChildren().addAll(principalLabel, interestLabel);

        // Progress bar for active loans
        if (Loan.STATUS_ACTIVE.equals(loan.getStatus()) || Loan.STATUS_OVERDUE.equals(loan.getStatus())) {
            double progress = (loan.getTotalAmount() - loan.getRemainingBalance()) / loan.getTotalAmount();
            ProgressBar progressBar = new ProgressBar(progress);
            progressBar.setPrefWidth(Double.MAX_VALUE);
            progressBar.setStyle("-fx-accent: #10B981;");
            
            Label remainingLabel = new Label(String.format("Remaining: %,.2f RWF (%.0f%% paid)", 
                                                          loan.getRemainingBalance(), progress * 100));
            remainingLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #059669; -fx-font-weight: bold;");
            
            amountBox.getChildren().addAll(progressBar, remainingLabel);
        }

        // Dates
        VBox datesBox = new VBox(4);
        if (loan.getApplicationDate() != null) {
            Label appDate = new Label("Applied: " + loan.getApplicationDate().format(DATE_FORMAT));
            appDate.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
            datesBox.getChildren().add(appDate);
        }
        if (loan.getDueDate() != null) {
            long daysUntilDue = loan.daysUntilDue();
            String dueDateText = "Due: " + loan.getDueDate().format(DATE_FORMAT);
            if (daysUntilDue > 0 && daysUntilDue <= 7) {
                dueDateText += " (âš ï¸ " + daysUntilDue + " days left!)";
            } else if (loan.isOverdue()) {
                dueDateText += " (ðŸš¨ " + loan.daysOverdue() + " days overdue!)";
            }
            Label dueDate = new Label(dueDateText);
            dueDate.setStyle("-fx-font-size: 12px; -fx-text-fill: " + 
                           (loan.isOverdue() ? "#DC2626" : "#6B7280") + "; -fx-font-weight: bold;");
            datesBox.getChildren().add(dueDate);
        }

        // Purpose
        if (loan.getPurpose() != null && !loan.getPurpose().isEmpty()) {
            Label purposeLabel = new Label("Purpose: " + loan.getPurpose());
            purposeLabel.setWrapText(true);
            purposeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4B5563; -fx-font-style: italic;");
            datesBox.getChildren().add(purposeLabel);
        }

        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        if (Loan.STATUS_ACTIVE.equals(loan.getStatus()) || Loan.STATUS_OVERDUE.equals(loan.getStatus())) {
            Button repayBtn = new Button("ðŸ’° Make Payment");
            repayBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; " +
                            "-fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
            repayBtn.setOnAction(e -> onRepayLoan(loan));
            buttonBox.getChildren().add(repayBtn);
        }

        Button detailsBtn = new Button("â„¹ï¸ Details");
        detailsBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; " +
                          "-fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        detailsBtn.setOnAction(e -> onViewDetails(loan));
        buttonBox.getChildren().add(detailsBtn);

        // Assemble card
        card.getChildren().addAll(headerRow, new Separator(), amountBox, datesBox, buttonBox);
        return card;
    }

    private String getStatusColor(String status) {
        return switch (status) {
            case Loan.STATUS_ACTIVE -> "#ECFDF5";
            case Loan.STATUS_PENDING -> "#FEF3C7";
            case Loan.STATUS_PAID -> "#DBEAFE";
            case Loan.STATUS_OVERDUE, Loan.STATUS_DEFAULTED -> "#FEE2E2";
            case Loan.STATUS_REJECTED -> "#F3F4F6";
            default -> "#FFFFFF";
        };
    }

    private String getStatusBorderColor(String status) {
        return switch (status) {
            case Loan.STATUS_ACTIVE -> "#10B981";
            case Loan.STATUS_PENDING -> "#F59E0B";
            case Loan.STATUS_PAID -> "#3B82F6";
            case Loan.STATUS_OVERDUE, Loan.STATUS_DEFAULTED -> "#EF4444";
            case Loan.STATUS_REJECTED -> "#6B7280";
            default -> "#E5E7EB";
        };
    }

    private String getStatusIcon(String status) {
        return switch (status) {
            case Loan.STATUS_ACTIVE -> "âœ…";
            case Loan.STATUS_PENDING -> "â³";
            case Loan.STATUS_PAID -> "ðŸŽ‰";
            case Loan.STATUS_OVERDUE -> "âš ï¸";
            case Loan.STATUS_DEFAULTED -> "ðŸš¨";
            case Loan.STATUS_REJECTED -> "âŒ";
            default -> "ðŸ“‹";
        };
    }

    private String getStatusBadgeStyle(String status) {
        String bgColor = switch (status) {
            case Loan.STATUS_ACTIVE -> "#D1FAE5";
            case Loan.STATUS_PENDING -> "#FEF3C7";
            case Loan.STATUS_PAID -> "#DBEAFE";
            case Loan.STATUS_OVERDUE, Loan.STATUS_DEFAULTED -> "#FEE2E2";
            case Loan.STATUS_REJECTED -> "#F3F4F6";
            default -> "#FFFFFF";
        };
        
        String textColor = switch (status) {
            case Loan.STATUS_ACTIVE -> "#065F46";
            case Loan.STATUS_PENDING -> "#92400E";
            case Loan.STATUS_PAID -> "#1E40AF";
            case Loan.STATUS_OVERDUE, Loan.STATUS_DEFAULTED -> "#991B1B";
            case Loan.STATUS_REJECTED -> "#374151";
            default -> "#000000";
        };
        
        return "-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor + "; " +
               "-fx-padding: 6 12; -fx-background-radius: 12; -fx-font-size: 12px; -fx-font-weight: bold;";
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // ACTIONS
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    @FXML
    private void onApplyForLoan() {
        Customer customer = state.getCurrentCustomer();
        if (customer == null) return;
        boolean hasActiveLoan = allLoans.stream()
            .anyMatch(l -> Loan.STATUS_ACTIVE.equals(l.getStatus()) || 
                          Loan.STATUS_PENDING.equals(l.getStatus()) ||
                          Loan.STATUS_OVERDUE.equals(l.getStatus()));

        if (hasActiveLoan) {
            showError("You already have an active loan. Please repay it before applying for a new one.");
            return;
        }
        List<Account> accounts = state.getPaymentService().getAccountsByCustomer(customer.getId());
        if (accounts.isEmpty()) {
            showError("You need at least one account to apply for a loan.");
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Apply for Loan");
        dialog.setHeaderText("ðŸ’° Loan Application");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label infoLabel = new Label("Loan Terms:\nâ€¢ Interest Rate: 10%\nâ€¢ Duration: 1 month\nâ€¢ Min: 10,000 RWF | Max: 5,000,000 RWF");
        infoLabel.setStyle("-fx-background-color: #EFF6FF; -fx-padding: 10; -fx-background-radius: 8; -fx-text-fill: #1E40AF;");

        Label amountLabel = new Label("Loan Amount (RWF):");
        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount (10,000 - 5,000,000)");

        Label accountLabel = new Label("Deposit to Account:");
        ComboBox<Account> accountCombo = new ComboBox<>();
        accountCombo.getItems().addAll(accounts);
        accountCombo.setPromptText("Select account");
        accountCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Account acc, boolean empty) {
                super.updateItem(acc, empty);
                setText(empty || acc == null ? null : 
                       acc.getAccountType() + " - " + String.format("%,.2f RWF", acc.getBalance()));
            }
        });
        accountCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Account acc, boolean empty) {
                super.updateItem(acc, empty);
                setText(empty || acc == null ? null : 
                       acc.getAccountType() + " - " + String.format("%,.2f RWF", acc.getBalance()));
            }
        });

        Label purposeLabel = new Label("Purpose:");
        TextArea purposeArea = new TextArea();
        purposeArea.setPromptText("Why do you need this loan?");
        purposeArea.setPrefRowCount(3);

        content.getChildren().addAll(infoLabel, amountLabel, amountField, 
                                     accountLabel, accountCombo, purposeLabel, purposeArea);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                Account account = accountCombo.getValue();
                String purpose = purposeArea.getText().trim();

                if (account == null) {
                    showError("Please select an account");
                    return;
                }

                if (purpose.isEmpty()) {
                    showError("Please provide a purpose for the loan");
                    return;
                }

                Loan loan = state.getLoanService().applyForLoan(customer.getId(), account.getId(), amount, purpose);
                showSuccess("Loan application submitted successfully!\n\nLoan ID: " + loan.getId() + 
                          "\nAmount: " + String.format("%,.2f RWF", amount) + 
                          "\n\nYour application is pending admin approval.");
                loadLoans();

            } catch (NumberFormatException e) {
                showError("Invalid amount. Please enter a valid number.");
            } catch (IgirePayException e) {
                showError(e.getMessage());
            }
        }
    }

    private void onRepayLoan(Loan loan) {
        Customer customer = state.getCurrentCustomer();
        if (customer == null) return;

        List<Account> accounts = state.getPaymentService().getAccountsByCustomer(customer.getId());
        if (accounts.isEmpty()) {
            showError("You need an account to make payments.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Repay Loan");
        dialog.setHeaderText("ðŸ’° Make Loan Payment");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label infoLabel = new Label(String.format("Loan #%d\nRemaining Balance: %,.2f RWF", 
                                                  loan.getId(), loan.getRemainingBalance()));
        infoLabel.setStyle("-fx-background-color: #ECFDF5; -fx-padding: 10; -fx-background-radius: 8; " +
                          "-fx-text-fill: #065F46; -fx-font-weight: bold; -fx-font-size: 14px;");

        Label amountLabel = new Label("Payment Amount (RWF):");
        TextField amountField = new TextField();
        amountField.setPromptText("Enter amount (max: " + String.format("%,.2f", loan.getRemainingBalance()) + ")");

        Button fullPaymentBtn = new Button("Pay Full Amount");
        fullPaymentBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white;");
        fullPaymentBtn.setOnAction(e -> amountField.setText(String.valueOf(loan.getRemainingBalance())));

        Label accountLabel = new Label("Pay from Account:");
        ComboBox<Account> accountCombo = new ComboBox<>();
        accountCombo.getItems().addAll(accounts);
        accountCombo.setPromptText("Select account");
        accountCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Account acc, boolean empty) {
                super.updateItem(acc, empty);
                setText(empty || acc == null ? null : 
                       acc.getAccountType() + " - Balance: " + String.format("%,.2f RWF", acc.getBalance()));
            }
        });
        accountCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Account acc, boolean empty) {
                super.updateItem(acc, empty);
                setText(empty || acc == null ? null : 
                       acc.getAccountType() + " - Balance: " + String.format("%,.2f RWF", acc.getBalance()));
            }
        });

        content.getChildren().addAll(infoLabel, amountLabel, amountField, fullPaymentBtn, accountLabel, accountCombo);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                Account account = accountCombo.getValue();

                if (account == null) {
                    showError("Please select an account");
                    return;
                }

                boolean success = state.getLoanService().repayLoan(loan.getId(), account.getId(), amount);
                if (success) {
                    showSuccess("Payment successful!\n\nAmount: " + String.format("%,.2f RWF", amount) + 
                              "\n\nThank you for your payment!");
                    loadLoans();
                }

            } catch (NumberFormatException e) {
                showError("Invalid amount. Please enter a valid number.");
            } catch (IgirePayException e) {
                showError(e.getMessage());
            }
        }
    }

    private void onViewDetails(Loan loan) {
        StringBuilder details = new StringBuilder();
        details.append("LOAN DETAILS\n");
        details.append("â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”\n\n");
        details.append("Loan ID: ").append(loan.getId()).append("\n");
        details.append("Status: ").append(getStatusIcon(loan.getStatus())).append(" ").append(loan.getStatus()).append("\n\n");
        
        details.append("FINANCIAL INFORMATION\n");
        details.append("â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”\n");
        details.append("Principal Amount: ").append(String.format("%,.2f RWF", loan.getPrincipalAmount())).append("\n");
        details.append("Interest Rate: ").append(String.format("%.1f%%", loan.getInterestRate())).append("\n");
        details.append("Total Amount: ").append(String.format("%,.2f RWF", loan.getTotalAmount())).append("\n");
        details.append("Amount Paid: ").append(String.format("%,.2f RWF", loan.getAmountPaid())).append("\n");
        details.append("Remaining Balance: ").append(String.format("%,.2f RWF", loan.getRemainingBalance())).append("\n");
        details.append("Duration: ").append(loan.getDurationMonths()).append(" month(s)\n\n");
        
        details.append("DATES\n");
        details.append("â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”\n");
        if (loan.getApplicationDate() != null) {
            details.append("Applied: ").append(loan.getApplicationDate().format(DATE_FORMAT)).append("\n");
        }
        if (loan.getApprovalDate() != null) {
            details.append("Approved: ").append(loan.getApprovalDate().format(DATE_FORMAT)).append("\n");
        }
        if (loan.getDisbursementDate() != null) {
            details.append("Disbursed: ").append(loan.getDisbursementDate().format(DATE_FORMAT)).append("\n");
        }
        if (loan.getDueDate() != null) {
            details.append("Due Date: ").append(loan.getDueDate().format(DATE_FORMAT));
            if (loan.isOverdue()) {
                details.append(" (").append(loan.daysOverdue()).append(" days overdue)");
            } else if (Loan.STATUS_ACTIVE.equals(loan.getStatus())) {
                details.append(" (").append(loan.daysUntilDue()).append(" days remaining)");
            }
            details.append("\n");
        }
        if (loan.getLastPaymentDate() != null) {
            details.append("Last Payment: ").append(loan.getLastPaymentDate().format(DATE_FORMAT)).append("\n");
        }
        
        if (loan.getPurpose() != null && !loan.getPurpose().isEmpty()) {
            details.append("\nPURPOSE\n");
            details.append("â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”\n");
            details.append(loan.getPurpose()).append("\n");
        }
        
        if (loan.getRejectionReason() != null && !loan.getRejectionReason().isEmpty()) {
            details.append("\nREJECTION REASON\n");
            details.append("â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”â”\n");
            details.append(loan.getRejectionReason()).append("\n");
        }

        showInfo("Loan Details", "Loan #" + loan.getId(), details.toString());
    }

    @FXML
    private void onRefresh() {
        System.out.println("[Loans] Refreshing...");
        loadLoans();
    }

    @FXML
    private void onFilterAll() {
        currentFilter = "ALL";
        applyFilter();
    }

    @FXML
    private void onFilterActive() {
        currentFilter = "ACTIVE";
        applyFilter();
    }

    @FXML
    private void onFilterPending() {
        currentFilter = "PENDING";
        applyFilter();
    }

    @FXML
    private void onFilterPaid() {
        currentFilter = "PAID";
        applyFilter();
    }

    private void updateFilterButtons() {
        filterAllBtn.getStyleClass().setAll("filter-btn");
        filterActiveBtn.getStyleClass().setAll("filter-btn");
        filterPendingBtn.getStyleClass().setAll("filter-btn");
        filterPaidBtn.getStyleClass().setAll("filter-btn");

        switch (currentFilter) {
            case "ACTIVE" -> filterActiveBtn.getStyleClass().setAll("filter-btn-active");
            case "PENDING" -> filterPendingBtn.getStyleClass().setAll("filter-btn-active");
            case "PAID" -> filterPaidBtn.getStyleClass().setAll("filter-btn-active");
            default -> filterAllBtn.getStyleClass().setAll("filter-btn-active");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Loan Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Success!");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.setResizable(true);
        alert.getDialogPane().setPrefWidth(600);
        alert.showAndWait();
    }
}
