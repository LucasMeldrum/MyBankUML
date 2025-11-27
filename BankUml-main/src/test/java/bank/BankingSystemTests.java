package bank;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Collection;

public class BankingSystemTests {

    private DatabaseManager db;
    private TellerDatabaseManager tellerDB;
    private LoginManager loginManager;
    private Customer customer;
    private Teller teller;
    private Account account;

    @Before
    public void setUp() {
        db = DatabaseManager.getInstance();
        tellerDB = TellerDatabaseManager.getInstance();
        loginManager = new LoginManager(20);
        customer = db.getCustomer("1");
        account = db.getAccountByNumber("ACC101");
        teller = tellerDB.getTeller("T001");
        if (teller != null) {
            teller.setAuthenticated(true);
        }
        loginManager.loginAttempt(true);
    }

    // CUSTOMER TESTS 

    @Test
    public void testCustomerDepositValid() {
        double initialBalance = account.getBalance();
        
        boolean result = customer.deposit(account, 200.0, loginManager);
        
        assertTrue(result);
        assertEquals(initialBalance + 200.0, account.getBalance(), 0.01);
    }

    @Test
    public void testCustomerDepositInvalid() {
        double initialBalance = account.getBalance();
        
        boolean result = customer.deposit(account, -50.0, loginManager);
        
        assertFalse(result);
        assertEquals(initialBalance, account.getBalance(), 0.01);
    }

    @Test
    public void testCustomerWithdrawValid() {
        account.unfreezeAccount();
        account.updateBalance(1000);
        double initialBalance = account.getBalance();
        
        LoginManager lm = new LoginManager(20);
        lm.loginAttempt(true);
        
        boolean result = customer.withdraw(account, 100.0, lm);
        
        assertTrue(result);
        assertEquals(initialBalance - 100.0, account.getBalance(), 0.01);
    }

    @Test
    public void testCustomerWithdrawInsufficientFunds() {
        double withdrawAmount = account.getBalance() + 1000;
        double initialBalance = account.getBalance();
        
        boolean result = customer.withdraw(account, withdrawAmount, loginManager);
        
        assertFalse(result);
        assertEquals(initialBalance, account.getBalance(), 0.01);
    }

    @Test
    public void testCustomerTransferValid() {
        Account sourceAccount = db.getAccountByNumber("ACC101");
        Account targetAccount = db.getAccountByNumber("ACC201");
        
        sourceAccount.updateBalance(1000);
        double sourceInitial = sourceAccount.getBalance();
        double targetInitial = targetAccount.getBalance();
        
        boolean result = customer.transfer(sourceAccount, targetAccount, 100.0, loginManager);
        
        assertTrue(result);
        assertEquals(sourceInitial - 100.0, sourceAccount.getBalance(), 0.01);
        assertEquals(targetInitial + 100.0, targetAccount.getBalance(), 0.01);
    }

    @Test
    public void testCustomerTransferNonExistent() {
        double initialBalance = account.getBalance();
        
        try {
            boolean result = customer.transfer(account, null, 50.0, loginManager);
            assertFalse(result);
        } catch (NullPointerException e) {
            
        }
        
        assertEquals(initialBalance, account.getBalance(), 0.01);
    }

    @Test
    public void testCustomerViewAccount() {
        assertNotNull(account);
        assertEquals("ACC101", account.getAccountNumber());
        assertTrue(account.getBalance() >= 0);
    }

    @Test
    public void testCustomerReportStolenCard() {
        account.unfreezeAccount();
        
        account.freezeAccount();
        
        assertEquals("FROZEN", account.getStatus());
        assertTrue(account.isCardStolen());
    }

    @Test
    public void testCustomerMultipleAccounts() {
        Customer johnDoe = db.getCustomer("1");
        
        assertNotNull(johnDoe);
        assertTrue(johnDoe.getAccounts().size() >= 1);
    }

    @Test
    public void testCustomerTransactionHistory() {
        customer.deposit(account, 50.0, loginManager);
        
        assertNotNull(account.getTransactions());
        assertTrue(account.getTransactions().size() > 0);
    }

