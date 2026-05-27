package principal;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;

public class BibliotecaController implements Initializable {

    @FXML private TextField txtBuscar;
    @FXML private Button btnSincronizar;
    @FXML private TableView<Libro> tblLibros;
    @FXML private TableColumn<Libro, String> colIsbn;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, String> colAutor;
    @FXML private TableColumn<Libro, String> colEstado;
    @FXML private Button btnPrestar;
    @FXML private Button btnDevolver;
    @FXML private Button btnAgregar;
    @FXML private Label lblEstado;
    @FXML private ProgressBar progressCarga;

    private Connection conn;
    private final ObservableList<Libro> listaLibros = FXCollections.observableArrayList();
    private FilteredList<Libro> librosFiltrados;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colAutor.setCellValueFactory(new PropertyValueFactory<>("autor"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        connect();
        cargarLibros();

        // BUSCADOR
        librosFiltrados = new FilteredList<>(listaLibros, p -> true);

        txtBuscar.textProperty().addListener((obs, oldText, newText) -> {
            String filtro = newText.toLowerCase().trim();

            librosFiltrados.setPredicate(libro -> {
                if (filtro.isEmpty()) return true;

                return libro.getIsbn().toLowerCase().contains(filtro)
                    || libro.getTitulo().toLowerCase().contains(filtro)
                    || libro.getAutor().toLowerCase().contains(filtro)
                    || libro.getEstado().toLowerCase().contains(filtro);
            });
        });

        SortedList<Libro> ordenada = new SortedList<>(librosFiltrados);
        ordenada.comparatorProperty().bind(tblLibros.comparatorProperty());
        tblLibros.setItems(ordenada);
    }

    private void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/BibliotecaProyecto?useSSL=false&serverTimezone=UTC",
                "root",
                ""
            );
            System.out.println("Conectado a MySQL");
        } catch (Exception e) {
            mostrarAlerta("Error BD", "No se pudo conectar a MySQL");
            e.printStackTrace();
        }
    }

    private void cargarLibros() {
        listaLibros.clear();
        try {
            var stmt = conn.prepareStatement("SELECT * FROM libros");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Libro l = new Libro(
                    rs.getString("isbn"),
                    rs.getString("titulo"),
                    rs.getString("autor"),
                    rs.getString("estado"),
                    rs.getString("prestamo")
                );
                listaLibros.add(l);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void btnSincronizarActionPerformed(ActionEvent event) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Sincronizando...");
                for (int i = 1; i <= 100; i++) {
                    Thread.sleep(15);
                    updateProgress(i, 100);
                }
                return null;
            }
        };

        lblEstado.textProperty().bind(task.messageProperty());
        progressCarga.progressProperty().bind(task.progressProperty());

        task.setOnSucceeded(e -> {
            lblEstado.textProperty().unbind();
            progressCarga.progressProperty().unbind();
            cargarLibros();
            mostrarInfo("Sincronización", "Base de datos sincronizada");
        });

        new Thread(task).start();
    }

    @FXML
    private void btnPrestarActionPerformed(ActionEvent event) {

        Libro libro = tblLibros.getSelectionModel().getSelectedItem();
        if (libro == null) {
            mostrarAlerta("Aviso", "Selecciona un libro");
            return;
        }

        if (libro.getEstado().equals("Prest.")) {
            mostrarAlerta(
                "No disponible",
                "Libro prestado a: " + libro.getPrestamo()
            );
            return;
        }

        TextInputDialog d = new TextInputDialog();
        d.setTitle("Préstamo");
        d.setHeaderText(libro.getTitulo());
        d.setContentText("Nombre del solicitante:");

        Optional<String> res = d.showAndWait();
        if (res.isEmpty() || res.get().trim().isEmpty()) return;

        try {
            var stmt = conn.prepareStatement(
                "UPDATE libros SET estado='Prest.', prestamo=? WHERE isbn=?"
            );
            stmt.setString(1, res.get().trim());
            stmt.setString(2, libro.getIsbn());
            stmt.executeUpdate();

            cargarLibros();
            mostrarInfo("Préstamo", "Libro prestado correctamente");

        } catch (Exception e) {
            mostrarAlerta("Error BD", e.getMessage());
        }
    }

    @FXML
    private void btnDevolverActionPerformed(ActionEvent event) {

        Libro libro = tblLibros.getSelectionModel().getSelectedItem();
        if (libro == null) {
            mostrarAlerta("Aviso", "Selecciona un libro");
            return;
        }

        if (!libro.getEstado().equals("Prest.")) {
            mostrarAlerta("Aviso", "El libro no está prestado");
            return;
        }

        try {
            var stmt = conn.prepareStatement(
                "UPDATE libros SET estado='Disp.', prestamo=NULL WHERE isbn=?"
            );
            stmt.setString(1, libro.getIsbn());
            stmt.executeUpdate();

            cargarLibros();
            mostrarInfo("Devolución", "Libro devuelto correctamente");

        } catch (Exception e) {
            mostrarAlerta("Error BD", e.getMessage());
        }
    }

    @FXML
    private void btnAgregarActionPerformed(ActionEvent event) {

        TextInputDialog d1 = new TextInputDialog();
        d1.setHeaderText("ISBN");
        Optional<String> i = d1.showAndWait();
        if (i.isEmpty()) return;

        TextInputDialog d2 = new TextInputDialog();
        d2.setHeaderText("Título");
        Optional<String> t = d2.showAndWait();
        if (t.isEmpty()) return;

        TextInputDialog d3 = new TextInputDialog();
        d3.setHeaderText("Autor");
        Optional<String> a = d3.showAndWait();
        if (a.isEmpty()) return;

        try {
            var stmt = conn.prepareStatement(
                "INSERT INTO libros (isbn, titulo, autor, estado, prestamo) VALUES (?, ?, ?, 'Disp.', NULL)"
            );
            stmt.setString(1, i.get());
            stmt.setString(2, t.get());
            stmt.setString(3, a.get());
            stmt.executeUpdate();

            cargarLibros();
            mostrarInfo("Libro agregado", "Registro guardado en la BD");

        } catch (Exception e) {
            mostrarAlerta("Error BD", e.getMessage());
        }
    }

    private void mostrarAlerta(String t, String m) {
        new Alert(AlertType.WARNING, m).showAndWait();
    }

    private void mostrarInfo(String t, String m) {
        new Alert(AlertType.INFORMATION, m).showAndWait();
    }

    public static class Libro {

        private String isbn, titulo, autor, estado, prestamo;

        public Libro(String i, String t, String a, String e, String p) {
            isbn = i;
            titulo = t;
            autor = a;
            estado = e;
            prestamo = p;
        }

        public String getIsbn() { return isbn; }
        public String getTitulo() { return titulo; }
        public String getAutor() { return autor; }
        public String getEstado() { return estado; }
        public String getPrestamo() { return prestamo; }
    }
}