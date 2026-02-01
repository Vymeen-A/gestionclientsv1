package tp.gestion_cleints;

public class Transaction {
    private int id;
    private int clientId;
    private double amount;
    private String date;
    private String notes;

    public Transaction(int id, int clientId, double amount, String date, String notes) {
        this.id = id;
        this.clientId = clientId;
        this.amount = amount;
        this.date = date;
        this.notes = notes;
    }

    public Transaction(int clientId, double amount, String date, String notes) {
        this.clientId = clientId;
        this.amount = amount;
        this.date = date;
        this.notes = notes;
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
}
