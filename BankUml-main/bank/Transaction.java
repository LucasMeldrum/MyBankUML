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

        boolean authorized = loginManager.authorize(
                "TransactionSystem",
                "execute",
                "transaction#" + transactionId
        );

        if (!authorized) {
            status = "unauthorized";
            return false;
        }

        if (amount <= 0) {
            status = "invalid amount";
            return false;
        }

        if ((type.equals("withdraw") || type.equals("transfer")) && sourceAccount.getBalance() < amount) {
            status = "insufficient funds";
            return false;
        }

        status = "validated";
        return true;
    }

    public boolean apply() {
        if (!status.equals("validated"))
            return false;

        switch (type) {
            case "deposit":
                targetAccount.updateBalance(+amount);
                break;
            case "withdraw":
                sourceAccount.updateBalance(-amount);
                break;
            case "transfer":
                sourceAccount.updateBalance(-amount);
                targetAccount.updateBalance(+amount);
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
    }
}
