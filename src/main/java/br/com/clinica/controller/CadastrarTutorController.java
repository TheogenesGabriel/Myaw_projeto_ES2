package br.com.clinica.controller;

import br.com.clinica.MainApp;
import br.com.clinica.dao.TutorDAO;
import br.com.clinica.model.Tutor;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class CadastrarTutorController {

    @FXML private TextField txtNome;
    @FXML private TextField txtCpf;
    @FXML private TextField txtTelefone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtEndereco;

    private final TutorDAO tutorDAO = new TutorDAO();

    @FXML
    private void handleSalvar() {
        String nome = txtNome.getText() != null ? txtNome.getText().trim() : "";
        String cpf = txtCpf.getText() != null ? txtCpf.getText().trim() : "";
        String telefone = txtTelefone.getText() != null ? txtTelefone.getText().trim() : "";
        String email = txtEmail.getText() != null ? txtEmail.getText().trim() : "";
        String endereco = txtEndereco.getText() != null ? txtEndereco.getText().trim() : "";

        // Validações
        if (nome.isEmpty() || cpf.isEmpty() || telefone.isEmpty() || email.isEmpty() || endereco.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Obrigatórios", "Atenção", "Preencha todos os campos do formulário para concluir o cadastro.");
            return;
        }

        try {
            if (tutorDAO.existeCpf(cpf)) {
                mostrarAlerta(Alert.AlertType.ERROR, "CPF Já Cadastrado", "Erro de Validação", "Já existe um tutor cadastrado com o CPF informado.");
                return;
            }

            Tutor tutor = new Tutor(nome, cpf, telefone, email, endereco);
            tutorDAO.cadastrar(tutor);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Tutor Cadastrado", "Tutor " + nome + " cadastrado com sucesso no banco de dados SQLite!");
            handleVoltar();
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro de Banco de Dados", "Falha ao Salvar", "Ocorreu um erro ao salvar no SQLite: " + e.getMessage());
        }
    }

    @FXML
    private void handleVoltar() {
        MainApp.navegarPara("/br/com/clinica/view/ServicosView.fxml", "Serviços da Clínica");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String cabecalho, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecalho);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
