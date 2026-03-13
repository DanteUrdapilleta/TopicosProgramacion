package principal;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class Practica5Controller {

    @FXML private RadioButton rb1, rb2, rb3, rb11, rb22, rb33;
    @FXML private CheckBox ck4, ck5, ck6, ck44, ck55, ck66;
    @FXML private TextField tfOriginal, tfEspejo;
    @FXML private ComboBox<String> cbOriginal, cbEspejo;
    @FXML private Spinner<Integer> spOriginal, spEspejo;

    private ToggleGroup grupoOriginal = new ToggleGroup();
    private ToggleGroup grupoEspejo = new ToggleGroup();

    @FXML
    public void initialize() {
        rb1.setToggleGroup(grupoOriginal); rb2.setToggleGroup(grupoOriginal); rb3.setToggleGroup(grupoOriginal);
        rb11.setToggleGroup(grupoEspejo); rb22.setToggleGroup(grupoEspejo); rb33.setToggleGroup(grupoEspejo);
        
        cbEspejo.setItems(cbOriginal.getItems());

        spOriginal.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0));
        spEspejo.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0));
        
        spOriginal.valueProperty().addListener((obs, oldVal, newVal) -> {
            spEspejo.getValueFactory().setValue(newVal);
        });
    }

    @FXML
    private void syncRadios() {
        if (rb1.isSelected()) rb11.setSelected(true);
        if (rb2.isSelected()) rb22.setSelected(true);
        if (rb3.isSelected()) rb33.setSelected(true);
    }

    @FXML
    private void syncChecks() {
        ck44.setSelected(ck4.isSelected());
        ck55.setSelected(ck5.isSelected());
        ck66.setSelected(ck6.isSelected());
    }

    @FXML
    private void handleText(KeyEvent event) {
        tfEspejo.setText(tfOriginal.getText());
        if (event.getCode() == KeyCode.ENTER && !tfOriginal.getText().isEmpty()) {
            cbOriginal.getItems().add(tfOriginal.getText());
            tfOriginal.clear();
            tfEspejo.clear();
        }
    }
}