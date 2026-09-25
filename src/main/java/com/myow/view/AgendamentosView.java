package com.myow.view;

import com.myow.dao.UsuarioDAO;
import com.myow.model.*;
import com.myow.service.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AgendamentosView extends VBox {
    private final AgendamentoService agendamentoService = new AgendamentoService();
    private final PacienteService pacienteService = new PacienteService();
    private final TutorService tutorService = new TutorService();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private final TableView<Consulta> table = new TableView<>();
    private final ObservableList<Consulta> data = FXCollections.observableArrayList();

    public AgendamentosView() {
        setSpacing(16);
        setPadding(new Insets(0, 24, 24, 24));
        setStyle("-fx-background-color: transparent;");
        initUI();
        carregarDados();
    }

    private void initUI() {
        // 1. Top Floating Header Card (Figma Screenshot 2)
        Button btnNovo = UIUtils.createPrimaryButton("Adicionar Agendamento");
        btnNovo.setOnAction(e -> abrirModalNovaConsulta());

        HBox headerCard = UIUtils.createHeaderCard("📅  Consultas Agendadas", btnNovo);

        // 2. Main Content Card (Figma Screenshot 2)
        VBox contentCard = UIUtils.createCard("-fx-padding: 20;");
        VBox.setVgrow(contentCard, Priority.ALWAYS);

        // Filter Bar
        HBox filterBox = new HBox(12);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(0, 0, 14, 0));

        TextField searchField = new TextField();
        searchField.setPromptText("Buscar por paciente, tutor ou veterinário...");
        searchField.setPrefWidth(280);
        searchField.setStyle("-fx-padding: 8 12; -fx-background-radius: 8; -fx-border-color: #cbd5e1; -fx-border-radius: 8;");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle("-fx-padding: 6;");

        ComboBox<String> statusFilter = new ComboBox<>(FXCollections.observableArrayList("Todos os Status", "Agendada", "Aguardando Atendimento", "Em Andamento", "Realizada", "Cancelada"));
        statusFilter.setValue("Todos os Status");

        Button btnFiltrar = UIUtils.createSecondaryButton("Filtrar");
        btnFiltrar.setOnAction(e -> {
            LocalDate selectedDate = datePicker.getValue();
            String dateStr = selectedDate != null ? selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
            String statusStr = statusFilter.getValue();
            String termo = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";

            List<Consulta> filtradas = agendamentoService.listarTodas().stream().filter(c -> {
                boolean matchDate = dateStr.isEmpty() || c.getData().equals(dateStr);
                boolean matchStatus = statusStr.equals("Todos os Status") || c.getStatus().getDescricao().equalsIgnoreCase(statusStr);

                Paciente pac = pacienteService.buscarPorId(c.getPacienteId()).orElse(null);
                Tutor tut = tutorService.buscarPorId(c.getTutorId()).orElse(null);
                boolean matchTermo = termo.isEmpty() ||
                        (pac != null && pac.getNome().toLowerCase().contains(termo)) ||
                        (tut != null && tut.getNomeCompleto().toLowerCase().contains(termo)) ||
                        c.getMotivo().toLowerCase().contains(termo);

                return matchDate && matchStatus && matchTermo;
            }).collect(Collectors.toList());

            data.setAll(filtradas);
        });

        Button btnLimpar = UIUtils.createSecondaryButton("Limpar");
        btnLimpar.setOnAction(e -> {
            searchField.clear();
            datePicker.setValue(null);
            statusFilter.setValue("Todos os Status");
            carregarDados();
        });

        Region filterSpacer = new Region();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);

        filterBox.getChildren().addAll(searchField, new Label("Data:"), datePicker, new Label("Status:"), statusFilter, btnFiltrar, btnLimpar);

        // Configuração Fina da Tabela conforme o Figma
        // Coluna 1: DATA E HORÁRIO (Texto Teal em negrito)
        TableColumn<Consulta, String> colDataHora = new TableColumn<>("DATA E HORÁRIO");
        colDataHora.setCellValueFactory(c -> new SimpleStringProperty(formatarDataHora(c.getValue().getData(), c.getValue().getHorario())));
        colDataHora.setPrefWidth(140);
        colDataHora.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label lbl = new Label(item);
                    lbl.setStyle("-fx-text-fill: #00776b; -fx-font-weight: bold; -fx-font-size: 12px;");
                    setGraphic(lbl);
                }
            }
        });

        // Coluna 2: PACIENTE (Ícone círculo mint + Nome + Raça)
        TableColumn<Consulta, Void> colPaciente = new TableColumn<>("PACIENTE");
        colPaciente.setPrefWidth(210);
        colPaciente.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Consulta c = getTableView().getItems().get(getIndex());
                    Paciente pac = pacienteService.buscarPorId(c.getPacienteId()).orElse(null);

                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER_LEFT);

                    Label iconBadge = new Label("🐾");
                    iconBadge.setStyle("-fx-background-color: #cbf3ea; -fx-text-fill: #064e43; -fx-font-size: 13px; -fx-min-width: 32px; -fx-min-height: 32px; -fx-max-width: 32px; -fx-max-height: 32px; -fx-alignment: center; -fx-background-radius: 16;");

                    VBox textBox = new VBox(2);
                    Label lblNome = new Label(pac != null ? pac.getNome() : "Paciente");
                    lblNome.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0f172a;");

                    String desc = pac != null ? pac.getEspecie() + " • " + pac.getRaca() + " • " + pac.getIdade() + " anos" : "Cadastro geral";
                    Label lblDesc = new Label(desc);
                    lblDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

                    textBox.getChildren().addAll(lblNome, lblDesc);
                    box.getChildren().addAll(iconBadge, textBox);
                    setGraphic(box);
                }
            }
        });

        // Coluna 3: TUTOR & CONTATO
        TableColumn<Consulta, Void> colTutor = new TableColumn<>("TUTOR & CONTATO");
        colTutor.setPrefWidth(200);
        colTutor.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Consulta c = getTableView().getItems().get(getIndex());
                    Tutor tut = tutorService.buscarPorId(c.getTutorId()).orElse(null);

                    VBox box = new VBox(2);
                    Label lblNome = new Label(tut != null ? tut.getNomeCompleto() : "Tutor Não Localizado");
                    lblNome.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #0f172a;");

                    Label lblFone = new Label(tut != null ? "📞 " + tut.getTelefone() : "Sem telefone");
                    lblFone.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

                    box.getChildren().addAll(lblNome, lblFone);
                    setGraphic(box);
                }
            }
        });

        // Coluna 4: VETERINÁRIO
        TableColumn<Consulta, Void> colVet = new TableColumn<>("VETERINÁRIO");
        colVet.setPrefWidth(170);
        colVet.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Consulta c = getTableView().getItems().get(getIndex());
                    Usuario vet = usuarioDAO.listarTodos().stream()
                            .filter(u -> u.getId().equals(c.getVeterinarioId()))
                            .findFirst().orElse(null);

                    VBox box = new VBox(2);
                    Label lblNome = new Label(vet != null ? vet.getNome() : "Não Atribuído");
                    lblNome.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #0f172a;");

                    Label lblCrmv = new Label("CRMV Ativo");
                    lblCrmv.setStyle("-fx-font-size: 11px; -fx-text-fill: #0d9488;");

                    box.getChildren().addAll(lblNome, lblCrmv);
                    setGraphic(box);
                }
            }
        });

        // Coluna 5: STATUS (Pill badge)
        TableColumn<Consulta, Void> colStatus = new TableColumn<>("STATUS");
        colStatus.setPrefWidth(130);
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Consulta c = getTableView().getItems().get(getIndex());
                    Label badge = new Label(c.getStatus().getDescricao());
                    badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));

                    switch (c.getStatus()) {
                        case AGENDADA:
                            badge.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0284c7; -fx-padding: 3 8; -fx-background-radius: 10;");
                            break;
                        case AGUARDANDO_ATENDIMENTO:
                            badge.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-padding: 3 8; -fx-background-radius: 10;");
                            break;
                        case EM_ANDAMENTO:
                            badge.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-padding: 3 8; -fx-background-radius: 10;");
                            break;
                        case REALIZADA:
                            badge.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-padding: 3 8; -fx-background-radius: 10;");
                            break;
                        case CANCELADA:
                            badge.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-padding: 3 8; -fx-background-radius: 10;");
                            break;
                    }
                    setGraphic(badge);
                }
            }
        });

        // Coluna 6: AÇÕES (Botão "Selecionar" do Figma e Ações)
        TableColumn<Consulta, Void> colAcoes = new TableColumn<>("AÇÕES");
        colAcoes.setPrefWidth(210);
        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btnSelecionar = UIUtils.createSelectButton("🔍 Selecionar");
            private final Button btnRemarcar = new Button("Remarcar");
            private final Button btnCancelar = new Button("Cancelar");
            private final HBox pane = new HBox(6, btnSelecionar, btnRemarcar, btnCancelar);

            {
                btnRemarcar.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 5 10; -fx-background-radius: 6;");
                btnCancelar.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 5 10; -fx-background-radius: 6;");
                pane.setAlignment(Pos.CENTER_LEFT);

                btnSelecionar.setOnAction(e -> {
                    Consulta c = getTableView().getItems().get(getIndex());
                    abrirModalRemarcar(c);
                });

                btnRemarcar.setOnAction(e -> {
                    Consulta c = getTableView().getItems().get(getIndex());
                    abrirModalRemarcar(c);
                });

                btnCancelar.setOnAction(e -> {
                    Consulta c = getTableView().getItems().get(getIndex());
                    abrirModalCancelar(c);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Consulta c = getTableView().getItems().get(getIndex());
                    if (c.getStatus() == StatusConsulta.REALIZADA || c.getStatus() == StatusConsulta.CANCELADA) {
                        Button btnVer = UIUtils.createSelectButton("📋 Ver Detalhes");
                        btnVer.setOnAction(e -> abrirModalRemarcar(c));
                        setGraphic(btnVer);
                    } else {
                        setGraphic(pane);
                    }
                }
            }
        });

        table.getColumns().addAll(colDataHora, colPaciente, colTutor, colVet, colStatus, colAcoes);
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        // Barra de Paginação idêntica ao Figma Screenshot 2
        HBox paginationBar = UIUtils.createPaginationBar(1, Math.min(9, data.size()), data.size(), "consultas agendadas", 1, 3);

        contentCard.getChildren().addAll(filterBox, table, paginationBar);

        getChildren().addAll(headerCard, contentCard);
    }

    private String formatarDataHora(String dataStr, String horaStr) {
        if (dataStr == null) return "";
        try {
            LocalDate dt = LocalDate.parse(dataStr);
            return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " " + (horaStr != null ? horaStr : "");
        } catch (Exception e) {
            return dataStr + " " + horaStr;
        }
    }

    private void carregarDados() {
        data.setAll(agendamentoService.listarTodas());
    }

    /**
     * Modal Duas Colunas idêntico ao Figma Screenshot 3 (Alterar / Agendar Consulta)
     */
    private void abrirModalNovaConsulta() {
        Dialog<Consulta> dialog = new Dialog<>();
        dialog.setTitle("Agendamento de Consultas");

        ButtonType btnSalvar = new ButtonType("Confirmar Agendamento", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSalvar, ButtonType.CANCEL);

        List<Paciente> pacientes = pacienteService.listarTodos();
        List<Usuario> veterinarios = usuarioDAO.listarTodos().stream().filter(u -> u.getPerfil() == Perfil.VETERINARIO).collect(Collectors.toList());

        ComboBox<Paciente> pacCombo = new ComboBox<>(FXCollections.observableArrayList(pacientes));
        if (!pacientes.isEmpty()) pacCombo.setValue(pacientes.get(0));

        ComboBox<Usuario> vetCombo = new ComboBox<>(FXCollections.observableArrayList(veterinarios));
        if (!veterinarios.isEmpty()) vetCombo.setValue(veterinarios.get(0));

        DatePicker dtPicker = new DatePicker(LocalDate.now());
        ComboBox<String> horaCombo = new ComboBox<>(FXCollections.observableArrayList(
                "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00"
        ));
        horaCombo.setValue("10:00");

        TextField motivoField = new TextField("Consulta de rotina");

        // Painel Resumo Dinâmico (Lado Direito do Figma Screenshot 3)
        Label lblResumoPac = new Label();
        Label lblResumoPacSub = new Label();
        Label lblResumoTut = new Label();
        Label lblResumoTutSub = new Label();
        Label lblResumoVet = new Label();
        Label lblResumoVetSub = new Label();
        Label lblResumoData = new Label();

        Runnable atualizarResumo = () -> {
            Paciente p = pacCombo.getValue();
            if (p != null) {
                lblResumoPac.setText(p.getNome());
                lblResumoPacSub.setText(p.getEspecie() + " • " + p.getRaca() + " • " + p.getIdade() + " anos");

                Tutor t = tutorService.buscarPorId(p.getTutorId()).orElse(null);
                if (t != null) {
                    lblResumoTut.setText(t.getNomeCompleto());
                    lblResumoTutSub.setText("📞 " + t.getTelefone());
                } else {
                    lblResumoTut.setText("Tutor não vinculado");
                    lblResumoTutSub.setText("");
                }
            }

            Usuario v = vetCombo.getValue();
            if (v != null) {
                lblResumoVet.setText(v.getNome());
                lblResumoVetSub.setText("CRMV-SP Ativo • Clínica Geral");
            }

            LocalDate d = dtPicker.getValue();
            String h = horaCombo.getValue();
            if (d != null && h != null) {
                lblResumoData.setText(d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " às " + h);
            }
        };

        pacCombo.valueProperty().addListener((obs, o, n) -> atualizarResumo.run());
        vetCombo.valueProperty().addListener((obs, o, n) -> atualizarResumo.run());
        dtPicker.valueProperty().addListener((obs, o, n) -> atualizarResumo.run());
        horaCombo.valueProperty().addListener((obs, o, n) -> atualizarResumo.run());
        atualizarResumo.run();

        // Montagem do Layout Duas Colunas
        HBox columnsBox = new HBox(20);
        columnsBox.setPrefWidth(780);
        columnsBox.setPadding(new Insets(16));

        // Coluna Esquerda: Formulários
        VBox leftCol = new VBox(14);
        leftCol.setPrefWidth(440);

        // Card 1: Paciente e Tutor
        VBox card1 = UIUtils.createCard("-fx-padding: 16;");
        Label c1Title = new Label("🐾  Paciente e Tutor");
        c1Title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0f172a;");
        card1.getChildren().addAll(c1Title, new Label("Selecione o Paciente:"), pacCombo, new Label("Motivo:"), motivoField);

        // Card 2: Profissional e Especialidade
        VBox card2 = UIUtils.createCard("-fx-padding: 16;");
        Label c2Title = new Label("🩺  Profissional e Especialidade");
        c2Title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0f172a;");
        card2.getChildren().addAll(c2Title, new Label("Veterinário(a) Responsável *:"), vetCombo);

        // Card 3: Data e Horário
        VBox card3 = UIUtils.createCard("-fx-padding: 16;");
        Label c3Title = new Label("📅  Data e Grade de Horários");
        c3Title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0f172a;");
        HBox dtBox = new HBox(12, new VBox(4, new Label("Data:"), dtPicker), new VBox(4, new Label("Horário:"), horaCombo));
        card3.getChildren().addAll(c3Title, dtBox);

        leftCol.getChildren().addAll(card1, card2, card3);

        // Coluna Direita: Resumo da Marcação (Figma Screenshot 3)
        VBox rightCol = UIUtils.createCard("-fx-padding: 20;");
        rightCol.setPrefWidth(320);

        HBox badgeHorario = new HBox(new Label("✓ Horário Disponível"));
        badgeHorario.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4 10; -fx-background-radius: 8;");
        badgeHorario.setAlignment(Pos.CENTER_RIGHT);

        Label resumoTitle = new Label("Resumo da Marcação");
        resumoTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #0f172a;");

        VBox itemPac = criarItemResumo("🐾", "Paciente Cadastrado", lblResumoPac, lblResumoPacSub);
        VBox itemTut = criarItemResumo("👤", "Tutor / Contato Principal", lblResumoTut, lblResumoTutSub);
        VBox itemVet = criarItemResumo("🩺", "Veterinária & Especialidade", lblResumoVet, lblResumoVetSub);
        VBox itemData = criarItemResumo("🕒", "Data & Horário Marcado", lblResumoData, new Label("Duração prevista: 45 min"));

        rightCol.getChildren().addAll(badgeHorario, resumoTitle, new Separator(), itemPac, itemTut, itemVet, itemData);

        columnsBox.getChildren().addAll(leftCol, rightCol);
        dialog.getDialogPane().setContent(columnsBox);

        dialog.setResultConverter(btn -> {
            if (btn == btnSalvar) {
                if (pacCombo.getValue() == null || vetCombo.getValue() == null || dtPicker.getValue() == null || horaCombo.getValue() == null) {
                    UIUtils.showError("Campos Obrigatórios", "Todos os campos marcados com * devem ser preenchidos.");
                    return null;
                }

                Paciente pac = pacCombo.getValue();
                String dataStr = dtPicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String horaStr = horaCombo.getValue();
                String vetId = vetCombo.getValue().getId();

                OperationResult<Consulta> res = agendamentoService.agendar(
                        dataStr, horaStr, pac.getId(), pac.getTutorId(), vetId, motivoField.getText(), ""
                );

                if (!res.isSuccess()) {
                    UIUtils.showError("Horário Indisponível", res.getMessage());
                    return null;
                }
                return res.getData();
            }
            return null;
        });

        Optional<Consulta> result = dialog.showAndWait();
        result.ifPresent(c -> {
            carregarDados();
            UIUtils.showInfo("Sucesso", "Consulta agendada para " + c.getData() + " às " + c.getHorario() + "!");
        });
    }

    private void abrirModalRemarcar(Consulta consulta) {
        Dialog<Consulta> dialog = new Dialog<>();
        dialog.setTitle("Alterar Consulta");

        ButtonType btnSalvar = new ButtonType("Salvar Alterações", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSalvar, ButtonType.CANCEL);

        DatePicker dtPicker = new DatePicker(LocalDate.parse(consulta.getData()));
        ComboBox<String> horaCombo = new ComboBox<>(FXCollections.observableArrayList(
                "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00"
        ));
        horaCombo.setValue(consulta.getHorario());

        List<Usuario> veterinarios = usuarioDAO.listarTodos().stream().filter(u -> u.getPerfil() == Perfil.VETERINARIO).collect(Collectors.toList());
        ComboBox<Usuario> vetCombo = new ComboBox<>(FXCollections.observableArrayList(veterinarios));
        veterinarios.stream().filter(u -> u.getId().equals(consulta.getVeterinarioId())).findFirst().ifPresent(vetCombo::setValue);

        Paciente pac = pacienteService.buscarPorId(consulta.getPacienteId()).orElse(null);
        Tutor tut = tutorService.buscarPorId(consulta.getTutorId()).orElse(null);

        // Layout de Duas Colunas do Figma Screenshot 3
        HBox columnsBox = new HBox(20);
        columnsBox.setPrefWidth(780);
        columnsBox.setPadding(new Insets(16));

        VBox leftCol = new VBox(14);
        leftCol.setPrefWidth(440);

        // Card 1: Paciente e Tutor (ReadOnly)
        VBox card1 = UIUtils.createCard("-fx-padding: 16;");
        Label c1Title = new Label("🐾  Paciente e Tutor");
        c1Title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0f172a;");

        HBox pacHeader = new HBox(8);
        Label pacNome = new Label(pac != null ? pac.getNome() : "Paciente");
        pacNome.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #0f172a;");
        Label badgeCad = new Label("Cadastrado");
        badgeCad.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 4;");
        pacHeader.getChildren().addAll(pacNome, badgeCad);

        Label pacInfo = new Label(pac != null ? pac.getEspecie() + " • " + pac.getRaca() + " • " + pac.getIdade() + " anos" : "");
        pacInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        Label tutInfo = new Label(tut != null ? "Tutor: " + tut.getNomeCompleto() + " (" + tut.getTelefone() + ")" : "");
        tutInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: #334155;");

        card1.getChildren().addAll(c1Title, pacHeader, pacInfo, tutInfo);

        // Card 2: Profissional
        VBox card2 = UIUtils.createCard("-fx-padding: 16;");
        Label c2Title = new Label("🩺  Profissional e Especialidade");
        c2Title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0f172a;");
        card2.getChildren().addAll(c2Title, new Label("Veterinário(a) Responsável *:"), vetCombo);

        // Card 3: Data e Horário
        VBox card3 = UIUtils.createCard("-fx-padding: 16;");
        Label c3Title = new Label("📅  Data e Grade de Horários");
        c3Title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0f172a;");
        HBox dtBox = new HBox(12, new VBox(4, new Label("Data:"), dtPicker), new VBox(4, new Label("Horário:"), horaCombo));
        card3.getChildren().addAll(c3Title, dtBox);

        leftCol.getChildren().addAll(card1, card2, card3);

        // Coluna Direita: Resumo da Marcação
        VBox rightCol = UIUtils.createCard("-fx-padding: 20;");
        rightCol.setPrefWidth(320);

        Label resumoTitle = new Label("Resumo da Marcação");
        resumoTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #0f172a;");

        Label lblResumoData = new Label();
        Runnable attData = () -> {
            if (dtPicker.getValue() != null && horaCombo.getValue() != null) {
                lblResumoData.setText(dtPicker.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " às " + horaCombo.getValue());
            }
        };
        dtPicker.valueProperty().addListener((obs, o, n) -> attData.run());
        horaCombo.valueProperty().addListener((obs, o, n) -> attData.run());
        attData.run();

        VBox itemPac = criarItemResumo("🐾", "Paciente Cadastrado", new Label(pac != null ? pac.getNome() : ""), new Label(pac != null ? pac.getEspecie() + " • " + pac.getRaca() : ""));
        VBox itemTut = criarItemResumo("👤", "Tutor / Contato Principal", new Label(tut != null ? tut.getNomeCompleto() : ""), new Label(tut != null ? tut.getTelefone() : ""));
        VBox itemData = criarItemResumo("🕒", "Data & Horário Marcado", lblResumoData, new Label("Duração prevista: 45 min"));

        rightCol.getChildren().addAll(resumoTitle, new Separator(), itemPac, itemTut, itemData);

        columnsBox.getChildren().addAll(leftCol, rightCol);
        dialog.getDialogPane().setContent(columnsBox);

        dialog.setResultConverter(btn -> {
            if (btn == btnSalvar) {
                String dataStr = dtPicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String horaStr = horaCombo.getValue();
                String vetId = vetCombo.getValue() != null ? vetCombo.getValue().getId() : consulta.getVeterinarioId();

                OperationResult<Consulta> res = agendamentoService.alterar(consulta.getId(), dataStr, horaStr, vetId, null, null);
                if (!res.isSuccess()) {
                    UIUtils.showError("Atenção", res.getMessage());
                    return null;
                }
                return res.getData();
            }
            return null;
        });

        Optional<Consulta> result = dialog.showAndWait();
        result.ifPresent(c -> {
            carregarDados();
            UIUtils.showInfo("Sucesso", "Consulta remarcada para " + c.getData() + " às " + c.getHorario() + "!");
        });
    }

    private VBox criarItemResumo(String icon, String label, Label titleLabel, Label subLabel) {
        VBox item = new VBox(3);
        item.setPadding(new Insets(8, 0, 8, 0));

        Label tag = new Label(icon + "  " + label);
        tag.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        subLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        item.getChildren().addAll(tag, titleLabel, subLabel);
        return item;
    }

    private void abrirModalCancelar(Consulta consulta) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cancelar Consulta");
        dialog.setHeaderText("Confirmação de Cancelamento de Consulta");
        dialog.setContentText("Informe o motivo do cancelamento:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(motivo -> {
            if (motivo.trim().isEmpty()) {
                UIUtils.showError("Atenção", "É obrigatório informar o motivo do cancelamento.");
                return;
            }
            OperationResult<Consulta> res = agendamentoService.cancelar(consulta.getId(), motivo.trim());
            if (res.isSuccess()) {
                carregarDados();
                UIUtils.showInfo("Cancelada", "A consulta foi cancelada com sucesso.");
            } else {
                UIUtils.showError("Erro", res.getMessage());
            }
        });
    }
}

