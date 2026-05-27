package com.igirepay.igirepaypaymentgateway.ui.controller;

import com.igirepay.igirepaypaymentgateway.LAB3.exception.IgirePayException;
import com.igirepay.igirepaypaymentgateway.LAB3.util.AdminSetupUtil;
import com.igirepay.igirepaypaymentgateway.ui.IgirePayApp;
import com.igirepay.igirepaypaymentgateway.ui.SceneHelper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * AdminSetupController â€” Create the first ADMIN account.
 *
 * This screen is shown ONLY if no admin exists in the database.
 * Once an admin is created, users go to the login screen.
 */
public class AdminSetupController implements Initializable {

    @FXML private TextField     fullNameField;
    @FXML private TextField     emailField;
    @FXML private TextField     phoneField;
    @FXML private PasswordField pinField;
    @FXML private Label         errorLabel;
    @FXML private Button        createAdminBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("[AdminSetupController] Initializing...");
        errorLabel.setText("");
    }

    @FXML
    private void onCreateAdmin() {
        errorLabel.setText("");
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String pin = pinField.getText().trim();
        if (fullName.isEmpty()) {
            showError("Full name is required");
            return;
        }
        if (email.isEmpty()) {
            showError("Email is required");
            return;
        }
        if (phone.isEmpty()) {
            showError("Phone number is required");
            return;
        }
        if (pin.isEmpty()) {
            showError("PIN is required");
            return;
        }

        // Try to create admin
        try {
            var admin = AdminSetupUtil.createAdmin(fullName, email, phone, pin);
            
            System.out.println("[AdminSetupController] âœ“ Admin created: " + admin.getEmail());
            
            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Admin Account Created!");
            alert.setContentText("Admin account created successfully.\n\nYou can now login.");
            alert.showAndWait();

            // Navigate to login screen
            try {
                SceneHelper.switchTo(IgirePayApp.getPrimaryStage(), "login");
            } catch (Exception e) {
                System.err.println("[AdminSetupController] Error navigating to login: " + e.getMessage());
            }

        } catch (IgirePayException e) {
            System.err.println("[AdminSetupController] Error: " + e.getMessage());
            showError(e.getMessage());
        } catch (Exception e) {
            System.err.println("[AdminSetupController] Unexpected error: " + e.getMessage());
            showError("Unexpected error: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText("âœ— " + message);
        errorLabel.setStyle("-fx-text-fill: #d9534f;");
    }
}
