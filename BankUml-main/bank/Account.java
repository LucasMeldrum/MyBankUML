package bank;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class Account {

    protected String accountNumber;
    protected Customer customer;
    protected double balance;
    protected AccountType type;
    protected String status; // ACTIVE or FROZEN
    protected boolean cardStolen;
    protected List<Transaction> transactions;

    public Account(Customer customer, AccountType type) {
        this.customer = customer;
        this.type = type;
        this.balance = 0.0;
        this.status = "ACTIVE";
        this.cardStolen = false;
        this.transactions = new ArrayList<>();
    }

    public Account(Customer customer, AccountType type, double initialBalance) {
        this.customer = customer;
        this.type = type;
        this.balance = initialBalance;
        this.status = "ACTIVE";
        this.cardStolen = false;
        this.transactions = new ArrayList<>();
    }

    // Add a transaction
    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
    }

    // Balance update
    public void updateBalance(double delta) {
        this.balance += delta;
    }

    // Freeze and unfreeze
    public void freezeAccount() {
        this.status = "FROZEN";
        this.cardStolen = true;
    }

    public void unfreezeAccount() {
        this.status = "ACTIVE";
        this.cardStolen = false;
    }

    // info display
    public void printAccountInfo() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Account Type: " + type);
        System.out.println("Owner: " + customer.getName());
        System.out.println("Balance: $" + String.format("%.2f", balance));
        System.out.println("Status: " + status);
        System.out.println("Transactions: " + transactions.size());
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}
