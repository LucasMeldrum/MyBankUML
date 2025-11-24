import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

public class Teller {
    private String employeeId;
    private String name;
    private String email;
    private boolean isAuthenticated;
    
    private SearchEngine searchEngine;
    private SecurityManager securityManager;
    private DatabaseManager databaseManager;
    private AuditLogger auditLogger;
    
    public Teller(String employeeId, String name, String email) {
        this.employeeId = employeeId;
        this.name = name;
        this.email = email;
        this.isAuthenticated = false;
        this.searchEngine = new SearchEngine();
        this.securityManager = SecurityManager.getInstance();
        this.databaseManager = DatabaseManager.getInstance();
        this.auditLogger = AuditLogger.getInstance();
    }
    
    public boolean authenticate(String credentials) {
        boolean authorized = securityManager.authorize(employeeId, "TELLER");
        if (authorized) {
            this.isAuthenticated = true;
            auditLogger.logTellerAction(employeeId, "LOGIN", null, LocalDateTime.now());
        }
        return authorized;
    }
    
    public List<AccountInfo> searchAccounts(String searchCriteria) {
        if (!isAuthenticated) throw new SecurityException("Not authenticated");
        if (!securityManager.verifyAuthorization(employeeId, "SEARCH_ACCOUNTS")) {
            throw new SecurityException("Not authorized");
        }
        
        List<AccountInfo> results = searchEngine.search(searchCriteria);
        auditLogger.logTellerAction(employeeId, "SEARCH_ACCOUNTS", searchCriteria, LocalDateTime.now());
        return results;
    }
    
    public List<AccountInfo> searchAccountsByAttribute(String attribute, String value) {
        if (!isAuthenticated) throw new SecurityException("Not authenticated");
        if (!securityManager.verifyAuthorization(employeeId, "SEARCH_ACCOUNTS")) {
            throw new SecurityException("Not authorized");
        }
        
        List<AccountInfo> results = new ArrayList<>();
        switch (attribute.toUpperCase()) {
            case "ID":
                AccountInfo account = searchEngine.searchByID(value);
                if (account != null) results.add(account);
                break;
            case "NAME":
                results = searchEngine.searchByName(value);
                break;
            case "DOB":
                results = searchEngine.searchByDateOfBirth(value);
                break;
            default:
                throw new IllegalArgumentException("Invalid attribute: " + attribute);
        }
        
        auditLogger.logTellerAction(employeeId, "SEARCH_BY_" + attribute, value, LocalDateTime.now());
        return results;
    }
    
    public Transaction assistTransaction(String accountId, String transactionType, double amount) {
        if (!isAuthenticated) throw new SecurityException("Not authenticated");
        if (!securityManager.verifyAuthorization(employeeId, "ASSIST_TRANSACTION")) {
            throw new SecurityException("Not authorized");
        }
        
        AccountInfo account = databaseManager.retrieveAccount(accountId);
        if (account == null || account.getStatus().equals("FROZEN")) return null;
        
        Transaction transaction = new Transaction(accountId, transactionType, amount, employeeId);
        if (transaction.validate()) {
            transaction.apply(account);
            databaseManager.updateAccount(account);
            auditLogger.logTellerAction(employeeId, "ASSIST_TRANSACTION_SUCCESS", accountId, LocalDateTime.now());
            return transaction;
        }
        return null;
    }
    
    public Transaction assistTransfer(String sourceAccountId, String destinationAccountId, double amount) {
        if (!isAuthenticated) throw new SecurityException("Not authenticated");
        if (!securityManager.verifyAuthorization(employeeId, "ASSIST_TRANSACTION")) {
            throw new SecurityException("Not authorized");
        }
        
        AccountInfo sourceAccount = databaseManager.retrieveAccount(sourceAccountId);
        AccountInfo destAccount = databaseManager.retrieveAccount(destinationAccountId);
        
        if (sourceAccount == null || destAccount == null) return null;
        if (sourceAccount.getStatus().equals("FROZEN") || destAccount.getStatus().equals("FROZEN")) return null;
        
        Transaction transfer = new Transaction(sourceAccountId, "TRANSFER", amount, employeeId);
        transfer.setDestinationAccount(destinationAccountId);
        
        if (transfer.validate()) {
            transfer.apply(sourceAccount);
            destAccount.updateBalance(amount);
            databaseManager.updateAccount(sourceAccount);
            databaseManager.updateAccount(destAccount);
            auditLogger.logTellerAction(employeeId, "ASSIST_TRANSFER_SUCCESS", sourceAccountId, LocalDateTime.now());
            return transfer;
        }
        return null;
    }
    
    public boolean unfreezeAccount(String accountId) {
        if (!isAuthenticated) throw new SecurityException("Not authenticated");
        if (!securityManager.verifyAuthorization(employeeId, "UNFREEZE_ACCOUNT")) {
            throw new SecurityException("Not authorized");
        }
        
        AccountInfo account = databaseManager.retrieveAccount(accountId);
        if (account == null || !account.getStatus().equals("FROZEN")) return false;
        
        account.unfreezeAccount();
        databaseManager.updateAccount(account);
        securityManager.handleUnfreeze(accountId, employeeId);
        auditLogger.logTellerAction(employeeId, "UNFREEZE_ACCOUNT", accountId, LocalDateTime.now());
        return true;
    }
    
    public AccountInfo viewAccountDetails(String accountId) {
        if (!isAuthenticated) throw new SecurityException("Not authenticated");
        AccountInfo account = databaseManager.retrieveAccount(accountId);
        if (account != null) {
            auditLogger.logTellerAction(employeeId, "VIEW_ACCOUNT_DETAILS", accountId, LocalDateTime.now());
        }
        return account;
    }
    
    public List<AccountInfo> getFrozenAccounts() {
        if (!isAuthenticated) throw new SecurityException("Not authenticated");
        
        List<AccountInfo> frozenAccounts = new ArrayList<>();
        for (AccountInfo account : databaseManager.retrieveAllAccounts()) {
            if (account.getStatus().equals("FROZEN")) frozenAccounts.add(account);
        }
        auditLogger.logTellerAction(employeeId, "VIEW_FROZEN_ACCOUNTS", null, LocalDateTime.now());
        return frozenAccounts;
    }
    
    public void logout() {
        if (isAuthenticated) {
            auditLogger.logTellerAction(employeeId, "LOGOUT", null, LocalDateTime.now());
            this.isAuthenticated = false;
        }
    }
    
    public String getEmployeeId() { return employeeId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public boolean isAuthenticated() { return isAuthenticated; }
}