package principal;

import java.io.File;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Practica4Controller {

    @FXML
    private TextField tfRuta;

    @FXML
    private void btnSelectActionPerformed(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar archivo");
        
        Stage stage = (Stage) tfRuta.getScene().getWindow();
        File selectedFile = fc.showOpenDialog(stage);
        
        if (selectedFile != null) {
            tfRuta.setText(selectedFile.getAbsolutePath());
        }
    }
}