package com.myow;

import com.myow.model.Usuario;
import com.myow.view.LoginView;
import com.myow.view.MainLayout;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MyowApplication extends Application {
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("Myow - Sistema de Gestão Veterinária Integrada");
        primaryStage.setWidth(1200);
        primaryStage.setHeight(780);
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(640);

        mostrarTelaLogin();
        primaryStage.show();
    }

    private void mostrarTelaLogin() {
        LoginView loginView = new LoginView(this::onLoginSucesso);
        Scene scene = new Scene(loginView, 1200, 780);
        primaryStage.setScene(scene);
    }

    private void onLoginSucesso(Usuario usuario) {
        MainLayout mainLayout = new MainLayout(this::mostrarTelaLogin);
        Scene scene = new Scene(mainLayout, 1200, 780);
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
