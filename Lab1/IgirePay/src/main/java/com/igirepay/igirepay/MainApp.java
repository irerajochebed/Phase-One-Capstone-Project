package com.igirepay.igirepay;

// JavaFX imports

import com.igirepay.igirepay.model.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.time.LocalDate;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        Label label = new Label("IgirePay is running!");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 600, 400);
        stage.setTitle("IgirePay Payment Gateway");
        stage.setScene(scene);
        stage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}