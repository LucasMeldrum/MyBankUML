package bank;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;

public class IntegrationTests {

    private DatabaseManager db;
    private TellerDatabaseManager tellerDB;
    private TransactionsDatabaseManager txDB;
    private LoginManager loginManager;
    private Customer customer;
    private Teller teller;
    private Account testAccount;

    @Before
    public void setUp() {
        db = DatabaseManager.getInstance();
        tellerDB = TellerDatabaseManager.getInstance();
        txDB = TransactionsDatabaseManager.getInstance();
        loginManager = new LoginManager(20);
        
        customer = db.getCustomer("1");
        testAccount = db.getAccountByNumber("ACC101");
        teller = tellerDB.getTeller("T001");
        
        if (teller != null) {
            teller.setAuthenticated(true);
        }
        if (testAccount != null) {
            testAccount.unfreezeAccount();
        }
    }

    // INTEGRATION TEST #1 
    // Transaction + Account + DatabaseManager Integration
    
    @Test
    public void testTransactionAccountDatabaseIntegration() {
        testAccount.unfreezeAccount();
        db.updateAccount(testAccount);
        
        double initialBalance = testAccount.getBalance();
        double depositAmount = 250.0;
        
        loginManager.loginAttempt(true);
        
        Transaction tx = new Transaction(
            999001,
            depositAmount,
            "deposit",
            null,
            testAccount
        );
        
        boolean validated = tx.validate(loginManager);
        assertTrue("Transaction should validate", validated);
        assertEquals("Transaction status should be 'validated'", "validated", tx.getStatus());
        
        boolean applied = tx.apply();
        assertTrue("Transaction should apply successfully", applied);
        assertEquals("Transaction status should be 'completed'", "completed", tx.getStatus());
        
        assertEquals("Account balance should increase", 
            initialBalance + depositAmount, 
            testAccount.getBalance(), 
            0.01);
        
        db.updateAccount(testAccount);
        
        Account retrievedAccount = db.getAccountByNumber("ACC101");
        assertEquals("Database should persist balance change", 
            initialBalance + depositAmount, 
            retrievedAccount.getBalance(), 
            0.01);
        
        txDB.saveTransaction(tx);
        List<Transaction> transactions = txDB.loadTransactionsForAccount("ACC101");
        assertTrue("Transaction should be logged in database", transactions.size() > 0);
    }

    @Test
    public void testTransactionValidationPreventsInvalidOperation() {
        testAccount.freezeAccount();
        db.updateAccount(testAccount);
        
        double initialBalance = testAccount.getBalance();
        
        loginManager.loginAttempt(true);
        
        Transaction tx = new Transaction(999002, 100.0, "deposit", null, testAccount);
        
        tx.validate(loginManager);
        boolean applied = tx.apply();
        
        assertFalse("Transaction should fail on frozen account", applied);
        assertEquals("Balance should not change", initialBalance, testAccount.getBalance(), 0.01);
        
        testAccount.unfreezeAccount();
    }

    @Test
    public void testTransactionWithdrawalIntegration() {
        testAccount.unfreezeAccount();
        testAccount.updateBalance(1000);
        db.updateAccount(testAccount);
        
        double initialBalance = testAccount.getBalance();
        double withdrawAmount = 150.0;
        
        loginManager.loginAttempt(true);
        
        Transaction tx = new Transaction(999003, withdrawAmount, "withdraw", testAccount, null);
        
        tx.validate(loginManager);
        tx.apply();
        
        assertEquals("Balance should decrease", 
            initialBalance - withdrawAmount, 
            testAccount.getBalance(), 
            0.01);
        
        db.updateAccount(testAccount);
        Account retrieved = db.getAccountByNumber("ACC101");
        assertEquals("Database should persist withdrawal", 
            initialBalance - withdrawAmount, 
            retrieved.getBalance(), 
            0.01);
    }

    // INTEGRATION TEST #2 
    // Teller + Account + DatabaseManager Integration
    
    @Test
    public void testTellerSearchAccountIntegration() {
        Account aliceAccount = db.getAccountByNumber("ACC201");
        assertNotNull("Test account should exist", aliceAccount);
        
        List<Account> searchResults = teller.searchAccounts("Alice Smith");
        
        assertNotNull("Search should return results", searchResults);
        assertTrue("Should find at least one account", searchResults.size() > 0);
        
        Account foundAccount = searchResults.get(0);
        assertEquals("Should find Alice's account", "Alice Smith", foundAccount.getCustomer().getName());
        assertEquals("Account details should match database", 
            aliceAccount.getAccountNumber(), 
            foundAccount.getAccountNumber());
        assertEquals("Balance should match database", 
            aliceAccount.getBalance(), 
            foundAccount.getBalance(), 
            0.01);
    }

