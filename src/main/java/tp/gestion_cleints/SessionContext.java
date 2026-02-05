package tp.gestion_cleints;

public class SessionContext {
    private static SessionContext instance;
    private Year currentYear;
    private User currentUser;

    private SessionContext() {
    }

    public static SessionContext getInstance() {
        if (instance == null) {
            instance = new SessionContext();
        }
        return instance;
    }

    public Year getCurrentYear() {
        return currentYear;
    }

    public void setCurrentYear(Year currentYear) {
        this.currentYear = currentYear;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public boolean isAccountant() {
        return currentUser != null && currentUser.isAccountant();
    }
}
