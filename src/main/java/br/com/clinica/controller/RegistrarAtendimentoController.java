package br.com.clinica.controller;

import br.com.clinica.MainApp;
import br.com.clinica.dao.AtendimentoDAO;
import br.com.clinica.dao.ConsultaDAO;
import br.com.clinica.model.Atendimento;
import br.com.clinica.model.Consulta;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

public class RegistrarAtendimentoController {

    @FXML private TableView<Consulta> tvConsultas;
    @FXML private TableColumn<Consulta, Integer> colId;
    @FXML private TableColumn<Consulta, String> colPaciente;
    @FXML private TableColumn<Consulta, String> colTutor;
    @FXML private TableColumn<Consulta, String> colData;
    @FXML private TableColumn<Consulta, String> colHorario;

    @FXML private Label lblPacienteInfo;
    @FXML private TextField txtPeso;
    @FXML private TextField txtTemperatura;
    @FXML private TextField txtFrequencia;
    @FXML private TextArea txtDiagnostico;
    @FXML private TextArea txtProcedimentos;
    @FXML private TextArea txtPrescricao;
    @FXML private TextArea txtRecomendacoes;

    private final ConsultaDAO consultaDAO = new ConsultaDAO();
    private final AtendimentoDAO atendimentoDAO = new AtendimentoDAO();
    private Consulta consultaSelecionada = null;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPaciente.setCellValueFactory(new PropertyValueFactory<>("pacienteNome"));
        colTutor.setCellValueFactory(new PropertyValueFactory<>("tutorNome"));
        colData.setCellValueFactory(new PropertyValueFactory<>("dataConsulta"));
        colHorario.setCellValueFactory(new PropertyValueFactory<>("horario"));

        tvConsultas.getSelectionModel().selectedItemProperty().addListener((obs, antigo, selecionado) -> {
            if (selecionado != null) {
                this.consultaSelecionada = selecionado;
                lblPacienteInfo.setText("Atendendo Paciente: " + selecionado.getPacienteNome() + " | Tutor: " + selecionado.getTutorNome() + " (Consulta #" + selecionado.getId() + ")");
            }
        });

        carregarConsultasAguardando();
    }

    private void carregarConsultasAguardando() {
        try {
            List<Consulta> lista = consultaDAO.listar("AGENDADA");
            tvConsultas.setItems(FXCollections.observableArrayList(lista));
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao Carregar Consultas", e.getMessage());
        }
    }

    @FXML
    private void handleSalvarAtendimento() {
        if (consultaSelecionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleção Obrigatória", "Atenção", "Selecione uma consulta agendada na tabela para registrar o atendimento.");
            return;
        }

        String pesoStr = txtPeso.getText() != null ? txtPeso.getText().trim() : "";
        String tempStr = txtTemperatura.getText() != null ? txtTemperatura.getText().trim() : "";
        String freqStr = txtFrequencia.getText() != null ? txtFrequencia.getText().trim() : "";
        String diagnostico = txtDiagnostico.getText() != null ? txtDiagnostico.getText().trim() : "";
        String procedimentos = txtProcedimentos.getText() != null ? txtProcedimentos.getText().trim() : "";
        String prescricao = txtPrescricao.getText() != null ? txtPrescricao.getText().trim() : "";
        String recomendacoes = txtRecomendacoes.getText() != null ? txtRecomendacoes.getText().trim() : "";

        if (diagnostico.isEmpty() || procedimentos.isEmpty() || prescricao.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Clínicos Obrigatórios", "Atenção", "Diagnóstico, Procedimentos e Prescrição Medicamentosa são de preenchimento obrigatório no prontuário.");
            return;
        }

        double peso = 0.0;
        double temp = 0.0;
        int freq = 0;

        try {
            if (!pesoStr.isEmpty()) peso = Double.parseDouble(pesoStr.replace(",", "."));
            if (!tempStr.isEmpty()) temp = Double.parseDouble(tempStr.replace(",", "."));
            if (!freqStr.isEmpty()) freq = Integer.parseInt(freqStr);
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sinais Vitais Inválidos", "Atenção", "Verifique os valores numéricos informados em Peso, Temperatura ou Frequência Cardíaca.");
            return;
        }

        try {
            Atendimento atendimento = new Atendimento(
                    consultaSelecionada.getId(),
                    peso,
                    temp,
                    freq,
                    diagnostico,
                    procedimentos,
                    prescricao,
                    recomendacoes
            );
            atendimentoDAO.registrar(atendimento);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Atendimento Registrado", "Sucesso!",
                    "Prontuário médico salvo no SQLite e consulta do paciente " + consultaSelecionada.getPacienteNome() + " marcada como REALIZADA!");

            limparFormulario();
            carregarConsultasAguardando();
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Erro ao Salvar Prontuário", e.getMessage());
        }
    }

    private void limparFormulario() {
        txtPeso.clear();
        txtTemperatura.clear();
        txtFrequencia.clear();
        txtDiagnostico.clear();
        txtProcedimentos.clear();
        txtPrescricao.clear();
        txtRecomendacoes.clear();
        lblPacienteInfo.setText("Selecione uma consulta na tabela para iniciar o atendimento");
        consultaSelecionada = null;
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
