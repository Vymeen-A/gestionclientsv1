package tp.gestion_cleints;

public enum Role {
    ADMIN("Admin"),
    ACCOUNTANT("Accountant"),
    READ_ONLY("Read-Only");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromString(String text) {
        for (Role b : Role.values()) {
            if (b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return READ_ONLY; // Default lowest role
    }
}
