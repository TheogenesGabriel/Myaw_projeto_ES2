package com.myow.view;

import com.myow.dao.ConsultaDAO;
import com.myow.dao.FaturamentoDAO;
import com.myow.dao.ItemEstoqueDAO;
import com.myow.dao.PacienteDAO;
import com.myow.dao.TutorDAO;
import com.myow.model.Consulta;
import com.myow.model.ItemEstoque;
import com.myow.model.StatusConsulta;
import com.myow.service.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DashboardView extends ScrollPane {
    private final ConsultaDAO consultaDAO = new ConsultaDAO();
    private final ItemEstoqueDAO itemEstoqueDAO = new ItemEstoqueDAO();
    private final FaturamentoDAO faturamentoDAO = new FaturamentoDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final TutorDAO tutorDAO = new TutorDAO();
    private final Consumer<String> navigationHandler;

    public DashboardView(Consumer<String> navigationHandler) {
        this.navigationHandler = navigationHandler;
        setFitToWidth(true);
        setStyle("-fx-background-color: transparent; -fx-background: #f8fafc;");
        initUI();
    }

    private void initUI() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(0, 24, 24, 24));
        root.setStyle("-fx-background-color: transparent;");

        // 1. Header Card (Figma Layout Pattern)
        Button btnNovoAgendamentoTop = UIUtils.createPrimaryButton("Adicionar/Alterar consulta");
        btnNovoAgendamentoTop.setOnAction(e -> navigationHandler.accept("Agendamentos"));

        HBox headerCard = UIUtils.createHeaderCard("📊  Painel de Controle", btnNovoAgendamentoTop);

        // Welcome Box inside Header / Subtitle
        String nomeUsuario = SessionManager.getInstance().getUsuarioLogado() != null ? SessionManager.getInstance().getUsuarioLogado().getNome() : "Colaborador";
        Label welcomeSub = new Label("Bem-vindo(a) de volta, " + nomeUsuario + " • Acompanhe o fluxo clínico e financeiro de hoje.");
        welcomeSub.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        welcomeSub.setTextFill(Color.web("#475569"));

        // KPI Cards Row
        HBox kpiRow = new HBox(16);
        kpiRow.setAlignment(Pos.CENTER_LEFT);

        String hoje = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<Consulta> todasConsultas = consultaDAO.listarTodas();
        long consultasHoje = todasConsultas.stream().filter(c -> c.getData().equals(hoje)).count();
        long pacientesAguardando = todasConsultas.stream().filter(c -> c.getStatus() == StatusConsulta.AGUARDANDO_ATENDIMENTO).count();
        long itensCriticos = itemEstoqueDAO.listarTodos().stream().filter(ItemEstoque::isEstoqueBaixo).count();
        double faturamentoTotal = faturamentoDAO.listarTodos().stream().mapToDouble(f -> f.getValorTotal()).sum();

        VBox card1 = createKpiCard("📅 Consultas Hoje", String.valueOf(consultasHoje), "Agendadas para hoje", "#0a362f");
        VBox card2 = createKpiCard("⏳ Na Fila de Espera", String.valueOf(pacientesAguardando), "Aguardando atendimento", "#0d5c4d");
        VBox card3 = createKpiCard("⚠️ Estoque Crítico", String.valueOf(itensCriticos), "Abaixo do estoque mínimo", "#dc2626");
        VBox card4 = createKpiCard("💰 Faturamento Total", String.format("R$ %.2f", faturamentoTotal), "Total acumulado no sistema", "#059669");

        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);
        HBox.setHgrow(card4, Priority.ALWAYS);

        kpiRow.getChildren().addAll(card1, card2, card3, card4);

        // Low stock warning banner if any item is critical
        VBox alertBoxContainer = new VBox();
        if (itensCriticos > 0) {
            HBox alertBanner = new HBox(12);
            alertBanner.setAlignment(Pos.CENTER_LEFT);
            alertBanner.setPadding(new Insets(12, 16, 12, 16));
            alertBanner.setStyle("-fx-background-color: #fff1f2; -fx-border-color: #fecdd3; -fx-border-radius: 10; -fx-background-radius: 10;");

            Label alertText = new Label("⚠️ Atenção: Há " + itensCriticos + " item(ns) com saldo em nível crítico no estoque!");
            alertText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            alertText.setTextFill(Color.web("#9f1239"));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button btnVerEstoque = new Button("Conferir Estoque →");
            btnVerEstoque.setStyle("-fx-background-color: #e11d48; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 6 12; -fx-background-radius: 6; -fx-cursor: hand;");
            btnVerEstoque.setOnAction(e -> navigationHandler.accept("Estoque"));

            alertBanner.getChildren().addAll(alertText, spacer, btnVerEstoque);
            alertBoxContainer.getChildren().add(alertBanner);
        }

        // Quick Actions Row
        HBox actionsRow = new HBox(12);
        Button btnNovoAgendamento = UIUtils.createPrimaryButton("Adicionar/Alterar consulta");
        btnNovoAgendamento.setOnAction(e -> navigationHandler.accept("Agendamentos"));

        Button btnFilaAtendimento = UIUtils.createSecondaryButton("🩺 Atendimento Clínico");
        btnFilaAtendimento.setOnAction(e -> navigationHandler.accept("Atendimento Clínico"));

        Button btnBuscarProntuario = UIUtils.createSecondaryButton("📋 Prontuário Médico");
        btnBuscarProntuario.setOnAction(e -> navigationHandler.accept("Prontuário"));

        Button btnFaturamento = UIUtils.createSecondaryButton("💳 Caixa & Faturamento");
        btnFaturamento.setOnAction(e -> navigationHandler.accept("Faturamento"));

        actionsRow.getChildren().addAll(btnNovoAgendamento, btnFilaAtendimento, btnBuscarProntuario, btnFaturamento);

        // Next appointments table card
        VBox tableCard = UIUtils.createCard("-fx-padding: 20;");
        
        HBox tableHeader = new HBox(12);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label tableTitle = new Label("Agenda do Dia");
        tableTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        tableTitle.setTextFill(Color.web("#0a362f"));
        
        Region tblSpacer = new Region();
        HBox.setHgrow(tblSpacer, Priority.ALWAYS);
        
        Button btnVerTodos = UIUtils.createSelectButton("Ver todos →");
        btnVerTodos.setOnAction(e -> navigationHandler.accept("Agendamentos"));
        
        tableHeader.getChildren().addAll(tableTitle, tblSpacer, btnVerTodos);

        VBox listContainer = new VBox(8);
        listContainer.setPadding(new Insets(12, 0, 0, 0));

        List<Consulta> consultas = consultaDAO.listarTodas().stream().limit(5).collect(Collectors.toList());
        for (Consulta c : consultas) {
            HBox itemRow = new HBox(16);
            itemRow.setAlignment(Pos.CENTER_LEFT);
            itemRow.setPadding(new Insets(12, 16, 12, 16));
            itemRow.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

            Label horaLabel = new Label("⏰ " + c.getHorario());
            horaLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            horaLabel.setTextFill(Color.web("#064e43"));
            horaLabel.setPrefWidth(90);

            String pacNome = pacienteDAO.buscarPorId(c.getPacienteId()).map(p -> p.getNome() + " (" + p.getEspecie() + ")").orElse("Paciente");
            String tutNome = tutorDAO.buscarPorId(c.getTutorId()).map(t -> t.getNomeCompleto()).orElse("Tutor");

            Label pacienteLabel = new Label(pacNome);
            pacienteLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            pacienteLabel.setTextFill(Color.web("#0f172a"));
            pacienteLabel.setPrefWidth(180);

            Label tutorLabel = new Label("Tutor: " + tutNome);
            tutorLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
            tutorLabel.setTextFill(Color.web("#64748b"));
            tutorLabel.setPrefWidth(180);

            Label motivoLabel = new Label("Motivo: " + c.getMotivo());
            motivoLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
            motivoLabel.setTextFill(Color.web("#475569"));
            HBox.setHgrow(motivoLabel, Priority.ALWAYS);

            Label statusBadge = new Label(c.getStatus().getDescricao());
            statusBadge.setStyle("-fx-background-color: #cbf3ea; -fx-text-fill: #064e43; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 12;");

            itemRow.getChildren().addAll(horaLabel, pacienteLabel, tutorLabel, motivoLabel, statusBadge);
            listContainer.getChildren().add(itemRow);
        }

        tableCard.getChildren().addAll(tableHeader, listContainer);

        root.getChildren().addAll(headerCard, welcomeSub);
        if (itensCriticos > 0) {
            root.getChildren().add(alertBoxContainer);
        }
        root.getChildren().addAll(kpiRow, actionsRow, tableCard);

        setContent(root);
    }

    private VBox createKpiCard(String title, String value, String desc, String colorHex) {
        VBox card = UIUtils.createCard("-fx-padding: 18;");
        Label titleLbl = new Label(title);
        titleLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        titleLbl.setTextFill(Color.web("#64748b"));

        Label valLbl = new Label(value);
        valLbl.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 24));
        valLbl.setTextFill(Color.web(colorHex));

        Label descLbl = new Label(desc);
        descLbl.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        descLbl.setTextFill(Color.web("#94a3b8"));

        card.getChildren().addAll(titleLbl, valLbl, descLbl);
        return card;
    }
}
