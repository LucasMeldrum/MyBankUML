package bank;

import lombok.Getter;

import java.util.List;
import java.util.ArrayList;

public class Teller {

    @Getter
    private String employeeId;
    @Getter
    private String name;
    @Getter
    private String email;
    @Getter
    private String password;
    private boolean isAuthenticated;

    private LoginManager loginManager;
    private DatabaseManager databaseManager;

    public Teller(String employeeId, String name, String email, String password) {
        this.employeeId = employeeId;
        this.name = name;
        this.email = email;
        this.isAuthenticated = false;

        this.loginManager = new LoginManager(20);
        this.databaseManager = DatabaseManager.getInstance();
    }

    // ================= AUTH =================
    //not used anywhere
    /*public boolean login(String username, String password) {
        boolean ok = loginManager.login(username, password);
        if (ok) isAuthenticated = true;
        return ok;
    }
    */
    private void checkSession() {
        loginManager.checkSession();
        if (!isAuthenticated) {
            throw new SecurityException("Session expired. Please log in again.");
        }
    }

    private void requireAuth() {
        if (!isAuthenticated)
            throw new SecurityException("Not authenticated.");
        checkSession();
    }

    public void logout() {
        isAuthenticated = false;
        loginManager.logout();
    }

    public boolean isAuthenticated() { return isAuthenticated; }

    // ================= SEARCH =================

    private Account searchById(String id) {
        return databaseManager.getAccountByNumber(id);
    }

    private List<Account> searchByName(String name) {
        List<Account> results = new ArrayList<>();

        for (Account acc : databaseManager.retrieveAllAccounts()) {
            if (acc.getCustomer().getName().equalsIgnoreCase(name)) {
                results.add(acc);
            }
        }
        return results;
    }

    public List<Account> searchAccounts(String query) {
        requireAuth();

        // Search by account ID
        Account byId = searchById(query);
        if (byId != null) return List.of(byId);

        // Search by customer name
        List<Account> byName = searchByName(query);
        return byName;
    }

    // =============== TRANSACTIONS =================

    public Transaction assistTransaction(Account account, String type, double amount) {
        requireAuth();

        if (account == null) return null;
        if (account.getStatus().equals("FROZEN")) return null;

        Transaction tx = new Transaction(
                (int)(Math.random() * 1_000_000),
                amount,
                type,
                type.equalsIgnoreCase("withdraw") ? account : null,
                type.equalsIgnoreCase("deposit") ? account : null
        );

        if (!tx.validate(loginManager)) return null;

        tx.apply();
        databaseManager.updateAccount(account);

        return tx;
    }

    public Transaction assistTransaction(String accountId, String type, double amount) {
        requireAuth();
        Account account = databaseManager.getAccountByNumber(accountId);
        return assistTransaction(account, type, amount);
    }

    public Transaction assistTransfer(Account source, Account dest, double amount) {
        requireAuth();

        if (source == null || dest == null) return null;
        if (source.getStatus().equals("FROZEN") || dest.getStatus().equals("FROZEN"))
            return null;

        Transaction tx = new Transaction(
                (int)(Math.random() * 1_000_000),
                amount,
                "transfer",
                source,
                dest
        );

        if (!tx.validate(loginManager)) return null;

        tx.apply();
        databaseManager.updateAccount(source);
        databaseManager.updateAccount(dest);

        return tx;
    }

    public Transaction assistTransfer(String sourceId, String destId, double amount) {
        requireAuth();
        Account src = databaseManager.getAccountByNumber(sourceId);
        Account dst = databaseManager.getAccountByNumber(destId);
        return assistTransfer(src, dst, amount);
    }

    // ================= ACCOUNT CONTROL =================

    public boolean unfreezeAccount(String accountId) {
        requireAuth();

        Account acc = databaseManager.getAccountByNumber(accountId);
        if (acc == null) return false;
        if (!acc.getStatus().equals("FROZEN")) return false;

        acc.unfreezeAccount();
        databaseManager.updateAccount(acc);
        return true;
    }

    public List<Account> getFrozenAccounts() {
        requireAuth();

        List<Account> frozen = new ArrayList<>();
        for (Account acc : databaseManager.retrieveAllAccounts()) {
            if (acc.getStatus().equals("FROZEN")) {
                frozen.add(acc);
            }
        }
        return frozen;
    }

    public Account viewAccountDetails(String accountId) {
        requireAuth();
        return databaseManager.getAccountByNumber(accountId);
    }

}
