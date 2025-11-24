package bank;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class Transaction {

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
        // Ensure active session
        loginManager.manageSession();

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
                    targetAccount.updateBalance(amount);
                }
                break;
            case "withdraw":
                if (sourceAccount != null) {
                    sourceAccount.updateBalance(-amount);
                }
                break;
            case "transfer":
                if (sourceAccount != null && targetAccount != null) {
                    sourceAccount.updateBalance(-amount);
                    targetAccount.updateBalance(amount);
                }
                break;
            default:
                status = "failed";
                return false;
        }

        status = "completed";
        return true;
    }

    public void pay() {
        System.out.println("Payment transaction is done.");
    }

    public void receipt() {
        System.out.println("Transaction receipt.");
        System.out.println("Transaction ID: " + transactionId);
        System.out.println("Type: " + type);
        System.out.println("Amount: $" + amount);
        System.out.println("Status: " + status);
        System.out.println("Timestamp: " + timestamp);
    }
}