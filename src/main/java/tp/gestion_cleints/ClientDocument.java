package tp.gestion_cleints;

public class ClientDocument {
    private int id;
    private int clientId;
    private String fileName;
    private String filePath;
    private String uploadDate;

    public ClientDocument(int id, int clientId, String fileName, String filePath, String uploadDate) {
        this.id = id;
        this.clientId = clientId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.uploadDate = uploadDate;
    }

    public int getId() {
        return id;
    }

    public int getClientId() {
        return clientId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getUploadDate() {
        return uploadDate;
    }
}
