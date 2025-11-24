package bank;

import java.io.*;
import java.util.*;

public class TellerDatabaseManager {

    private static TellerDatabaseManager instance;

    private final String CSV_FILE = "tellers.csv";
    private Map<String, Teller> tellers = new HashMap<>();

    private TellerDatabaseManager() {
        tellers = loadCsv();
        if (tellers.isEmpty()) {
            initializeCsv();
            loadSampleTellers();
        }
    }

    public static synchronized TellerDatabaseManager getInstance() {
        if (instance == null) {
            instance = new TellerDatabaseManager();
        }
        return instance;
    }

    // Load tellers from CSV
    private Map<String, Teller> loadCsv() {
        Map<String, Teller> map = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {

            String line;
            boolean first = true;

            while ((line = br.readLine()) != null) {

                if (first) {
                    first = false;
                    continue;
                }

                String[] row = line.split(",");
                if (row.length < 4) continue;

                String id = row[0].trim();
                String name = row[1].trim();
                String email = row[2].trim();
                String password = row[3].trim();

                Teller t = new Teller(id, name, email, password);
                map.put(id, t);
            }

        } catch (Exception e) {
            System.out.println("No tellers.csv found, creating new file...");
            initializeCsv();
        }

        return map;
    }

    // Creates file with header if missing
    private void initializeCsv() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {
            writer.println("employeeId,name,email,password");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void loadSampleTellers() {
        Teller t1 = new Teller("T001", "Jane Smith", "jane.smith@bank.com", "SecurePass1");
        Teller t2 = new Teller("T002", "Mark Johnson", "mark.j@bank.com", "Admin123");
        Teller t3 = new Teller("T003", "Sarah Lee", "sarah.lee@bank.com", "Password99");

        addTeller(t1);
        addTeller(t2);
        addTeller(t3);
    }
    // Add new teller
    public void addTeller(Teller t) {
        tellers.put(t.getEmployeeId(), t);
        saveCsv();
    }

    // Save tellers to CSV
    private void saveCsv() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) {
            writer.println("employeeId,name,email,password");

            for (Teller t : tellers.values()) {
                writer.println(String.format("%s,%s,%s,%s",
                        t.getEmployeeId(),
                        t.getName(),
                        t.getEmail(),
                        t.getPassword()
                ));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Login method
    public Teller authenticate(String idOrEmail, String password) {
        for (Teller t : tellers.values()) {
            if ((t.getEmployeeId().equalsIgnoreCase(idOrEmail)
                    || t.getEmail().equalsIgnoreCase(idOrEmail))
                    && t.getPassword().equals(password)) {
                return t;
            }
        }
        return null;
    }

    // Get teller by ID
    public Teller getTeller(String id) {
        return tellers.get(id);
    }

    public Collection<Teller> getAllTellers() {
        return tellers.values();
    }
    public void removeTeller(String id) {
        tellers.remove(id);
        saveCsv();
    }
}
