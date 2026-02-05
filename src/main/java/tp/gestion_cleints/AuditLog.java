package tp.gestion_cleints;

public class AuditLog {
    private int id;
    private String username;
    private String action;
    private String entityType;
    private String entityId;
    private String details;
    private String timestamp;

    public AuditLog(int id, String username, String action, String entityType, String entityId, String details,
            String timestamp) {
        this.id = id;
        this.username = username;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
        this.timestamp = timestamp;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getDetails() {
        return details;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
