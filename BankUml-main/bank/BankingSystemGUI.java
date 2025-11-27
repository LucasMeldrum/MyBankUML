package bank;

import javax.swing.*;
import java.awt.*;

public class BankingSystemGUI {

    private JFrame frame;
    private DatabaseManager db;
    private LoginManager loginManager;
    private Teller currentTeller;
    private Customer currentCustomer;

    private final String ADMIN_USER = "admin";
    private final String ADMIN_PASS = "admin";

    public BankingSystemGUI() {
        this.db = DatabaseManager.getInstance();
        this.loginManager = new LoginManager(20);

        // Set system look and feel for native Mac appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // Make UI components have rounded corners
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextField.arc", 10);
        } catch (Exception e) {
            e.printStackTrace();
        }

        createMainFrame();
        showMainMenu();
    }

    private void createMainFrame() {
        frame = new JFrame("Banking System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null); // Center on screen
    }

    // ==================== SESSION CHECK HELPERS ====================
    private boolean checkTellerSession() {
        if (!loginManager.checkSession()) {
            JOptionPane.showMessageDialog(frame,
                    "Session expired. Please login again.",
                    "Session Expired",
                    JOptionPane.WARNING_MESSAGE);
            currentTeller = null;
            showMainMenu();
            return false;
        }
        loginManager.refreshSession();
        return true;
    }

    private boolean checkCustomerSession() {
        if (!loginManager.checkSession()) {
            JOptionPane.showMessageDialog(frame,
                    "Session expired. Please login again.",
                    "Session Expired",
                    JOptionPane.WARNING_MESSAGE);
            currentCustomer = null;
            showMainMenu();
            return false;
        }
        loginManager.refreshSession();
        return true;
    }

    // ==================== MAIN MENU ====================
    private void showMainMenu() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(Color.WHITE); // Clean white background
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("MyBankUML App");
        title.setFont(new Font("Segoe UI", Font.BOLD, 38));
        title.setForeground(new Color(0, 128, 55)); // TD Green
        title.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        gbc.insets = new Insets(30, 10, 25, 10);
        panel.add(title, gbc);

        // Segmented control for user type (smaller)
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 10, 20, 10);
        JPanel segmentedControl = createSegmentedControl();
        panel.add(segmentedControl, gbc);

        // Username field
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 10, 5, 10);
        JLabel userLabel = new JLabel("Email or Account ID");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userLabel.setForeground(new Color(100, 100, 100));
        panel.add(userLabel, gbc);

        JTextField usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        usernameField.setPreferredSize(new Dimension(350, 45));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(22, new Color(200, 200, 200)), // Pill shape
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        gbc.gridy = 3;
        gbc.insets = new Insets(5, 10, 10, 10);
        panel.add(usernameField, gbc);

        // Password field
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 10, 5, 10);
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passLabel.setForeground(new Color(100, 100, 100));
        panel.add(passLabel, gbc);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passwordField.setPreferredSize(new Dimension(350, 45));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(22, new Color(200, 200, 200)), // Pill shape
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        gbc.gridy = 5;
        gbc.insets = new Insets(5, 10, 25, 10); // More bottom margin since no forgot password link
        panel.add(passwordField, gbc);

        // Sign In button - TD Green with rounded corners
        JButton signInBtn = new JButton("Sign In") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        signInBtn.setFont(new Font("Segoe UI", Font.BOLD, 17));
        signInBtn.setPreferredSize(new Dimension(350, 50));
        signInBtn.setForeground(Color.WHITE);
        signInBtn.setBackground(new Color(0, 150, 65)); // TD Green
        signInBtn.setFocusPainted(false);
        signInBtn.setBorderPainted(false);
        signInBtn.setContentAreaFilled(false);
        signInBtn.setOpaque(false);
        signInBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        signInBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                signInBtn.setBackground(new Color(0, 170, 75));
                signInBtn.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                signInBtn.setBackground(new Color(0, 150, 65));
                signInBtn.repaint();
            }
        });

        signInBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill in all fields.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String selectedType = getSelectedUserType();
            switch (selectedType) {
                case "Customer" -> handleCustomerLogin(username, password);
                case "Teller" -> handleTellerLogin(username, password);
                case "Admin" -> handleAdminLogin(username, password);
            }
        });

        gbc.gridy = 6;
        gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(signInBtn, gbc);

        // Exit button - fully rounded
        JButton exitBtn = new JButton("Exit") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 19, 19);
                g2.setColor(new Color(200, 200, 200));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 19, 19);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        exitBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        exitBtn.setPreferredSize(new Dimension(350, 38));
        exitBtn.setForeground(new Color(120, 120, 120));
        exitBtn.setBackground(Color.WHITE);
        exitBtn.setFocusPainted(false);
        exitBtn.setBorderPainted(false);
        exitBtn.setContentAreaFilled(false);
        exitBtn.setOpaque(false);
        exitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitBtn.addActionListener(e -> System.exit(0));

        gbc.gridy = 7;
        gbc.insets = new Insets(10, 10, 30, 10);
        panel.add(exitBtn, gbc);

        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    // Rounded border class for text fields
    static class RoundedBorder extends javax.swing.border.AbstractBorder {
        private int radius;
        private Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 2, 2);
        }
    }

    // Segmented control buttons
    private JButton customerBtn, tellerBtn, adminBtn;

    private JPanel createSegmentedControl() {
        JPanel segmentPanel = new JPanel();
        segmentPanel.setLayout(new GridLayout(1, 3, 20, 0)); // More spacing between cards
        segmentPanel.setPreferredSize(new Dimension(450, 80)); // Adjust height without icons
        segmentPanel.setBackground(Color.WHITE);
        segmentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Create three card buttons for the segmented control
        customerBtn = createSegmentCard("Customer", "", true);
        tellerBtn = createSegmentCard("Teller", "", false);
        adminBtn = createSegmentCard("Admin", "", false);

        // Add click listeners to toggle selection
        customerBtn.addActionListener(e -> selectSegment(customerBtn));
        tellerBtn.addActionListener(e -> selectSegment(tellerBtn));
        adminBtn.addActionListener(e -> selectSegment(adminBtn));

        segmentPanel.add(customerBtn);
        segmentPanel.add(tellerBtn);
        segmentPanel.add(adminBtn);

        return segmentPanel;
    }

    private JButton createSegmentCard(String text, String emoji, boolean selected) {
        JButton btn = new JButton(text) {
            private boolean isSelected = selected;
            
            public void setSelected(boolean sel) {
                isSelected = sel;
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw card background with shadow effect
                if (isSelected) {
                    g2.setColor(new Color(0, 150, 65, 30)); // Slight shadow
                    g2.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 15, 15);
                }

                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 15, 15);

                // Draw border
                if (isSelected) {
                    g2.setColor(new Color(0, 150, 65));
                    g2.setStroke(new BasicStroke(2.5f));
                } else {
                    g2.setColor(new Color(220, 220, 220));
                    g2.setStroke(new BasicStroke(1.5f));
                }
                g2.drawRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 15, 15);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(false);
        btn.setVerticalAlignment(SwingConstants.CENTER);
        btn.setHorizontalAlignment(SwingConstants.CENTER);

        if (selected) {
            btn.setBackground(new Color(245, 255, 250)); // Light green tint
            btn.setForeground(new Color(0, 150, 65));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(100, 100, 100));
        }

        return btn;
    }

    private void selectSegment(JButton selectedBtn) {
        // Reset all buttons to unselected state
        resetSegmentButton(customerBtn, false);
        resetSegmentButton(tellerBtn, false);
        resetSegmentButton(adminBtn, false);

        // Highlight selected button
        resetSegmentButton(selectedBtn, true);
    }

    private void resetSegmentButton(JButton btn, boolean selected) {
        if (selected) {
            btn.setBackground(new Color(245, 255, 250)); // Light green tint
            btn.setForeground(new Color(0, 150, 65));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(100, 100, 100));
        }
        // Update the custom button's selection state
        try {
            btn.getClass().getMethod("setSelected", boolean.class).invoke(btn, selected);
        } catch (Exception e) {
            // Ignore if method doesn't exist
        }
        btn.repaint();
    }

    private String getSelectedUserType() {
        if (customerBtn.getBackground().equals(new Color(245, 255, 250))) {
            return "Customer";
        } else if (tellerBtn.getBackground().equals(new Color(245, 255, 250))) {
            return "Teller";
        } else {
            return "Admin";
        }
    }

    private void handleCustomerLogin(String id, String password) {
        Customer customer = db.getCustomer(id);

        if (customer == null) {
            loginManager.loginAttempt(false);
            JOptionPane.showMessageDialog(frame, "Customer not found.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean correct = customer.getPassword().equals(password);

        if (!loginManager.loginAttempt(correct)) {
            JOptionPane.showMessageDialog(frame, "Incorrect password.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        currentCustomer = customer;
        loginManager.refreshSession();
        JOptionPane.showMessageDialog(frame, "Welcome, " + customer.getName(),
                "Success", JOptionPane.INFORMATION_MESSAGE);
        selectCustomerAccount();
    }

    private void handleTellerLogin(String username, String password) {
        Teller teller = TellerDatabaseManager.getInstance().authenticate(username, password);
        boolean correct = (teller != null);

        if (!loginManager.loginAttempt(correct)) {
            JOptionPane.showMessageDialog(frame,
                    "Invalid credentials or account locked",
                    "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        currentTeller = teller;
        currentTeller.setAuthenticated(true); // CRITICAL FIX: Set teller as authenticated
        loginManager.refreshSession();
        JOptionPane.showMessageDialog(frame, "Welcome, " + teller.getName(),
                "Success", JOptionPane.INFORMATION_MESSAGE);
        showTellerMenu();
    }

    private void handleAdminLogin(String username, String password) {
        if (!username.equals(ADMIN_USER) || !password.equals(ADMIN_PASS)) {
            JOptionPane.showMessageDialog(frame, "Invalid admin credentials.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(frame, "Admin login successful.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
        showAdminMenu();
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(250, 50));
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(color);
            }
        });
        return btn;
    }

    // ==================== TELLER MENU ====================
    private void showTellerMenu() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(30, 10, 20, 10));
        JLabel title = new JLabel("Teller Menu");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(0, 150, 65));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(title);
        
        // Button panel with grid layout
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.weightx = 1.0;

        String[] options = {
                "Search Account",
                "View All Accounts",
                "Assist Transaction",
                "Assist Transfer",
                "View Frozen Accounts",
                "Unfreeze Account",
                "Create New Account",
                "Create New Customer",
                "Logout"
        };

        // Create 2-column grid layout
        for (int i = 0; i < options.length; i++) {
            JButton btn = createPillButton(options[i], new Color(0, 150, 65));
            btn.setPreferredSize(new Dimension(250, 50));
            
            gbc.gridx = i % 2;
            gbc.gridy = i / 2;

            final int index = i;
            btn.addActionListener(e -> {
                switch (index) {
                    case 0 -> searchAccount();
                    case 1 -> viewAllAccounts();
                    case 2 -> assistTransaction();
                    case 3 -> assistTransfer();
                    case 4 -> viewFrozenAccounts();
                    case 5 -> unfreezeAccount();
                    case 6 -> createNewAccount();
                    case 7 -> createNewCustomer();
                    case 8 -> {
                        currentTeller = null;
                        showMainMenu();
                    }
                }
            });

            buttonPanel.add(btn, gbc);
        }

        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        
        frame.setContentPane(mainPanel);
        frame.revalidate();
    }

    private void searchAccount() {
        if (!checkTellerSession()) return;

        String query = showStyledInputDialog("Search Account", "Enter Account ID or Customer Name:");
        if (query == null || query.trim().isEmpty()) return;

        try {
            java.util.List<Account> results = currentTeller.searchAccounts(query.trim());

            if (results.isEmpty()) {
                showStyledMessage("Search Results", "No accounts found.", JOptionPane.INFORMATION_MESSAGE);
            } else {
                StringBuilder sb = new StringBuilder("Found " + results.size() + " account(s):\n\n");
                for (Account acc : results) {
                    sb.append(String.format("Account: %s | Owner: %s | Balance: $%.2f | Status: %s\n",
                            acc.getAccountNumber(), acc.getCustomer().getName(),
                            acc.getBalance(), acc.getStatus()));
                }
                showStyledMessage("Search Results", sb.toString(), JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            showStyledMessage("Error", "Error searching accounts: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewAllAccounts() {
        if (!checkTellerSession()) return;

        java.util.List<Account> accounts = db.retrieveAllAccounts();

        String[] columns = {"Account ID", "Owner", "Type", "Balance", "Status"};
        Object[][] data = new Object[accounts.size()][5];

        for (int i = 0; i < accounts.size(); i++) {
            Account acc = accounts.get(i);
            data[i][0] = acc.getAccountNumber();
            data[i][1] = acc.getCustomer().getName();
            data[i][2] = acc.getType();
            data[i][3] = String.format("$%.2f", acc.getBalance());
            data[i][4] = acc.getStatus();
        }

        JTable table = new JTable(data, columns);
        table.setEnabled(false);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(700, 400));

        JOptionPane.showMessageDialog(frame, scrollPane, "All Accounts",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void assistTransaction() {
        if (!checkTellerSession()) return;

        String id = showStyledInputDialog("Assist Transaction", "Enter Account ID:");
        if (id == null || id.trim().isEmpty()) return;

        Account account = db.getAccountByNumber(id.trim());
        if (account == null) {
            showStyledMessage("Error", "Account not found.", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String type = showStyledInputDialog("Transaction Type", "Transaction Type (deposit/withdraw):");
        if (type == null || type.trim().isEmpty()) return;

        String amountStr = showStyledInputDialog("Amount", "Amount:");
        if (amountStr == null || amountStr.trim().isEmpty()) return;

        try {
            double amount = Double.parseDouble(amountStr);
            Transaction tx = currentTeller.assistTransaction(account, type.trim(), amount);

            if (tx != null) {
                showStyledMessage("Success", 
                        "Transaction successful!\nNew balance: $" + String.format("%.2f", account.getBalance()),
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                showStyledMessage("Failed", 
                        "Transaction failed. Account may be frozen or insufficient funds.",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            showStyledMessage("Invalid Input", "Invalid amount.", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            showStyledMessage("Error", "Error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void assistTransfer() {
        if (!checkTellerSession()) return;

        String sourceId = showStyledInputDialog("Assist Transfer", "Source Account ID:");
        if (sourceId == null || sourceId.trim().isEmpty()) return;

        String destId = showStyledInputDialog("Destination Account", "Destination Account ID:");
        if (destId == null || destId.trim().isEmpty()) return;

        Account source = db.getAccountByNumber(sourceId.trim());
        Account dest = db.getAccountByNumber(destId.trim());

        if (source == null || dest == null) {
            showStyledMessage("Error", "Invalid accounts.", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String amountStr = showStyledInputDialog("Amount", "Amount:");
        if (amountStr == null || amountStr.trim().isEmpty()) return;

        try {
            double amount = Double.parseDouble(amountStr);
            Transaction tx = currentTeller.assistTransfer(source, dest, amount);

            if (tx != null) {
                showStyledMessage("Success",
                        "Transfer successful!\n" +
                                "Source balance: $" + String.format("%.2f", source.getBalance()) + "\n" +
                                "Destination balance: $" + String.format("%.2f", dest.getBalance()),
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                showStyledMessage("Failed", 
                        "Transfer failed. Check account status and balance.",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            showStyledMessage("Invalid Input", "Invalid amount.", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            showStyledMessage("Error", "Error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewFrozenAccounts() {
        if (!checkTellerSession()) return;

        try {
            java.util.List<Account> frozen = currentTeller.getFrozenAccounts();

            if (frozen.isEmpty()) {
                showStyledMessage("Frozen Accounts", "No frozen accounts.", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            StringBuilder sb = new StringBuilder("Frozen Accounts:\n\n");
            for (Account acc : frozen) {
                sb.append(String.format("%s | %s | $%.2f\n",
                        acc.getAccountNumber(), acc.getCustomer().getName(), acc.getBalance()));
            }

            showStyledMessage("Frozen Accounts", sb.toString(), JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showStyledMessage("Error", 
                    "Error viewing frozen accounts: " + ex.getMessage(),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void unfreezeAccount() {
        if (!checkTellerSession()) return;

        String id = showStyledInputDialog("Unfreeze Account", "Enter Account ID to unfreeze:");
        if (id == null || id.trim().isEmpty()) return;

        try {
            boolean success = currentTeller.unfreezeAccount(id.trim());
            if (success) {
                showStyledMessage("Success", "Account unfrozen successfully!", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showStyledMessage("Failed",
                        "Failed to unfreeze account. Account may not exist or is not frozen.",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            showStyledMessage("Error", "Error: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createNewAccount() {
        if (!checkTellerSession()) return;

        String customerId = showStyledInputDialog("Create Account", "Existing Customer ID:");
        if (customerId == null || customerId.trim().isEmpty()) return;

        Customer customer = db.getCustomer(customerId.trim());
        if (customer == null) {
            showStyledMessage("Error", "Customer not found.", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String type = showStyledInputDialog("Account Type", "Account Type (card/check/checking/saving):");
        if (type == null || type.trim().isEmpty()) return;

        String balanceStr = showStyledInputDialog("Initial Balance", "Initial Balance:");
        if (balanceStr == null || balanceStr.trim().isEmpty()) return;

        try {
            double balance = Double.parseDouble(balanceStr);
            Account acc = switch (type.trim().toLowerCase()) {
                case "card" -> new Card(customer, balance);
                case "check" -> new Check(customer, balance);
                case "checking" -> new Checking(customer, balance);
                case "saving", "savings" -> new Saving(customer, balance);
                default -> null;
            };

            if (acc == null) {
                showStyledMessage("Error", "Invalid account type.", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String newId = db.generateNextAccountNumber(customer);
            acc.setAccountNumber(newId);
            customer.addAccount(acc);
            db.updateAccount(acc);

            showStyledMessage("Success",
                    "Created account " + newId + " for " + customer.getName(),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            showStyledMessage("Invalid Input", "Invalid balance.", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createNewCustomer() {
        if (!checkTellerSession()) return;

        String name = showStyledInputDialog("Create Customer", "Full Name:");
        if (name == null || name.trim().isEmpty()) return;

        String password = showStyledInputDialog("Password", "Password:");
        if (password == null || password.trim().isEmpty()) return;

        String newCustomerId = db.generateNextCustomerId();
        Customer newCustomer = new Customer(Integer.parseInt(newCustomerId), name.trim(), password.trim());
        db.addCustomer(newCustomer);

        showStyledMessage("Success",
                "Customer Created!\nAssigned Customer ID: " + newCustomerId,
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ==================== CUSTOMER LOGIN ====================
    private void selectCustomerAccount() {
        java.util.List<Account> accounts = currentCustomer.getAccounts();

        if (accounts.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "You have no accounts.");
            showMainMenu();
            return;
        }

        String[] options = new String[accounts.size()];
        for (int i = 0; i < accounts.size(); i++) {
            Account acc = accounts.get(i);
            options[i] = String.format("%s (%s) - $%.2f - %s",
                    acc.getAccountNumber(), acc.getType(), acc.getBalance(), acc.getStatus());
        }

        String choice = (String) JOptionPane.showInputDialog(frame,
                "Select an account:", "Account Selection",
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice != null) {
            int index = java.util.Arrays.asList(options).indexOf(choice);
            Account selected = accounts.get(index);
            showCustomerMenu(selected);
        } else {
            showMainMenu();
        }
    }

    private void showCustomerMenu(Account account) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(30, 10, 20, 10));
        JLabel title = new JLabel("Customer Menu");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(0, 150, 65));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(title);
        
        // Button panel with grid layout
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.weightx = 1.0;

        String[] options = {
                "View Account Details",
                "Deposit",
                "Withdraw",
                "Transfer",
                "Transaction History",
                "Report Stolen Card",
                "Logout"
        };

        // Create 2-column grid layout
        for (int i = 0; i < options.length; i++) {
            JButton btn = createPillButton(options[i], new Color(0, 150, 65));
            btn.setPreferredSize(new Dimension(250, 50));
            
            gbc.gridx = i % 2;
            gbc.gridy = i / 2;

            final int index = i;
            btn.addActionListener(e -> {
                switch (index) {
                    case 0 -> viewAccountDetails(account);
                    case 1 -> customerDeposit(account);
                    case 2 -> customerWithdraw(account);
                    case 3 -> customerTransfer(account);
                    case 4 -> viewTransactionHistory(account);
                    case 5 -> reportStolenCard(account);
                    case 6 -> {
                        currentCustomer = null;
                        showMainMenu();
                    }
                }
            });

            buttonPanel.add(btn, gbc);
        }

        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        
        frame.setContentPane(mainPanel);
        frame.revalidate();
    }

    // Add this new helper method right after showCustomerMenu
    private JButton createPillButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(0, 170, 75));
                btn.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(color);
                btn.repaint();
            }
        });

        return btn;
    }

    private void viewAccountDetails(Account account) {
        // Refresh account data from database
        Account freshAccount = db.getAccountByNumber(account.getAccountNumber());
        if (freshAccount != null) {
            account = freshAccount;
        }

        String details = String.format(
                "Account Number: %s\nType: %s\nBalance: $%.2f\nStatus: %s",
                account.getAccountNumber(), account.getType(),
                account.getBalance(), account.getStatus());
        showStyledMessage("Account Details", details, JOptionPane.INFORMATION_MESSAGE);
    }

    private void customerDeposit(Account account) {
        if (!checkCustomerSession()) return;

        // Check if account is frozen
        Account freshAccount = db.getAccountByNumber(account.getAccountNumber());
        if (freshAccount != null && freshAccount.getStatus().equals("FROZEN")) {
            showStyledMessage("Account Frozen",
                    "This account is frozen. Please contact a teller.",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String amountStr = showStyledInputDialog("Deposit", "Amount to deposit:");
        if (amountStr == null || amountStr.trim().isEmpty()) return;

        try {
            double amount = Double.parseDouble(amountStr);
            boolean success = currentCustomer.deposit(account, amount, loginManager);

            if (success) {
                db.updateAccount(account);
                showStyledMessage("Success",
                        "Deposit successful!\nNew balance: $" + String.format("%.2f", account.getBalance()),
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                showStyledMessage("Failed", "Deposit failed.", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            showStyledMessage("Invalid Input", "Please enter a valid amount.", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void customerWithdraw(Account account) {
        if (!checkCustomerSession()) return;

        // Check if account is frozen
        Account freshAccount = db.getAccountByNumber(account.getAccountNumber());
        if (freshAccount != null && freshAccount.getStatus().equals("FROZEN")) {
            showStyledMessage("Account Frozen",
                    "This account is frozen. Please contact a teller.",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String amountStr = showStyledInputDialog("Withdraw", "Amount to withdraw:");
        if (amountStr == null || amountStr.trim().isEmpty()) return;

        try {
            double amount = Double.parseDouble(amountStr);
            boolean success = currentCustomer.withdraw(account, amount, loginManager);

            if (success) {
                db.updateAccount(account);
                showStyledMessage("Success",
                        "Withdrawal successful!\nNew balance: $" + String.format("%.2f", account.getBalance()),
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                showStyledMessage("Failed", "Withdrawal failed. Check balance.", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            showStyledMessage("Invalid Input", "Please enter a valid amount.", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void customerTransfer(Account source) {
        if (!checkCustomerSession()) return;

        // Check if source account is frozen
        Account freshSource = db.getAccountByNumber(source.getAccountNumber());
        if (freshSource != null && freshSource.getStatus().equals("FROZEN")) {
            showStyledMessage("Account Frozen",
                    "This account is frozen. Please contact a teller.",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String destId = showStyledInputDialog("Transfer", "Destination Account ID:");
        if (destId == null || destId.trim().isEmpty()) return;

        Account dest = db.getAccountByNumber(destId.trim());
        if (dest == null) {
            showStyledMessage("Error", "Destination account not found.", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (dest.getStatus().equals("FROZEN")) {
            showStyledMessage("Error", "Destination account is frozen.", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String amountStr = showStyledInputDialog("Transfer", "Amount to transfer:");
        if (amountStr == null || amountStr.trim().isEmpty()) return;

        try {
            double amount = Double.parseDouble(amountStr);
            boolean success = currentCustomer.transfer(source, dest, amount, loginManager);

            if (success) {
                db.updateAccount(source);
                db.updateAccount(dest);
                showStyledMessage("Success",
                        "Transfer successful!\nYour new balance: $" + String.format("%.2f", source.getBalance()),
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                showStyledMessage("Failed", "Transfer failed. Check balance.", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            showStyledMessage("Invalid Input", "Please enter a valid amount.", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewTransactionHistory(Account account) {
        if (!checkCustomerSession()) return;

        java.util.List<Transaction> transactions =
                TransactionsDatabaseManager.getInstance().loadTransactionsForAccount(account.getAccountNumber());

        if (transactions.isEmpty()) {
            showStyledMessage("Transaction History", "No transactions found.", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder("Transaction History:\n\n");
        for (Transaction tx : transactions) {
            sb.append(String.format("ID: %d | %s | $%.2f | %s\n",
                    tx.getTransactionId(), tx.getType(), tx.getAmount(), tx.getStatus()));
        }

        showStyledMessage("Transaction History", sb.toString(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void reportStolenCard(Account account) {
        if (!checkCustomerSession()) return;

        int confirm = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to freeze this account?\nYou will need to contact a teller to unfreeze it.",
                "Confirm Freeze",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            account.freezeAccount();
            db.updateAccount(account);
            showStyledMessage("Account Frozen",
                    "Your account has been frozen.\nContact a teller to unfreeze it.",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ==================== ADMIN MENU ====================
    private void showAdminMenu() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(30, 10, 20, 10));
        JLabel title = new JLabel("Admin Menu");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(0, 150, 65));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(title);
        
        // Button panel with grid layout
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.weightx = 1.0;

        String[] options = {"View All Tellers", "Add Teller", "Remove Teller", "Logout"};

        // Create 2-column grid layout
        for (int i = 0; i < options.length; i++) {
            JButton btn = createPillButton(options[i], new Color(0, 150, 65));
            btn.setPreferredSize(new Dimension(250, 50));
            
            gbc.gridx = i % 2;
            gbc.gridy = i / 2;

            final int index = i;
            btn.addActionListener(e -> {
                switch (index) {
                    case 0 -> viewAllTellers();
                    case 1 -> addTeller();
                    case 2 -> removeTeller();
                    case 3 -> showMainMenu();
                }
            });

            buttonPanel.add(btn, gbc);
        }

        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        
        frame.setContentPane(mainPanel);
        frame.revalidate();
    }

    private void viewAllTellers() {
        StringBuilder sb = new StringBuilder("All Tellers:\n\n");
        for (Teller t : TellerDatabaseManager.getInstance().getAllTellers()) {
            sb.append(String.format("%s | %s | %s\n",
                    t.getEmployeeId(), t.getName(), t.getEmail()));
        }
        showStyledMessage("All Tellers", sb.toString(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void addTeller() {
        String id = showStyledInputDialog("Add Teller", "Employee ID:");
        if (id == null || id.trim().isEmpty()) return;

        String name = showStyledInputDialog("Name", "Name:");
        if (name == null || name.trim().isEmpty()) return;

        String email = showStyledInputDialog("Email", "Email:");
        if (email == null || email.trim().isEmpty()) return;

        String password = showStyledInputDialog("Password", "Password:");
        if (password == null || password.trim().isEmpty()) return;

        Teller newTeller = new Teller(id, name, email, password);
        TellerDatabaseManager.getInstance().addTeller(newTeller);
        showStyledMessage("Success", "Teller added successfully!", JOptionPane.INFORMATION_MESSAGE);
    }

    private void removeTeller() {
        String id = showStyledInputDialog("Remove Teller", "Enter Teller ID to remove:");
        if (id == null || id.trim().isEmpty()) return;

        TellerDatabaseManager tdm = TellerDatabaseManager.getInstance();
        Teller t = tdm.getTeller(id.trim());

        if (t == null) {
            showStyledMessage("Error", "Teller not found.", JOptionPane.ERROR_MESSAGE);
            return;
        }

        tdm.removeTeller(id.trim());
        showStyledMessage("Success", "Teller removed successfully!", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BankingSystemGUI());
    }

    // ==================== STYLED INPUT DIALOG ====================
    private String showStyledInputDialog(String title, String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 15, 0);

        JLabel msgLabel = new JLabel(message);
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msgLabel.setForeground(new Color(60, 60, 60));
        gbc.gridy = 0;
        panel.add(msgLabel, gbc);

        JTextField inputField = new JTextField(20);
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        inputField.setPreferredSize(new Dimension(300, 40));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(8, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(inputField, gbc);

        int result = JOptionPane.showConfirmDialog(frame, panel, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            return inputField.getText();
        }
        return null;
    }

    private void showStyledMessage(String title, String message, int messageType) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel msgLabel = new JLabel("<html><div style='width: 300px; text-align: left;'>" + message.replace("\n", "<br>") + "</div></html>");
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msgLabel.setForeground(new Color(60, 60, 60));
        panel.add(msgLabel, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(frame, panel, title, messageType);
    }
}
