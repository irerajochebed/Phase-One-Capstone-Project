module com.igirepay.igirepaypaymentgateway {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
   
    opens com.igirepay.igirepaypaymentgateway.ui            to javafx.fxml;
    opens com.igirepay.igirepaypaymentgateway.ui.controller to javafx.fxml;
    exports com.igirepay.igirepaypaymentgateway.LAB1;
    exports com.igirepay.igirepaypaymentgateway.LAB1.model;
    exports com.igirepay.igirepaypaymentgateway.LAB1.service;
    exports com.igirepay.igirepaypaymentgateway.LAB2;
    exports com.igirepay.igirepaypaymentgateway.LAB2.db;
    exports com.igirepay.igirepaypaymentgateway.LAB2.model;
    exports com.igirepay.igirepaypaymentgateway.LAB2.dao;
    exports com.igirepay.igirepaypaymentgateway.LAB2.service;
    exports com.igirepay.igirepaypaymentgateway.LAB2.util;
    exports com.igirepay.igirepaypaymentgateway.LAB3;
    exports com.igirepay.igirepaypaymentgateway.LAB3.exception;
    exports com.igirepay.igirepaypaymentgateway.LAB3.service;
    exports com.igirepay.igirepaypaymentgateway.LAB3.ui;
    exports com.igirepay.igirepaypaymentgateway.ui;
    exports com.igirepay.igirepaypaymentgateway.ui.controller;
}
