package com.igirepay.igirepaypaymentgateway.LAB2.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConnection {
    private static final String URL      = "jdbc:postgresql://localhost:5432/igirepay_payment_gateway";
    private static final String USER     = "postgres";
    private static final String PASSWORD = "1234";

    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
