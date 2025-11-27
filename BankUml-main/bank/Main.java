package bank;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Launch GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new BankingSystemGUI();
        });
    }
}