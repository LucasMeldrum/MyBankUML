package bank;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * AccountInfo represents detailed account data for the MyBankUML project.
 * This does NOT replace the existing Account.java — it is your new, richer
 * account model used by Customer, Teller, Transaction, etc.
 */
public class AccountInfo {

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

    private Customer owner;                      // uses bank.Customer
    private List<Transaction> transactionHistory; // uses bank.Transaction

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public AccountInfo(String accountNumber,
                       AccountType type,
                       Customer owner,
                       double initialBalance) {

        this(accountNumber, type, owner, initialBalance,
                AccountStatus.ACTIVE, false, Collections.emptyList());
    }

    public AccountInfo(String accountNumber,
                       AccountType type,
                       Customer owner,
                       double balance,
                       AccountStatus status,
                       boolean cardStolen,
                       List<Transaction> transactionHistory) {

        this.accountNumber = Objects.requireNonNull(accountNumber);
        this.type = Objects.requireNonNull(type);
        this.owner = Objects.requireNonNull(owner);
        this.status = Objects.requireNonNull(status);
        this.balance = balance;
        this.cardStolen = cardStolen;

        this.transactionHistory =
                transactionHistory == null ? Collections.emptyList() : List.copyOf(transactionHistory);
    }

    // ============================================================
    // GETTERS & SETTERS
    // ============================================================

    public String getAccountNumber() {
        return accountNumber;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = Objects.requireNonNull(type);
    }

    public AccountStatus getStatus() {
        return status;
    }

    public double getBalance() {
        return balance;
    }

    public boolean isCardStolen() {
        return cardStolen;
    }

    public Customer getOwner() {
        return owner;
    }

    public void setOwner(Customer owner) {
        this.owner = Objects.requireNonNull(owner);
    }

    public List<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    public void setTransactionHistory(List<Transaction> tx) {
        this.transactionHistory =
                tx == null ? Collections.emptyList() : List.copyOf(tx);
    }

    // ============================================================
    // ACCOUNT OPERATIONS
    // ============================================================

    /**
     * Low-level balance update. Higher-level logic (Customer, Teller, Transaction)
     * must validate the amount before calling this.
     */
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
    // ISSUE #5 — Secure info retrieval
    // ============================================================

    /**
     * Returns account details if ACTIVE.
     * If FROZEN/CLOSED → returns restricted view.
     */
    public AccountDetailsView getAccountDetails() {

        if (status != AccountStatus.ACTIVE) {
            return AccountDetailsView.forError(
                    "Account is " + status.name().toLowerCase()
            );
        }

        return AccountDetailsView.forActive(
                accountNumber,
                type,
                status,
                balance,
                owner,
                transactionHistory
        );
    }

    // ============================================================
    // INNER VIEW DTO (Immutable)
    // ============================================================

    public static final class AccountDetailsView {

        private final boolean viewable;
        private final String message;

        private final String accountNumber;
        private final AccountType type;
        private final AccountStatus status;
        private final Double balance;
        private final Customer owner;
        private final List<Transaction> transactionHistory;

        private AccountDetailsView(boolean viewable,
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
            this.transactionHistory =
                    tx == null ? null : List.copyOf(tx);
        }

        public static AccountDetailsView forError(String msg) {
            return new AccountDetailsView(false, msg,
                    null, null, null, null, null, null);
        }

        public static AccountDetailsView forActive(String accNum,
                                                   AccountType type,
                                                   AccountStatus status,
                                                   double balance,
                                                   Customer owner,
                                                   List<Transaction> tx) {
            return new AccountDetailsView(true, null,
                    accNum, type, status, balance, owner, tx);
        }

        // GETTERS ONLY (IMMUTABLE)
        public boolean isViewable() { return viewable; }
        public String getMessage() { return message; }
        public String getAccountNumber() { return accountNumber; }
        public AccountType getType() { return type; }
        public AccountStatus getStatus() { return status; }
        public Double getBalance() { return balance; }
        public Customer getOwner() { return owner; }
        public List<Transaction> getTransactionHistory() { return transactionHistory; }
    }

    @Override
    public String toString() {
        return "AccountInfo{" +
                "accountNumber='" + accountNumber + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", balance=" + balance +
                ", cardStolen=" + cardStolen +
                ", owner=" + (owner != null ? owner.getName() : "null") +
                '}';
    }
}
