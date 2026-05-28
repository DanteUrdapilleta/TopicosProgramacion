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
import javafx.scene.control.cell.TextFieldTableCell;

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

        // Habilitar la edición interactiva en la TableView mediante doble clic
        tblLibros.setEditable(true);
        
        // Asignar celdas de tipo texto a las columnas editables
        colIsbn.setCellFactory(TextFieldTableCell.forTableColumn());
        colTitulo.setCellFactory(TextFieldTableCell.forTableColumn());
        colAutor.setCellFactory(TextFieldTableCell.forTableColumn());

        // Evento al confirmar edición del ISBN (Valida que no se duplique)
        colIsbn.setOnEditCommit(event -> {
            String antiguoIsbn = event.getOldValue();
            String nuevoIsbn = event.getNewValue().trim();
            Libro libro = event.getRowValue();

            if (nuevoIsbn.isEmpty()) {
                mostrarAlerta("Error", "El ISBN no puede estar vacío.");
                tblLibros.refresh();
                return;
            }

            if (antiguoIsbn.equals(nuevoIsbn)) return;

            if (existeIsbn(nuevoIsbn)) {
                mostrarAlerta("Error de duplicado", "El ISBN '" + nuevoIsbn + "' ya está registrado.");
                tblLibros.refresh(); 
            } else {
                actualizarCampoEnBD("isbn", nuevoIsbn, antiguoIsbn);
                libro.setIsbn(nuevoIsbn);
            }
        });

        // Evento al confirmar edición del Título
        colTitulo.setOnEditCommit(event -> {
            String nuevoTitulo = event.getNewValue().trim();
            Libro libro = event.getRowValue();
            
            if (nuevoTitulo.isEmpty()) {
                mostrarAlerta("Error", "El título no puede estar vacío.");
                tblLibros.refresh();
                return;
            }
            
            actualizarCampoEnBD("titulo", nuevoTitulo, libro.getIsbn());
            libro.setTitulo(nuevoTitulo);
        });

        // Evento al confirmar edición del Autor
        colAutor.setOnEditCommit(event -> {
            String nuevoAutor = event.getNewValue().trim();
            Libro libro = event.getRowValue();
            
            if (nuevoAutor.isEmpty()) {
                mostrarAlerta("Error", "El autor no puede estar vacío.");
                tblLibros.refresh();
                return;
            }

            actualizarCampoEnBD("autor", nuevoAutor, libro.getIsbn());
            libro.setAutor(nuevoAutor);
        });

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

    private boolean existeIsbn(String isbn) {
        try {
            var stmt = conn.prepareStatement("SELECT COUNT(*) FROM libros WHERE isbn = ?");
            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void actualizarCampoEnBD(String columna, String nuevoValor, String isbnReferencia) {
        try {
            String query = "UPDATE libros SET " + columna + " = ? WHERE isbn = ?";
            var stmt = conn.prepareStatement(query);
            stmt.setString(1, nuevoValor);
            stmt.setString(2, isbnReferencia);
            stmt.executeUpdate();
            System.out.println("Base de datos actualizada correctamente.");
        } catch (Exception e) {
            mostrarAlerta("Error BD", "No se pudo actualizar la base de datos: " + e.getMessage());
            cargarLibros(); 
        }
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
            // 1. Romper el enlace con la tarea finalizada
            lblEstado.textProperty().unbind();
            progressCarga.progressProperty().unbind();
            
            // 2. Limpiar la interfaz (Devolver la barra a 0 y vaciar el texto)
            lblEstado.setText("");
            progressCarga.setProgress(0.0);
            
            // 3. Cargar la base de datos y lanzar notificación
            cargarLibros();
            mostrarInfo("Sincronización", "Base de datos sincronizada");
        });

        // Manejo alternativo en caso de que la tarea falle o se cancele
        task.setOnFailed(e -> {
            lblEstado.textProperty().unbind();
            progressCarga.progressProperty().unbind();
            lblEstado.setText("Error en la sincronización.");
            progressCarga.setProgress(0.0);
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
        public void setIsbn(String isbn) { this.isbn = isbn; }

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }

        public String getAutor() { return autor; }
        public void setAutor(String autor) { this.autor = autor; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }

        public String getPrestamo() { return prestamo; }
        public void setPrestamo(String prestamo) { this.prestamo = prestamo; }
    }
}