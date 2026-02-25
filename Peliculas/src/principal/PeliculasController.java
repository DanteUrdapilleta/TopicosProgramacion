package principal;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class PeliculasController implements Initializable {
    @FXML
    private TextField txtPelicula;
    @FXML
    private ComboBox<String> peliculas;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }
    @FXML
    private void btnAgregar() {

        if (txtPelicula.getText().isEmpty() || txtPelicula.getText().trim().length() == 0) {
            return;
        }

        String texto = txtPelicula.getText().toUpperCase();

        for (String item : peliculas.getItems()) {
            if (item.toUpperCase().equals(texto)) {
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setHeaderText(null);
                alerta.setTitle("Error");
                alerta.setContentText("Película repetida");
                alerta.showAndWait();
                return;
            }
        }
        peliculas.getItems().add(txtPelicula.getText());
        txtPelicula.clear();
    }
}