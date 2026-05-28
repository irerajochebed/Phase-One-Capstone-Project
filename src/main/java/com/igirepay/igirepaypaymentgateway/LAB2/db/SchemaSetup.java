package com.igirepay.igirepaypaymentgateway.LAB2.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


public class SchemaSetup {

    public static void createTables() {

        

        String createCustomers = """
                CREATE TABLE IF NOT EXISTS customers (
                    id                 SERIAL PRIMARY KEY,
                    full_name          VARCHAR(100) NOT NULL,
                    email              VARCHAR(100) UNIQUE NOT NULL,
                    phone_number       VARCHAR(20)  UNIQUE NOT NULL,
                    pin                VARCHAR(10)  NOT NULL,
                    role               VARCHAR(10)  NOT NULL DEFAULT 'USER',
                    failed_pin_attempts INT         NOT NULL DEFAULT 0,
                    is_locked          BOOLEAN      NOT NULL DEFAULT FALSE,
                    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;

        
        String createAccounts = """
                CREATE TABLE IF NOT EXISTS accounts (
                    id           SERIAL PRIMARY KEY,
                    customer_id  INT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
                    account_type VARCHAR(20)  NOT NULL,
                    balance      NUMERIC(15,2) NOT NULL DEFAULT 0.00,
                    currency     VARCHAR(10)  NOT NULL DEFAULT 'RWF',
                    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;

       
        String createTransactions = """
                CREATE TABLE IF NOT EXISTS transactions (
                    id               SERIAL PRIMARY KEY,
                    account_id       INT NOT NULL REFERENCES accounts(id),
                    reference_id     VARCHAR(100) NOT NULL,
                    transaction_type VARCHAR(20)  NOT NULL,
                    amount           NUMERIC(15,2) NOT NULL,
                    status           VARCHAR(20)  NOT NULL DEFAULT 'SUCCESS',
                    description      TEXT,
                    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;

        
        String createProcessedRequests = """
                CREATE TABLE IF NOT EXISTS processed_requests (
                    id           SERIAL PRIMARY KEY,
                    reference_id VARCHAR(100) UNIQUE NOT NULL,
                    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;

        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createCustomers);
            System.out.println("[SchemaSetup]  Table 'customers' ready.");

            stmt.execute(createAccounts);
            System.out.println("[SchemaSetup]  Table 'accounts' ready.");

            stmt.execute(createTransactions);
            System.out.println("[SchemaSetup]  Table 'transactions' ready.");

            stmt.execute(createProcessedRequests);
            System.out.println("[SchemaSetup]  Table 'processed_requests' ready.");

            
            String createNotifications = """
                CREATE TABLE IF NOT EXISTS notifications (
                    id           SERIAL PRIMARY KEY,
                    customer_id  INT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
                    type         VARCHAR(20)  NOT NULL,
                    title        VARCHAR(200) NOT NULL,
                    message      TEXT NOT NULL,
                    amount       NUMERIC(15,2) NOT NULL DEFAULT 0.00,
                    fee          NUMERIC(15,2) NOT NULL DEFAULT 0.00,
                    reference_id VARCHAR(100),
                    is_read      BOOLEAN NOT NULL DEFAULT FALSE,
                    sender_name  VARCHAR(100),
                    receiver_name VARCHAR(100),
                    action_required BOOLEAN NOT NULL DEFAULT FALSE,
                    action_type  VARCHAR(50),
                    related_transaction_id INT,
                    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;
            
            
            String createDisputes = """
                CREATE TABLE IF NOT EXISTS disputes (
                    id              SERIAL PRIMARY KEY,
                    customer_id     INT NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
                    transaction_id  INT NOT NULL REFERENCES transactions(id),
                    dispute_type    VARCHAR(50) NOT NULL,
                    description     TEXT NOT NULL,
                    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                    admin_notes     TEXT,
                    resolved_by     INT REFERENCES customers(id),
                    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    resolved_at     TIMESTAMP
                );
                """;
            
            
            String createFrozenTransactions = """
                CREATE TABLE IF NOT EXISTS frozen_transactions (
                    id              SERIAL PRIMARY KEY,
                    transaction_id  INT NOT NULL REFERENCES transactions(id),
                    dispute_id      INT REFERENCES disputes(id),
                    frozen_by       INT NOT NULL REFERENCES customers(id),
                    reason          TEXT NOT NULL,
                    status          VARCHAR(20) NOT NULL DEFAULT 'FROZEN',
                    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    unfrozen_at     TIMESTAMP
                );
                """;
            
            stmt.execute(createNotifications);
            System.out.println("[SchemaSetup]  Table 'notifications' ready.");

            stmt.execute(createDisputes);
            System.out.println("[SchemaSetup]  Table 'disputes' ready.");

            stmt.execute(createFrozenTransactions);
            System.out.println("[SchemaSetup]  Table 'frozen_transactions' ready.");
           
            stmt.execute("ALTER TABLE customers ADD COLUMN IF NOT EXISTS role VARCHAR(10) NOT NULL DEFAULT 'USER'");
            stmt.execute("ALTER TABLE customers ADD COLUMN IF NOT EXISTS failed_pin_attempts INT NOT NULL DEFAULT 0");
            stmt.execute("ALTER TABLE customers ADD COLUMN IF NOT EXISTS is_locked BOOLEAN NOT NULL DEFAULT FALSE");
            System.out.println("[SchemaSetup]  Customer security columns ready.");
            stmt.execute("ALTER TABLE accounts ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE");
            stmt.execute("ALTER TABLE accounts ADD COLUMN IF NOT EXISTS last_transaction_date TIMESTAMP");
            System.out.println("[SchemaSetup]  Account status columns ready.");
            stmt.execute("ALTER TABLE notifications ADD COLUMN IF NOT EXISTS sender_name VARCHAR(100)");
            stmt.execute("ALTER TABLE notifications ADD COLUMN IF NOT EXISTS receiver_name VARCHAR(100)");
            stmt.execute("ALTER TABLE notifications ADD COLUMN IF NOT EXISTS action_required BOOLEAN NOT NULL DEFAULT FALSE");
            stmt.execute("ALTER TABLE notifications ADD COLUMN IF NOT EXISTS action_type VARCHAR(50)");
            stmt.execute("ALTER TABLE notifications ADD COLUMN IF NOT EXISTS related_transaction_id INT");
            System.out.println("[SchemaSetup]  Notification enhancement columns ready.");

          String createLoans = """
                CREATE TABLE IF NOT EXISTS loans (
                    id SERIAL PRIMARY KEY,
                    customer_id INTEGER NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
                    account_id INTEGER NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
                    principal_amount DECIMAL(15,2) NOT NULL,
                    interest_rate DECIMAL(5,2) NOT NULL DEFAULT 10.0,
                    total_amount DECIMAL(15,2) NOT NULL,
                    amount_paid DECIMAL(15,2) NOT NULL DEFAULT 0,
                    remaining_balance DECIMAL(15,2) NOT NULL,
                    duration_months INTEGER NOT NULL DEFAULT 1,
                    application_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    approval_date TIMESTAMP,
                    disbursement_date TIMESTAMP,
                    due_date TIMESTAMP,
                    last_payment_date TIMESTAMP,
                    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                    purpose TEXT,
                    approved_by INTEGER REFERENCES customers(id),
                    rejection_reason TEXT,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
            """;
            
            stmt.execute(createLoans);
            System.out.println("[SchemaSetup]  Table 'loans' ready.");

           String createWithdrawalRequests = """
                CREATE TABLE IF NOT EXISTS withdrawal_requests (
                    id SERIAL PRIMARY KEY,
                    account_id INTEGER NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
                    customer_id INTEGER NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
                    amount DECIMAL(15,2) NOT NULL,
                    request_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    available_date TIMESTAMP NOT NULL,
                    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                    processed_date TIMESTAMP,
                    reference_id VARCHAR(100) UNIQUE,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
            """;
            
            stmt.execute(createWithdrawalRequests);
            System.out.println("[SchemaSetup]  Table 'withdrawal_requests' ready.");

            String addUniqueConstraint = """
                DO $$
                BEGIN
                    IF NOT EXISTS (
                        SELECT 1 FROM pg_constraint 
                        WHERE conname = 'unique_customer_account_type'
                    ) THEN
                        ALTER TABLE accounts 
                        ADD CONSTRAINT unique_customer_account_type 
                        UNIQUE (customer_id, account_type);
                    END IF;
                END $$;
            """;
            
            stmt.execute(addUniqueConstraint);
            System.out.println("[SchemaSetup]  Unique constraint 'unique_customer_account_type' ready.");

            System.out.println("[SchemaSetup] All tables created successfully.\n");

        } catch (SQLException e) {
            System.err.println("[SchemaSetup] ERROR creating tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
