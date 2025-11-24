package bank;

import lombok.Getter;

public class LoginManager {

    // ================= SETTINGS ====================
    private int sessionTimeoutMin = 20;   // session auto-logout time
    private int lockoutTimeMin = 2;       // lockout duration
    private int maxAttempts = 3;          // attempts before lockout

    // ================= STATE =======================
    private int loginAttempts = 0;
    private boolean isLockedOut = false;
    private long lockoutStartTime = 0;

    private long sessionStartTime = 0;
    // ===========================================================
    // GETTERS
    // ===========================================================
    @Getter
    private boolean sessionActive = false;


    // Constructor
    public LoginManager(int sessionTimeoutMin) {
        this.sessionTimeoutMin = sessionTimeoutMin;
    }


    public boolean loginAttempt(boolean credentialsCorrect) {

        // ========= 1. Check lockout first =========
        if (isLockedOut) {
            long elapsedMin = (System.currentTimeMillis() - lockoutStartTime) / (1000 * 60);

            if (elapsedMin < lockoutTimeMin) {
                System.out.println(" Locked out. Try again in " +
                        (lockoutTimeMin - elapsedMin) + " minute(s).");
                return false;
            } else {
                // Lockout expired
                isLockedOut = false;
                loginAttempts = 0;
                System.out.println("Lockout expired. You may try again.");
            }
        }

        // ========= 2. If login is correct =========
        if (credentialsCorrect) {
            loginAttempts = 0;     // reset attempts
            startSession();        // start session
            return true;
        }

        // ========= 3. If login failed =========
        loginAttempts++;
        System.out.println("Login failed. Attempt " + loginAttempts + "/" + maxAttempts);

        if (loginAttempts >= maxAttempts) {
            isLockedOut = true;
            lockoutStartTime = System.currentTimeMillis();
            System.out.println("🔒 Too many failed attempts. Locked out for " +
                    lockoutTimeMin + " minutes.");
        }

        return false;
    }


    // ===========================================================
    // START SESSION
    // ===========================================================
    public void startSession() {
        sessionStartTime = System.currentTimeMillis();
        sessionActive = true;
    }

    // ===========================================================
    // CHECK & REFRESH SESSION (Called after each user action)
    // ===========================================================
    public boolean checkSession() {
        if (!sessionActive) {
            System.out.println("No active session.");
            return false;
        }

        long elapsedMinutes =
                (System.currentTimeMillis() - sessionStartTime) / (1000 * 60);

        if (elapsedMinutes >= sessionTimeoutMin) {
            System.out.println("⏳ Session expired.");
            logout();
            return false;
        }

        return true;
    }

    public void refreshSession() {
        sessionStartTime = System.currentTimeMillis();
    }

    // ===========================================================
    // LOGOUT
    // ===========================================================
    public void logout() {
        sessionActive = false;
        System.out.println("Logged out.");
    }

    public boolean isLockedOut() {
        return isLockedOut;
    }
}