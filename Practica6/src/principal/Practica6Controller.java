package principal;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

public class Practica6Controller implements Initializable {

    @FXML private RadioButton rbWin;
    @FXML private RadioButton rbLnx;
    @FXML private RadioButton rbMac;

    @FXML private CheckBox chPrg;
    @FXML private CheckBox chGrf;
    @FXML private CheckBox chAdm;

    @FXML private Slider sdHrs;
    @FXML private Label lbHrs;

    private ToggleGroup tgSO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        tgSO = new ToggleGroup();
        rbWin.setToggleGroup(tgSO);
        rbLnx.setToggleGroup(tgSO);
        rbMac.setToggleGroup(tgSO);
        rbWin.setSelected(true);

        lbHrs.setText(String.valueOf((int) sdHrs.getValue()));

        sdHrs.valueProperty().addListener((obs, oldV, newV) ->
            lbHrs.setText(String.valueOf(newV.intValue()))
        );
    }

    @FXML
    private void btnGuardarAction() {
        guardarInfo();
    }

    private void guardarInfo() {
        String sSO;
        String sPrg = chPrg.isSelected() ? "S" : "N";
        String sGrf = chGrf.isSelected() ? "S" : "N";
        String sAdm = chAdm.isSelected() ? "S" : "N";
        int iHrs = (int) sdHrs.getValue();

        if (rbWin.isSelected()) {
            sSO = "Windows";
        } else if (rbLnx.isSelected()) {
            sSO = "Linux";
        } else {
            sSO = "Mac";
        }

        guardarArchivo(sSO, sPrg, sGrf, sAdm, iHrs);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("Datos guardados correctamente");
        alert.showAndWait();
    }

    private void guardarArchivo(String sSO, String sPrg, String sGrf, String sAdm, int iHrs) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter("encuesta.txt", true))) {
            out.write(String.format("%s,%s,%s,%s,%d%n", sSO, sPrg, sGrf, sAdm, iHrs));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}