package br.com.clinica.controller;

import br.com.clinica.MainApp;
import br.com.clinica.dao.ConsultaDAO;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.model.Consulta;
import br.com.clinica.model.Paciente;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AgendarConsultaController {

    @FXML private ComboBox<Paciente> cbPaciente;
    @FXML private ComboBox<String> cbVeterinario;
    @FXML private DatePicker dpData;
    @FXML private ComboBox<String> cbHorario;
    @FXML private TextField txtMotivo;
    @FXML private TextArea txtObservacoes;

    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final ConsultaDAO consultaDAO = new ConsultaDAO();

    @FXML
    private void initialize() {
        // Carrega veterinários da clínica
        cbVeterinario.setItems(FXCollections.observableArrayList(
                "Dra. Camila Torres (CRMV-SP 18.442) - Clínica Geral",
                "Dr. Lucas Mendes (CRMV-SP 22.810) - Cirurgia & Ortopedia",
                "Dra. Beatriz Lima (CRMV-SP 15.933) - Dermatologia Veterinária"
        ));

        // Horários de atendimento
        cbHorario.setItems(FXCollections.observableArrayList(
                "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
                "11:00", "11:30", "14:00", "14:30", "15:00", "15:30",
                "16:00", "16:30", "17:00", "17:30"
        ));

        // Define data mínima como hoje
        dpData.setValue(LocalDate.now());

        carregarPacientes();
    }

    private void carregarPacientes() {
        try {
            List<Paciente> pacientes = pacienteDAO.listarTodos();
            cbPaciente.setItems(FXCollections.observableArrayList(pacientes));
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao Carregar Pacientes", e.getMessage());
        }
    }

    @FXML
    private void handleAgendar() {
        Paciente paciente = cbPaciente.getValue();
        String veterinario = cbVeterinario.getValue();
        LocalDate data = dpData.getValue();
        String horario = cbHorario.getValue();
        String motivo = txtMotivo.getText() != null ? txtMotivo.getText().trim() : "";
        String obs = txtObservacoes.getText() != null ? txtObservacoes.getText().trim() : "";

        if (paciente == null || veterinario == null || data == null || horario == null || motivo.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Obrigatórios", "Atenção", "Preencha o paciente, veterinário, data, horário e motivo da consulta.");
            return;
        }

        try {
            Consulta novaConsulta = new Consulta(
                    0,
                    paciente.getId(),
                    veterinario,
                    data.toString(),
                    horario,
                    motivo,
                    "AGENDADA",
                    obs
            );
            consultaDAO.agendar(novaConsulta);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Consulta Agendada", "Sucesso!",
                    "Consulta para o paciente " + paciente.getNome() + " agendada com sucesso para " + data + " às " + horario + ".");
            handleVoltar();
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao Agendar Consulta", e.getMessage());
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
