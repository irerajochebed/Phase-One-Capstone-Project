package com.igirepay.igirepaypaymentgateway.ui.controller;

import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB3.exception.IgirePayException;
import com.igirepay.igirepaypaymentgateway.ui.AppState;
import com.igirepay.igirepaypaymentgateway.ui.IgirePayApp;
import com.igirepay.igirepaypaymentgateway.ui.SceneHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML private TextField     nameField;
    @FXML private TextField     emailField;
    @FXML private TextField     phoneField;
    @FXML private PasswordField pinField;
    @FXML private PasswordField confirmPinField;

    @FXML
    private void onRegister() {
        String name    = nameField.getText().trim();
        String email   = emailField.getText().trim();
        String phone   = phoneField.getText().trim();
        String pin     = pinField.getText().trim();
        String confirm = confirmPinField.getText().trim();
        if (name.isEmpty()) {
            showErrorDialog("Missing Information", "Full name is required", 
                "Please enter your full name to continue.");
            nameField.requestFocus();
            return;
        }
        
        if (email.isEmpty()) {
            showErrorDialog("Missing Information", "Email is required", 
                "Please enter your email address to continue.");
            emailField.requestFocus();
            return;
        }
        
        if (phone.isEmpty()) {
            showErrorDialog("Missing Information", "Phone number is required", 
                "Please enter your phone number to continue.");
            phoneField.requestFocus();
            return;
        }
        
        if (pin.isEmpty()) {
            showErrorDialog("Missing Information", "PIN is required", 
                "Please create a 4-digit PIN to secure your account.");
            pinField.requestFocus();
            return;
        }
        
        if (!pin.matches("\\d{4}")) {
            showErrorDialog("Invalid PIN", "PIN must be exactly 4 digits", 
                "Please enter a 4-digit number (e.g., 1234).\n\n" +
                "Your PIN will be used to:\n" +
                "â€¢ Login to your account\n" +
                "â€¢ Confirm transactions\n" +
                "â€¢ Access sensitive features");
            pinField.clear();
            confirmPinField.clear();
            pinField.requestFocus();
            return;
        }
        
        if (!pin.equals(confirm)) {
            showErrorDialog("PIN Mismatch", "PINs do not match", 
                "The PIN you entered in the confirmation field doesn't match.\n\n" +
                "Please make sure both PINs are identical.");
            confirmPinField.clear();
            confirmPinField.requestFocus();
            return;
        }

        try {
            Customer customer = AppState.getInstance()
                    .getPaymentService()
                    .registerCustomer(name, email, phone, pin);

            // Show success message
            showSuccessDialog("Account Created Successfully!", 
                "Welcome to IgirePay, " + name + "!", 
                "Your account has been created successfully.\n\n" +
                "You can now:\n" +
                "â€¢ Send and receive money\n" +
                "â€¢ Manage multiple accounts\n" +
                "â€¢ Track your transactions\n\n" +
                "Logging you in...");

            // Auto-login after registration
            AppState.getInstance().setCurrentCustomer(customer);
            AppState.getInstance().getAuthService().login(email, pin);

            SceneHelper.switchTo(IgirePayApp.getPrimaryStage(), "dashboard");

        } catch (IgirePayException e) {
            // Handle validation errors with specific, helpful messages
            switch (e.getErrorType()) {
                case INVALID_PHONE_NUMBER:
                    showErrorDialog("Invalid Phone Number", 
                        "Phone number format is incorrect", 
                        "Rwandan phone numbers must:\n\n" +
                        "âœ“ Be exactly 10 digits\n" +
                        "âœ“ Start with 078 or 079\n\n" +
                        "Examples:\n" +
                        "â€¢ 0781234567 âœ“\n" +
                        "â€¢ 0791234567 âœ“\n" +
                        "â€¢ 0721234567 âœ— (wrong prefix)\n" +
                        "â€¢ 078123456 âœ— (too short)\n\n" +
                        "Your phone: " + phone);
                    phoneField.requestFocus();
                    phoneField.selectAll();
                    break;
                    
                case INVALID_EMAIL:
                    showErrorDialog("Invalid Email Address", 
                        "Email format is incorrect", 
                        "A valid email address must:\n\n" +
                        "âœ“ Contain an @ symbol\n" +
                        "âœ“ Contain a dot (.)\n" +
                        "âœ“ Have @ before the dot\n\n" +
                        "Examples:\n" +
                        "â€¢ user@example.com âœ“\n" +
                        "â€¢ name@gmail.com âœ“\n" +
                        "â€¢ user@domain âœ— (missing dot)\n" +
                        "â€¢ userdomain.com âœ— (missing @)\n\n" +
                        "Your email: " + email);
                    emailField.requestFocus();
                    emailField.selectAll();
                    break;
                    
                case DATABASE_ERROR:
                    showErrorDialog("Account Already Exists", 
                        "This email or phone number is already registered", 
                        "An account with this information already exists.\n\n" +
                        "Options:\n" +
                        "â€¢ Try logging in instead\n" +
                        "â€¢ Use a different email or phone number\n" +
                        "â€¢ Contact support if you forgot your PIN\n\n" +
                        "Email: " + email + "\n" +
                        "Phone: " + phone);
                    break;
                    
                default:
                    showErrorDialog("Registration Error", 
                        "Unable to create account", 
                        e.getMessage());
            }
        } catch (Exception e) {
            showErrorDialog("Unexpected Error", 
                "Something went wrong", 
                "An unexpected error occurred:\n\n" + e.getMessage() + "\n\n" +
                "Please try again or contact support if the problem persists.");
        }
    }

    @FXML
    private void onGoLogin() {
        try {
            System.out.println("[RegisterController] Navigating to login...");
            clearForm();
            SceneHelper.switchTo(IgirePayApp.getPrimaryStage(), "login");
        } catch (Exception e) {
            System.err.println("[RegisterController] Navigation error: " + e.getMessage());
            showErrorDialog("Navigation Error", 
                "Cannot open login page", 
                "An error occurred while trying to navigate to the login page.");
        }
    }

    private void clearForm() {
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        pinField.clear();
        confirmPinField.clear();
    }

    /**
     * Show an error dialog with detailed information
     */
    private void showErrorDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
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
}