    @Test
    public void testTellerUnfreezeAccountIntegration() {
        testAccount.freezeAccount();
        db.updateAccount(testAccount);
        
        assertEquals("Account should be frozen", "FROZEN", testAccount.getStatus());
        
        boolean unfrozeResult = teller.unfreezeAccount("ACC101");
        
        assertTrue("Unfreeze should succeed", unfrozeResult);
        assertEquals("Account status should be ACTIVE", "ACTIVE", testAccount.getStatus());
        
        Account retrievedAccount = db.getAccountByNumber("ACC101");
        assertEquals("Database should persist status change", "ACTIVE", retrievedAccount.getStatus());
        assertFalse("Card stolen flag should be cleared", retrievedAccount.isCardStolen());
    }

    @Test
    public void testTellerAssistTransactionIntegration() {
        testAccount.unfreezeAccount();
        testAccount.updateBalance(500);
        db.updateAccount(testAccount);
        
        double initialBalance = testAccount.getBalance();
        double depositAmount = 300.0;
        
        Transaction tx = teller.assistTransaction(testAccount, "deposit", depositAmount);
        
        assertNotNull("Transaction should be created", tx);
        assertEquals("Account balance should increase", 
            initialBalance + depositAmount, 
            testAccount.getBalance(), 
            0.01);
        
        db.updateAccount(testAccount);
        Account retrieved = db.getAccountByNumber("ACC101");
        assertEquals("Database should persist teller-assisted transaction", 
            initialBalance + depositAmount, 
            retrieved.getBalance(), 
            0.01);
        
        List<Transaction> transactions = txDB.loadTransactionsForAccount("ACC101");
        assertTrue("Transaction should be logged", transactions.size() > 0);
    }

    @Test
    public void testTellerViewFrozenAccountsIntegration() {
        Account bobAccount = db.getAccountByNumber("ACC301");
        if (bobAccount != null) {
            bobAccount.freezeAccount();
            db.updateAccount(bobAccount);
        }
        
        List<Account> frozenAccounts = teller.getFrozenAccounts();
        
        assertNotNull("Should return frozen accounts list", frozenAccounts);
        assertTrue("Should find at least one frozen account", frozenAccounts.size() > 0);
        
        for (Account acc : frozenAccounts) {
            assertEquals("All returned accounts should be frozen", "FROZEN", acc.getStatus());
        }
        
        Account verifyInDB = db.getAccountByNumber("ACC301");
        if (verifyInDB != null) {
            assertEquals("Database should show frozen status", "FROZEN", verifyInDB.getStatus());
        }
    }

    //  INTEGRATION TEST #3 
    // Frozen Account Access Restriction
    
    @Test
    public void testFrozenAccountAccessRestriction() {
        testAccount.freezeAccount();
        db.updateAccount(testAccount);
        
        loginManager.loginAttempt(true);
        
        Transaction tx = new Transaction(999010, 100.0, "deposit", null, testAccount);
        tx.validate(loginManager);
        boolean result = tx.apply();
        
        assertFalse("Transactions should be blocked on frozen account", result);
        
        assertEquals("Account should remain frozen", "FROZEN", testAccount.getStatus());
        assertTrue("Card stolen flag should be set", testAccount.isCardStolen());
        
        testAccount.unfreezeAccount();
    }

    @Test
    public void testFrozenAccountCannotWithdraw() {
        testAccount.freezeAccount();
        testAccount.updateBalance(1000);
        db.updateAccount(testAccount);
        
        double initialBalance = testAccount.getBalance();
        
        loginManager.loginAttempt(true);
        boolean result = customer.withdraw(testAccount, 50.0, loginManager);
        
        assertFalse("Withdrawal should fail on frozen account", result);
        assertEquals("Balance should not change", initialBalance, testAccount.getBalance(), 0.01);
        
        testAccount.unfreezeAccount();
    }

    // INTEGRATION TEST #4
    // LoginManager + DatabaseManager Integration
    
