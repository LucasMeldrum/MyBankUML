package bank;

public class Saving extends Account {

    public Saving(Customer customer) {
        super(customer, AccountType.SAVING);
    }

    public Saving(Customer customer, double initialBalance) {
        super(customer, AccountType.SAVING, initialBalance);
    }

    // Deposit money
    public boolean deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Deposit failed: Amount must be positive");
            return false;
        }

        balance += amount;

        System.out.println("Deposited $" + String.format("%.2f", amount) +
                " to " + customer.getName() + "'s saving account");
        System.out.println("New balance: $" + String.format("%.2f", balance));

        Transaction tx = new Transaction(
                generateTransactionId(),
                amount,
                "deposit",
                null,
                this
        );
        addTransaction(tx);

        return true;
    }

    // Withdraw money
    public boolean withdraw(double amount) {
        if (!validateTransaction(amount)) {
            return false;
        }

        balance -= amount;

        System.out.println("Withdrawn $" + String.format("%.2f", amount) +
                " from " + customer.getName() + "'s saving account");
        System.out.println("New balance: $" + String.format("%.2f", balance));

        Transaction tx = new Transaction(
                generateTransactionId(),
                amount,
                "withdraw",
                this,
                null
        );
        addTransaction(tx);

        return true;
    }

    // Validate basic withdrawal rules
    public boolean validateTransaction(double amount) {
        if (amount <= 0) {
            System.out.println("Transaction failed: Amount must be positive");
            return false;
        }
        if (amount > balance) {
            System.out.println("Transaction failed: Insufficient funds");
            System.out.println("Balance: $" + String.format("%.2f", balance) +
                    ", Requested: $" + String.format("%.2f", amount));
            return false;
        }
        return true;
    }

    public void printBalance() {
        System.out.println(customer.getName() +
                "'s Savings Account Balance: $" + String.format("%.2f", balance));
    }

    private int generateTransactionId() {
        return (int) (Math.random() * 1_000_000);
    }
}
