package tp.gestion_cleints;

public class Transaction {
    private int id;
    private int clientId;
    private double amount;
    private String date;
    private String notes;
    private String type;

    // Transaction Types
    public static final String TYPE_PAYMENT = "PAYMENT";
    public static final String TYPE_CHARGE = "CHARGE"; // Charge non courante
    public static final String TYPE_HONORAIRE_CONTRACT = "HONORAIRE_CONTRACT";
    public static final String TYPE_HONORAIRE_EXTRA = "HONORAIRE_EXTRA";
    public static final String TYPE_PRODUIT = "PRODUIT"; // Produit non courant

    public Transaction(int id, int clientId, double amount, String date, String notes, String type) {
        this.id = id;
        this.clientId = clientId;
        this.amount = amount;
        this.date = date;
        this.notes = notes;
        this.type = type;
    }

    public Transaction(int clientId, double amount, String date, String notes, String type) {
        this.clientId = clientId;
        this.amount = amount;
        this.date = date;
        this.notes = notes;
        this.type = type;
    }

    public Transaction(int id, int clientId, double amount, String date, String notes) {
        this(id, clientId, amount, date, notes, TYPE_PAYMENT); // Default to Payment
    }

    public Transaction(int clientId, double amount, String date, String notes) {
        this(clientId, amount, date, notes, TYPE_PAYMENT); // Default to Payment
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