    @Test
    public void testLoginManagerAuthenticationIntegration() {
        Teller authenticatedTeller = tellerDB.authenticate("T001", "SecurePass1");
        
        assertNotNull("Authentication should succeed with correct credentials", authenticatedTeller);
        assertEquals("Should return correct teller", "T001", authenticatedTeller.getEmployeeId());
        assertEquals("Teller data should match database", "Jane Smith", authenticatedTeller.getName());
    }

    @Test
    public void testLoginManagerFailedAuthenticationIntegration() {
        Teller result = tellerDB.authenticate("T001", "WrongPassword");
        
        assertNull("Authentication should fail with wrong password", result);
    }

    @Test
    public void testLoginManagerLockoutIntegration() {
        LoginManager lm = new LoginManager(20);
        
        lm.loginAttempt(false);
        lm.loginAttempt(false);
        lm.loginAttempt(false);
        
        assertTrue("Account should be locked after 3 failures", lm.isLockedOut());
        
        boolean lockedAttempt = lm.loginAttempt(true);
        assertFalse("Login should be denied during lockout", lockedAttempt);
    }

    @Test
    public void testLoginManagerSessionManagement() {
        LoginManager lm = new LoginManager(20);
        
        boolean loginSuccess = lm.loginAttempt(true);
        assertTrue("Login should succeed", loginSuccess);
        assertTrue("Session should be active", lm.isSessionActive());
        
        boolean sessionCheck = lm.checkSession();
        assertTrue("Session should remain active", sessionCheck);
        
        lm.logout();
        assertFalse("Session should end after logout", lm.isSessionActive());
    }

    // INTEGRATION TEST #5 
    // Customer + Transaction + Account + DatabaseManager Integration
    
    @Test
    public void testCustomerDepositIntegration() {
        testAccount.unfreezeAccount();
        testAccount.updateBalance(500);
        db.updateAccount(testAccount);
        
        double initialBalance = testAccount.getBalance();
        double depositAmount = 200.0;
        
        LoginManager lm = new LoginManager(20);
        lm.loginAttempt(true);
        
        boolean result = customer.deposit(testAccount, depositAmount, lm);
        
        assertTrue("Deposit should succeed", result);
        assertEquals("Account balance should increase", 
            initialBalance + depositAmount, 
            testAccount.getBalance(), 
            0.01);
        
        db.updateAccount(testAccount);
        Account retrieved = db.getAccountByNumber("ACC101");
        assertEquals("Database should persist deposit", 
            initialBalance + depositAmount, 
            retrieved.getBalance(), 
            0.01);
    }

    @Test
    public void testCustomerWithdrawalIntegration() {
        testAccount.unfreezeAccount();
        testAccount.updateBalance(1000);
        db.updateAccount(testAccount);
        
        double initialBalance = testAccount.getBalance();
        double withdrawAmount = 100.0;
        
        LoginManager lm = new LoginManager(20);
        lm.loginAttempt(true);
        
        boolean result = customer.withdraw(testAccount, withdrawAmount, lm);
        
        assertTrue("Withdrawal should succeed", result);
        assertEquals("Account balance should decrease", 
            initialBalance - withdrawAmount, 
            testAccount.getBalance(), 
            0.01);
        
        db.updateAccount(testAccount);
        Account retrieved = db.getAccountByNumber("ACC101");
        assertEquals("Database should persist withdrawal", 
            initialBalance - withdrawAmount, 
            retrieved.getBalance(), 
            0.01);
    }

    @Test
    public void testCustomerTransferIntegration() {
        Account sourceAccount = db.getAccountByNumber("ACC101");
        Account targetAccount = db.getAccountByNumber("ACC201");
        
        sourceAccount.unfreezeAccount();
        targetAccount.unfreezeAccount();
        sourceAccount.updateBalance(1500);
        db.updateAccount(sourceAccount);
        db.updateAccount(targetAccount);
        
        double sourceInitial = sourceAccount.getBalance();
        double targetInitial = targetAccount.getBalance();
        double transferAmount = 250.0;
        
        LoginManager lm = new LoginManager(20);
        lm.loginAttempt(true);
        
        boolean result = customer.transfer(sourceAccount, targetAccount, transferAmount, lm);
        
        assertTrue("Transfer should succeed", result);
        assertEquals("Source balance should decrease", 
            sourceInitial - transferAmount, 
            sourceAccount.getBalance(), 
            0.01);
        assertEquals("Target balance should increase", 
            targetInitial + transferAmount, 
            targetAccount.getBalance(), 
            0.01);
        
        db.updateAccount(sourceAccount);
        db.updateAccount(targetAccount);
        
        Account retrievedSource = db.getAccountByNumber("ACC101");
        Account retrievedTarget = db.getAccountByNumber("ACC201");
        
        assertEquals("Database should persist source balance", 
            sourceInitial - transferAmount, 
            retrievedSource.getBalance(), 
            0.01);
        assertEquals("Database should persist target balance", 
            targetInitial + transferAmount, 
            retrievedTarget.getBalance(), 
            0.01);
    }

