package bank;

public class Checking extends Account {

    private static final double OVERDRAFT_LIMIT = 500.0;
    private double overdraftUsed = 0.0;

    public Checking(Customer customer) {
        super(customer);
    }

    public Checking(Customer customer, double initialBalance) {
        super(customer, initialBalance);
    }

    @Override
    public String getAccountType() {
        return "Checking";
    }

    public void title() {
        System.out.println("**Checking Account**");
    }

    @Override
    public void pay() {
        title();
        System.out.println("Check payment for customer: " + customer.getName());
    }

    @Override
    public void receipt() {
        System.out.println("Checking account receipt for customer: " + customer.getName());
    }

    // Checking accounts can have overdraft
    public double getAvailableBalance() {
        return balance + (OVERDRAFT_LIMIT - overdraftUsed);
    }

    public boolean deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Deposit failed: Amount must be positive");
            return false;
        }

        // If in overdraft, pay that back first
        if (overdraftUsed > 0) {
            double overdraftPayment = Math.min(amount, overdraftUsed);
            overdraftUsed -= overdraftPayment;
            amount -= overdraftPayment;
            System.out.println("Paid back $" + String.format("%.2f", overdraftPayment) + " of overdraft");
        }

        balance += amount;
        System.out.println("Deposited $" + String.format("%.2f", amount) + " to checking account");
        System.out.println("New balance: $" + String.format("%.2f", balance));

        Transaction transaction = new Transaction(
                generateTransactionId(),
                amount,
                "deposit",
                null,
                this
        );
        addTransaction(transaction);
        return true;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0) {
            System.out.println("Withdrawal failed: Amount must be positive");
            return false;
        }

        double available = getAvailableBalance();
        if (amount > available) {
            System.out.println("Withdrawal failed: Insufficient funds (including overdraft)");
            return false;
        }

        if (amount > balance) {
            double overdraftNeeded = amount - balance;
            overdraftUsed += overdraftNeeded;
            balance = 0;
            System.out.println("Used $" + String.format("%.2f", overdraftNeeded) + " of overdraft");
        } else {
            balance -= amount;
        }

        System.out.println("Withdrawn $" + String.format("%.2f", amount));
        System.out.println("New balance: $" + String.format("%.2f", balance));

        Transaction transaction = new Transaction(
                generateTransactionId(),
                amount,
                "withdraw",
                this,
                null
        );
        addTransaction(transaction);
        return true;
    }

    private int generateTransactionId() {
        return (int) (Math.random() * 1000000);
    }
}