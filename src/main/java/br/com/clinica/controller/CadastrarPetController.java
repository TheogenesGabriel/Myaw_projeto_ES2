package br.com.clinica.controller;

import br.com.clinica.MainApp;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.dao.TutorDAO;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Tutor;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.List;

public class CadastrarPetController {

    @FXML private ComboBox<Tutor> cbTutor;
    @FXML private TextField txtNomePet;
    @FXML private ComboBox<String> cbEspecie;
    @FXML private TextField txtRaca;
    @FXML private TextField txtIdade;
    @FXML private RadioButton rbMacho;
    @FXML private RadioButton rbFemea;
    @FXML private ToggleGroup tgSexo;
    @FXML private TextArea txtHistorico;

    private final TutorDAO tutorDAO = new TutorDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();

    @FXML
    private void initialize() {
        // Carrega espécies comuns
        cbEspecie.setItems(FXCollections.observableArrayList(
                "Canina", "Felina", "Ave", "Roedor", "Réptil", "Outro"
        ));

        // Carrega lista de tutores do banco SQLite
        carregarTutores();
    }

    private void carregarTutores() {
        try {
            List<Tutor> tutores = tutorDAO.listarTodos();
            cbTutor.setItems(FXCollections.observableArrayList(tutores));
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao Carregar Tutores", e.getMessage());
        }
    }

    @FXML
    private void handleSalvar() {
        Tutor tutorSelecionado = cbTutor.getValue();
        String nome = txtNomePet.getText() != null ? txtNomePet.getText().trim() : "";
        String especie = cbEspecie.getValue();
        String raca = txtRaca.getText() != null ? txtRaca.getText().trim() : "";
        String idadeStr = txtIdade.getText() != null ? txtIdade.getText().trim() : "";
        String sexo = rbMacho.isSelected() ? "Macho" : (rbFemea.isSelected() ? "Fêmea" : "");
        String historico = txtHistorico.getText() != null ? txtHistorico.getText().trim() : "";

        if (tutorSelecionado == null || nome.isEmpty() || especie == null || raca.isEmpty() || idadeStr.isEmpty() || sexo.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Obrigatórios", "Atenção", "Preencha todos os campos obrigatórios do paciente.");
            return;
        }

        int idade;
        try {
            idade = Integer.parseInt(idadeStr);
            if (idade < 0 || idade > 50) {
                mostrarAlerta(Alert.AlertType.WARNING, "Idade Inválida", "Atenção", "A idade deve ser um número válido entre 0 e 50 anos.");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Idade Inválida", "Atenção", "Por favor, digite apenas números na idade.");
            return;
        }

        try {
            Paciente paciente = new Paciente(tutorSelecionado.getId(), nome, especie, raca, idade, sexo, historico);
            pacienteDAO.cadastrar(paciente);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Paciente Cadastrado", "Paciente " + nome + " cadastrado com sucesso e vinculado a " + tutorSelecionado.getNome() + "!");
            handleVoltar();
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro de Banco", "Falha ao Salvar Paciente", e.getMessage());
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
