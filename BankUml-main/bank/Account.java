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
    protected String status; // "ACTIVE" or "FROZEN"
    protected boolean cardStolen;
    protected List<Transaction> transactions;

    public Account(Customer customer) {
        this.customer = customer;
        this.balance = 0.0;
        this.status = "ACTIVE";
        this.cardStolen = false;
        this.transactions = new ArrayList<>();
    }

    public Account(Customer customer, double initialBalance) {
        this.customer = customer;
        this.balance = initialBalance;
        this.status = "ACTIVE";
        this.cardStolen = false;
        this.transactions = new ArrayList<>();
    }

    // Abstract methods to be implemented by subclasses
    public abstract void pay();
    public abstract void receipt();

    // Common methods
    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
    }

    public void updateBalance(double delta) {
        this.balance += delta;
    }

    public void freezeAccount() {
        this.status = "FROZEN";
        this.cardStolen = true;
    }

    public void unfreezeAccount() {
        this.status = "ACTIVE";
        this.cardStolen = false;
    }

    public Customer getOwner() {
        return customer;
    }

    public void printAccountInfo() {
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Owner: " + customer.getName());
        System.out.println("Balance: $" + balance);
        System.out.println("Status: " + status);
        System.out.println("Transactions: " + transactions.size());
    }
}