package bank;

public class Card extends Account {

    public Card(Customer customer) {
        super(customer, AccountType.CARD);
    }

    public Card(Customer customer, double initialBalance) {
        super(customer, AccountType.CARD, initialBalance);
    }

    // Card accounts currently have no extra behaviors specific to card,
    // but you can add custom methods here if needed later.
}
