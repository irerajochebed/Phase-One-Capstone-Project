package com.igirepay.igirepaypaymentgateway.ui;

import com.igirepay.igirepaypaymentgateway.ui.controller.MainShellController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class SceneHelper {

    public static final int W = 720;
    public static final int H = 860;

    private static final String CSS_PATH =
            "/com/igirepay/igirepaypaymentgateway/ui/styles.css";

    private static final java.util.Set<String> FULL_SCREENS =
            java.util.Set.of("login", "register", "admin-setup", "admin-panel", "admin-loans");

    private static final java.util.Map<String, String> CONTENT_MAP =
            java.util.Map.ofEntries(
                    java.util.Map.entry("dashboard", "dashboard-content"),
                    java.util.Map.entry("send-money", "send-money-content"),
                    java.util.Map.entry("deposit", "deposit-content"),
                    java.util.Map.entry("withdraw", "withdraw-content"),
                    java.util.Map.entry("transactions", "transactions-content"),
                    java.util.Map.entry("accounts", "accounts-content"),
                    java.util.Map.entry("account-panel", "account-panel-content"),
                    java.util.Map.entry("notifications", "notifications-content"),
                    java.util.Map.entry("loans", "loans-content")
            );

    public static void switchTo(Stage stage, String name) throws IOException {
        System.out.println("[SceneHelper] switchTo called: " + name);
        System.out.println("[SceneHelper] Current scene: " + (stage.getScene() != null ? "exists" : "null"));
        System.out.println("[SceneHelper] Is full screen? " + FULL_SCREENS.contains(name));
        if (FULL_SCREENS.contains(name)) {
            System.out.println("[SceneHelper] Loading as full-screen view");
            loadFullScreen(stage, name);
            return;
        }
        String contentName = CONTENT_MAP.getOrDefault(name, name + "-content");
        System.out.println("[SceneHelper] Content name: " + contentName);
        
        MainShellController shellController = MainShellController.getInstance();
        Scene currentScene = stage.getScene();
        boolean isShellActive = false;
        
        if (shellController != null && currentScene != null) {
            try {
                if (currentScene.getRoot() != null && 
                    currentScene.getRoot().lookup("#contentArea") != null) {
                    System.out.println("[SceneHelper] Shell is active on stage");
                    isShellActive = true;
                } else {
                    System.out.println("[SceneHelper] Shell exists but not active on stage (full-screen view showing)");
                }
            } catch (Exception e) {
                System.out.println("[SceneHelper] Error checking shell status: " + e.getMessage());
            }
        } else {
            System.out.println("[SceneHelper] Shell controller: " + (shellController != null ? "exists" : "null") + 
                             ", Current scene: " + (currentScene != null ? "exists" : "null"));
        }
        
        if (!isShellActive) {
            System.out.println("[SceneHelper] Shell not active, reloading shell and setting on stage");
            loadShell(stage, contentName);
            return;
        }

         System.out.println("[SceneHelper] Shell is active, swapping content");
        shellController.showContent(contentName);
        String tab = switch (name) {
            case "send-money"    -> "send";
            case "transactions"  -> "history";
            case "account-panel" -> "settings";
            case "accounts"      -> "settings";
            case "withdrawal-requests" -> "settings";
            default              -> "home";
        };
        shellController.setActiveTab(tab);
        System.out.println("[SceneHelper] Content swapped, tab set to: " + tab);
    }

    private static void loadFullScreen(Stage stage, String name) throws IOException {
        String path = "/com/igirepay/igirepaypaymentgateway/ui/" + name + "-view.fxml";
        FXMLLoader loader = new FXMLLoader(SceneHelper.class.getResource(path));
        Parent root = loader.load();

        Scene scene = new Scene(root, W, H);
        scene.getStylesheets().add(
                SceneHelper.class.getResource(CSS_PATH).toExternalForm());

        stage.setScene(scene);
        if (!stage.isShowing()) {
            stage.setWidth(W);
            stage.setHeight(H);
            stage.centerOnScreen();
        }
    }

    private static void loadShell(Stage stage, String initialContent) throws IOException {
        System.out.println("[SceneHelper] loadShell called with initialContent: " + initialContent);
        String path = "/com/igirepay/igirepaypaymentgateway/ui/main-shell-view.fxml";
        FXMLLoader loader = new FXMLLoader(SceneHelper.class.getResource(path));
        Parent root = loader.load();
        System.out.println("[SceneHelper] Shell FXML loaded");
        
        double w = stage.isShowing() ? stage.getScene().getWidth()  : W;
        double h = stage.isShowing() ? stage.getScene().getHeight() : H;

        Scene scene = new Scene(root, w, h);
        scene.getStylesheets().add(
                SceneHelper.class.getResource(CSS_PATH).toExternalForm());

        stage.setScene(scene);
        System.out.println("[SceneHelper] Shell scene set on stage");
        
        if (!stage.isShowing()) {
            stage.setWidth(W);
            stage.setHeight(H);
            stage.centerOnScreen();
        }
        if (!"dashboard-content".equals(initialContent)) {
            System.out.println("[SceneHelper] Loading non-default content: " + initialContent);
            MainShellController.getInstance().showContent(initialContent);
        }
        System.out.println("[SceneHelper] loadShell complete");
    }
}
