package com.myow;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class PacienteController {

    @FXML
    private TextField txtNomePet;
    @FXML
    private TextField txtEspecie;
    @FXML
    private TextField txtRaca;
    @FXML
    private TextField txtIdade;
    @FXML
    private TextField txtSexo;
    @FXML
    private TextField txtHistorico;
    @FXML
    private Button btnCadastrar;

    @FXML
    public void salvarPaciente() {
        // 1. Pegar os textos digitados
        String nome = txtNomePet.getText();
        String especie = txtEspecie.getText();
        String raca = txtRaca.getText();
        String idadeTexto = txtIdade.getText();
        String sexo = txtSexo.getText();
        String historico = txtHistorico.getText();

        // 2. Validação: Campos Obrigatórios (Fluxo Alternativo FA01 do UC03)
        if (nome.isEmpty() || especie.isEmpty() || raca.isEmpty() || idadeTexto.isEmpty() || sexo.isEmpty()) {
            mostrarAlerta("Erro de Validação", "Por favor, preencha todos os campos obrigatórios (*).", Alert.AlertType.ERROR);
            return;
        }

        // 3. Conversão de Idade para Inteiro (Proteção contra letras)
        int idade;
        try {
            idade = Integer.parseInt(idadeTexto);
        } catch (NumberFormatException e) {
            mostrarAlerta("Erro de Validação", "A idade deve ser um número inteiro válido.", Alert.AlertType.ERROR);
            return;
        }

        // 4. Criar o objeto Paciente
        // Nota: O ID (0) será gerado pelo banco de dados depois. O Tutor (null) será vinculado em uma tela futura.
        Paciente novoPaciente = new Paciente(0, nome, idade, raca, especie, sexo, historico, null);

        // 5. Integração futura com o Banco de Dados:
        // PacienteDAO dao = new PacienteDAO();
        // dao.salvar(novoPaciente);

        // 6. Mensagem de Sucesso e Limpeza
        mostrarAlerta("Sucesso", "Paciente cadastrado com sucesso!", Alert.AlertType.INFORMATION);
        limparCampos();
    }

    private void mostrarAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    private void limparCampos() {
        txtNomePet.clear();
        txtEspecie.clear();
        txtRaca.clear();
        txtIdade.clear();
        txtSexo.clear();
        txtHistorico.clear();
    }
}