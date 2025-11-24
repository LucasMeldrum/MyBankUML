package bank;

import java.util.*;
import java.io.*;

public class DatabaseManager {

    private static DatabaseManager instance;
    private List<Account> accounts = new ArrayList<>();
    private final String CSV_FILE = "accounts.csv";

    private DatabaseManager() {
        loadCsv();
        // If CSV is empty or doesn't exist, load sample data
        if (accounts.isEmpty()) {
            loadSampleData();
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // Retrieve a single account by ID
    public Account getAccountNumber(String id) {
        for (Account acc : accounts) {
            if (acc.getAccountNumber() != null && acc.getAccountNumber().equals(id)) {
                return acc;
            }
        }
        return null;
    }

    // Return all accounts
    public List<Account> retrieveAllAccounts() {
        return new ArrayList<>(accounts);
    }

    // Replace an account in the list
    public void updateAccount(Account updated) {
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getAccountNumber().equals(updated.getAccountNumber())) {
                accounts.set(i, updated);
                saveCsv();
                return;
            }
        }
    }

    // Add new account
    public void addAccount(Account account) {
        accounts.add(account);
        saveCsv();
    }

    // Load data from CSV
    private void loadCsv() {
        File file = new File(CSV_FILE);
        if (!file.exists()) {
            System.out.println("CSV file not found. Starting with empty database.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }

                String[] values = line.split(",");
                if (values.length >= 6) {
                    String accountId = values[0];
                    String customerName = values[1];
                    String dob = values[2];
                    String accountType = values[3];
                    double balance = Double.parseDouble(values[4]);
                    String status = values[5];

                    Customer customer = new Customer(customerName, dob);
                    customer.setAccountNumber(accountId);

                    Account account = null;
                    switch (accountType.toUpperCase()) {
                        case "CARD":
                            account = new Card(customer, balance);
                            break;
                        case "CHECK":
                            account = new Check(customer, balance);
                            break;
                        case "SAVING":
                            account = new Saving(customer, balance);
                            break;
                    }

                    if (account != null) {
                        account.setAccountNumber(accountId);
                        account.setStatus(status);
                        accounts.add(account);
                    }
                }
            }
            System.out.println("Loaded " + accounts.size() + " accounts from CSV.");
        } catch (IOException e) {
            System.out.println("Error reading CSV: " + e.getMessage());
        }
    }

    // Save data to CSV
    private void saveCsv() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {
            writer.println("accountId,name,dob,type,balance,status");

            for (Account acc : accounts) {
                String type = acc.getClass().getSimpleName().toUpperCase();
                String dob = acc.getCustomer().getDateOfBirth() != null ?
                        acc.getCustomer().getDateOfBirth() : "N/A";

                writer.println(String.format("%s,%s,%s,%s,%.2f,%s",
                        acc.getAccountNumber(),
                        acc.getCustomer().getName(),
                        dob,
                        type,
                        acc.getBalance(),
                        acc.getStatus()
                ));
            }
        } catch (IOException e) {
            System.out.println("Error writing CSV: " + e.getMessage());
        }
    }

    // Load sample data for testing
    private void loadSampleData() {
        System.out.println("Loading sample data...");

        Customer c1 = new Customer("John Doe", "1998-04-12");
        Customer c2 = new Customer("Alice Smith", "2001-11-23");
        Customer c3 = new Customer("Bob Johnson", "1995-08-02");

        Account a1 = new Card(c1, 500);
        a1.setAccountNumber("A101");

        Account a2 = new Check(c2, 1500);
        a2.setAccountNumber("A102");

        Account a3 = new Saving(c3, 3000);
        a3.setAccountNumber("A103");
        a3.freezeAccount();

        accounts.add(a1);
        accounts.add(a2);
        accounts.add(a3);

        saveCsv();
        System.out.println("Sample data loaded successfully.");
    }
}