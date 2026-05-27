package com.igirepay.igirepaypaymentgateway.ui.controller;

import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB3.exception.IgirePayException;
import com.igirepay.igirepaypaymentgateway.ui.AppState;
import com.igirepay.igirepaypaymentgateway.ui.IgirePayApp;
import com.igirepay.igirepaypaymentgateway.ui.SceneHelper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class AccountPanelController implements Initializable {

    @FXML private Label       userNameLabel;
    @FXML private Label       userPhoneLabel;
    @FXML private VBox        changePinBox;
    @FXML private PasswordField currentPinField;
    @FXML private PasswordField newPinField;
    @FXML private PasswordField confirmPinField;
    @FXML private Label       pinStatusLabel;

    private final AppState state = AppState.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Customer customer = state.getCurrentCustomer();
        if (customer == null) return;
        userNameLabel.setText(customer.getFullName());
        userPhoneLabel.setText(customer.getPhoneNumber());
    }

    @FXML private void onClose()        { nav("dashboard"); }
    @FXML private void onAccounts()     { nav("accounts"); }
    @FXML private void onTransactions() { nav("transactions"); }

    private void nav(String screen) {
        try {
            com.igirepay.igirepaypaymentgateway.ui.SceneHelper
                    .switchTo(com.igirepay.igirepaypaymentgateway.ui.IgirePayApp
                            .getPrimaryStage(), screen);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void onChangePin() {
        // Toggle the change PIN form
        boolean show = !changePinBox.isVisible();
        changePinBox.setVisible(show);
        changePinBox.setManaged(show);
    }

    @FXML
    private void onSavePin() {
        try {
            state.getAuthService().changePin(
                    currentPinField.getText().trim(),
                    newPinField.getText().trim(),
                    confirmPinField.getText().trim()
            );
            pinStatusLabel.setText("✓ PIN changed successfully.");
            pinStatusLabel.getStyleClass().removeAll("msg-error");
            if (!pinStatusLabel.getStyleClass().contains("msg-success"))
                pinStatusLabel.getStyleClass().add("msg-success");
            currentPinField.clear(); newPinField.clear(); confirmPinField.clear();
        } catch (IgirePayException e) {
            pinStatusLabel.setText(e.getMessage());
            pinStatusLabel.getStyleClass().removeAll("msg-success");
            if (!pinStatusLabel.getStyleClass().contains("msg-error"))
                pinStatusLabel.getStyleClass().add("msg-error");
        }
    }

    @FXML
    private void onSignOut() {
        state.getAuthService().logout();
        state.setCurrentCustomer(null);
        // Navigate to login — this replaces the whole scene (no shell)
        try {
            com.igirepay.igirepaypaymentgateway.ui.SceneHelper
                    .switchTo(com.igirepay.igirepaypaymentgateway.ui.IgirePayApp
                            .getPrimaryStage(), "login");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void go(String s) {
        try { SceneHelper.switchTo(IgirePayApp.getPrimaryStage(), s); }
        catch (Exception e) { e.printStackTrace(); }
    }
}
