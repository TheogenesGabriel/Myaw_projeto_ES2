package com.myow.view;

import com.myow.dao.UsuarioDAO;
import com.myow.model.*;
import com.myow.service.*;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class ProntuarioView extends BorderPane {
    private final PacienteService pacienteService = new PacienteService();
    private final TutorService tutorService = new TutorService();
    private final AtendimentoService atendimentoService = new AtendimentoService();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private final ListView<Paciente> pacientesList = new ListView<>();
    private final VBox historicoContainer = new VBox(16);
    private final Label lblTituloPaciente = new Label("Selecione um paciente para consultar o histórico médico");
    private final Label lblSubtituloPaciente = new Label("Todos os atendimentos clínicos anteriores organizados cronologicamente.");

    public ProntuarioView() {
        setStyle("-fx-background-color: transparent;");
        setPadding(new Insets(0, 24, 24, 24));
        initUI();
    }

    private void initUI() {
        // Top Header Card (Figma Style)
        Button btnAtualizar = UIUtils.createSecondaryButton("🔄 Atualizar Lista");
        btnAtualizar.setOnAction(e -> {
            pacientesList.setItems(FXCollections.observableArrayList(pacienteService.listarTodos()));
            if (!pacientesList.getItems().isEmpty()) {
                pacientesList.getSelectionModel().select(0);
            }
        });

        HBox headerCard = UIUtils.createHeaderCard("📋  Prontuário Médico Veterinário", btnAtualizar);
        setTop(headerCard);

        // Center split
        SplitPane split = new SplitPane();
        split.setStyle("-fx-box-border: transparent; -fx-background-color: transparent; -fx-padding: 16 0 0 0;");

        // Left: Patient Search & List
        VBox leftPane = UIUtils.createCard("-fx-padding: 18;");
        leftPane.setPrefWidth(300);
        leftPane.setMaxWidth(350);

        Label searchLbl = new Label("Localizar Paciente");
        searchLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        searchLbl.setTextFill(Color.web("#0a362f"));

        TextField searchField = new TextField();
        searchField.setPromptText("Buscar paciente...");
        searchField.setStyle("-fx-padding: 8 10; -fx-background-radius: 8; -fx-border-color: #cbd5e1; -fx-border-radius: 8;");
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            pacientesList.setItems(FXCollections.observableArrayList(pacienteService.filtrar(newV)));
        });

        pacientesList.setItems(FXCollections.observableArrayList(pacienteService.listarTodos()));
        pacientesList.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        pacientesList.setCellFactory(p -> new ListCell<>() {
            @Override
            protected void updateItem(Paciente pac, boolean empty) {
                super.updateItem(pac, empty);
                if (empty || pac == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPadding(new Insets(4, 2, 4, 2));

                    Label avatar = new Label("🐾");
                    avatar.setStyle("-fx-background-color: #cbf3ea; -fx-min-width: 28px; -fx-min-height: 28px; -fx-max-width: 28px; -fx-max-height: 28px; -fx-alignment: center; -fx-background-radius: 14; -fx-font-size: 12px;");

                    VBox b = new VBox(2);
                    Label n = new Label(pac.getNome());
                    n.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                    n.setTextFill(Color.web("#0f172a"));

                    Label s = new Label(pac.getEspecie() + " • " + pac.getRaca());
                    s.setFont(Font.font("Segoe UI", 11));
                    s.setTextFill(Color.web("#64748b"));
                    b.getChildren().addAll(n, s);

                    row.getChildren().addAll(avatar, b);
                    setGraphic(row);
                }
            }
        });

        pacientesList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                carregarProntuario(newV);
            }
        });

        VBox.setVgrow(pacientesList, Priority.ALWAYS);
        leftPane.getChildren().addAll(searchLbl, searchField, pacientesList);

        // Right: Timeline / Clinical Record history
        VBox rightPane = UIUtils.createCard("-fx-padding: 20;");
        lblTituloPaciente.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblTituloPaciente.setTextFill(Color.web("#064e43"));

        lblSubtituloPaciente.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        lblSubtituloPaciente.setTextFill(Color.web("#64748b"));

        VBox topInfo = new VBox(4, lblTituloPaciente, lblSubtituloPaciente);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: white;");

        historicoContainer.setPadding(new Insets(14, 0, 14, 0));
        scroll.setContent(historicoContainer);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        rightPane.getChildren().addAll(topInfo, new Separator(), scroll);

        split.getItems().addAll(leftPane, rightPane);
        split.setDividerPositions(0.3);

        setCenter(split);

        // Select first patient if available
        if (!pacienteService.listarTodos().isEmpty()) {
            pacientesList.getSelectionModel().select(0);
        }
    }

    private void carregarProntuario(Paciente pac) {
        Tutor tutor = tutorService.buscarPorId(pac.getTutorId()).orElse(null);
        String tutorStr = tutor != null ? tutor.getNomeCompleto() + " (" + tutor.getTelefone() + ")" : "Não identificado";

        lblTituloPaciente.setText("🐾 Prontuário de " + pac.getNome() + " (" + pac.getEspecie() + " - " + pac.getRaca() + ", " + pac.getIdade() + " anos, " + pac.getSexo() + ")");
        lblSubtituloPaciente.setText("Tutor Responsável: " + tutorStr + " | Cadastrado em: " + pac.getDataCadastro());

        historicoContainer.getChildren().clear();

        // Baseline history note
        VBox baseCard = new VBox(6);
        baseCard.setStyle("-fx-background-color: #f8fafc; -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        Label baseTitle = new Label("Anamnese / Histórico Clínico de Base:");
        baseTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        Label baseText = new Label(pac.getHistoricoMedico() != null && !pac.getHistoricoMedico().isEmpty() ? pac.getHistoricoMedico() : "Nenhuma ocorrência pregressa anotada no cadastro.");
        baseText.setFont(Font.font("Segoe UI", 12));
        baseText.setTextFill(Color.web("#475569"));
        baseText.setWrapText(true);
        baseCard.getChildren().addAll(baseTitle, baseText);
        historicoContainer.getChildren().add(baseCard);

        List<Atendimento> historico = atendimentoService.listarPorPaciente(pac.getId());

        if (historico.isEmpty()) {
            VBox emptyBox = new VBox(10);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(30));
            emptyBox.setStyle("-fx-background-color: #eff6ff; -fx-background-radius: 8; -fx-border-color: #bfdbfe; -fx-border-radius: 8;");

            Label emptyLabel = new Label("Primeira Consulta - Paciente sem histórico clínico prévio");
            emptyLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            emptyLabel.setTextFill(Color.web("#1d4ed8"));

            Label emptySub = new Label("Este paciente ainda não possui atendimentos médicos finalizados no sistema.");
            emptySub.setFont(Font.font("Segoe UI", 12));
            emptySub.setTextFill(Color.web("#3b82f6"));

            emptyBox.getChildren().addAll(emptyLabel, emptySub);
            historicoContainer.getChildren().add(emptyBox);
            return;
        }

        for (Atendimento a : historico) {
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: white; -fx-padding: 16; -fx-background-radius: 10; -fx-border-color: #cbd5e1; -fx-border-radius: 10;");

            HBox header = new HBox(12);
            header.setAlignment(Pos.CENTER_LEFT);

            Label dtLabel = new Label("📅 " + a.getData() + " às " + a.getHora());
            dtLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            dtLabel.setTextFill(Color.web("#047857"));

            String vetNome = usuarioDAO.listarTodos().stream().filter(u -> u.getId().equals(a.getVeterinarioId())).map(Usuario::getNome).findFirst().orElse("Veterinário(a)");
            Label vetLabel = new Label("Atendido por: " + vetNome);
            vetLabel.setFont(Font.font("Segoe UI", 12));
            vetLabel.setTextFill(Color.web("#64748b"));

            header.getChildren().addAll(dtLabel, vetLabel);

            Label diagTitle = new Label("Diagnóstico:");
            diagTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            Label diagText = new Label(a.getDiagnostico());
            diagText.setWrapText(true);

            Label procTitle = new Label("Procedimentos:");
            procTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            Label procText = new Label(a.getProcedimentos());
            procText.setWrapText(true);

            card.getChildren().addAll(header, new Separator(), diagTitle, diagText, procTitle, procText);

            if (a.getPrescricoes() != null && !a.getPrescricoes().isEmpty()) {
                Label presTitle = new Label("Prescrição Farmacológica:");
                presTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                Label presText = new Label(a.getPrescricoes());
                presText.setWrapText(true);
                card.getChildren().addAll(presTitle, presText);
            }

            if (!a.getItensUtilizados().isEmpty()) {
                Label matTitle = new Label("Medicamentos e Materiais Utilizados:");
                matTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                VBox itensBox = new VBox(2);
                for (ItemUtilizado it : a.getItensUtilizados()) {
                    Label l = new Label("• " + it.getNome() + " - " + it.getQuantidade() + " un. (R$ " + String.format("%.2f", it.getSubtotal()) + ")");
                    l.setFont(Font.font("Segoe UI", 11));
                    l.setTextFill(Color.web("#475569"));
                    itensBox.getChildren().add(l);
                }
                card.getChildren().addAll(matTitle, itensBox);
            }

            historicoContainer.getChildren().add(card);
        }
    }
}
