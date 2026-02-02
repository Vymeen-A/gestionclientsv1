package tp.gestion_cleints;

public class Year {
    private int id;
    private String name;
    private boolean softDeleted;
    private String deletedAt;

    public Year(int id, String name, boolean softDeleted, String deletedAt) {
        this.id = id;
        this.name = name;
        this.softDeleted = softDeleted;
        this.deletedAt = deletedAt;
    }

    public Year(int id, String name) {
        this(id, name, false, null);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isSoftDeleted() {
        return softDeleted;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    @Override
    public String toString() {
        return name;
    }
}
