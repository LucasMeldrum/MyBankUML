package bank;


public class LoginManager {

    // Settings
    private int sessionTimeoutMin = 20;
    private int lockoutTimeMin = 2;   // lockout time in minutes
    private int maxAttempts = 3;

    // State
    private int loginAttempts = 0;
    private long lockoutStartTime = 0;
    private boolean isLockedOut = false;

    private long sessionStartTime;
    private boolean sessionActive = false;

    public LoginManager(int sessionTimeoutMin) {
        this.sessionTimeoutMin = sessionTimeoutMin;
    }

    // Placeholder authentication (replace with DB logic)
    public boolean authenticate(String username, String password) {
        return username.equals("admin") && password.equals("password123");
    }

    public boolean login(String username, String password) {

        // ========== 1. Check lockout status ==========
        if (isLockedOut) {
            long elapsedMin = (System.currentTimeMillis() - lockoutStartTime) / (1000 * 60);

            if (elapsedMin < lockoutTimeMin) {
                System.out.println("You are locked out. Try again in " +
                        (lockoutTimeMin - elapsedMin) + " minute(s).");
                return false;
            } else {
                // Lockout expired → reset state
                isLockedOut = false;
                loginAttempts = 0;
                System.out.println("Lockout expired. You may try again.");
            }
        }

        // ========== 2. Try authenticating ==========
        if (authenticate(username, password)) {
            loginAttempts = 0;  // reset attempts
            startSession();
            return true;
        } else {
            loginAttempts++;
            System.out.println("Login failed. Attempts: " + loginAttempts);

            // ========== 3. Trigger lockout ==========
            if (loginAttempts >= maxAttempts) {
                isLockedOut = true;
                lockoutStartTime = System.currentTimeMillis();
                System.out.println("Too many failed attempts. You are locked out for "
                        + lockoutTimeMin + " minutes.");
            }

            return false;
        }
    }

    public void startSession() {
    	// to be called whener the user performs an action
        sessionStartTime = System.currentTimeMillis();
        sessionActive = true;
    }

    public void logout() {
        sessionActive = false;
        //add ui logic
        System.out.println("Logged out.");
    }

    public void manageSession() {
        if (!sessionActive) {
            System.out.println("No active session.");
            return;
        }

        long elapsedMinutes =
                (System.currentTimeMillis() - sessionStartTime) / (1000 * 60);

        if (elapsedMinutes >= sessionTimeoutMin) {
            System.out.println("Session expired. Logging out...");
            logout();
        } else {
            System.out.println("Session active. Time left: "
                    + (sessionTimeoutMin - elapsedMinutes) + " minutes.");
        }
    }
}