    @Test
    public void testCustomerReportStolenCardIntegration() {
        testAccount.unfreezeAccount();
        db.updateAccount(testAccount);
        
        assertEquals("Account should start as ACTIVE", "ACTIVE", testAccount.getStatus());
        
        testAccount.freezeAccount();
        db.updateAccount(testAccount);
        
        assertEquals("Account should be frozen", "FROZEN", testAccount.getStatus());
        assertTrue("Card stolen flag should be set", testAccount.isCardStolen());
        
        Account retrieved = db.getAccountByNumber("ACC101");
        assertEquals("Database should persist frozen status", "FROZEN", retrieved.getStatus());
        
        LoginManager lm = new LoginManager(20);
        lm.loginAttempt(true);
        boolean depositResult = customer.deposit(testAccount, 100.0, lm);
        
        assertFalse("Transactions should be blocked on frozen account", depositResult);
        
        testAccount.unfreezeAccount();
    }

    // INTEGRATION TEST #6 
    // Multi-Module Data Consistency
    
    @Test
    public void testMultiModuleDataConsistency() {
        testAccount.unfreezeAccount();
        testAccount.updateBalance(1000);
        db.updateAccount(testAccount);
        
        double initialBalance = testAccount.getBalance();
        
        LoginManager lm = new LoginManager(20);
        lm.loginAttempt(true);
        
        customer.deposit(testAccount, 100.0, lm);
        customer.withdraw(testAccount, 50.0, lm);
        
        double expectedBalance = initialBalance + 100.0 - 50.0;
        
        assertEquals("Account object should reflect all changes", 
            expectedBalance, 
            testAccount.getBalance(), 
            0.01);
        
        db.updateAccount(testAccount);
        Account fromDB = db.getAccountByNumber("ACC101");
        assertEquals("Database should match account object", 
            expectedBalance, 
            fromDB.getBalance(), 
            0.01);
        
        List<Transaction> transactions = txDB.loadTransactionsForAccount("ACC101");
        assertNotNull("Transaction history should be available", transactions);
    }

    @Test
    public void testTellerCustomerDataConsistency() {
        testAccount.unfreezeAccount();
        testAccount.updateBalance(800);
        db.updateAccount(testAccount);
        
        double initialBalance = testAccount.getBalance();
        
        teller.assistTransaction(testAccount, "deposit", 200.0);
        
        LoginManager lm = new LoginManager(20);
        lm.loginAttempt(true);
        customer.withdraw(testAccount, 100.0, lm);
        
        double expectedBalance = initialBalance + 200.0 - 100.0;
        
        db.updateAccount(testAccount);
        Account fromDB = db.getAccountByNumber("ACC101");
        assertEquals("All operations should maintain data consistency", 
            expectedBalance, 
            fromDB.getBalance(), 
            0.01);
    }

    @Test
    public void testAccountStatusConsistencyAcrossModules() {
        testAccount.unfreezeAccount();
        db.updateAccount(testAccount);
        
        List<Account> allAccounts = db.retrieveAllAccounts();
        Account foundAccount = null;
        for (Account acc : allAccounts) {
            if (acc.getAccountNumber().equals("ACC101")) {
                foundAccount = acc;
                break;
            }
        }
        
        assertNotNull("Account should be found in database", foundAccount);
        assertEquals("Status should be ACTIVE", "ACTIVE", foundAccount.getStatus());
        
        testAccount.freezeAccount();
        db.updateAccount(testAccount);
        
        Account refetchedAccount = db.getAccountByNumber("ACC101");
        assertEquals("Status should update to FROZEN", "FROZEN", refetchedAccount.getStatus());
        
        List<Account> frozenAccounts = teller.getFrozenAccounts();
        boolean foundInFrozen = false;
        for (Account acc : frozenAccounts) {
            if (acc.getAccountNumber().equals("ACC101")) {
                foundInFrozen = true;
                break;
            }
        }
        assertTrue("Frozen account should appear in teller's frozen list", foundInFrozen);
        
        testAccount.unfreezeAccount();
    }
}