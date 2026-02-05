package tp.gestion_cleints;

public class Client {
    private int id;
    private String raisonSociale;
    private String nomPrenom;
    private String adresse;
    private String ville;
    private String ice;
    private String rc;
    private String tp;
    private String taxeHabit;
    private String tva; // Percentage
    private String regimeTva;
    private String fax;
    private String email;
    private String rib;
    private String username; // Reference username
    private String password; // Reference password, not for login
    private String secteur;
    private String debutAct;
    private double fixedTotalAmount;
    private double ttc;
    private int yearId;
    private boolean isHidden;
    private String status; // GOOD, LATE, RISKY, INACTIVE

    public Client(int id, String raisonSociale, String nomPrenom, String adresse, String ville,
            String ice, String rc, String tp, String taxeHabit, String tva,
            String regimeTva, String fax, String email, String rib,
            String username, String password, String secteur, String debutAct,
            double fixedTotalAmount, double ttc, int yearId, boolean isHidden) {
        this.id = id;
        this.raisonSociale = raisonSociale;
        this.nomPrenom = nomPrenom;
        this.adresse = adresse;
        this.ville = ville;
        this.ice = ice;
        this.rc = rc;
        this.tp = tp;
        this.taxeHabit = taxeHabit;
        this.tva = tva;
        this.regimeTva = regimeTva;
        this.fax = fax;
        this.email = email;
        this.rib = rib;
        this.username = username;
        this.password = password;
        this.secteur = secteur;
        this.debutAct = debutAct;
        this.fixedTotalAmount = fixedTotalAmount;
        this.ttc = ttc;
        this.yearId = yearId;
        this.isHidden = isHidden;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRaisonSociale() {
        return raisonSociale;
    }

    public void setRaisonSociale(String raisonSociale) {
        this.raisonSociale = raisonSociale;
    }

    public String getNomPrenom() {
        return nomPrenom;
    }

    public void setNomPrenom(String nomPrenom) {
        this.nomPrenom = nomPrenom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getIce() {
        return ice;
    }

    public void setIce(String ice) {
        this.ice = ice;
    }

    public String getRc() {
        return rc;
    }

    public void setRc(String rc) {
        this.rc = rc;
    }

    public String getTp() {
        return tp;
    }

    public void setTp(String tp) {
        this.tp = tp;
    }

    public String getTaxeHabit() {
        return taxeHabit;
    }

    public void setTaxeHabit(String taxeHabit) {
        this.taxeHabit = taxeHabit;
    }

    public String getTva() {
        return tva;
    }

    public void setTva(String tva) {
        this.tva = tva;
    }

    public String getRegimeTva() {
        return regimeTva;
    }

    public void setRegimeTva(String regimeTva) {
        this.regimeTva = regimeTva;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRib() {
        return rib;
    }

    public void setRib(String rib) {
        this.rib = rib;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecteur() {
        return secteur;
    }

    public void setSecteur(String secteur) {
        this.secteur = secteur;
    }

    public String getDebutAct() {
        return debutAct;
    }

    public void setDebutAct(String debutAct) {
        this.debutAct = debutAct;
    }

    public double getFixedTotalAmount() {
        return fixedTotalAmount;
    }

    public void setFixedTotalAmount(double fixedTotalAmount) {
        this.fixedTotalAmount = fixedTotalAmount;
    }

    public double getTtc() {
        return ttc;
    }

    public void setTtc(double ttc) {
        this.ttc = ttc;
    }

    public int getYearId() {
        return yearId;
    }

    public void setYearId(int yearId) {
        this.yearId = yearId;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
