package tp.gestion_cleints;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.File;

public class RestoreWizardController {

    @FXML
    private Label fileNameLabel;
    @FXML
    private Label sizeLabel;

    private boolean confirmed = false;

    public void setBackupFile(File file) {
        fileNameLabel.setText(file.getName());
        sizeLabel.setText(DatabaseBackup.getBackupMetadata(file));
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    @FXML
    public void handleRestore() {
        confirmed = true;
        ((Stage) fileNameLabel.getScene().getWindow()).close();
    }

    @FXML
    public void handleCancel() {
        confirmed = false;
        ((Stage) fileNameLabel.getScene().getWindow()).close();
    }
}
