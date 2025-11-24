package bank;

import java.util.List;
import java.util.ArrayList;

public class Teller {
    private String employeeId;
    private String name;
    private String email;
    private boolean isAuthenticated;

    private LoginManager loginManager;
    private DatabaseManager databaseManager;

    public Teller(String employeeId, String name, String email) {
        this.employeeId = employeeId;
        this.name = name;
        this.email = email;
        this.isAuthenticated = false;

        this.loginManager = new LoginManager(20);
        this.databaseManager = DatabaseManager.getInstance();
    }

    // AUTHENTICATION
    public boolean login(String username, String password) {
        boolean ok = loginManager.login(username, password);
        if (ok) {
            isAuthenticated = true;
        }
        return ok;
    }

    private void checkSession() {
        loginManager.manageSession();
        if (!loginManagerSessionActive()) {
            isAuthenticated = false;
            throw new SecurityException("Session expired. Login required.");
        }
    }

    private boolean loginManagerSessionActive() {
        try {
            loginManager.manageSession();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void touchSession() {
        if (isAuthenticated) {
            loginManager.startSession();
        }
    }

    private void requireAuth() {
        if (!isAuthenticated)
            throw new SecurityException("Not authenticated.");
        checkSession();
        touchSession();
    }

    // SEARCH METHODS
    private Account searchById(String id) {
        return databaseManager.getAccountNumber(id);
    }

    private List<Account> searchByName(String name) {
        List<Account> results = new ArrayList<>();
        for (Account acc : databaseManager.retrieveAllAccounts()) {
            if (acc.getOwner().getName().equalsIgnoreCase(name)) {
                results.add(acc);
            }
        }
        return results;
    }

    private List<Account> searchByDateOfBirth(String dob) {
        List<Account> results = new ArrayList<>();
        for (Account acc : databaseManager.retrieveAllAccounts()) {
            String accountDob = acc.getCustomer().getDateOfBirth();
            if (accountDob != null && accountDob.equals(dob)) {
                results.add(acc);
            }
        }
        return results;
    }

    public List<Account> searchAccounts(String query) {
        requireAuth();

        List<Account> results = new ArrayList<>();

        Account byId = searchById(query);
        if (byId != null) {
            results.add(byId);
            return results;
        }

        results = searchByName(query);
        if (!results.isEmpty()) return results;

        return searchByDateOfBirth(query);
    }

    public List<Account> searchAccountsByAttribute(String attribute, String value) {
        requireAuth();

        switch (attribute.toUpperCase()) {
            case "ID":
                Account acc = searchById(value);
                return acc == null ? new ArrayList<>() : List.of(acc);

            case "NAME":
                return searchByName(value);

            case "DOB":
                return searchByDateOfBirth(value);

            default:
                throw new IllegalArgumentException("Unknown search type: " + attribute);
        }
    }

    // TRANSACTIONS
    public Transaction assistTransaction(String accountId, String transactionType, double amount) {
        requireAuth();

        Account account = databaseManager.getAccountNumber(accountId);
        if (account == null || account.getStatus().equals("FROZEN"))
            return null;

        Transaction tx = new Transaction(
                (int)(Math.random() * 1000000),
                amount,
                transactionType,
                transactionType.equalsIgnoreCase("withdraw") ? account : null,
                transactionType.equalsIgnoreCase("deposit") ? account : null
        );

        if (tx.validate(loginManager)) {
            tx.apply();
            databaseManager.updateAccount(account);
            return tx;
        }

        return null;
    }

    public Transaction assistTransfer(String sourceId, String destId, double amount) {
        requireAuth();

        Account src = databaseManager.getAccountNumber(sourceId);
        Account dst = databaseManager.getAccountNumber(destId);

        if (src == null || dst == null) return null;
        if (src.getStatus().equals("FROZEN") || dst.getStatus().equals("FROZEN")) return null;

        Transaction tx = new Transaction(
                (int)(Math.random() * 1000000),
                amount,
                "transfer",
                src,
                dst
        );

        if (tx.validate(loginManager)) {
            tx.apply();
            databaseManager.updateAccount(src);
            databaseManager.updateAccount(dst);
            return tx;
        }

        return null;
    }

    // ACCOUNT MANAGEMENT
    public boolean unfreezeAccount(String accountId) {
        requireAuth();

        Account account = databaseManager.getAccountNumber(accountId);
        if (account == null || !account.getStatus().equals("FROZEN"))
            return false;

        account.unfreezeAccount();
        databaseManager.updateAccount(account);
        return true;
    }

    public Account viewAccountDetails(String accountId) {
        requireAuth();
        return databaseManager.getAccountNumber(accountId);
    }

    public List<Account> getFrozenAccounts() {
        requireAuth();

        List<Account> frozen = new ArrayList<>();
        for (Account acc : databaseManager.retrieveAllAccounts()) {
            if (acc.getStatus().equals("FROZEN"))
                frozen.add(acc);
        }
        return frozen;
    }

    public void logout() {
        isAuthenticated = false;
        loginManager.logout();
    }

    // GETTERS
    public String getEmployeeId() { return employeeId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public boolean isAuthenticated() { return isAuthenticated; }
}