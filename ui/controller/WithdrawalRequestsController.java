package com.igirepay.igirepaypaymentgateway.ui.controller;

import com.igirepay.igirepaypaymentgateway.LAB2.model.Account;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB2.model.WithdrawalRequest;
import com.igirepay.igirepaypaymentgateway.LAB3.exception.IgirePayException;
import com.igirepay.igirepaypaymentgateway.LAB3.service.WithdrawalService;
import com.igirepay.igirepaypaymentgateway.ui.AppState;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class WithdrawalRequestsController implements Initializable {

    @FXML private VBox requestsContainer;
    @FXML private VBox emptyState;
    @FXML private Label countLabel;
    @FXML private Button allBtn, pendingBtn, availableBtn;

    private final AppState state = AppState.getInstance();
    private final WithdrawalService withdrawalService = new WithdrawalService();
    private String currentFilter = "all";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadRequests("all");
    }

    @FXML
    private void onShowAll() {
        loadRequests("all");
    }

    @FXML
    private void onShowPending() {
        loadRequests("pending");
    }

    @FXML
    private void onShowAvailable() {
        loadRequests("available");
    }

    private void loadRequests(String filter) {
        currentFilter = filter;
        Customer customer = state.getCurrentCustomer();
        if (customer == null) return;
        allBtn.getStyleClass().removeAll("btn-primary", "btn-link");
        pendingBtn.getStyleClass().removeAll("btn-primary", "btn-link");
        availableBtn.getStyleClass().removeAll("btn-primary", "btn-link");

        switch (filter) {
            case "all" -> {
                allBtn.getStyleClass().add("btn-primary");
                pendingBtn.getStyleClass().add("btn-link");
                availableBtn.getStyleClass().add("btn-link");
            }
            case "pending" -> {
                allBtn.getStyleClass().add("btn-link");
                pendingBtn.getStyleClass().add("btn-primary");
                availableBtn.getStyleClass().add("btn-link");
            }
            case "available" -> {
                allBtn.getStyleClass().add("btn-link");
                pendingBtn.getStyleClass().add("btn-link");
                availableBtn.getStyleClass().add("btn-primary");
            }
        }
        List<WithdrawalRequest> requests = switch (filter) {
            case "pending" -> withdrawalService.getPendingWithdrawalRequests(customer.getId());
            case "available" -> withdrawalService.getAvailableWithdrawalRequests(customer.getId());
            default -> withdrawalService.getCustomerWithdrawalRequests(customer.getId());
        };

        displayRequests(requests);
    }

    private void displayRequests(List<WithdrawalRequest> requests) {
        requestsContainer.getChildren().clear();

        if (requests.isEmpty()) {
            requestsContainer.setVisible(false);
            emptyState.setVisible(true);
            countLabel.setText("No requests found");
            return;
        }

        requestsContainer.setVisible(true);
        emptyState.setVisible(false);
        countLabel.setText(requests.size() + " request(s) found");

        for (WithdrawalRequest request : requests) {
            requestsContainer.getChildren().add(createRequestCard(request));
        }
    }

    private VBox createRequestCard(WithdrawalRequest request) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color:white; -fx-border-color:#e0e0e0; " +
                     "-fx-border-radius:10; -fx-background-radius:10; -fx-padding:16;");

        // Header with amount and status
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label amountLabel = new Label(String.format("%,.2f RWF", request.getAmount()));
        amountLabel.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = new Label(request.getStatus());
        statusLabel.setStyle(getStatusStyle(request.getStatus()));

        header.getChildren().addAll(amountLabel, spacer, statusLabel);

        // Account info
        Account account = state.getPaymentService().getAccountsByCustomer(request.getCustomerId())
                .stream()
                .filter(a -> a.getId() == request.getAccountId())
                .findFirst()
                .orElse(null);

        Label accountLabel = new Label("From: " + (account != null ? account.getAccountType() : "Unknown") + " Account");
        accountLabel.setStyle("-fx-text-fill:#666;");

        // Reference
        Label refLabel = new Label("Reference: " + request.getReferenceId());
        refLabel.setStyle("-fx-text-fill:#666; -fx-font-size:12px;");

        // Dates
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Label requestDateLabel = new Label("Requested: " + request.getRequestDate().format(fmt));
        requestDateLabel.setStyle("-fx-text-fill:#666; -fx-font-size:12px;");

        Label availableDateLabel = new Label("Available: " + request.getAvailableDate().format(fmt));
        availableDateLabel.setStyle("-fx-text-fill:#666; -fx-font-size:12px;");

        // Time remaining or status message
        Label timeLabel = new Label();
        if (request.getStatus().equals(WithdrawalRequest.STATUS_PENDING)) {
            if (request.isAvailable()) {
                timeLabel.setText("âœ… Ready to process now!");
                timeLabel.setStyle("-fx-text-fill:#4CAF50; -fx-font-weight:bold;");
            } else {
                long hoursRemaining = request.getHoursRemaining();
                timeLabel.setText("â³ Available in " + hoursRemaining + " hours");
                timeLabel.setStyle("-fx-text-fill:#FF9800; -fx-font-weight:bold;");
            }
        } else if (request.getStatus().equals(WithdrawalRequest.STATUS_PROCESSED)) {
            timeLabel.setText("âœ“ Processed on " + 
                (request.getProcessedDate() != null ? request.getProcessedDate().format(fmt) : "N/A"));
            timeLabel.setStyle("-fx-text-fill:#4CAF50;");
        } else if (request.getStatus().equals(WithdrawalRequest.STATUS_CANCELLED)) {
            timeLabel.setText("âœ— Cancelled");
            timeLabel.setStyle("-fx-text-fill:#F44336;");
        }

        // Action buttons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        if (request.getStatus().equals(WithdrawalRequest.STATUS_PENDING)) {
            if (request.isAvailable()) {
                Button processBtn = new Button("Process Withdrawal");
                processBtn.getStyleClass().add("btn-primary");
                processBtn.setOnAction(e -> processRequest(request));
                actions.getChildren().add(processBtn);
            }

            Button cancelBtn = new Button("Cancel");
            cancelBtn.getStyleClass().add("btn-link");
            cancelBtn.setOnAction(e -> cancelRequest(request));
            actions.getChildren().add(cancelBtn);
        }

        card.getChildren().addAll(
            header,
            accountLabel,
            refLabel,
            requestDateLabel,
            availableDateLabel,
            timeLabel,
            actions
        );

        return card;
    }

    private String getStatusStyle(String status) {
        return switch (status) {
            case "PENDING" -> "-fx-background-color:#FFF3E0; -fx-text-fill:#F57C00; " +
                             "-fx-padding:4 12; -fx-background-radius:12; -fx-font-size:12px;";
            case "PROCESSED" -> "-fx-background-color:#E8F5E9; -fx-text-fill:#2E7D32; " +
                               "-fx-padding:4 12; -fx-background-radius:12; -fx-font-size:12px;";
            case "CANCELLED" -> "-fx-background-color:#FFEBEE; -fx-text-fill:#C62828; " +
                               "-fx-padding:4 12; -fx-background-radius:12; -fx-font-size:12px;";
            default -> "-fx-background-color:#F5F5F5; -fx-text-fill:#666; " +
                      "-fx-padding:4 12; -fx-background-radius:12; -fx-font-size:12px;";
        };
    }

    private void processRequest(WithdrawalRequest request) {
        try {
            // Confirm with user
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Process Withdrawal");
            confirm.setHeaderText("Confirm Withdrawal Processing");
            confirm.setContentText(
                "Amount: " + String.format("%,.2f", request.getAmount()) + " RWF\n" +
                "Reference: " + request.getReferenceId() + "\n\n" +
                "This will withdraw the money from your account.\n" +
                "Do you want to proceed?"
            );

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = withdrawalService.processWithdrawalRequest(
                    request.getId(),
                    "Savings withdrawal (48-hour period completed)"
                );

                if (success) {
                    Alert success_alert = new Alert(Alert.AlertType.INFORMATION);
                    success_alert.setTitle("Success");
                    success_alert.setHeaderText("Withdrawal Processed");
                    success_alert.setContentText(
                        "Your withdrawal has been processed successfully!\n\n" +
                        "Amount: " + String.format("%,.2f", request.getAmount()) + " RWF\n" +
                        "Reference: " + request.getReferenceId()
                    );
                    success_alert.showAndWait();

                    // Reload requests
                    loadRequests(currentFilter);
                }
            }

        } catch (IgirePayException e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Processing Failed");
            error.setHeaderText("Could not process withdrawal");
            error.setContentText(e.getMessage());
            error.showAndWait();
        }
    }

    private void cancelRequest(WithdrawalRequest request) {
        try {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Cancel Request");
            confirm.setHeaderText("Cancel Withdrawal Request");
            confirm.setContentText(
                "Are you sure you want to cancel this withdrawal request?\n\n" +
                "Amount: " + String.format("%,.2f", request.getAmount()) + " RWF\n" +
                "Reference: " + request.getReferenceId()
            );

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean success = withdrawalService.cancelWithdrawalRequest(
                    request.getId(),
                    state.getCurrentCustomer().getId()
                );

                if (success) {
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Cancelled");
                    info.setHeaderText("Request Cancelled");
                    info.setContentText("Your withdrawal request has been cancelled.");
                    info.showAndWait();

                    // Reload requests
                    loadRequests(currentFilter);
                }
            }

        } catch (IgirePayException e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Cancellation Failed");
            error.setHeaderText("Could not cancel request");
            error.setContentText(e.getMessage());
            error.showAndWait();
        }
    }
}
