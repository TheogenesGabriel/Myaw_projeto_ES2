package br.com.clinica.controller;

import br.com.clinica.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtSenha;

    @FXML
    private void initialize() {
        // Inicializa focado no usuário
    }

    @FXML
    private void handleLogin() {
        String usuario = txtUsuario.getText() != null ? txtUsuario.getText().trim() : "";
        String senha = txtSenha.getText() != null ? txtSenha.getText().trim() : "";

        // Requisito da especificação: apenas usuário 'admin' e senha 'admin'
        if ("admin".equals(usuario) && "admin".equals(senha)) {
            MainApp.navegarPara("/br/com/clinica/view/ServicosView.fxml", "Serviços da Clínica - Administrador");
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro de Autenticação");
            alert.setHeaderText("Acesso Negado");
            alert.setContentText("Usuário ou senha incorretos! Por favor, utilize usuário 'admin' e senha 'admin'.");
            alert.showAndWait();
        }
    }
}
