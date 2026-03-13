import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class Practica1Controller implements Initializable {

    @FXML
    private Label lbl1;

    @FXML
    private TextField tfNombre;

    @FXML
    private void btnSaludarActionPerformed(ActionEvent event) {
        String nombre = tfNombre.getText();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Saludo");

        if (nombre.isEmpty()) {
            alert.setContentText("¡Hola desconocido!");
        } else {
            alert.setContentText("¡Hola " + nombre + "!");
        }

        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // opcional
    }
}