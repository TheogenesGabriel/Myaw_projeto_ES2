package br.com.clinica;

import br.com.clinica.database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Ponto de entrada da aplicação JavaFX da Clínica Veterinária.
 * Controla o Stage principal e a navegação entre os casos de uso.
 */
public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Clínica Veterinária - Sistema de Gerenciamento");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(680);

        // Inicializa o banco SQLite
        try {
            DatabaseConnection.getConnection();
            System.out.println("Banco de dados SQLite inicializado com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao conectar no SQLite: " + e.getMessage());
        }

        // Inicia na tela de Login (admin / admin)
        navegarPara("/br/com/clinica/view/LoginView.fxml", "Login - Clínica Veterinária");
        primaryStage.show();
    }

    public static void navegarPara(String fxmlPath, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, 950, 720);
            
            // Adiciona a folha de estilo CSS
            var cssUrl = MainApp.class.getResource("/br/com/clinica/css/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            primaryStage.setScene(scene);
            if (titulo != null) {
                primaryStage.setTitle(titulo);
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar FXML [" + fxmlPath + "]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
