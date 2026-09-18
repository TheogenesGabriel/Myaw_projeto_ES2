package br.com.clinica.controller;

import br.com.clinica.MainApp;
import br.com.clinica.dao.ConsultaDAO;
import br.com.clinica.model.Consulta;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

public class CancelarConsultaController {

    @FXML private TableView<Consulta> tvConsultas;
    @FXML private TableColumn<Consulta, Integer> colId;
    @FXML private TableColumn<Consulta, String> colPaciente;
    @FXML private TableColumn<Consulta, String> colTutor;
    @FXML private TableColumn<Consulta, String> colData;
    @FXML private TableColumn<Consulta, String> colHorario;
    @FXML private TableColumn<Consulta, String> colVeterinario;

    @FXML private Label lblConsultaInfo;
    @FXML private TextArea txtMotivoCancelamento;

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

        tvConsultas.getSelectionModel().selectedItemProperty().addListener((obs, antigo, selecionado) -> {
            if (selecionado != null) {
                this.consultaSelecionada = selecionado;
                lblConsultaInfo.setText("Consulta Selecionada: #" + selecionado.getId() + " - " + selecionado.getPacienteNome() + " (" + selecionado.getDataConsulta() + " às " + selecionado.getHorario() + ")");
            }
        });

        carregarConsultas();
    }

    private void carregarConsultas() {
        try {
            List<Consulta> lista = consultaDAO.listar("AGENDADA");
            tvConsultas.setItems(FXCollections.observableArrayList(lista));
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao Carregar Consultas", e.getMessage());
        }
    }

    @FXML
    private void handleConfirmarCancelamento() {
        if (consultaSelecionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleção Obrigatória", "Atenção", "Selecione uma consulta agendada para cancelar.");
            return;
        }

        String motivo = txtMotivoCancelamento.getText() != null ? txtMotivoCancelamento.getText().trim() : "";
        if (motivo.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Justificativa Obrigatória", "Atenção", "Informe o motivo do cancelamento da consulta.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Cancelamento");
        confirm.setHeaderText("Deseja realmente cancelar a consulta #" + consultaSelecionada.getId() + "?");
        confirm.setContentText("Esta ação cancelará o agendamento e liberará o horário na agenda médica.");

        var result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                consultaDAO.cancelar(consultaSelecionada.getId(), motivo);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Consulta Cancelada", "A consulta #" + consultaSelecionada.getId() + " foi cancelada com sucesso.");
                txtMotivoCancelamento.clear();
                lblConsultaInfo.setText("Selecione uma consulta na tabela acima");
                consultaSelecionada = null;
                carregarConsultas();
            } catch (SQLException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao Cancelar Consulta", e.getMessage());
            }
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
