
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;


public class NumerosAleatoriosController implements Initializable {
    @FXML
    private Spinner<Integer>SpinnerMin;
    @FXML
    private Spinner<Integer>SpinnerMax;
    @FXML
    private TextField txtResultado;
    private final Random random = new Random();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        SpinnerMin.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 1)
        );

        SpinnerMax.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 10)
        );
    }    
    @FXML
    private void generarNumero(){
        int min= SpinnerMin.getValue();
        int max= SpinnerMax.getValue();
        
        if(min>max){
            txtResultado.setText("Error");
            return;
        }
        int numero=random.nextInt((max-min)+1)+min;
        txtResultado.setText(String.valueOf(numero));
    }
}
