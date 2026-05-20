package principal;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * FXML Controller class
 *
 * @author etnad
 */
public class Practica7Controller implements Initializable {

    // --- Componentes FXML ---
    @FXML private TableView<Respuesta> tblRespuestas;
    @FXML private TableColumn<Respuesta, Integer> colId;
    @FXML private TableColumn<Respuesta, String> colSisOper;
    @FXML private TableColumn<Respuesta, String> colProgra;
    @FXML private TableColumn<Respuesta, String> colDiseno;
    @FXML private TableColumn<Respuesta, String> colAdmon;
    @FXML private TableColumn<Respuesta, Integer> colHoras;

    @FXML private Button btnActualizar;
    @FXML private Button jButton1; // Botón Guardar
    @FXML private Button jButton2; // Botón +
    @FXML private Button jButton3; // Botón -
    @FXML private ComboBox<String> jComboBox1;
    @FXML private CheckBox jCheckBox1;
    @FXML private CheckBox jCheckBox2;
    @FXML private CheckBox jCheckBox3;
    @FXML private Spinner<Integer> jSpinner1;

    // --- Variables de conexión de datos ---
    private Connection conn;
    private ObservableList<Respuesta> listaRespuestas = FXCollections.observableArrayList();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configurar columnas de la tabla
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSisOper.setCellValueFactory(new PropertyValueFactory<>("sSisOper"));
        colProgra.setCellValueFactory(new PropertyValueFactory<>("cProgra"));
        colDiseno.setCellValueFactory(new PropertyValueFactory<>("cDiseno"));
        colAdmon.setCellValueFactory(new PropertyValueFactory<>("cAdmon"));
        colHoras.setCellValueFactory(new PropertyValueFactory<>("iHoras"));

        tblRespuestas.setItems(listaRespuestas);

        // Opciones del ComboBox y Rango del Spinner
        jComboBox1.setItems(FXCollections.observableArrayList("Windows", "Linux", "Mac OS", "Otro"));
        jSpinner1.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 24, 0));

        try {
            loadDriver();
            System.out.println("Driver de BD cargado exitosamente");
            connect(); 
            btnActualizarActionPerformed(null); 
        } catch (ClassNotFoundException ex) {
            System.out.println("Driver de BD no encontrado");
        }
    }
    
    @FXML
    private void btnActualizarActionPerformed(ActionEvent event) {
        Statement stmSQL;
        ResultSet rstResp;
        String sqlSelect = "SELECT id, sSisOper, cProgra, cDiseno, cAdmon, iHoras FROM respuestas";

        try {
            stmSQL = conn.createStatement();
            rstResp = stmSQL.executeQuery(sqlSelect);
            listaRespuestas.clear(); 

            while (rstResp.next()) {
                listaRespuestas.add(new Respuesta(
                        rstResp.getInt("id"),
                        rstResp.getString("sSisOper"),
                        rstResp.getString("cProgra"),
                        rstResp.getString("cDiseno"),
                        rstResp.getString("cAdmon"),
                        rstResp.getInt("iHoras")
                ));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Practica7Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    private void jButton1ActionPerformed(ActionEvent event) {
        // 1. VALIDACIÓN: Verificar si el usuario seleccionó un sistema operativo
        if (jComboBox1.getValue() == null) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Campos Incompletos");
            alerta.setHeaderText("Formulario Vacío");
            alerta.setContentText("Por favor, seleccione un Sistema Operativo para poder guardar la encuesta.");
            alerta.showAndWait();
            return; // Corta la ejecución aquí para que NO guarde nada en la BD
        }

        // 2. Si pasó la validación, recuperamos los valores de la interfaz
        String sistemaOperativo = jComboBox1.getValue();
        String programacion = jCheckBox1.isSelected() ? "Sí" : "No";
        String diseno = jCheckBox2.isSelected() ? "Sí" : "No";
        String administracion = jCheckBox3.isSelected() ? "Sí" : "No";
        int horas = jSpinner1.getValue();

        String sqlInsert = "INSERT INTO respuestas (sSisOper, cProgra, cDiseno, cAdmon, iHoras) VALUES (?, ?, ?, ?, ?)";

        try {
            java.sql.PreparedStatement pstm = conn.prepareStatement(sqlInsert);
            
            pstm.setString(1, sistemaOperativo);
            pstm.setString(2, programacion);
            pstm.setString(3, diseno);
            pstm.setString(4, administracion);
            pstm.setInt(5, horas);

            int filasAfectadas = pstm.executeUpdate();

            if (filasAfectadas > 0) {
                System.out.println("¡Registro guardado exitosamente!");
                limpiarFormulario();
                btnActualizarActionPerformed(event);
            }
            
            pstm.close();

        } catch (SQLException ex) {
            Logger.getLogger(Practica7Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void limpiarFormulario() {
        jComboBox1.setValue(null);
        jCheckBox1.setSelected(false);
        jCheckBox2.setSelected(false);
        jCheckBox3.setSelected(false);
        jSpinner1.getValueFactory().setValue(0);
    }

    @FXML
    private void jButton3ActionPerformed(ActionEvent event) {
        Respuesta seleccionada = tblRespuestas.getSelectionModel().getSelectedItem();
        
        if (seleccionada != null) {
            try {
                int id = seleccionada.getId();
                Statement stmDel = conn.createStatement();
                stmDel.execute("DELETE FROM respuestas WHERE id = " + id);
                btnActualizarActionPerformed(event);
            } catch (SQLException ex) {
                Logger.getLogger(Practica7Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void loadDriver() throws ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
    }

    private boolean connect() {
        boolean conectado = false;
        try {
            this.conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/encuesta?zeroDateTimeBehavior=CONVERT_TO_NULL"
                    + "&user=encuesta_user&password=encuesta_pass");
            conectado = true;
        } catch (SQLException ex) {
            Logger.getLogger(Practica7Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conectado;
    }

    @FXML
    private void jButton2ActionPerformed(ActionEvent event) {
        limpiarFormulario();
        System.out.println("Formulario reiniciado para una nueva encuesta.");
    }

    // Clase Modelo interna
    public static class Respuesta {
        private final int id;
        private final String sSisOper;
        private final String cProgra;
        private final String cDiseno;
        private final String cAdmon;
        private final int iHoras;

        public Respuesta(int id, String sSisOper, String cProgra, String cDiseno, String cAdmon, int iHoras) {
            this.id = id;
            this.sSisOper = sSisOper;
            this.cProgra = cProgra;
            this.cDiseno = cDiseno;
            this.cAdmon = cAdmon;
            this.iHoras = iHoras;
        }

        public int getId() { return id; }
        public String getSSisOper() { return sSisOper; }
        public String getCProgra() { return cProgra; }
        public String getCDiseno() { return cDiseno; }
        public String getCAdmon() { return cAdmon; }
        public int getIHoras() { return iHoras; }
    }
}