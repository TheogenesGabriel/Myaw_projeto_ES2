package com.myow;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class TutorController {

    // A anotação @FXML liga essas variáveis exatamente aos fx:id que colocamos na tela
    @FXML
    private TextField txtNomeTutor;
    @FXML
    private TextField txtCpf;
    @FXML
    private TextField txtTelefone;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtEndereco;
    @FXML
    private Button btnCadastrarTutor;

    // Método que será chamado quando o botão for clicado
    @FXML
    public void salvarTutor() {
        // 1. Pegar os textos digitados
        String nome = txtNomeTutor.getText();
        String cpf = txtCpf.getText();
        String telefone = txtTelefone.getText();
        String email = txtEmail.getText();
        String endereco = txtEndereco.getText();

        // 2. Validação: Campos Obrigatórios (Conforme Fluxo Alternativo FA01 do UC02)
        if (nome.isEmpty() || cpf.isEmpty() || telefone.isEmpty()) {
            mostrarAlerta("Erro de Validação", "Por favor, preencha todos os campos obrigatórios (*).", Alert.AlertType.ERROR);
            return; // Interrompe a execução aqui se faltar algo
        }

        // 3. Criar o objeto usando sua classe Tutor
        Tutor novoTutor = new Tutor(nome, cpf, telefone, email, endereco);

        // 4. Aqui entrará a comunicação com o Banco de Dados no futuro:
        // TutorDAO dao = new TutorDAO();
        // dao.salvar(novoTutor);

        // 5. Mostrar mensagem de sucesso e limpar a tela
        mostrarAlerta("Sucesso", "Tutor cadastrado com sucesso!", Alert.AlertType.INFORMATION);
        limparCampos();
    }

    // Método auxiliar para criar janelas de aviso (Pop-ups)
    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    // Método auxiliar para esvaziar os TextFields após salvar
    private void limparCampos() {
        txtNomeTutor.clear();
        txtCpf.clear();
        txtTelefone.clear();
        txtEmail.clear();
        txtEndereco.clear();
    }
}