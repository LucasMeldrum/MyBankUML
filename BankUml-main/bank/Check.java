package bank;

public class Check extends Account {

    public Check(Customer customer) {
        super(customer, AccountType.CHECK);
    }

    public Check(Customer customer, double initialBalance) {
        super(customer, AccountType.CHECK, initialBalance);
    }

    // Add Check-specific behavior if needed later.
}
