package bank;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class TransactionsDatabaseManager {

    private static TransactionsDatabaseManager instance;
    private final String CSV_FILE = "transactions.csv";

    private TransactionsDatabaseManager() {
        try {
            File file = new File(CSV_FILE);
            if (!file.exists()) {
                PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE));
                writer.println("transactionId,accountNumber,type,amount,status,timestamp");
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized TransactionsDatabaseManager getInstance() {
        if (instance == null) {
            instance = new TransactionsDatabaseManager();
        }
        return instance;
    }

    public void saveTransaction(Transaction tx) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE, true))) {

            String accountNumber = tx.getSourceAccount() != null ?
                    tx.getSourceAccount().getAccountNumber() :
                    tx.getTargetAccount().getAccountNumber();

            writer.println(String.format(
                    "%d,%s,%s,%.2f,%s,%s",
                    tx.getTransactionId(),
                    accountNumber,
                    tx.getType(),
                    tx.getAmount(),
                    tx.getStatus(),
                    LocalDateTime.now()
            ));

        } catch (IOException e) {
            System.out.println("Error saving transaction: " + e.getMessage());
        }
    }

    // Load all transactions for one account
    public List<Transaction> loadTransactionsForAccount(String accountNumber) {
        List<Transaction> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {

            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {

                if (first) {
                    first = false;
                    continue; // skip header
                }

                String[] row = line.split(",");

                if (!row[1].equals(accountNumber)) continue;

                int txId = Integer.parseInt(row[0]);
                String type = row[2];
                double amount = Double.parseDouble(row[3]);
                String status = row[4];

                Transaction tx = new Transaction(txId, amount, type, null, null);
                tx.setStatus(status);

                list.add(tx);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
