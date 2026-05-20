package principal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // 1. Carga el archivo FXML una sola vez (asegúrate de que las mayúsculas/minúsculas de "practica7.fxml" coincidan con tu archivo)
        Parent root = FXMLLoader.load(getClass().getResource("practica7.fxml"));

        // 2. Crea la escena una sola vez
        Scene scene = new Scene(root);
        
        // 3. Configura el título y la escena en el escenario
        stage.setTitle("Práctica 7 - Administrador de Respuestas");
        stage.setScene(scene);
        
        // 4. Muestra la ventana
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}