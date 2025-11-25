package bank;

import java.util.List;
import java.util.Scanner;

public class BankingSystem {
    private final String ADMIN_USER = "admin";
    private final String ADMIN_PASS = "admin";
    private DatabaseManager db;
    private LoginManager loginManager;
    private Teller currentTeller;
    private Customer currentCustomer;
    private boolean isRunning;
    private Scanner scanner;

    public BankingSystem() {
        this.db = DatabaseManager.getInstance();
        this.loginManager = new LoginManager(20);
        this.isRunning = true;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   Welcome to the Banking System        ║");
        System.out.println("╚════════════════════════════════════════╝");

        while (isRunning) {
            showMainMenu();
        }

        scanner.close();
    }

    private void showMainMenu() {
        System.out.println("======Main Menu=====");
                System.out.println("1. Teller Login");
        System.out.println("2. Customer Login");
        System.out.println("3. Sys Admin");
        System.out.println("4. Exit");
        System.out.print("Choose an option: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                tellerLogin();
                break;
            case "2":
                customerLogin();
                break;
            case "3":
                admin();
                break;
            case "4":
                quit();
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void tellerLogin() {
        System.out.println("\n════ TELLER LOGIN ════");

        System.out.print("Teller ID or Email: ");
        String user = scanner.nextLine().trim();

        System.out.print("Password: ");
        String pass = scanner.nextLine().trim();

        Teller teller = TellerDatabaseManager.getInstance().authenticate(user, pass);

        boolean correct = (teller != null);

        if (!loginManager.loginAttempt(correct)) {
            return;
        }

        currentTeller = teller;
        loginManager.refreshSession();

        System.out.println("✓ Welcome, " + teller.getName());
        tellerMenu();
    }

    private void tellerMenu() {
        while (currentTeller != null && currentTeller.isAuthenticated()) {
            System.out.println("TeLLer Menu");
                    System.out.println("1. Search Account");
            System.out.println("2. View All Accounts");
            System.out.println("3. Assist Transaction");
            System.out.println("4. Assist Transfer");
            System.out.println("5. View Frozen Accounts");
            System.out.println("6. Unfreeze Account");
            System.out.println("7. Create New Account for Existing Customer");
            System.out.println("8. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1": searchAccount(); break;
                    case "2": viewAllAccounts(); break;
                    case "3": assistTransaction(); break;
                    case "4": assistTransfer(); break;
                    case "5": viewFrozenAccounts(); break;
                    case "6": unfreezeAccount(); break;
                    case "7": createNewAccount(); break;
                    case "8": createNewCustomer(); break;
                    case "9": tellerLogout(); return;
                    default: System.out.println("Invalid option.");
                }
            } catch (SecurityException e) {
                System.out.println(e.getMessage());
                tellerLogout();
                return;
            }
        }
    }

    private void createNewCustomer() {
        System.out.println("\nCREATE NEW CUSTOMER");

        System.out.print("Full Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        // Generate next available customerId
        String newCustomerId = db.generateNextCustomerId();

        // Create the customer object
        Customer newCustomer = new Customer(Integer.parseInt(newCustomerId), name, password);

        // Add the customer to the database
        db.addCustomer(newCustomer);

        System.out.println("Customer Created Successfully!");
        System.out.println("Assigned Customer ID: " + newCustomerId);

        // Optional: Ask if they want to create their first account
        System.out.print("Create an account for this customer now? (y/n): ");
        String choice = scanner.nextLine().trim().toLowerCase();

        if (choice.equals("y")) {
            createNewAccountForCustomer(newCustomer);
        }
    }
    private void createNewAccountForCustomer(Customer customer) {
        System.out.print("Account Type (card/check/checking/saving): ");
        String type = scanner.nextLine().trim().toLowerCase();

        System.out.print("Initial Balance: $");
        double balance = Double.parseDouble(scanner.nextLine().trim());

        Account account = switch (type) {
            case "card"     -> new Card(customer, balance);
            case "check"    -> new Check(customer, balance);
            case "checking" -> new Checking(customer, balance);
            case "saving", "savings" -> new Saving(customer, balance);
            default -> null;
        };

        if (account == null) {
            System.out.println("Invalid account type.");
            return;
        }

        // Generate per-customer account numbering
        String newAccountId = db.generateNextAccountNumber(customer);

        account.setAccountNumber(newAccountId);
        customer.addAccount(account);

        db.updateAccount(account);

        System.out.println("Account created: " + newAccountId);
    }

    private void tellerLogout() {
        if (currentTeller != null) {
            currentTeller.logout();
            currentTeller = null;
            System.out.println("Teller logged out.");
        }
    }


    private void searchAccount() {
        System.out.print("Enter Account ID or Customer Name: ");
        String query = scanner.nextLine().trim();

        List<Account> results = currentTeller.searchAccounts(query);

        if (results.isEmpty()) {
            System.out.println("No accounts found.");
        } else {
            System.out.println("Found " + results.size() + " account(s):");
            for (Account acc : results) acc.printAccountInfo();
        }
    }

    private void viewAllAccounts() {
        List<Account> accounts = db.retrieveAllAccounts();
        if (accounts.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }

        for (Account acc : accounts) {
            System.out.printf("%-10s | %-20s | %-10s | $%.2f | %s%n",
                    acc.getAccountNumber(),
                    acc.getCustomer().getName(),
                    acc.getType(),
                    acc.getBalance(),
                    acc.getStatus());
        }
    }
    
    private void assistTransaction() {
        System.out.print("Enter Account ID: ");
        String id = scanner.nextLine().trim();
        Account account = db.getAccountByNumber(id);

        if (account == null) {
            System.out.println("Account not found.");
            return;
        }

        System.out.print("Transaction Type (deposit/withdraw): ");
        String type = scanner.nextLine().trim();

        System.out.print("Amount: $");
        double amount = Double.parseDouble(scanner.nextLine().trim());

        Transaction tx = currentTeller.assistTransaction(account, type, amount);

        if (tx != null) {
            System.out.println("Transaction successful.");
            db.updateAccount(account);
        } else {
            System.out.println("Transaction failed.");
        }
    }

    private void assistTransfer() {
        System.out.print("Source Account ID: ");
        Account source = db.getAccountByNumber(scanner.nextLine().trim());

        System.out.print("Destination Account ID: ");
        Account dest = db.getAccountByNumber(scanner.nextLine().trim());

        if (source == null || dest == null) {
            System.out.println("Invalid accounts.");
            return;
        }

        System.out.print("Amount: $");
        double amount = Double.parseDouble(scanner.nextLine().trim());

        Transaction tx = currentTeller.assistTransfer(source, dest, amount);
        if (tx != null) {
            System.out.println("Transfer successful.");
            db.updateAccount(source);
            db.updateAccount(dest);
        } else {
            System.out.println("Transfer failed.");
        }
    }
    
    private void viewFrozenAccounts() {
        List<Account> frozen = currentTeller.getFrozenAccounts();

        if (frozen.isEmpty()) {
            System.out.println("No frozen accounts.");
            return;
        }

        for (Account acc : frozen) {
            System.out.printf("%-10s | %-20s | $%.2f%n",
                    acc.getAccountNumber(), acc.getCustomer().getName(), acc.getBalance());
        }
    }

    private void unfreezeAccount() {
        System.out.print("Enter Account ID to unfreeze: ");
        boolean success = currentTeller.unfreezeAccount(scanner.nextLine().trim());
        System.out.println(success ? "Account unfrozen." : "Failed to unfreeze account.");
    }

    private void createNewAccount() {
        System.out.print("Existing Customer ID: ");
        String customerId = scanner.nextLine().trim();
        Customer customer = db.getCustomer(customerId);

        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        System.out.print("Account Type (card/check/checking/saving): ");
        String type = scanner.nextLine().trim().toLowerCase();

        System.out.print("Initial Balance: $");
        double balance = Double.parseDouble(scanner.nextLine().trim());

        Account acc = switch (type) {
            case "card" -> new Card(customer, balance);
            case "check" -> new Check(customer, balance);
            case "checking" -> new Checking(customer, balance);
            case "saving", "savings" -> new Saving(customer, balance);
            default -> null;
        };

        if (acc == null) {
            System.out.println("Invalid account type.");
            return;
        }

        String newId = db.generateNextAccountNumber(customer);
        acc.setAccountNumber(newId);

        customer.addAccount(acc);
        db.updateAccount(acc);

        System.out.println("Created account " + newId + " for " + customer.getName());
    }

    private void customerLogin() {
        System.out.println("\n════ CUSTOMER LOGIN ════");

        System.out.print("Customer ID: ");
        String id = scanner.nextLine().trim();

        Customer customer = db.getCustomer(id);

        if (customer == null) {
            loginManager.loginAttempt(false); // fail attempt
            System.out.println(" Customer not found.");
            return;
        }

        System.out.print("Password: ");
        String pass = scanner.nextLine().trim();

        boolean correct = customer.getPassword().equals(pass);

        if (!loginManager.loginAttempt(correct)) {
            System.out.println(" Incorrect password.");
            return;
        }

        currentCustomer = customer;
        loginManager.refreshSession();

        System.out.println("✓ Welcome, " + customer.getName());
        selectCustomerAccount();
    }

        private void selectCustomerAccount() {
            List<Account> accounts = currentCustomer.getAccounts();

            if (accounts.isEmpty()) {
                System.out.println("You have no accounts.");
                return;
            }

            System.out.println("\nSelect an account:");
            for (int i = 0; i < accounts.size(); i++) {
                Account acc = accounts.get(i);
                System.out.printf("%d. %s (%s) - $%.2f%n", i + 1, acc.getAccountNumber(), acc.getType(), acc.getBalance());
            }

            System.out.print("Choice: ");
            int choice = Integer.parseInt(scanner.nextLine().trim());

            if (choice < 1 || choice > accounts.size()) {
                System.out.println("Invalid choice.");
                return;
            }

            Account selected = accounts.get(choice - 1);
            customerMenu(selected);
        }

        private void customerMenu(Account account) {
            while (currentCustomer != null) {
                System.out.println("\nCUSTOMER MENU");
                System.out.println("1. View Account Details");
                System.out.println("2. Deposit");
                System.out.println("3. Withdraw");
                System.out.println("4. Transfer");
                System.out.println("5. Transaction History");
                System.out.println("6. Report Stolen Card");
                System.out.println("7. Logout");
                System.out.print("Choose an option: ");

                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        account.printAccountInfo();
                        break;
                    case "2":
                        customerDeposit(account);
                        break;
                    case "3":
                        customerWithdraw(account);
                        break;
                    case "4":
                        customerTransfer(account);
                        break;
                    case "5":
                        viewTransactionHistory(account);
                        break;
                    case "6":
                        reportStolenCard(account);
                        break;
                    case "7":
                        customerLogout();
                        return;
                    default:
                        System.out.println("Invalid option.");
                }
            }
        }

        private void customerDeposit(Account account) {
            System.out.print("Amount to deposit: $");
            double amount = Double.parseDouble(scanner.nextLine().trim());

            boolean success = currentCustomer.deposit(account, amount, loginManager);

            if (success) {
                db.updateAccount(account);
                System.out.println("Deposit successful.");
            }
        }

        private void customerWithdraw(Account account) {
            System.out.print("Amount to withdraw: $");
            double amount = Double.parseDouble(scanner.nextLine().trim());

            boolean success = currentCustomer.withdraw(account, amount, loginManager);

            if (success) {
                db.updateAccount(account);
                System.out.println("Withdrawal successful.");
            }
        }

        private void customerTransfer(Account source) {
            System.out.print("Destination Account ID: ");
            Account dest = db.getAccountByNumber(scanner.nextLine().trim());

            if (dest == null) {
                System.out.println("Destination account not found.");
                return;
            }

            System.out.print("Amount to transfer: $");
            double amount = Double.parseDouble(scanner.nextLine().trim());

            boolean success = currentCustomer.transfer(source, dest, amount, loginManager);

            if (success) {
                db.updateAccount(source);
                db.updateAccount(dest);
                System.out.println("Transfer successful.");
            }
        }

        private void viewTransactionHistory(Account account) {
            // Load from CSV
            List<Transaction> persisted =
                    TransactionsDatabaseManager.getInstance().loadTransactionsForAccount(account.getAccountNumber());

            if (persisted.isEmpty()) {
                System.out.println("No transactions found.");
                return;
            }

            for (Transaction tx : persisted) {
                System.out.printf("ID: %d | %s | $%.2f | %s%n",
                        tx.getTransactionId(),
                        tx.getType(),
                        tx.getAmount(),
                        tx.getStatus());
            }
        }

        private void reportStolenCard(Account account) {
            account.freezeAccount();
            db.updateAccount(account);
            System.out.println("Your account has been frozen. Contact a teller to unfreeze.");
        }

        private void customerLogout() {
            currentCustomer = null;
            System.out.println("Logged out.");
        }
    private void admin() {
        System.out.println("\n══════ SYSTEM ADMIN LOGIN ══════");

        System.out.print("Username: ");
        String user = scanner.nextLine().trim();

        System.out.print("Password: ");
        String pass = scanner.nextLine().trim();

        if (!user.equals(ADMIN_USER) || !pass.equals(ADMIN_PASS)) {
            System.out.println("❌ Invalid admin credentials.");
            return;
        }

        System.out.println("✓ Admin login successful.");
        adminMenu();
    }
    private void adminMenu() {
        while (true) {
            System.out.println("\n══════ ADMIN MENU ══════");
            System.out.println("1. View All Tellers");
            System.out.println("2. Add Teller");
            System.out.println("3. Remove Teller");
            System.out.println("4. Logout");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAllTellers();
                case "2" -> addTeller();
                case "3" -> removeTeller();
                case "4" -> { System.out.println("Admin logged out."); return; }
                default -> System.out.println("Invalid option.");
            }
        }
    }
    private void viewAllTellers() {
        System.out.println("\n════ ALL TELLERS ════");

        for (Teller t : TellerDatabaseManager.getInstance().getAllTellers()) {
            System.out.printf("%s | %s | %s%n",
                    t.getEmployeeId(),
                    t.getName(),
                    t.getEmail()
            );
        }
    }
    private void addTeller() {
        System.out.println("\nADD NEW TELLER");

        System.out.print("Employee ID: ");
        String id = scanner.nextLine().trim();

        System.out.print("Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Password: ");
        String pass = scanner.nextLine().trim();

        Teller newTeller = new Teller(id, name, email, pass);

        TellerDatabaseManager.getInstance().addTeller(newTeller);

        System.out.println("✓ Teller added successfully.");
    }
    private void removeTeller() {
        System.out.print("\nEnter Teller ID to remove: ");
        String id = scanner.nextLine().trim();

        TellerDatabaseManager tdm = TellerDatabaseManager.getInstance();

        Teller t = tdm.getTeller(id);
        if (t == null) {
            System.out.println("❌ Teller not found.");
            return;
        }

        tdm.removeTeller(id);
        System.out.println("✓ Teller removed successfully.");
    }
        private void quit() {
            System.out.println("Thank you for using the system.");
            isRunning = false;
        }
    }
