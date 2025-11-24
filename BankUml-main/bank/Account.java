package bank;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Account {

    // ============================================================
    // ENUMS
    // ============================================================

    public enum AccountStatus {
        ACTIVE,
        FROZEN,
        CLOSED
    }

    public enum AccountType {
        CHECKING,
        SAVINGS,
        CARD,
        CHECK
    }

    // ============================================================
    // FIELDS
    // ============================================================

    private final String accountNumber;
    private AccountType type;
    private AccountStatus status;
    private double balance;
    private boolean cardStolen;

    private Customer owner;              
    private List<Transaction> transactions;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public Account(String accountNumber,
                   AccountType type,
                   Customer owner,
                   double initialBalance) {

        this(accountNumber, type, owner, initialBalance,
                AccountStatus.ACTIVE, false, Collections.emptyList());
    }

    public Account(String accountNumber,
                   AccountType type,
                   Customer owner,
                   double balance,
                   AccountStatus status,
                   boolean cardStolen,
                   List<Transaction> transactions) {

        this.accountNumber = Objects.requireNonNull(accountNumber);
        this.type = Objects.requireNonNull(type);
        this.owner = Objects.requireNonNull(owner);
        this.status = Objects.requireNonNull(status);
        this.balance = balance;
        this.cardStolen = cardStolen;
        this.transactions =
                transactions == null ? Collections.emptyList() : List.copyOf(transactions);
    }

    // ============================================================
    // GETTERS & SETTERS
    // ============================================================

    public String getAccountNumber() { return accountNumber; }

    public AccountType getType() { return type; }

    public void setType(AccountType type) { this.type = Objects.requireNonNull(type); }

    public AccountStatus getStatus() { return status; }

    public double getBalance() { return balance; }

    public boolean isCardStolen() { return cardStolen; }

    public Customer getOwner() { return owner; }

    public void setOwner(Customer owner) { this.owner = Objects.requireNonNull(owner); }

    public List<Transaction> getTransactions() { return transactions; }

    public void addTransaction(Transaction txn) {
        this.transactions.add(txn);
    }

    // ============================================================
    // OPERATIONS
    // ============================================================

    public synchronized void updateBalance(double delta) {
        this.balance += delta;
    }

    public synchronized void freezeAccount() {
        this.status = AccountStatus.FROZEN;
        this.cardStolen = true;
    }

    public synchronized void unfreezeAccount() {
        this.status = AccountStatus.ACTIVE;
        this.cardStolen = false;
    }

    public synchronized void updateAccountStatus(AccountStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus);
    }

    // ============================================================
    // ISSUE #5 — secure restricted view
    // ============================================================

    public AccountView getAccountDetails() {

        if (status != AccountStatus.ACTIVE) {
            return AccountView.forError(
                    "Account is " + status.name().toLowerCase()
            );
        }

        return AccountView.forActive(
                accountNumber,
                type,
                status,
                balance,
                owner,
                transactions
        );
    }

    // ============================================================
    // INNER DTO
    // ============================================================

    public static final class AccountView {

        private final boolean viewable;
        private final String message;

        private final String accountNumber;
        private final AccountType type;
        private final AccountStatus status;
        private final Double balance;
        private final Customer owner;
        private final List<Transaction> transactions;

        private AccountView(boolean viewable,
                            String message,
                            String accountNumber,
                            AccountType type,
                            AccountStatus status,
                            Double balance,
                            Customer owner,
                            List<Transaction> tx) {

            this.viewable = viewable;
            this.message = message;
            this.accountNumber = accountNumber;
            this.type = type;
            this.status = status;
            this.balance = balance;
            this.owner = owner;
            this.transactions = tx == null ? null : List.copyOf(tx);
        }

        public static AccountView forError(String msg) {
            return new AccountView(false, msg, null, null, null, null, null, null);
        }

        public static AccountView forActive(String accNum,
                                            AccountType type,
                                            AccountStatus status,
                                            double balance,
                                            Customer owner,
                                            List<Transaction> tx) {
            return new AccountView(true, null,
                    accNum, type, status, balance, owner, tx);
        }

        // GETTERS
        public boolean isViewable() { return viewable; }
        public String getMessage() { return message; }
        public String getAccountNumber() { return accountNumber; }
        public AccountType getType() { return type; }
        public AccountStatus getStatus() { return status; }
        public Double getBalance() { return balance; }
        public Customer getOwner() { return owner; }
        public List<Transaction> getTransactions() { return transactions; }
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountNumber='" + accountNumber + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", balance=" + balance +
                ", cardStolen=" + cardStolen +
                ", owner=" + (owner != null ? owner.getName() : "null") +
                '}';
    }
}
