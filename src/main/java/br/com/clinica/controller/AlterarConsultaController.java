package br.com.clinica.controller;

import br.com.clinica.MainApp;
import br.com.clinica.dao.ConsultaDAO;
import br.com.clinica.model.Consulta;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AlterarConsultaController {

    @FXML private TableView<Consulta> tvConsultas;
    @FXML private TableColumn<Consulta, Integer> colId;
    @FXML private TableColumn<Consulta, String> colPaciente;
    @FXML private TableColumn<Consulta, String> colTutor;
    @FXML private TableColumn<Consulta, String> colData;
    @FXML private TableColumn<Consulta, String> colHorario;
    @FXML private TableColumn<Consulta, String> colVeterinario;

    @FXML private Label lblConsultaSelecionada;
    @FXML private ComboBox<String> cbVeterinario;
    @FXML private DatePicker dpNovaData;
    @FXML private ComboBox<String> cbNovoHorario;
    @FXML private TextField txtNovoMotivo;

    private final ConsultaDAO consultaDAO = new ConsultaDAO();
    private Consulta consultaSelecionada = null;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPaciente.setCellValueFactory(new PropertyValueFactory<>("pacienteNome"));
        colTutor.setCellValueFactory(new PropertyValueFactory<>("tutorNome"));
        colData.setCellValueFactory(new PropertyValueFactory<>("dataConsulta"));
        colHorario.setCellValueFactory(new PropertyValueFactory<>("horario"));
        colVeterinario.setCellValueFactory(new PropertyValueFactory<>("veterinario"));

        cbVeterinario.setItems(FXCollections.observableArrayList(
                "Dra. Camila Torres (CRMV-SP 18.442) - Clínica Geral",
                "Dr. Lucas Mendes (CRMV-SP 22.810) - Cirurgia & Ortopedia",
                "Dra. Beatriz Lima (CRMV-SP 15.933) - Dermatologia Veterinária"
        ));

        cbNovoHorario.setItems(FXCollections.observableArrayList(
                "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
                "11:00", "11:30", "14:00", "14:30", "15:00", "15:30",
                "16:00", "16:30", "17:00", "17:30"
        ));

        tvConsultas.getSelectionModel().selectedItemProperty().addListener((obs, antigo, selecionado) -> {
            if (selecionado != null) {
                selecionarConsulta(selecionado);
            }
        });

        carregarConsultasAgendadas();
    }

    private void carregarConsultasAgendadas() {
        try {
            List<Consulta> lista = consultaDAO.listar("AGENDADA");
            tvConsultas.setItems(FXCollections.observableArrayList(lista));
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao Carregar Consultas", e.getMessage());
        }
    }

    private void selecionarConsulta(Consulta c) {
        this.consultaSelecionada = c;
        lblConsultaSelecionada.setText("Editando Consulta #" + c.getId() + " - " + c.getPacienteNome() + " (Tutor: " + c.getTutorNome() + ")");
        cbVeterinario.setValue(c.getVeterinario());
        try {
            dpNovaData.setValue(LocalDate.parse(c.getDataConsulta()));
        } catch (Exception ignored) {}
        cbNovoHorario.setValue(c.getHorario());
        txtNovoMotivo.setText(c.getMotivo());
    }

    @FXML
    private void handleSalvarAlteracao() {
        if (consultaSelecionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleção Obrigatória", "Atenção", "Selecione uma consulta na tabela acima para alterar.");
            return;
        }

        String veterinario = cbVeterinario.getValue();
        LocalDate data = dpNovaData.getValue();
        String horario = cbNovoHorario.getValue();
        String motivo = txtNovoMotivo.getText() != null ? txtNovoMotivo.getText().trim() : "";

        if (veterinario == null || data == null || horario == null || motivo.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Obrigatórios", "Atenção", "Preencha todos os campos para salvar a alteração.");
            return;
        }

        try {
            consultaDAO.alterar(consultaSelecionada.getId(), veterinario, data.toString(), horario, motivo);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Consulta Alterada", "Sucesso!", "A consulta #" + consultaSelecionada.getId() + " foi alterada com sucesso!");
            carregarConsultasAgendadas();
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao Alterar Consulta", e.getMessage());
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
