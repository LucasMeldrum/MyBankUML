package bank;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class Transaction {
   // private final TransactionsDatabaseManager txDB = TransactionsDatabaseManager.getInstance();
    private int transactionId;
    private double amount;
    private LocalDateTime timestamp;
    private String type;
    private String status;
    private Account sourceAccount;
    private Account targetAccount;

    public Transaction() {
        this.transactionId = (int) (Math.random() * 1000000);
        this.timestamp = LocalDateTime.now();
        this.status = "pending";
    }

    public Transaction(int transactionId, double amount, String type, Account sourceAccount, Account targetAccount) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.status = "pending";
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
    }

    public boolean validate(LoginManager loginManager) {


        // Validate amount
        if (amount <= 0) {
            status = "invalid amount";
            return false;
        }

        // Validate funds for withdraw / transfer
        if ((type.equalsIgnoreCase("withdraw") || type.equalsIgnoreCase("transfer"))
                && sourceAccount != null && sourceAccount.getBalance() < amount) {
            status = "insufficient funds";
            return false;
        }

        status = "validated";
        return true;
    }

    public boolean apply() {
        if (!status.equals("validated"))
            return false;

        switch (type.toLowerCase()) {
            case "deposit":
                if (targetAccount != null) {
                    if (!targetAccount.getStatus().equals("FROZEN")) {
                        targetAccount.updateBalance(amount);
                        targetAccount.addTransaction(this);
                        TransactionsDatabaseManager.getInstance().saveTransaction(this);
                    }
                    else {
                        status = "failed due to FROZEN account or other issue";
                        return false;
                    }
                }
                break;
            case "withdraw":
                if (sourceAccount != null) {
                    if (!sourceAccount.getStatus().equals("FROZEN")) {
                        sourceAccount.updateBalance(-amount);
                        sourceAccount.addTransaction(this);
                        TransactionsDatabaseManager.getInstance().saveTransaction(this);
                        }
                    else {
                        status = "failed due to FROZEN account or other issue";
                        return false;
                    }

                }
                break;
            case "transfer":
                if (sourceAccount != null && targetAccount != null) {
                    if (!sourceAccount.getStatus().equals("FROZEN") && (!targetAccount.getStatus().equals("FROZEN"))) {
                        sourceAccount.updateBalance(-amount);
                        targetAccount.updateBalance(amount);
                        TransactionsDatabaseManager.getInstance().saveTransaction(this);
                        targetAccount.addTransaction(this);
                        sourceAccount.addTransaction(this);
                    }

                    else {
                        status = "failed due to FROZEN account or other issue";
                        return false;
                    }

                }
                break;
            default:
                status = "failed due to FROZEN account or other issue";
                return false;
        }

        status = "completed";
        return true;
    }

    public void pay() {
        System.out.println("Payment transaction is done.");
    }

    public void receipt() {
        System.out.println("━━━━━━━━━━━━━ TRANSACTION RECEIPT ━━━━━━━━━━━━━");
        System.out.println("Transaction ID: " + transactionId);
        System.out.println("Type: " + type);
        System.out.println("Amount: $" + String.format("%.2f", amount));
        System.out.println("Status: " + status);
        System.out.println("Timestamp: " + timestamp);
        if (sourceAccount != null) {
            System.out.println("From Account: " + sourceAccount.getAccountNumber());
        }
        if (targetAccount != null) {
            System.out.println("To Account: " + targetAccount.getAccountNumber());
        }
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}