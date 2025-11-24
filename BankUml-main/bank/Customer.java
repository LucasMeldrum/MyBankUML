package bank;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Customer {

    private String name;
    private String address;
    private String phone;
    private String email;
    private String accountNumber;

    // Constructor
    public Customer(String name) {
        this.name = name;
    }

    // Constructor with full recipient info
    public Customer(String name, String address, String phone, String email, String accountNumber) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.accountNumber = accountNumber;
    }

    // Display customers info
    public void printCustomerInfo() {
        System.out.println("Customer's info: " );
        System.out.println("Name: " + name);
        if (address != null) System.out.println("Address: " + address);
        if (phone != null) System.out.println("Phone: " + phone);
        if (email != null) System.out.println("Email: " + email);
        if (accountNumber != null) System.out.println("Account Number: " + accountNumber);
    }

    // Display recipient info
    public void printRecipientInfo() {
        System.out.println("Recipient Info: ");
        System.out.println("Name: " + name);
        System.out.println("Account Number: " + (accountNumber != null ? accountNumber : "N/A"));
    }
}

