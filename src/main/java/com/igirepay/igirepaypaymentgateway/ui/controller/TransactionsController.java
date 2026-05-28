package com.igirepay.igirepaypaymentgateway.ui.controller;

import com.igirepay.igirepaypaymentgateway.LAB2.model.Account;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Customer;
import com.igirepay.igirepaypaymentgateway.LAB2.model.Transaction;
import com.igirepay.igirepaypaymentgateway.LAB3.service.ReportService;
import com.igirepay.igirepaypaymentgateway.ui.AppState;
import com.igirepay.igirepaypaymentgateway.ui.IgirePayApp;
import com.igirepay.igirepaypaymentgateway.ui.SceneHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TransactionsController implements Initializable {

    @FXML private ComboBox<String>           accountFilterCombo;
    @FXML private ComboBox<String>           typeFilterCombo;
    @FXML private TextField                  keywordField;
    @FXML private TableView<TxRow>           txTable;
    @FXML private TableColumn<TxRow, String> colDate;
    @FXML private TableColumn<TxRow, String> colRef;
    @FXML private TableColumn<TxRow, String> colType;
    @FXML private TableColumn<TxRow, String> colAmount;
    @FXML private TableColumn<TxRow, String> colDesc;
    @FXML private TableColumn<TxRow, String> colStatus;
    @FXML private Label                      countLabel;

    private final AppState state = AppState.getInstance();
    private final ReportService reportService = new ReportService();
    private List<Account>  accounts;
    private List<Transaction> currentTransactions = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Customer customer = state.getCurrentCustomer();
        if (customer == null) return;

        // Account filter dropdown
        accounts = state.getPaymentService().getAccountsByCustomer(customer.getId());
        ObservableList<String> accItems = FXCollections.observableArrayList();
        accItems.add("All Accounts");
        for (Account a : accounts)
            accItems.add("[" + a.getId() + "] " + a.getAccountType());
        accountFilterCombo.setItems(accItems);
        accountFilterCombo.getSelectionModel().selectFirst();

        // Type filter dropdown
        typeFilterCombo.setItems(FXCollections.observableArrayList(
                "All Types", "DEPOSIT", "WITHDRAWAL", "TRANSFER"));
        typeFilterCombo.getSelectionModel().selectFirst();

        // Wire table columns  each name must match a getter in TxRow
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colRef.setCellValueFactory(new PropertyValueFactory<>("ref"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadTransactions(null, null, null);
    }

    @FXML
    private void onSearch() {
        int selAcc = accountFilterCombo.getSelectionModel().getSelectedIndex();
        Integer accountId = (selAcc > 0) ? accounts.get(selAcc - 1).getId() : null;

        int selType = typeFilterCombo.getSelectionModel().getSelectedIndex();
        String type = (selType > 0)
                ? typeFilterCombo.getSelectionModel().getSelectedItem() : null;

        String keyword = keywordField.getText().trim();
        if (keyword.isEmpty()) keyword = null;

        loadTransactions(accountId, type, keyword);
    }

    @FXML
    private void onClear() {
        accountFilterCombo.getSelectionModel().selectFirst();
        typeFilterCombo.getSelectionModel().selectFirst();
        keywordField.clear();
        loadTransactions(null, null, null);
    }

    private void loadTransactions(Integer accountId, String type, String keyword) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        ObservableList<TxRow> rows = FXCollections.observableArrayList();
        currentTransactions.clear(); // Clear previous transactions

        List<Account> targets = (accountId != null)
                ? accounts.stream().filter(a -> a.getId() == accountId).toList()
                : accounts;

        for (Account acc : targets) {
            List<Transaction> txns = state.getPaymentService()
                    .searchTransactions(acc.getId(), type, null, null, keyword);
            currentTransactions.addAll(txns); // Store for export
            for (Transaction t : txns) {
                rows.add(new TxRow(
                        t.getCreatedAt() != null ? t.getCreatedAt().format(fmt) : "-",
                        t.getReferenceId(),
                        t.getTransactionType(),
                        String.format("%,.2f", t.getAmount()),
                        t.getDescription() != null ? t.getDescription() : "",
                        t.getStatus()
                ));
            }
        }

        rows.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        txTable.setItems(rows);
        countLabel.setText(rows.size() + " transaction(s) found.");
    }

    @FXML
    private void onExportCsv() {
        if (currentTransactions.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Data");
            alert.setHeaderText(null);
            alert.setContentText("No transactions to export. Please search for transactions first.");
            alert.showAndWait();
            return;
        }

        // File chooser to let user select where to save
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Transactions to CSV");
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Customer customer = state.getCurrentCustomer();
        String defaultName = "transactions_" + customer.getFullName().replace(" ", "_") + "_" + date + ".csv";
        fileChooser.setInitialFileName(defaultName);
        FileChooser.ExtensionFilter extFilter = 
                new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);
        
        // Show save dialog
        File file = fileChooser.showSaveDialog(txTable.getScene().getWindow());
        
        if (file != null) {
            boolean success = reportService.exportTransactionsToCsv(currentTransactions, file.getAbsolutePath());
            
            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText(null);
                alert.setContentText("Transactions exported successfully to:\n" + file.getAbsolutePath() + 
                                   "\n\nYou can open this file in Excel or any spreadsheet application.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Export Failed");
                alert.setHeaderText(null);
                alert.setContentText("Failed to export transactions. Please try again.");
                alert.showAndWait();
            }
        }
    }
    public static class TxRow {
        private final String date, ref, type, amount, description, status;

        public TxRow(String date, String ref, String type,
                     String amount, String description, String status) {
            this.date = date; this.ref = ref; this.type = type;
            this.amount = amount; this.description = description; this.status = status;
        }

        public String getDate()        { return date; }
        public String getRef()         { return ref; }
        public String getType()        { return type; }
        public String getAmount()      { return amount; }
        public String getDescription() { return description; }
        public String getStatus()      { return status; }
    }
}
