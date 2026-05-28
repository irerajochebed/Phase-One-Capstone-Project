package com.igirepay.igirepaypaymentgateway.ui.controller;

import com.igirepay.igirepaypaymentgateway.LAB2.dao.CustomerDAO;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Loan;
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
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * AdminLoansController  Admin interface for managing loan applications
 */
public class AdminLoansController implements Initializable {

    @FXML private Label pendingLoansLabel;
    @FXML private Label activeLoansLabel;
    @FXML private Label overdueLoansLabel;
    @FXML private VBox loansBox;
    @FXML private Button filterPendingBtn;
    @FXML private Button filterActiveBtn;
    @FXML private Button filterOverdueBtn;
    @FXML private Button filterAllBtn;

    private final AppState state = AppState.getInstance();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private List<Loan> allLoans;
    private String currentFilter = "PENDING";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("[AdminLoans] INITIALIZE CALLED");
        
        Customer admin = state.getCurrentCustomer();
        if (admin == null || !admin.isAdmin()) {
            showError("Access Denied", "Admin privileges required");
            go("dashboard");
            return;
        }

        System.out.println("[AdminLoans]  Admin loaded: " + admin.getFullName());
        loadLoans();
        loadStatistics();
    }

    private void loadLoans() {
        allLoans = state.getLoanService().getAllLoans();
        System.out.println("[AdminLoans] Loaded " + allLoans.size() + " loans");
        applyFilter();
    }

    private void loadStatistics() {
        long pending = allLoans.stream().filter(l -> Loan.STATUS_PENDING.equals(l.getStatus())).count();
        long active = allLoans.stream().filter(l -> Loan.STATUS_ACTIVE.equals(l.getStatus())).count();
        long overdue = allLoans.stream().filter(l -> Loan.STATUS_OVERDUE.equals(l.getStatus()) || 
                                                     Loan.STATUS_DEFAULTED.equals(l.getStatus())).count();

        pendingLoansLabel.setText(String.valueOf(pending));
        activeLoansLabel.setText(String.valueOf(active));
        overdueLoansLabel.setText(String.valueOf(overdue));
    }

    private void applyFilter() {
        List<Loan> filtered = switch (currentFilter) {
            case "PENDING" -> allLoans.stream()
                .filter(l -> Loan.STATUS_PENDING.equals(l.getStatus()))
                .collect(Collectors.toList());
            case "ACTIVE" -> allLoans.stream()
                .filter(l -> Loan.STATUS_ACTIVE.equals(l.getStatus()))
                .collect(Collectors.toList());
            case "OVERDUE" -> allLoans.stream()
                .filter(l -> Loan.STATUS_OVERDUE.equals(l.getStatus()) || 
                            Loan.STATUS_DEFAULTED.equals(l.getStatus()))
                .collect(Collectors.toList());
            default -> allLoans;
        };

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
            case "PENDING" -> " No pending loan applications";
            case "ACTIVE" -> " No active loans";
            case "OVERDUE" -> " No overdue loans";
            default -> " No loans in the system";
        };
    }

    private VBox createLoanCard(Loan loan) {
        VBox card = new VBox(12);
        
        String bgColor = getStatusColor(loan.getStatus());
        String borderColor = getStatusBorderColor(loan.getStatus());
        
        card.setStyle("-fx-background-color: " + bgColor + "; " +
                     "-fx-border-color: " + borderColor + "; " +
                     "-fx-border-width: 2; " +
                     "-fx-border-radius: 12; -fx-background-radius: 12; " +
                     "-fx-padding: 16; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");

        // Header
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label statusBadge = new Label(getStatusIcon(loan.getStatus()) + " " + loan.getStatus());
        statusBadge.setStyle(getStatusBadgeStyle(loan.getStatus()));

        Label idLabel = new Label("Loan #" + loan.getId());
        idLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 12px; -fx-font-weight: bold;");

        headerRow.getChildren().addAll(statusBadge, idLabel);

        // Customer info
        Customer customer = customerDAO.findById(loan.getCustomerId());
        Label customerLabel = new Label(" " + (customer != null ? customer.getFullName() : "Unknown"));
        customerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        // Amount info
        VBox amountBox = new VBox(6);
        Label principalLabel = new Label(String.format("Principal: %,.2f RWF", loan.getPrincipalAmount()));
        principalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #059669;");

        Label totalLabel = new Label(String.format("Total to Repay: %,.2f RWF (%.1f%% interest)", 
                                                   loan.getTotalAmount(), loan.getInterestRate()));
        totalLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");

        if (!Loan.STATUS_PENDING.equals(loan.getStatus())) {
            Label remainingLabel = new Label(String.format("Remaining: %,.2f RWF", loan.getRemainingBalance()));
            remainingLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #DC2626; -fx-font-weight: bold;");
            amountBox.getChildren().addAll(principalLabel, totalLabel, remainingLabel);
        } else {
            amountBox.getChildren().addAll(principalLabel, totalLabel);
        }

        // Dates
        VBox datesBox = new VBox(4);
        if (loan.getApplicationDate() != null) {
            Label appDate = new Label(" Applied: " + loan.getApplicationDate().format(DATE_FORMAT));
            appDate.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
            datesBox.getChildren().add(appDate);
        }
        if (loan.getDueDate() != null) {
            String dueDateText = " Due: " + loan.getDueDate().format(DATE_FORMAT);
            if (loan.isOverdue()) {
                dueDateText += " ( " + loan.daysOverdue() + " days overdue!)";
            }
            Label dueDate = new Label(dueDateText);
            dueDate.setStyle("-fx-font-size: 12px; -fx-text-fill: " + 
                           (loan.isOverdue() ? "#DC2626" : "#6B7280") + "; -fx-font-weight: bold;");
            datesBox.getChildren().add(dueDate);
        }

        // Purpose
        if (loan.getPurpose() != null && !loan.getPurpose().isEmpty()) {
            Label purposeLabel = new Label(" Purpose: " + loan.getPurpose());
            purposeLabel.setWrapText(true);
            purposeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4B5563; -fx-font-style: italic;");
            datesBox.getChildren().add(purposeLabel);
        }

        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        if (Loan.STATUS_PENDING.equals(loan.getStatus())) {
            Button approveBtn = new Button(" Approve & Disburse");
            approveBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; " +
                              "-fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
            approveBtn.setOnAction(e -> onApproveLoan(loan));

            Button rejectBtn = new Button(" Reject");
            rejectBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; " +
                             "-fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
            rejectBtn.setOnAction(e -> onRejectLoan(loan));

            buttonBox.getChildren().addAll(approveBtn, rejectBtn);
        }

        Button detailsBtn = new Button(" View Details");
        detailsBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; " +
                          "-fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
        detailsBtn.setOnAction(e -> onViewDetails(loan));
        buttonBox.getChildren().add(detailsBtn);

        // Assemble card
        card.getChildren().addAll(headerRow, new Separator(), customerLabel, amountBox, datesBox, buttonBox);
        return card;
    }

    private String getStatusColor(String status) {
        return switch (status) {
            case Loan.STATUS_ACTIVE -> "#ECFDF5";
            case Loan.STATUS_PENDING -> "#FEF3C7";
            case Loan.STATUS_PAID -> "#DBEAFE";
            case Loan.STATUS_OVERDUE, Loan.STATUS_DEFAULTED -> "#FEE2E2";
            default -> "#FFFFFF";
        };
    }

    private String getStatusBorderColor(String status) {
        return switch (status) {
            case Loan.STATUS_ACTIVE -> "#10B981";
            case Loan.STATUS_PENDING -> "#F59E0B";
            case Loan.STATUS_PAID -> "#3B82F6";
            case Loan.STATUS_OVERDUE, Loan.STATUS_DEFAULTED -> "#EF4444";
            default -> "#E5E7EB";
        };
    }

    private String getStatusIcon(String status) {
        return switch (status) {
            case Loan.STATUS_ACTIVE -> "";
            case Loan.STATUS_PENDING -> "";
            case Loan.STATUS_PAID -> "";
            case Loan.STATUS_OVERDUE -> "";
            case Loan.STATUS_DEFAULTED -> "";
            default -> "";
        };
    }

    private String getStatusBadgeStyle(String status) {
        String bgColor = switch (status) {
            case Loan.STATUS_ACTIVE -> "#D1FAE5";
            case Loan.STATUS_PENDING -> "#FEF3C7";
            case Loan.STATUS_PAID -> "#DBEAFE";
            case Loan.STATUS_OVERDUE, Loan.STATUS_DEFAULTED -> "#FEE2E2";
            default -> "#FFFFFF";
        };
        
        String textColor = switch (status) {
            case Loan.STATUS_ACTIVE -> "#065F46";
            case Loan.STATUS_PENDING -> "#92400E";
            case Loan.STATUS_PAID -> "#1E40AF";
            case Loan.STATUS_OVERDUE, Loan.STATUS_DEFAULTED -> "#991B1B";
            default -> "#000000";
        };
        
        return "-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor + "; " +
               "-fx-padding: 6 12; -fx-background-radius: 12; -fx-font-size: 12px; -fx-font-weight: bold;";
    }

    // 
    // ACTIONS
    // 

    private void onApproveLoan(Loan loan) {
        Customer customer = customerDAO.findById(loan.getCustomerId());
        
        boolean confirmed = showConfirmation(
            "Approve Loan",
            "Approve loan for " + (customer != null ? customer.getFullName() : "Unknown") + "?",
            String.format("This will:\n" +
                         " Approve the loan application\n" +
                         " Disburse %,.2f RWF to customer's account\n" +
                         " Set due date to 1 month from now\n" +
                         " Send approval notification to customer\n\n" +
                         "Total to be repaid: %,.2f RWF (%.1f%% interest)",
                         loan.getPrincipalAmount(), loan.getTotalAmount(), loan.getInterestRate())
        );

        if (!confirmed) return;

        try {
            Customer admin = state.getCurrentCustomer();
            boolean success = state.getLoanService().approveLoan(loan.getId(), admin.getId());
            
            if (success) {
                showSuccess("Loan Approved!", 
                    String.format("Loan #%d has been approved and funds disbursed.\n\n" +
                                 " %,.2f RWF credited to customer account\n" +
                                 " Customer notified\n" +
                                 " Due date set to 1 month from now",
                                 loan.getId(), loan.getPrincipalAmount()));
                loadLoans();
                loadStatistics();
            }
        } catch (IgirePayException e) {
            showError("Approval Failed", e.getMessage());
        }
    }

    private void onRejectLoan(Loan loan) {
        Customer customer = customerDAO.findById(loan.getCustomerId());
        
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reject Loan");
        dialog.setHeaderText("Reject loan for " + (customer != null ? customer.getFullName() : "Unknown") + "?");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label infoLabel = new Label(String.format("Loan #%d - %,.2f RWF", loan.getId(), loan.getPrincipalAmount()));
        infoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label reasonLabel = new Label("Rejection Reason:");
        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Enter reason for rejection...");
        reasonArea.setPrefRowCount(4);

        content.getChildren().addAll(infoLabel, reasonLabel, reasonArea);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return reasonArea.getText().trim();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().isEmpty()) {
            try {
                boolean success = state.getLoanService().rejectLoan(loan.getId(), result.get());
                
                if (success) {
                    showSuccess("Loan Rejected", 
                        String.format("Loan #%d has been rejected.\n\n" +
                                     " Customer notified with reason\n" +
                                     " Application closed",
                                     loan.getId()));
                    loadLoans();
                    loadStatistics();
                }
            } catch (IgirePayException e) {
                showError("Rejection Failed", e.getMessage());
            }
        } else if (result.isPresent()) {
            showError("Invalid Input", "Please provide a rejection reason");
        }
    }

    private void onViewDetails(Loan loan) {
        Customer customer = customerDAO.findById(loan.getCustomerId());
        
        StringBuilder details = new StringBuilder();
        details.append("LOAN INFORMATION\n");
        details.append("\n\n");
        details.append("Loan ID: ").append(loan.getId()).append("\n");
        details.append("Status: ").append(getStatusIcon(loan.getStatus())).append(" ").append(loan.getStatus()).append("\n");
        details.append("Customer: ").append(customer != null ? customer.getFullName() : "Unknown").append("\n");
        if (customer != null) {
            details.append("Email: ").append(customer.getEmail()).append("\n");
            details.append("Phone: ").append(customer.getPhoneNumber()).append("\n");
        }
        details.append("\n");
        
        details.append("FINANCIAL DETAILS\n");
        details.append("\n");
        details.append("Principal: ").append(String.format("%,.2f RWF", loan.getPrincipalAmount())).append("\n");
        details.append("Interest Rate: ").append(String.format("%.1f%%", loan.getInterestRate())).append("\n");
        details.append("Total Amount: ").append(String.format("%,.2f RWF", loan.getTotalAmount())).append("\n");
        details.append("Amount Paid: ").append(String.format("%,.2f RWF", loan.getAmountPaid())).append("\n");
        details.append("Remaining: ").append(String.format("%,.2f RWF", loan.getRemainingBalance())).append("\n\n");
        
        details.append("TIMELINE\n");
        details.append("\n");
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
            }
            details.append("\n");
        }
        
        if (loan.getPurpose() != null && !loan.getPurpose().isEmpty()) {
            details.append("\nPURPOSE\n");
            details.append("\n");
            details.append(loan.getPurpose()).append("\n");
        }

        showInfo("Loan Details", "Loan #" + loan.getId(), details.toString());
    }

    @FXML
    private void onSendReminders() {
        boolean confirmed = showConfirmation(
            "Send Payment Reminders",
            "Send reminders to all borrowers?",
            "This will send payment reminder notifications to all customers with loans due within 7 days.\n\n" +
            "Are you sure you want to proceed?"
        );

        if (!confirmed) return;

        int count = state.getLoanService().sendPaymentReminders();
        showSuccess("Reminders Sent", 
            String.format(" Sent %d payment reminder%s\n\n" +
                         "Customers with loans due within 7 days have been notified.",
                         count, count == 1 ? "" : "s"));
    }

    @FXML
    private void onSendOverdueNotices() {
        boolean confirmed = showConfirmation(
            "Send Overdue Notices",
            "Send overdue notices?",
            "This will:\n" +
            " Send urgent notifications to all overdue borrowers\n" +
            " Update loan statuses to OVERDUE\n" +
            " Mark severely overdue loans (>7 days) as DEFAULTED\n\n" +
            "Are you sure you want to proceed?"
        );

        if (!confirmed) return;

        int count = state.getLoanService().sendOverdueNotices();
        showSuccess("Overdue Notices Sent", 
            String.format(" Sent %d overdue notice%s\n\n" +
                         "All overdue borrowers have been notified.",
                         count, count == 1 ? "" : "s"));
        loadLoans();
        loadStatistics();
    }

    @FXML
    private void onRefresh() {
        System.out.println("[AdminLoans] Refreshing...");
        loadLoans();
        loadStatistics();
    }

    @FXML
    private void onFilterPending() {
        currentFilter = "PENDING";
        applyFilter();
    }

    @FXML
    private void onFilterActive() {
        currentFilter = "ACTIVE";
        applyFilter();
    }

    @FXML
    private void onFilterOverdue() {
        currentFilter = "OVERDUE";
        applyFilter();
    }

    @FXML
    private void onFilterAll() {
        currentFilter = "ALL";
        applyFilter();
    }

    private void updateFilterButtons() {
        filterPendingBtn.getStyleClass().setAll("filter-btn");
        filterActiveBtn.getStyleClass().setAll("filter-btn");
        filterOverdueBtn.getStyleClass().setAll("filter-btn");
        filterAllBtn.getStyleClass().setAll("filter-btn");

        switch (currentFilter) {
            case "PENDING" -> filterPendingBtn.getStyleClass().setAll("filter-btn-active");
            case "ACTIVE" -> filterActiveBtn.getStyleClass().setAll("filter-btn-active");
            case "OVERDUE" -> filterOverdueBtn.getStyleClass().setAll("filter-btn-active");
            case "ALL" -> filterAllBtn.getStyleClass().setAll("filter-btn-active");
        }
    }

    // 
    // NAVIGATION
    // 

    @FXML private void onBack()         { go("admin-panel"); }
    @FXML private void onHome()         { go("dashboard"); }
    @FXML private void onTransactions() { go("transactions"); }
    @FXML private void onAdminPanel()   { go("admin-panel"); }
    @FXML private void onAccounts()     { go("accounts"); }

    private void go(String scene) {
        try {
            SceneHelper.switchTo(IgirePayApp.getPrimaryStage(), scene);
        } catch (Exception e) {
            System.err.println("[AdminLoans] Navigation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 
    // DIALOGS
    // 

    private boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void showSuccess(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
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
