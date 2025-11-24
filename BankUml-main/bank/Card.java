package bank;

public class Card extends Account {

    public Card(Customer customer) {
        super(customer);
    }

    public Card(Customer customer, double initialBalance) {
        super(customer, initialBalance);
    }

    @Override
    public String getAccountType() {
        return "Card";
    }

    @Override
    public void pay() {
        System.out.println("Card payment for: " + customer.getName());
    }

    @Override
    public void receipt() {
        System.out.println("Card receipt for: " + customer.getName());
    }
}