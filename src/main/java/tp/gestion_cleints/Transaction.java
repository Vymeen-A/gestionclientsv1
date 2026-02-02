package tp.gestion_cleints;

public class Transaction {
    private int id;
    private int clientId;
    private double amount;
    private String date;
    private String notes;
    private String type;
    private int yearId;
    private int paymentTypeId;
    private String receiptNumber;

    // Transaction Types
    public static final String TYPE_PAYMENT = "PAYMENT";
    public static final String TYPE_CHARGE = "CHARGE";
    public static final String TYPE_HONORAIRE_CONTRACT = "HONORAIRE_CONTRACT";
    public static final String TYPE_HONORAIRE_EXTRA = "HONORAIRE_EXTRA";
    public static final String TYPE_PRODUIT = "PRODUIT";

    public Transaction(int id, int clientId, double amount, String date, String notes, String type, int yearId,
            int paymentTypeId, String receiptNumber) {
        this.id = id;
        this.clientId = clientId;
        this.amount = amount;
        this.date = date;
        this.notes = notes;
        this.type = type;
        this.yearId = yearId;
        this.paymentTypeId = paymentTypeId;
        this.receiptNumber = receiptNumber;
    }

    public Transaction(int clientId, double amount, String date, String notes, String type, int yearId,
            int paymentTypeId, String receiptNumber) {
        this(0, clientId, amount, date, notes, type, yearId, paymentTypeId, receiptNumber);
    }

    public Transaction(int id, int clientId, double amount, String date, String notes, String type) {
        this(id, clientId, amount, date, notes, type,
                SessionContext.getInstance().getCurrentYear() != null
                        ? SessionContext.getInstance().getCurrentYear().getId()
                        : 1,
                1, null);
    }

    public Transaction(int clientId, double amount, String date, String notes, String type) {
        this(0, clientId, amount, date, notes, type);
    }

    public Transaction(int id, int clientId, double amount, String date, String notes) {
        this(id, clientId, amount, date, notes, TYPE_PAYMENT);
    }

    public Transaction(int clientId, double amount, String date, String notes) {
        this(0, clientId, amount, date, notes, TYPE_PAYMENT);
    }

    // Getters and Setters
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

    public int getYearId() {
        return yearId;
    }

    public void setYearId(int yearId) {
        this.yearId = yearId;
    }

    public int getPaymentTypeId() {
        return paymentTypeId;
    }

    public void setPaymentTypeId(int paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }
}
