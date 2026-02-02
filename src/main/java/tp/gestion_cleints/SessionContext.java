package tp.gestion_cleints;

public class SessionContext {
    private static SessionContext instance;
    private Year currentYear;

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
}
