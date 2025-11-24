package bank;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Banking System Demo ===\n");

        // Initialize Database (loads from CSV or creates sample data)
        DatabaseManager db = DatabaseManager.getInstance();

        // Create a Teller
        Teller teller = new Teller("T001", "Jane Smith", "jane@bank.com");

        // Teller Login
        System.out.println("--- Teller Login ---");
        boolean loginSuccess = teller.login("admin", "password123");
        System.out.println("Login successful: " + loginSuccess);
        System.out.println();

        if (loginSuccess) {
            // Search for accounts
            System.out.println("--- Searching Accounts ---");
            List<Account> accounts = teller.searchAccounts("A101");
            System.out.println("Found " + accounts.size() + " accounts");
            if (!accounts.isEmpty()) {
                accounts.get(0).printAccountInfo();
            }
            System.out.println();

            // View all accounts
            System.out.println("--- All Accounts ---");
            for (Account acc : db.retrieveAllAccounts()) {
                System.out.println(acc.getAccountNumber() + " - " +
                        acc.getCustomer().getName() + " - $" +
                        acc.getBalance() + " - " + acc.getStatus());
            }
            System.out.println();

            // Assist with transaction
            System.out.println("--- Deposit Transaction ---");
            Transaction deposit = teller.assistTransaction("A101", "deposit", 200);
            if (deposit != null) {
                System.out.println("Deposit successful! Status: " + deposit.getStatus());
                Account updated = db.getAccountNumber("A101");
                System.out.println("New balance: $" + updated.getBalance());
            }
            System.out.println();

            // Transfer between accounts
            System.out.println("--- Transfer Transaction ---");
            Transaction transfer = teller.assistTransfer("A101", "A102", 100);
            if (transfer != null) {
                System.out.println("Transfer successful! Status: " + transfer.getStatus());
                System.out.println("A101 balance: $" + db.getAccountNumber("A101").getBalance());
                System.out.println("A102 balance: $" + db.getAccountNumber("A102").getBalance());
            }
            System.out.println();

            // View frozen accounts
            System.out.println("--- Frozen Accounts ---");
            List<Account> frozen = teller.getFrozenAccounts();
            System.out.println("Found " + frozen.size() + " frozen accounts");
            for (Account acc : frozen) {
                System.out.println(acc.getAccountNumber() + " - " + acc.getCustomer().getName());
            }
            System.out.println();

            // Unfreeze an account
            System.out.println("--- Unfreezing Account ---");
            boolean unfrozen = teller.unfreezeAccount("A103");
            System.out.println("Unfreeze successful: " + unfrozen);
            if (unfrozen) {
                Account acc = db.getAccountNumber("A103");
                System.out.println("A103 status: " + acc.getStatus());
            }
            System.out.println();

            // Customer operations
            System.out.println("--- Customer Operations ---");
            Customer customer = new Customer("John Doe", "1998-04-12");
            customer.printCustomerInfo();

            Account customerAccount = db.getAccountNumber("A101");
            customer.viewAccount(customerAccount);
            System.out.println();

            // Logout
            System.out.println("--- Logout ---");
            teller.logout();
            System.out.println("Teller logged out.");
        }

        System.out.println("\n=== Demo Complete ===");
        System.out.println("Data has been saved to accounts.csv");
    }
}