package com.igirepay.igirepaypaymentgateway.ui.controller;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * MainShellController â€” manages the persistent shell.
 *
 * The shell has a fixed top header and bottom nav that NEVER disappear.
 * Only the center StackPane (contentArea) is replaced when navigating.
 *
 * HOW TO USE FROM ANY CONTENT CONTROLLER:
 *   MainShellController.getInstance().showContent("dashboard-content");
 *
 * Content FXML files are named:  dashboard-content-view.fxml
 *                                 send-money-content-view.fxml  etc.
 */
public class MainShellController implements Initializable {

    // Singleton so any controller can call it
    private static MainShellController instance;
    public  static MainShellController getInstance() { return instance; }

    @FXML private StackPane contentArea;
    @FXML private Label notificationBadge;

    // Nav icon/label pairs for active state management
    @FXML private Label navHomeIcon;
    @FXML private Label navHomeLabel;
    @FXML private Label navSendIcon;
    @FXML private Label navSendLabel;
    @FXML private Label navHistoryIcon;
    @FXML private Label navHistoryLabel;
    @FXML private Label navSettingsIcon;
    @FXML private Label navSettingsLabel;

    private String currentTab = "home";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        showContent("dashboard-content");
        updateNotificationBadge();
    }

    /**
     * Update notification badge with unread count
     */
    public void updateNotificationBadge() {
        try {
            com.igirepay.igirepaypaymentgateway.ui.AppState state = 
                com.igirepay.igirepaypaymentgateway.ui.AppState.getInstance();
            com.igirepay.igirepaypaymentgateway.LAB2.model.Customer customer = state.getCurrentCustomer();
            
            if (customer != null && notificationBadge != null) {
                com.igirepay.igirepaypaymentgateway.LAB2.dao.NotificationDAO notificationDAO = 
                    new com.igirepay.igirepaypaymentgateway.LAB2.dao.NotificationDAO();
                int unreadCount = notificationDAO.countUnreadByCustomerId(customer.getId());
                
                if (unreadCount > 0) {
                    notificationBadge.setText(String.valueOf(unreadCount));
                    notificationBadge.setManaged(true);
                    notificationBadge.setVisible(true);
                } else {
                    notificationBadge.setManaged(false);
                    notificationBadge.setVisible(false);
                }
            }
        } catch (Exception e) {
            System.err.println("[MainShell] Error updating notification badge: " + e.getMessage());
        }
    }

    /**
     * Load a content FXML into the center area.
     * The top header and bottom nav stay untouched.
     *
     * @param name  e.g. "dashboard-content" loads "dashboard-content-view.fxml"
     */
    public void showContent(String name) {
        try {
            String path = "/com/igirepay/igirepaypaymentgateway/ui/"
                    + name + "-view.fxml";
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(path));
            Node content = loader.load();

            // Fill the center area â€” StackPane stretches to fill BorderPane center
            contentArea.getChildren().setAll(content);
            updateNotificationBadge();

        } catch (IOException e) {
            e.printStackTrace();
            // Show error in content area instead of crashing
            Label err = new Label("Error loading screen: " + name + "\n" + e.getMessage());
            err.setStyle("-fx-text-fill:red; -fx-padding:20;");
            contentArea.getChildren().setAll(err);
        }
    }

    @FXML private void onNavHome() {
        setActiveTab("home");
        showContent("dashboard-content");
    }

    @FXML private void onNavSend() {
        setActiveTab("send");
        showContent("send-money-content");
    }

    @FXML private void onNavHistory() {
        setActiveTab("history");
        showContent("transactions-content");
    }

    @FXML private void onNavSettings() {
        setActiveTab("settings");
        showContent("account-panel-content");
    }

    @FXML private void onAccount() {
        setActiveTab("settings");
        showContent("account-panel-content");
    }

    @FXML private void onNotifications() {
        System.out.println("[MainShell] Notifications button clicked");
        showContent("notifications-content");
        // Don't highlight any nav tab for notifications
        currentTab = "notifications";
    }

    /**
     * Update the nav bar to highlight the active tab.
     * Active = teal colour. Inactive = gray.
     */
    public void setActiveTab(String tab) {
        currentTab = tab;

        // Reset all to inactive
        setNavState(navHomeIcon,     navHomeLabel,     "ðŸ ", "Home",     false);
        setNavState(navSendIcon,     navSendLabel,     "âœˆ",  "Send",     false);
        setNavState(navHistoryIcon,  navHistoryLabel,  "ðŸ“‹", "History",  false);
        setNavState(navSettingsIcon, navSettingsLabel, "âš™",  "Settings", false);
        switch (tab) {
            case "home"     -> setNavState(navHomeIcon,     navHomeLabel,     "ðŸ ", "Home",     true);
            case "send"     -> setNavState(navSendIcon,     navSendLabel,     "âœˆ",  "Send",     true);
            case "history"  -> setNavState(navHistoryIcon,  navHistoryLabel,  "ðŸ“‹", "History",  true);
            case "settings" -> setNavState(navSettingsIcon, navSettingsLabel, "âš™",  "Settings", true);
        }
    }

    private void setNavState(Label icon, Label label,
                             String iconText, String labelText, boolean active) {
        icon.setText(iconText);
        label.setText(labelText);
        if (active) {
            icon.getStyleClass().setAll("nav-btn-icon-active");
            label.getStyleClass().setAll("nav-btn-label-active");
        } else {
            icon.getStyleClass().setAll("nav-btn-icon");
            label.getStyleClass().setAll("nav-btn-label");
        }
    }
}
