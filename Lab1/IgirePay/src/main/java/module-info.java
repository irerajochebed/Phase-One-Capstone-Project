module com.igirepay.igirepay {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.igirepay.igirepay to javafx.fxml;
    exports com.igirepay.igirepay;
    exports com.igirepay.igirepay.model;
    opens com.igirepay.igirepay.model to javafx.fxml;
}