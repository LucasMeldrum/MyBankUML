package bank;

import java.util.*;
import java.io.*;

public class DatabaseManager {

    private static DatabaseManager instance;
    private final String CSV_FILE = "accounts.csv";
    private Map<String, Customer> customers;
    private DatabaseManager() {
        customers = loadCsv();

        // If CSV has no customers at all, load sample data
        if (customers.isEmpty()) {
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
    public Account getAccountByNumber(String accountNumber) {
        for (Customer customer : customers.values()) {
            for (Account acc : customer.getAccounts()) {
                if (acc.getAccountNumber().equals(accountNumber)) {
                    return acc;
                }
            }
        }
        return null;
    }

    public List<Account> retrieveAllAccounts() {
        List<Account> all = new ArrayList<>();

        for (Customer customer : customers.values()) {
            all.addAll(customer.getAccounts());
        }

        return all;
    }

    // Replace an account in the list
    public void updateAccount(Account updated) {
        for (Customer customer : customers.values()) {
            List<Account> list = customer.getAccounts();

            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getAccountNumber().equals(updated.getAccountNumber())) {
                    list.set(i, updated);
                    saveCsv();
                    return;
                }
            }
        }
    }

    // Add new account
    public void addAccount(String customerId, Account account) {

        Customer customer = customers.get(customerId);
        if (customer == null) {
            System.out.println("❌ Customer not found: " + customerId);
            return;
        }

        customer.addAccount(account);
        saveCsv();
    }

    // Load data from CSV
    public Map<String, Customer> loadCsv() {
        Map<String, Customer> customers = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader("accounts.csv"))) {

            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {

                // Skip header row
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                // Split CSV row
                String[] row = line.split(",");

                if (row.length < 7) {  // CHANGED: Now expecting 7 columns instead of 6
                    System.err.println("Invalid row skipped: " + line);
                    continue;
                }

                String customerId   = row[0].trim();
                String name         = row[1].trim();
                String password     = row[2].trim();
                String accountNumber = row[3].trim();
                AccountType type     = AccountType.valueOf(row[4].trim());
                double balance       = Double.parseDouble(row[5].trim());
                String status        = row[6].trim();  // NEW: Read status

                // Retrieve or create the customer
                Customer customer = customers.get(customerId);
                if (customer == null) {
                    customer = new Customer(Integer.parseInt(customerId), name, password);
                    customers.put(customerId, customer);
                }

                // Create the account
                Account account = switch (type) {
                    case CARD     -> new Card(customer, balance);
                    case CHECK    -> new Check(customer, balance);
                    case CHECKING -> new Checking(customer, balance);
                    case SAVING   -> new Saving(customer, balance);
                };

                account.setAccountNumber(accountNumber);
                account.setStatus(status);  // NEW: Set status from CSV

                // Add account to customer
                customer.addAccount(account);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return customers;
    }

    // Save data to CSV
    private void saveCsv() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {

            // Write correct header with status column
            writer.println("customerId,customerName,password,accountNumber,accountType,balance,status");

            // Loop through all customers and their accounts
            for (Customer c : customers.values()) {
                for (Account acc : c.getAccounts()) {

                    writer.println(String.format("%s,%s,%s,%s,%s,%.2f,%s",
                            c.getCustomerId(),
                            c.getName(),
                            c.getPassword(),
                            acc.getAccountNumber(),
                            acc.type.toString(),   // account type (CARD, CHECK, etc.)
                            acc.getBalance(),
                            acc.getStatus()        // NEW: Save status (ACTIVE or FROZEN)
                    ));
                }
            }

        } catch (IOException e) {
            System.out.println("Error writing CSV: " + e.getMessage());
        }
    }

    // Load sample data for testing
    private void loadSampleData() {
        System.out.println("Loading sample data...");

        // --- Create Customers ---
        Customer c1 = new Customer(1, "John Doe", "john123");
        Customer c2 = new Customer(2, "Alice Smith", "alice123");
        Customer c3 = new Customer(3, "Bob Johnson", "bob123");

        // --- Create Accounts for John ---
        Account j1 = new Card(c1, 500);
        j1.setAccountNumber("ACC101");

        Account j2 = new Checking(c1, 1200);
        j2.setAccountNumber("ACC102");

        Account j3 = new Saving(c1, 9000);
        j3.setAccountNumber("ACC103");

        // --- Create Accounts for Alice ---
        Account a1 = new Check(c2, 1500);
        a1.setAccountNumber("ACC201");

        // --- Create Accounts for Bob ---
        Account b1 = new Saving(c3, 3000);
        b1.setAccountNumber("ACC301");

        Account b2 = new Card(c3, 200);
        b2.setAccountNumber("ACC302");

        // Freeze one account for testing
        b1.freezeAccount();

        // --- Add accounts to each customer ---
        c1.addAccount(j1);
        c1.addAccount(j2);
        c1.addAccount(j3);

        c2.addAccount(a1);

        c3.addAccount(b1);
        c3.addAccount(b2);

        // --- ADD CUSTOMERS TO THE MAP ---
        customers.put("1", c1);
        customers.put("2", c2);
        customers.put("3", c3);

        // Save all to CSV
        saveCsv();

        System.out.println("Sample data loaded successfully.");
    }
    public String generateNextAccountNumber(Customer customer) {
        int max = 0;

        for (Account acc : customer.getAccounts()) {
            String num = acc.getAccountNumber().replace("ACC", "");
            int value = Integer.parseInt(num);
            if (value > max) max = value;
        }

        // If no accounts exist for customer, start at 101, 201, 301 based on customer ID.
        if (max == 0) {
            int base = customer.getCustomerId() * 100;
            return "ACC" + (base + 1);
        }

        // Otherwise, increment last account number
        return "ACC" + (max + 1);
    }
    public Customer getCustomer(String customerId) {
        return customers.get(customerId);
    }
    public String generateNextCustomerId() {
        int max = 0;
        for (String id : customers.keySet()) {
            int val = Integer.parseInt(id);
            if (val > max) max = val;
        }
        return "" + (max + 1);
    }
    public void addCustomer(Customer customer) {
        customers.put(customer.getCustomerId()+"", customer);
        saveCsv();
    }
    public Customer getCustomerByName(String name) {
        for (Customer c : customers.values()) {
            if (c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }
}
