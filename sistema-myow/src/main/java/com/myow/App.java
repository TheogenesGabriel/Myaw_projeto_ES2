package com.myow;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // 1. Mudamos "primary" para "TelaCadastroTutor"
        // 2. Ajustamos o tamanho para 900x600 (tamanho do seu HBox no FXML)
        scene = new Scene(loadFXML("TelaCadastroTutor"), 900, 600);
        
        // 3. (Opcional) Coloca um título bonito na janela do programa
        stage.setTitle("Myow - Sistema Veterinário");
        
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}