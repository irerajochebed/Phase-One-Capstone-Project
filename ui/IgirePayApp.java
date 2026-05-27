package com.igirepay.igirepaypaymentgateway.ui;

import com.igirepay.igirepaypaymentgateway.LAB2.db.SchemaSetup;
import com.igirepay.igirepaypaymentgateway.LAB3.util.AdminSetupUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class IgirePayApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        SchemaSetup.createTables();

        stage.setTitle("IgirePay â€” Digital Wallet");
        stage.setResizable(true);   
        stage.setMinWidth(480);     
        stage.setMinHeight(600);
        if (!AdminSetupUtil.adminExists()) {
            
            SceneHelper.switchTo(stage, "admin-setup");
        } else {
            SceneHelper.switchTo(stage, "login");
        }
        
        stage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
