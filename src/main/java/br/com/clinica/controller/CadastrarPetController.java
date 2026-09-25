package br.com.clinica.controller;

import br.com.clinica.MainApp;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.dao.TutorDAO;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Tutor;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class CadastrarPetController {

    @FXML private Label lblNomeTutor;
    @FXML private ComboBox<Tutor> cbTutor;
    @FXML private TextField txtNomePet;
    @FXML private ComboBox<String> cbEspecie;
    @FXML private TextField txtRaca;
    @FXML private TextField txtIdade;
    @FXML private RadioButton rbMacho;
    @FXML private RadioButton rbFemea;
    @FXML private ToggleGroup tgSexo;
    @FXML private TextArea txtHistorico;

    private Tutor tutorSelecionado;

    private final TutorDAO tutorDAO = new TutorDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();

    private com.myow.model.Tutor tutorMyow;
    private final com.myow.service.PacienteService myowPacienteService = new com.myow.service.PacienteService();
    private final com.myow.service.TutorService myowTutorService = new com.myow.service.TutorService();

    public void setTutor(Tutor tutor) {
        this.tutorSelecionado = tutor;
        if (lblNomeTutor != null) {
            lblNomeTutor.setText(tutor != null ? "Tutor: " + tutor.getNome() : "Tutor: Nenhum selecionado");
        }
        if (cbTutor != null && tutor != null) {
            cbTutor.setValue(tutor);
        }
    }

    public void setTutor(com.myow.model.Tutor tutorMyow) {
        if (tutorMyow == null) return;
        this.tutorMyow = tutorMyow;

        int idInt = 1;
        try {
            List<Tutor> tutoresClinica = tutorDAO.listarTodos();
            for (Tutor tc : tutoresClinica) {
                if (tc.getCpf() != null && tc.getCpf().trim().equals(tutorMyow.getCpf().trim())) {
                    idInt = tc.getId();
                    break;
                }
            }
            if (idInt == 1 && (tutoresClinica.isEmpty() || !tutoresClinica.get(0).getCpf().equals(tutorMyow.getCpf()))) {
                Tutor novoTutorClinica = new Tutor(tutorMyow.getNomeCompleto(), tutorMyow.getCpf(), tutorMyow.getTelefone(), tutorMyow.getEmail(), tutorMyow.getEndereco());
                tutorDAO.cadastrar(novoTutorClinica);
                idInt = novoTutorClinica.getId();
            }
        } catch (Exception ignored) {}

        Tutor t = new Tutor(idInt, tutorMyow.getNomeCompleto(), tutorMyow.getCpf(), tutorMyow.getTelefone(), tutorMyow.getEmail(), tutorMyow.getEndereco());
        setTutor(t);
    }

    @FXML
    private void initialize() {
        // Carrega espécies comuns
        cbEspecie.setItems(FXCollections.observableArrayList(
                "Canina", "Felina", "Ave", "Roedor", "Réptil", "Outro"
        ));

        // Carrega lista de tutores do banco SQLite
        carregarTutores();

        if (tutorSelecionado != null && lblNomeTutor != null) {
            lblNomeTutor.setText("Tutor: " + tutorSelecionado.getNome());
        }
    }

    private void carregarTutores() {
        try {
            List<Tutor> tutores = tutorDAO.listarTodos();
            cbTutor.setItems(FXCollections.observableArrayList(tutores));
            if (tutorSelecionado != null) {
                cbTutor.setValue(tutorSelecionado);
            }
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao Carregar Tutores", e.getMessage());
        }
    }

    @FXML
    private void handleSalvar() {
        Tutor tutor = (this.tutorSelecionado != null) ? this.tutorSelecionado : cbTutor.getValue();
        String nome = txtNomePet.getText() != null ? txtNomePet.getText().trim() : "";
        String especie = cbEspecie.getValue();
        String raca = txtRaca.getText() != null ? txtRaca.getText().trim() : "";
        String idadeStr = txtIdade.getText() != null ? txtIdade.getText().trim() : "";
        String sexo = rbMacho.isSelected() ? "Macho" : (rbFemea.isSelected() ? "Fêmea" : "");
        String historico = txtHistorico.getText() != null ? txtHistorico.getText().trim() : "";

        if (tutor == null || nome.isEmpty() || especie == null || raca.isEmpty() || idadeStr.isEmpty() || sexo.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Obrigatórios", "Atenção", "Preencha todos os campos obrigatórios do paciente e certifique-se de que o tutor está vinculado.");
            return;
        }

        this.tutorSelecionado = tutor;

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

        // 1. Persiste no banco de dados Myow (database/myow.db) para exibir na tela de Pacientes
        String myowTutorId = (this.tutorMyow != null) ? this.tutorMyow.getId() : null;
        if (myowTutorId == null && tutor != null) {
            List<com.myow.model.Tutor> listaMyow = myowTutorService.listarTodos();
            for (com.myow.model.Tutor tm : listaMyow) {
                if (tm.getCpf() != null && tm.getCpf().trim().equals(tutor.getCpf().trim())) {
                    myowTutorId = tm.getId();
                    break;
                }
            }
            if (myowTutorId == null && !listaMyow.isEmpty()) {
                myowTutorId = listaMyow.get(0).getId();
            }
        }

        if (myowTutorId != null) {
            var resMyow = myowPacienteService.cadastrar(nome, especie, raca, idade, sexo, myowTutorId, historico, true);
            if (!resMyow.isSuccess()) {
                System.err.println("Aviso Myow: " + resMyow.getMessage());
            }
        }

        // 2. Persiste no banco SQLite da clínica (clinica_vet.db)
        try {
            Paciente paciente = new Paciente(tutorSelecionado.getId(), nome, especie, raca, idade, sexo, historico);
            pacienteDAO.cadastrar(paciente);
        } catch (SQLException e) {
            System.err.println("Aviso clinica_vet: " + e.getMessage());
        }

        mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Paciente Cadastrado", "Paciente " + nome + " cadastrado com sucesso e vinculado a " + tutorSelecionado.getNome() + "!");
        handleVoltar();
    }

    @FXML
    private void handleVoltar() {
        if (lblNomeTutor != null && lblNomeTutor.getScene() != null && lblNomeTutor.getScene().getWindow() instanceof Stage) {
            Stage currentStage = (Stage) lblNomeTutor.getScene().getWindow();
            if (currentStage != MainApp.getPrimaryStage()) {
                currentStage.close();
                return;
            }
        }
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
