package bank;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class Customer {
    private int customerId;
    private String name;
    private String password;
    private String dateOfBirth;
    private String address;
    private String phone;
    private String email;
    private String accountNumber;
    private List<Account> accounts = new ArrayList<>();

    public Customer(String name) {
        this.name = name;
    }

    public Customer(int customerId,String name, String password) {
        this.customerId = customerId;
        this.name = name;
        this.password = password;
    }

    public Customer(String name, String dateOfBirth, String address, String phone, String email) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    public void printCustomerInfo() {
        System.out.println("Customer's info: ");
        System.out.println("Name: " + name);
        if (dateOfBirth != null) System.out.println("Date of Birth: " + dateOfBirth);
        if (address != null) System.out.println("Address: " + address);
        if (phone != null) System.out.println("Phone: " + phone);
        if (email != null) System.out.println("Email: " + email);
        if (accountNumber != null) System.out.println("Account Number: " + accountNumber);
    }

    public void printRecipientInfo() {
        System.out.println("Recipient Info: ");
        System.out.println("Name: " + name);
        System.out.println("Account Number: " + (accountNumber != null ? accountNumber : "N/A"));
    }

    public void viewAccount(Account account) {
        if (account == null) {
            System.out.println("Account not found.");
            return;
        }

        System.out.println("=== Account Information ===");
        System.out.println("Account ID: " + account.getAccountNumber());
        System.out.println("Balance: $" + account.getBalance());
        System.out.println("Status: " + account.getStatus());
        System.out.println("===========================");
    }

    public boolean deposit(Account account, double amount, LoginManager loginManager) {
        Transaction t = new Transaction(
                generateTransactionId(),
                amount,
                "deposit",
                null,
                account
        );

        if (!t.validate(loginManager)) {
            System.out.println("Deposit failed: " + t.getStatus());
            return false;
        }

        if (!t.apply()) {
            System.out.println("Deposit failed: " + t.getStatus());
            return false;
        }

        System.out.println("Deposit successful. New balance: $" + account.getBalance());
        return true;
    }

    public boolean withdraw(Account account, double amount, LoginManager loginManager) {
        Transaction t = new Transaction(
                generateTransactionId(),
                amount,
                "withdraw",
                account,
                null
        );

        if (!t.validate(loginManager)) {
            System.out.println("Withdrawal failed: " + t.getStatus());
            return false;
        }

        if (!t.apply()) {
            System.out.println("Withdrawal failed: " + t.getStatus());
            return false;
        }

        System.out.println("Withdrawal successful. New balance: $" + account.getBalance());
        return true;
    }

    public boolean transfer(Account source, Account target, double amount, LoginManager loginManager) {
        Transaction t = new Transaction(
                generateTransactionId(),
                amount,
                "transfer",
                source,
                target
        );

        if (!t.validate(loginManager)) {
            System.out.println("Transfer failed: " + t.getStatus());
            return false;
        }

        if (!t.apply()) {
            System.out.println("Transfer failed: " + t.getStatus());
            return false;
        }

        System.out.println("Transfer successful.");
        System.out.println("New Source Balance: $" + source.getBalance());
        System.out.println("New Target Balance: $" + target.getBalance());

        return true;
    }
    public void addAccount(Account account) {
        accounts.add(account);
    }



    private int generateTransactionId() {
        return (int) (Math.random() * 1000000);
    }
}