package tp.gestion_cleints;

public class Invoice {
    private int id;
    private int clientId;
    private String clientName; // For display
    private String number;
    private String date;
    private String dueDate;
    private double totalHt;
    private double totalTva;
    private double totalTtc;
    private String status; // DRAFT, SENT, PAID, CANCELLED
    private int yearId;

    public Invoice(int id, int clientId, String number, String date, String dueDate,
            double totalHt, double totalTva, double totalTtc, String status, int yearId) {
        this.id = id;
        this.clientId = clientId;
        this.number = number;
        this.date = date;
        this.dueDate = dueDate;
        this.totalHt = totalHt;
        this.totalTva = totalTva;
        this.totalTtc = totalTtc;
        this.status = status;
        this.yearId = yearId;
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

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public double getTotalHt() {
        return totalHt;
    }

    public void setTotalHt(double totalHt) {
        this.totalHt = totalHt;
    }

    public double getTotalTva() {
        return totalTva;
    }

    public void setTotalTva(double totalTva) {
        this.totalTva = totalTva;
    }

    public double getTotalTtc() {
        return totalTtc;
    }

    public void setTotalTtc(double totalTtc) {
        this.totalTtc = totalTtc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getYearId() {
        return yearId;
    }

    public void setYearId(int yearId) {
        this.yearId = yearId;
    }
}
