package com.igirepay.igirepaypaymentgateway.ui.controller;

import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB3.exception.IgirePayException;
import com.igirepay.igirepaypaymentgateway.ui.AppState;
import com.igirepay.igirepaypaymentgateway.ui.IgirePayApp;
import com.igirepay.igirepaypaymentgateway.ui.SceneHelper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * LoginController  PIN-only login, just like MTN MoMo.
 *
 * The user picks their account from a dropdown (by name/email),
 * then enters only their 4-digit PIN. No password needed.
 */
public class LoginController implements Initializable {

    @FXML private ComboBox<String> accountCombo;
    @FXML private PasswordField    pinField;
    @FXML private Label            errorLabel;
    @FXML private Button           signInBtn;
    @FXML private ImageView        loginLogoView;

    private final AppState state = AppState.getInstance();
    private List<Customer> customers;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("[LoginController] Initializing...");
        try {
            File logoFile = new File("C:\\Users\\HP\\Downloads\\Igire_Rwanda_Logo.png");
            if (logoFile.exists()) {
                Image logoImage = new Image(logoFile.toURI().toString());
                loginLogoView.setImage(logoImage);
                System.out.println("[LoginController]  Logo loaded successfully");
            } else {
                System.out.println("[LoginController]  Logo file not found at: " + logoFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("[LoginController] Could not load logo: " + e.getMessage());
        }

        // Clear any previous error messages
        errorLabel.setText("");
        
        // Clear the PIN field
        pinField.clear();
        refreshCustomerList();

        // Allow pressing Enter in PIN field to trigger login
        pinField.setOnAction(e -> onLogin());
        
        System.out.println("[LoginController] Initialization complete");
    }

    /**
     * Refresh the customer list - called on initialize and when returning from registration
     */
    private void refreshCustomerList() {
        System.out.println("[LoginController] Refreshing customer list...");
        customers = state.getPaymentService().getAllCustomers();
        System.out.println("[LoginController] Found " + customers.size() + " customers");

        var items = FXCollections.<String>observableArrayList();
        for (Customer c : customers) {
            items.add(c.getFullName() + "  (" + c.getPhoneNumber() + ")");
        }

        if (items.isEmpty()) {
            items.add("No accounts yet  create one below");
        }

        accountCombo.setItems(items);
        accountCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void onLogin() {
        errorLabel.setText("");

        int selectedIdx = accountCombo.getSelectionModel().getSelectedIndex();
        if (selectedIdx < 0 || customers.isEmpty()) {
            showError("Please select an account.");
            return;
        }

        String pin = pinField.getText().trim();
        if (pin.isEmpty()) {
            showError("Please enter your PIN.");
            return;
        }

        Customer selected = customers.get(selectedIdx);
        
        System.out.println("[LoginController] Login attempt:");
        System.out.println("  - Email: " + selected.getEmail());
        System.out.println("  - Name: " + selected.getFullName());

        try {
            // Use email + PIN to authenticate
            Customer logged = state.getAuthService().login(selected.getEmail(), pin);
            
            System.out.println("[LoginController]  Login successful!");
            System.out.println("  - Customer ID: " + logged.getId());
            System.out.println("  - Name: " + logged.getFullName());
            System.out.println("  - Role: " + logged.getRole());
            System.out.println("  - isAdmin(): " + logged.isAdmin());
            
            state.setCurrentCustomer(logged);
            
            System.out.println("[LoginController] Customer set in AppState");
            System.out.println("  - AppState.getCurrentCustomer(): " + state.getCurrentCustomer());
            System.out.println("  - AppState customer isAdmin(): " + state.getCurrentCustomer().isAdmin());
            
            SceneHelper.switchTo(IgirePayApp.getPrimaryStage(), "dashboard");

        } catch (IgirePayException e) {
            System.err.println("[LoginController]  Login failed: " + e.getMessage());
            showError(e.getMessage());
            pinField.clear();
        } catch (Exception e) {
            System.err.println("[LoginController]  Connection error: " + e.getMessage());
            e.printStackTrace();
            showError("Connection error. Is PostgreSQL running?");
        }
    }

    @FXML
    private void onTogglePin() {
        // In a real app this would show/hide the PIN characters
        // For now just clear the hint
        pinField.requestFocus();
    }

    @FXML
    private void onForgotPin() {
        showError("Contact support to reset your PIN.");
    }

    @FXML
    private void onSwitchAccount() {
        // Reload the customer list and reset selection
        refreshCustomerList();
        pinField.clear();
        errorLabel.setText("");
    }

    @FXML
    private void onGoRegister() {
        try {
            SceneHelper.switchTo(IgirePayApp.getPrimaryStage(), "register");
        } catch (Exception e) {
            showError("Could not open registration.");
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }
}