    @Test
    public void testCustomerDepositFrozenAccount() {
        account.freezeAccount();
        double initialBalance = account.getBalance();
        
        boolean result = customer.deposit(account, 100.0, loginManager);
        
        assertFalse(result);
        assertEquals(initialBalance, account.getBalance(), 0.01);
        
        account.unfreezeAccount();
    }

    // TELLER TESTS

    @Test
    public void testTellerAuthentication() {
        Teller authenticated = tellerDB.authenticate("T001", "SecurePass1");
        
        assertNotNull(authenticated);
        assertEquals("T001", authenticated.getEmployeeId());
    }

    @Test
    public void testTellerAuthenticationWrong() {
        Teller result = tellerDB.authenticate("T001", "WrongPassword");
        
        assertNull(result);
    }

    @Test
    public void testTellerSearchByName() {
        List<Account> results = teller.searchAccounts("John Doe");
        
        assertNotNull(results);
        assertTrue(results.size() > 0);
        assertEquals("John Doe", results.get(0).getCustomer().getName());
    }

    @Test
    public void testTellerSearchByID() {
        List<Account> results = teller.searchAccounts("ACC101");
        
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("ACC101", results.get(0).getAccountNumber());
    }

    @Test
    public void testTellerSearchNoResults() {
        List<Account> results = teller.searchAccounts("NonExistent");
        
        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    public void testTellerViewAllAccounts() {
        List<Account> allAccounts = db.retrieveAllAccounts();
        
        assertNotNull(allAccounts);
        assertTrue(allAccounts.size() > 0);
    }

    @Test
    public void testTellerAssistDeposit() {
        account.unfreezeAccount();
        double initialBalance = account.getBalance();
        
        Transaction result = teller.assistTransaction(account, "deposit", 100.0);
        
        assertNotNull(result);
        assertEquals(initialBalance + 100.0, account.getBalance(), 0.01);
    }

    @Test
    public void testTellerAssistWithdraw() {
        account.unfreezeAccount();
        account.updateBalance(500);
        double initialBalance = account.getBalance();
        
        Transaction result = teller.assistTransaction(account, "withdraw", 50.0);
        
        assertNotNull(result);
        assertEquals(initialBalance - 50.0, account.getBalance(), 0.01);
    }

    @Test
    public void testTellerAssistFrozenAccount() {
        account.freezeAccount();
        
        Transaction result = teller.assistTransaction(account, "deposit", 100.0);
        
        assertNull(result);
        
        account.unfreezeAccount();
    }

    @Test
    public void testTellerAssistTransfer() {
        Account sourceAccount = db.getAccountByNumber("ACC101");
        Account destAccount = db.getAccountByNumber("ACC201");
        
        sourceAccount.unfreezeAccount();
        destAccount.unfreezeAccount();
        sourceAccount.updateBalance(1000);
        
        double sourceInitial = sourceAccount.getBalance();
        double destInitial = destAccount.getBalance();
        
        Transaction result = teller.assistTransfer(sourceAccount, destAccount, 200.0);
        
        assertNotNull(result);
        assertEquals(sourceInitial - 200.0, sourceAccount.getBalance(), 0.01);
        assertEquals(destInitial + 200.0, destAccount.getBalance(), 0.01);
    }

    @Test
    public void testTellerViewFrozenAccounts() {
        Account bobAccount = db.getAccountByNumber("ACC301");
        if (bobAccount != null) {
            bobAccount.freezeAccount();
            db.updateAccount(bobAccount);
        }
        
        List<Account> frozenAccounts = teller.getFrozenAccounts();
        
        assertNotNull(frozenAccounts);
        assertTrue(frozenAccounts.size() > 0);
        
        for (Account acc : frozenAccounts) {
            assertEquals("FROZEN", acc.getStatus());
        }
    }

    @Test
    public void testTellerUnfreezeAccount() {
        account.freezeAccount();
        db.updateAccount(account);
        
        boolean result = teller.unfreezeAccount("ACC101");
        
        assertTrue(result);
        assertEquals("ACTIVE", account.getStatus());
    }

    @Test
    public void testTellerUnfreezeAlreadyActive() {
        account.unfreezeAccount();
        db.updateAccount(account);
        
        boolean result = teller.unfreezeAccount("ACC101");
        
        assertFalse(result);
    }

    @Test
    public void testTellerViewAccountDetails() {
        Account result = teller.viewAccountDetails("ACC101");
        
        assertNotNull(result);
        assertEquals("ACC101", result.getAccountNumber());
    }

    @Test
    public void testTellerLogout() {
        teller.logout();
        
        assertFalse(teller.isAuthenticated());
    }

    // ADMIN TESTS

    @Test
    public void testAdminViewAllTellers() {
        Collection<Teller> tellers = tellerDB.getAllTellers();
        
        assertNotNull(tellers);
        assertTrue(tellers.size() >= 3);
    }

    @Test
    public void testAdminGetSpecificTeller() {
        Teller result = tellerDB.getTeller("T001");
        
        assertNotNull(result);
        assertEquals("T001", result.getEmployeeId());
        assertEquals("Jane Smith", result.getName());
    }

    @Test
    public void testAdminGetNonExistentTeller() {
        Teller result = tellerDB.getTeller("T999");
        
        assertNull(result);
    }

    @Test
    public void testAdminAddTeller() {
        int initialCount = tellerDB.getAllTellers().size();
        
        Teller newTeller = new Teller("T999", "Test Teller", "test@bank.com", "TestPass");
        tellerDB.addTeller(newTeller);
        
        assertEquals(initialCount + 1, tellerDB.getAllTellers().size());
        assertNotNull(tellerDB.getTeller("T999"));
        
        tellerDB.removeTeller("T999");
    }

    @Test
    public void testAdminRemoveTeller() {
        Teller tempTeller = new Teller("T777", "Temp", "temp@bank.com", "Pass");
        tellerDB.addTeller(tempTeller);
        
        int countBefore = tellerDB.getAllTellers().size();
        tellerDB.removeTeller("T777");
        int countAfter = tellerDB.getAllTellers().size();
        
        assertEquals(countBefore - 1, countAfter);
        assertNull(tellerDB.getTeller("T777"));
    }

    @Test
    public void testAdminAuthenticateWithEmail() {
        Teller result = tellerDB.authenticate("jane.smith@bank.com", "SecurePass1");
        
        assertNotNull(result);
        assertEquals("T001", result.getEmployeeId());
    }

    @Test
    public void testAdminTellerPersistence() {
        Teller newTeller = new Teller("T888", "Sarah", "sarah@bank.com", "Pass123");
        tellerDB.addTeller(newTeller);
        
        Teller retrieved = tellerDB.getTeller("T888");
        
        assertNotNull(retrieved);
        assertEquals("Sarah", retrieved.getName());
        
        tellerDB.removeTeller("T888");
    }

    //LOGIN MANAGER TESTS 

    @Test
    public void testLoginSuccess() {
        LoginManager lm = new LoginManager(20);
        
        boolean result = lm.loginAttempt(true);
        
        assertTrue(result);
        assertTrue(lm.isSessionActive());
    }

    @Test
    public void testLoginFail() {
        LoginManager lm = new LoginManager(20);
        
        boolean result = lm.loginAttempt(false);
        
        assertFalse(result);
        assertFalse(lm.isSessionActive());
    }

    @Test
    public void testLoginLockout() {
        LoginManager lm = new LoginManager(20);
        
        lm.loginAttempt(false);
        lm.loginAttempt(false);
        lm.loginAttempt(false);
        
        assertTrue(lm.isLockedOut());
        
        boolean result = lm.loginAttempt(true);
        assertFalse(result);
    }

    @Test
    public void testSessionActive() {
        LoginManager lm = new LoginManager(20);
        lm.loginAttempt(true);
        
        boolean result = lm.checkSession();
        
        assertTrue(result);
        assertTrue(lm.isSessionActive());
    }

    @Test
    public void testLogout() {
        LoginManager lm = new LoginManager(20);
        lm.loginAttempt(true);
        
        lm.logout();
        
        assertFalse(lm.isSessionActive());
    }

    // DATABASE TESTS

    @Test
    public void testDatabaseGetAccount() {
        Account result = db.getAccountByNumber("ACC101");
        
        assertNotNull(result);
        assertEquals("ACC101", result.getAccountNumber());
    }

    @Test
    public void testDatabaseGetAccountNotFound() {
        Account result = db.getAccountByNumber("ACC99999");
        
        assertNull(result);
    }

    @Test
    public void testDatabaseRetrieveAll() {
        List<Account> accounts = db.retrieveAllAccounts();
        
        assertNotNull(accounts);
        assertTrue(accounts.size() > 0);
    }

    @Test
    public void testDatabaseUpdateAccount() {
        Account acc = db.getAccountByNumber("ACC101");
        double newBalance = 12345.67;
        
        acc.updateBalance(newBalance - acc.getBalance());
        db.updateAccount(acc);
        
        Account updated = db.getAccountByNumber("ACC101");
        assertEquals(newBalance, updated.getBalance(), 0.01);
    }

    @Test
    public void testDatabaseGetCustomer() {
        Customer result = db.getCustomer("1");
        
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    public void testDatabaseGetCustomerByName() {
        Customer result = db.getCustomerByName("John Doe");
        
        assertNotNull(result);
        assertEquals(1, result.getCustomerId());
    }

    // TRANSACTION TESTS 

    @Test
    public void testTransactionValidateValid() {
        Transaction tx = new Transaction(123, 100.0, "deposit", null, account);
        
        boolean result = tx.validate(loginManager);
        
        assertTrue(result);
        assertEquals("validated", tx.getStatus());
    }

    @Test
    public void testTransactionValidateInvalid() {
        Transaction tx = new Transaction(124, -50.0, "deposit", null, account);
        
        boolean result = tx.validate(loginManager);
        
        assertFalse(result);
        assertEquals("invalid amount", tx.getStatus());
    }

    @Test
    public void testTransactionApplyDeposit() {
        double initialBalance = account.getBalance();
        Transaction tx = new Transaction(125, 100.0, "deposit", null, account);
        
        tx.validate(loginManager);
        boolean result = tx.apply();
        
        assertTrue(result);
        assertEquals(initialBalance + 100.0, account.getBalance(), 0.01);
    }

    @Test
    public void testTransactionApplyWithdraw() {
        account.updateBalance(200);
        double initialBalance = account.getBalance();
        Transaction tx = new Transaction(126, 50.0, "withdraw", account, null);
        
        tx.validate(loginManager);
        boolean result = tx.apply();
        
        assertTrue(result);
        assertEquals(initialBalance - 50.0, account.getBalance(), 0.01);
    }

    @Test
    public void testTransactionFrozenAccount() {
        account.freezeAccount();
        Transaction tx = new Transaction(127, 50.0, "deposit", null, account);
        
        tx.validate(loginManager);
        boolean result = tx.apply();
        
        assertFalse(result);
        
        account.unfreezeAccount();
    }

    //  ACCOUNT TYPE TESTS

    @Test
    public void testCheckingAccountOverdraft() {
        Customer cust = new Customer("Test");
        Checking checking = new Checking(cust, 100.0);
        
        double available = checking.getAvailableBalance();
        
        assertEquals(600.0, available, 0.01);
    }

    @Test
    public void testCardAccountCreation() {
        Customer cust = new Customer("Test");
        Card card = new Card(cust, 250.0);
        
        assertEquals(250.0, card.getBalance(), 0.01);
        assertEquals(AccountType.CARD, card.getType());
    }

    @Test
    public void testSavingAccountCreation() {
        Customer cust = new Customer("Test");
        Saving saving = new Saving(cust, 5000.0);
        
        assertEquals(5000.0, saving.getBalance(), 0.01);
        assertEquals(AccountType.SAVING, saving.getType());
    }
}