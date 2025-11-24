package bank;

public class Saving extends Account {

    public Saving(Customer customer) {
        super(customer);
    }

    public Saving(Customer customer, double initialBalance) {
        super(customer, initialBalance);
    }

    public void title() {
        System.out.println("**Payments**");
    }

    @Override
    public void pay() {
        title();
        System.out.println("Payment From saving account For: " + customer.getName());
    }

    @Override
    public void receipt() {
        System.out.println("Payment receipt from saving account for: " + customer.getName());
    }

    // Deposit money into the account
    public boolean deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Deposit failed: Amount must be positive");
            return false;
        }
        balance += amount;
        System.out.println("Deposited $" + amount + " to " + customer.getName() + "'s saving account");
        System.out.println("New balance: $" + balance);

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

    // Withdraw money from the account
    public boolean withdraw(double amount) {
        if (!validateTransaction(amount)) {
            return false;
        }
        balance -= amount;
        System.out.println("Withdrawn $" + amount + " from " + customer.getName() + "'s saving account");
        System.out.println("New balance: $" + balance);

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

    // Transfer money to another account
    public boolean transfer(double amount, Account recipient) {
        if (!validateTransaction(amount)) {
            return false;
        }

        if (recipient == null) {
            System.out.println("Transfer failed: Recipient account is invalid");
            return false;
        }

        balance -= amount;
        recipient.updateBalance(amount);

        System.out.println("Transferred $" + amount + " from " + customer.getName() +
                " to " + recipient.getCustomer().getName());
        System.out.println("Your new balance: $" + balance);

        recipient.getCustomer().printRecipientInfo();

        Transaction transaction = new Transaction(
                generateTransactionId(),
                amount,
                "transfer",
                this,
                recipient
        );
        addTransaction(transaction);
        return true;
    }

    // Validate if transaction is possible
    public boolean validateTransaction(double amount) {
        if (amount <= 0) {
            System.out.println("Transaction failed: Amount must be positive");
            return false;
        }
        if (amount > balance) {
            System.out.println("Transaction failed: Insufficient funds");
            System.out.println("Available balance: $" + balance + ", Requested: $" + amount);
            return false;
        }
        return true;
    }

    public void printBalance() {
        System.out.println(customer.getName() + "'s Saving Account Balance: $" + balance);
    }

    private int generateTransactionId() {
        return (int) (Math.random() * 1000000);
    }
}