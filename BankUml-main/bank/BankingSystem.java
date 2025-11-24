package bank;

import java.util.List;
import java.util.Scanner;

public class BankingSystem {

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
        System.out.println("║   Welcome to the Banking System   ║");
        System.out.println("╚════════════════════════════════════════╝");

        while (isRunning) {
            showMainMenu();
        }

        scanner.close();
    }

    private void showMainMenu() {
        System.out.println("\n═══════════════ MAIN MENU ═══════════════");
        System.out.println("1. Teller Login");
        System.out.println("2. Customer Login");
        System.out.println("3. Exit");
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
                exit();
                break;
            default:
                System.out.println("❌ Invalid option. Please try again.");
        }
    }

    private void tellerLogin() {
        System.out.println("\n═══════════════ TELLER LOGIN ═══════════════");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (loginManager.login(username, password)) {
            currentTeller = new Teller("T001", "Jane Smith", "jane@bank.com");
            currentTeller.login(username, password);
            System.out.println("✓ Login successful! Welcome, " + currentTeller.getName());
            tellerMenu();
        } else {
            System.out.println("❌ Login failed. Please check your credentials.");
        }
    }

    private void tellerMenu() {
        while (currentTeller != null && currentTeller.isAuthenticated()) {
            System.out.println("\n═══════════════ TELLER MENU ═══════════════");
            System.out.println("1. Search Account");
            System.out.println("2. View All Accounts");
            System.out.println("3. Assist Transaction (Deposit/Withdraw)");
            System.out.println("4. Assist Transfer");
            System.out.println("5. View Frozen Accounts");
            System.out.println("6. Unfreeze Account");
            System.out.println("7. Create New Account");
            System.out.println("8. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        searchAccount();
                        break;
                    case "2":
                        viewAllAccounts();
                        break;
                    case "3":
                        assistTransaction();
                        break;
                    case "4":
                        assistTransfer();
                        break;
                    case "5":
                        viewFrozenAccounts();
                        break;
                    case "6":
                        unfreezeAccount();
                        break;
                    case "7":
                        createNewAccount();
                        break;
                    case "8":
                        tellerLogout();
                        return;
                    default:
                        System.out.println("❌ Invalid option.");
                }
            } catch (SecurityException e) {
                System.out.println("❌ " + e.getMessage());
                tellerLogout();
                return;
            }
        }
    }

    private void searchAccount() {
        System.out.print("\nEnter Account ID, Name, or DOB: ");
        String query = scanner.nextLine().trim();

        List<Account> results = currentTeller.searchAccounts(query);

        if (results.isEmpty()) {
            System.out.println("❌ No accounts found.");
        } else {
            System.out.println("\n✓ Found " + results.size() + " account(s):");
            for (Account acc : results) {
                acc.printAccountInfo();
            }
        }
    }

    private void viewAllAccounts() {
        System.out.println("\n═══════════════ ALL ACCOUNTS ═══════════════");
        List<Account> accounts = db.retrieveAllAccounts();

        if (accounts.isEmpty()) {
            System.out.println("No accounts in the system.");
        } else {
            for (Account acc : accounts) {
                System.out.printf("%-10s | %-20s | %-10s | $%-10.2f | %s%n",
                        acc.getAccountNumber(),
                        acc.getCustomer().getName(),
                        acc.getAccountType(),
                        acc.getBalance(),
                        acc.getStatus()
                );
            }
        }
    }

    private void assistTransaction() {
        System.out.print("\nEnter Account ID: ");
        String accountId = scanner.nextLine().trim();

        System.out.print("Transaction Type (deposit/withdraw): ");
        String type = scanner.nextLine().trim();

        System.out.print("Amount: $");
        double amount = Double.parseDouble(scanner.nextLine().trim());

        Transaction tx = currentTeller.assistTransaction(accountId, type, amount);

        if (tx != null) {
            System.out.println("✓ Transaction successful!");
            System.out.println("Transaction ID: " + tx.getTransactionId());
            System.out.println("Status: " + tx.getStatus());
        } else {
            System.out.println("❌ Transaction failed.");
        }
    }

    private void assistTransfer() {
        System.out.print("\nSource Account ID: ");
        String sourceId = scanner.nextLine().trim();

        System.out.print("Destination Account ID: ");
        String destId = scanner.nextLine().trim();

        System.out.print("Amount: $");
        double amount = Double.parseDouble(scanner.nextLine().trim());

        Transaction tx = currentTeller.assistTransfer(sourceId, destId, amount);

        if (tx != null) {
            System.out.println("✓ Transfer successful!");
            System.out.println("Transaction ID: " + tx.getTransactionId());
        } else {
            System.out.println("❌ Transfer failed.");
        }
    }

    private void viewFrozenAccounts() {
        List<Account> frozen = currentTeller.getFrozenAccounts();

        if (frozen.isEmpty()) {
            System.out.println("\n✓ No frozen accounts.");
        } else {
            System.out.println("\n═══════════════ FROZEN ACCOUNTS ═══════════════");
            for (Account acc : frozen) {
                System.out.printf("%-10s | %-20s | $%-10.2f%n",
                        acc.getAccountNumber(),
                        acc.getCustomer().getName(),
                        acc.getBalance()
                );
            }
        }
    }

    private void unfreezeAccount() {
        System.out.print("\nEnter Account ID to unfreeze: ");
        String accountId = scanner.nextLine().trim();

        boolean success = currentTeller.unfreezeAccount(accountId);

        if (success) {
            System.out.println("✓ Account unfrozen successfully.");
        } else {
            System.out.println("❌ Failed to unfreeze account.");
        }
    }

    private void createNewAccount() {
        System.out.println("\n═══════════════ CREATE NEW ACCOUNT ═══════════════");
        System.out.print("Customer Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Date of Birth (YYYY-MM-DD): ");
        String dob = scanner.nextLine().trim();

        System.out.print("Account Type (card/check/checking/saving): ");
        String type = scanner.nextLine().trim();

        System.out.print("Initial Balance: $");
        double balance = Double.parseDouble(scanner.nextLine().trim());

        Customer customer = new Customer(name, dob);
        Account account = null;

        switch (type.toLowerCase()) {
            case "card":
                account = new Card(customer, balance);
                break;
            case "check":
                account = new Check(customer, balance);
                break;
            case "checking":
                account = new Checking(customer, balance);
                break;
            case "saving":
            case "savings":
                account = new Saving(customer, balance);
                break;
            default:
                System.out.println("❌ Invalid account type.");
                return;
        }

        String accountId = "A" + (100 + db.retrieveAllAccounts().size() + 1);
        account.setAccountNumber(accountId);
        customer.setAccountNumber(accountId);

        db.addAccount(account);
        System.out.println("✓ Account created successfully!");
        System.out.println("Account ID: " + accountId);
    }

    private void tellerLogout() {
        if (currentTeller != null) {
            currentTeller.logout();
            currentTeller = null;
            System.out.println("✓ Teller logged out successfully.");
        }
    }

    private void customerLogin() {
        System.out.println("\n═══════════════ CUSTOMER LOGIN ═══════════════");
        System.out.print("Account ID: ");
        String accountId = scanner.nextLine().trim();

        Account account = db.getAccountNumber(accountId);

        if (account != null) {
            currentCustomer = account.getCustomer();
            System.out.println("✓ Welcome, " + currentCustomer.getName() + "!");
            customerMenu(account);
        } else {
            System.out.println("❌ Account not found.");
        }
    }

    private void customerMenu(Account account) {
        while (currentCustomer != null) {
            System.out.println("\n═══════════════ CUSTOMER MENU ═══════════════");
            System.out.println("1. View Account Details");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transfer");
            System.out.println("5. View Transaction History");
            System.out.println("6. Report Stolen Card");
            System.out.println("7. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    currentCustomer.viewAccount(account);
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
                    System.out.println("❌ Invalid option.");
            }
        }
    }

    private void customerDeposit(Account account) {
        System.out.print("\nAmount to deposit: $");
        double amount = Double.parseDouble(scanner.nextLine().trim());

        boolean success = currentCustomer.deposit(account, amount, loginManager);

        if (success) {
            db.updateAccount(account);
        }
    }

    private void customerWithdraw(Account account) {
        System.out.print("\nAmount to withdraw: $");
        double amount = Double.parseDouble(scanner.nextLine().trim());

        boolean success = currentCustomer.withdraw(account, amount, loginManager);

        if (success) {
            db.updateAccount(account);
        }
    }

    private void customerTransfer(Account account) {
        System.out.print("\nDestination Account ID: ");
        String destId = scanner.nextLine().trim();

        Account destAccount = db.getAccountNumber(destId);

        if (destAccount == null) {
            System.out.println("❌ Destination account not found.");
            return;
        }

        System.out.print("Amount to transfer: $");
        double amount = Double.parseDouble(scanner.nextLine().trim());

        boolean success = currentCustomer.transfer(account, destAccount, amount, loginManager);

        if (success) {
            db.updateAccount(account);
            db.updateAccount(destAccount);
        }
    }

    private void viewTransactionHistory(Account account) {
        System.out.println("\n═══════════════ TRANSACTION HISTORY ═══════════════");
        List<Transaction> txs = account.getTransactions();

        if (txs.isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            for (Transaction tx : txs) {
                System.out.printf("ID: %d | Type: %-10s | Amount: $%-10.2f | Status: %s%n",
                        tx.getTransactionId(),
                        tx.getType(),
                        tx.getAmount(),
                        tx.getStatus()
                );
            }
        }
    }

    private void reportStolenCard(Account account) {
        System.out.println("\n⚠️  Reporting stolen card...");
        account.freezeAccount();
        db.updateAccount(account);
        System.out.println("✓ Your account has been frozen for security.");
        System.out.println("Please contact a teller to unfreeze your account.");
    }

    private void customerLogout() {
        currentCustomer = null;
        System.out.println("✓ Logged out successfully.");
    }

    private void exit() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   Thank you for using our system!    ║");
        System.out.println("╚════════════════════════════════════════╝");
        isRunning = false;
    }
}