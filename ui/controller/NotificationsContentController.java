package com.igirepay.igirepaypaymentgateway.ui.controller;

import com.igirepay.igirepaypaymentgateway.LAB2.dao.NotificationDAO;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Notification;
import com.igirepay.igirepaypaymentgateway.ui.AppState;
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
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * NotificationsContentController â€” Displays user notifications
 */
public class NotificationsContentController implements Initializable {

    @FXML private VBox notificationsBox;
    @FXML private Button markAllReadBtn;
    @FXML private Button filterAllBtn;
    @FXML private Button filterUnreadBtn;
    @FXML private Button filterActionsBtn;

    private final AppState state = AppState.getInstance();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private List<Notification> allNotifications;
    private String currentFilter = "ALL";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•");
        System.out.println("[Notifications] INITIALIZE CALLED");
        System.out.println("â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•");
        
        Customer customer = state.getCurrentCustomer();
        if (customer == null) {
            System.err.println("[Notifications] ERROR: No customer in session");
            showError("Please login first");
            return;
        }

        System.out.println("[Notifications] âœ“ Customer loaded: " + customer.getFullName());
        loadNotifications();
    }

    private void loadNotifications() {
        Customer customer = state.getCurrentCustomer();
        if (customer == null) return;

        allNotifications = notificationDAO.findByCustomerId(customer.getId());
        System.out.println("[Notifications] Loaded " + allNotifications.size() + " notifications");
        
        applyFilter();
    }

    private void applyFilter() {
        List<Notification> filtered;
        
        switch (currentFilter) {
            case "UNREAD":
                filtered = allNotifications.stream()
                    .filter(n -> !n.isRead())
                    .collect(Collectors.toList());
                break;
            case "ACTIONS":
                filtered = allNotifications.stream()
                    .filter(Notification::isActionRequired)
                    .collect(Collectors.toList());
                break;
            default: // ALL
                filtered = allNotifications;
        }

        displayNotifications(filtered);
        updateFilterButtons();
    }

    private void displayNotifications(List<Notification> notifications) {
        notificationsBox.getChildren().clear();

        if (notifications.isEmpty()) {
            Label empty = new Label(getEmptyMessage());
            empty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 16px; -fx-padding: 40;");
            notificationsBox.getChildren().add(empty);
            return;
        }

        for (Notification notification : notifications) {
            notificationsBox.getChildren().add(createNotificationCard(notification));
        }
    }

    private String getEmptyMessage() {
        return switch (currentFilter) {
            case "UNREAD" -> "ðŸ“­ No unread notifications";
            case "ACTIONS" -> "âœ… No actions required";
            default -> "ðŸ“­ No notifications yet";
        };
    }

    private VBox createNotificationCard(Notification notification) {
        VBox card = new VBox(10);
        
        // Style based on read status
        String bgColor = notification.isRead() ? "#F9FAFB" : "#EFF6FF";
        String borderColor = notification.isRead() ? "#E5E7EB" : "#3B82F6";
        
        card.setStyle("-fx-background-color: " + bgColor + "; " +
                     "-fx-border-color: " + borderColor + "; " +
                     "-fx-border-width: " + (notification.isRead() ? "1" : "2") + "; " +
                     "-fx-border-radius: 12; -fx-background-radius: 12; " +
                     "-fx-padding: 16; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");

        // Header row: Icon + Title + Time
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(getNotificationIcon(notification.getType()));
        iconLabel.setStyle("-fx-font-size: 24px;");

        VBox titleBox = new VBox(4);
        Label titleLabel = new Label(notification.getTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");
        
        Label timeLabel = new Label(notification.getCreatedAt().format(DATE_FORMAT));
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF;");
        
        titleBox.getChildren().addAll(titleLabel, timeLabel);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        // Unread badge
        if (!notification.isRead()) {
            Label unreadBadge = new Label("NEW");
            unreadBadge.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; " +
                               "-fx-padding: 4 10; -fx-background-radius: 10; " +
                               "-fx-font-size: 10px; -fx-font-weight: bold;");
            headerRow.getChildren().addAll(iconLabel, titleBox, unreadBadge);
        } else {
            headerRow.getChildren().addAll(iconLabel, titleBox);
        }

        // Message
        Label messageLabel = new Label(notification.getMessage());
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 14px;");

        // Amount (if applicable)
        VBox detailsBox = new VBox(6);
        if (notification.getAmount() > 0) {
            Label amountLabel = new Label(String.format("Amount: %,.2f RWF", notification.getAmount()));
            amountLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #059669;");
            detailsBox.getChildren().add(amountLabel);
            
            if (notification.getFee() > 0) {
                Label feeLabel = new Label(String.format("Fee: %,.2f RWF", notification.getFee()));
                feeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
                detailsBox.getChildren().add(feeLabel);
            }
        }

        // Sender/Receiver info
        if (notification.getSenderName() != null || notification.getReceiverName() != null) {
            HBox namesBox = new HBox(15);
            if (notification.getSenderName() != null) {
                Label senderLabel = new Label("From: " + notification.getSenderName());
                senderLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
                namesBox.getChildren().add(senderLabel);
            }
            if (notification.getReceiverName() != null) {
                Label receiverLabel = new Label("To: " + notification.getReceiverName());
                receiverLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
                namesBox.getChildren().add(receiverLabel);
            }
            detailsBox.getChildren().add(namesBox);
        }

        // Reference ID
        if (notification.getReferenceId() != null && !notification.getReferenceId().isEmpty()) {
            Label refLabel = new Label("Ref: " + notification.getReferenceId());
            refLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF; -fx-font-family: monospace;");
            detailsBox.getChildren().add(refLabel);
        }

        // Action required badge
        if (notification.isActionRequired()) {
            Label actionBadge = new Label("âš ï¸ ACTION REQUIRED: " + formatActionType(notification.getActionType()));
            actionBadge.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; " +
                               "-fx-padding: 8 12; -fx-background-radius: 8; " +
                               "-fx-font-size: 12px; -fx-font-weight: bold;");
            detailsBox.getChildren().add(actionBadge);
        }

        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        if (!notification.isRead()) {
            Button markReadBtn = new Button("âœ“ Mark as Read");
            markReadBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; " +
                               "-fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand;");
            markReadBtn.setOnAction(e -> onMarkAsRead(notification));
            buttonBox.getChildren().add(markReadBtn);
        }

        Button deleteBtn = new Button("ðŸ—‘ Delete");
        deleteBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; " +
                         "-fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> onDeleteNotification(notification));
        buttonBox.getChildren().add(deleteBtn);

        // Assemble card
        card.getChildren().addAll(headerRow, new Separator(), messageLabel);
        if (!detailsBox.getChildren().isEmpty()) {
            card.getChildren().add(detailsBox);
        }
        if (!buttonBox.getChildren().isEmpty()) {
            card.getChildren().add(buttonBox);
        }

        return card;
    }

    private String getNotificationIcon(String type) {
        return switch (type) {
            case Notification.TYPE_SENT -> "ðŸ“¤";
            case Notification.TYPE_RECEIVED -> "ðŸ“¥";
            case Notification.TYPE_DEPOSIT -> "ðŸ’°";
            case Notification.TYPE_WITHDRAWAL -> "ðŸ’¸";
            case Notification.TYPE_ADMIN_MESSAGE -> "ðŸ‘‘";
            case Notification.TYPE_PASSWORD_RESET -> "ðŸ”‘";
            case Notification.TYPE_DISPUTE_UPDATE -> "âš–ï¸";
            case Notification.TYPE_ACCOUNT_UNLOCKED -> "ðŸ”“";
            case Notification.TYPE_FROZEN_FUNDS -> "â„ï¸";
            default -> "ðŸ””";
        };
    }

    private String formatActionType(String actionType) {
        if (actionType == null) return "";
        return actionType.replace("_", " ");
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // ACTIONS
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    private void onMarkAsRead(Notification notification) {
        boolean success = notificationDAO.markAsRead(notification.getId());
        if (success) {
            System.out.println("[Notifications] Marked notification " + notification.getId() + " as read");
            loadNotifications();
        }
    }

    private void onDeleteNotification(Notification notification) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Notification");
        confirm.setHeaderText("Delete this notification?");
        confirm.setContentText("This action cannot be undone.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = notificationDAO.deleteNotification(notification.getId());
                if (success) {
                    System.out.println("[Notifications] Deleted notification " + notification.getId());
                    loadNotifications();
                }
            }
        });
    }

    @FXML
    private void onMarkAllRead() {
        Customer customer = state.getCurrentCustomer();
        if (customer == null) return;

        boolean success = notificationDAO.markAllAsRead(customer.getId());
        if (success) {
            System.out.println("[Notifications] Marked all notifications as read");
            loadNotifications();
        }
    }

    @FXML
    private void onRefresh() {
        System.out.println("[Notifications] Refreshing...");
        loadNotifications();
    }

    @FXML
    private void onFilterAll() {
        currentFilter = "ALL";
        applyFilter();
    }

    @FXML
    private void onFilterUnread() {
        currentFilter = "UNREAD";
        applyFilter();
    }

    @FXML
    private void onFilterActions() {
        currentFilter = "ACTIONS";
        applyFilter();
    }

    private void updateFilterButtons() {
        // Reset all to inactive
        filterAllBtn.getStyleClass().setAll("filter-btn");
        filterUnreadBtn.getStyleClass().setAll("filter-btn");
        filterActionsBtn.getStyleClass().setAll("filter-btn");
        switch (currentFilter) {
            case "UNREAD" -> filterUnreadBtn.getStyleClass().setAll("filter-btn-active");
            case "ACTIONS" -> filterActionsBtn.getStyleClass().setAll("filter-btn-active");
            default -> filterAllBtn.getStyleClass().setAll("filter-btn-active");
        }
    }

    private void showError(String message) {
        Label error = new Label("âŒ " + message);
        error.setStyle("-fx-text-fill: #DC2626; -fx-font-size: 16px; -fx-padding: 40;");
        notificationsBox.getChildren().setAll(error);
    }
}
