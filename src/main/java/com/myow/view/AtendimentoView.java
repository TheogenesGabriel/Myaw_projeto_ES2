package com.myow.view;

import com.myow.model.*;
import com.myow.service.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AtendimentoView extends BorderPane {
    private final AtendimentoService atendimentoService = new AtendimentoService();
    private final EstoqueService estoqueService = new EstoqueService();
    private final PacienteService pacienteService = new PacienteService();
    private final TutorService tutorService = new TutorService();

    private final ListView<Consulta> filaList = new ListView<>();
    private final ObservableList<Consulta> filaData = FXCollections.observableArrayList();

    private Consulta consultaSelecionada = null;

    // Form fields
    private final Label lblPacienteInfo = new Label("Selecione um paciente na fila à esquerda.");
    private final TextArea txtDiagnostico = new TextArea();
    private final TextArea txtProcedimentos = new TextArea();
    private final TextArea txtPrescricoes = new TextArea();
    private final TextArea txtObservacoes = new TextArea();
    private final TextField txtValorProcedimentos = new TextField("120.00");

    // Items list
    private final TableView<ItemUtilizado> itensTable = new TableView<>();
    private final ObservableList<ItemUtilizado> itensData = FXCollections.observableArrayList();
    private final Label lblTotal = new Label("Total: R$ 120,00");

    public AtendimentoView() {
        setStyle("-fx-background-color: transparent;");
        setPadding(new Insets(0, 24, 24, 24));
        initUI();
        carregarFila();
    }

    private void initUI() {
        // Top Header Card (Figma Style)
        Button btnAtualizarFila = UIUtils.createSecondaryButton("🔄 Atualizar Fila");
        btnAtualizarFila.setOnAction(e -> carregarFila());

        HBox headerCard = UIUtils.createHeaderCard("🩺  Atendimento Clínico Veterinário", btnAtualizarFila);
        setTop(headerCard);

        // Center split: Left = Queue, Right = Record Form
        SplitPane splitPane = new SplitPane();
        splitPane.setStyle("-fx-box-border: transparent; -fx-background-color: transparent; -fx-padding: 16 0 0 0;");

        // Left Queue Pane
        VBox leftPane = UIUtils.createCard("-fx-padding: 18;");
        leftPane.setPrefWidth(320);
        leftPane.setMaxWidth(380);

        Label queueTitle = new Label("Fila de Atendimento do Dia");
        queueTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        queueTitle.setTextFill(Color.web("#0a362f"));

        filaList.setItems(filaData);
        filaList.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        filaList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Consulta c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String pac = pacienteService.buscarPorId(c.getPacienteId()).map(Paciente::getNome).orElse("Paciente");
                    String hora = c.getHorario();
                    String status = c.getStatus().getDescricao();

                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);
                    row.setPadding(new Insets(6, 4, 6, 4));

                    Label avatar = new Label("🐾");
                    avatar.setStyle("-fx-background-color: #cbf3ea; -fx-min-width: 28px; -fx-min-height: 28px; -fx-max-width: 28px; -fx-max-height: 28px; -fx-alignment: center; -fx-background-radius: 14; -fx-font-size: 12px;");

                    VBox box = new VBox(2);
                    Label title = new Label(hora + " • " + pac);
                    title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                    title.setTextFill(Color.web("#0f172a"));

                    Label sub = new Label("Status: " + status);
                    sub.setFont(Font.font("Segoe UI", 11));
                    sub.setTextFill(Color.web("#059669"));
                    box.getChildren().addAll(title, sub);

                    row.getChildren().addAll(avatar, box);
                    setGraphic(row);
                }
            }
        });

        filaList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                selecionarConsulta(newV);
            }
        });

        VBox.setVgrow(filaList, Priority.ALWAYS);
        leftPane.getChildren().addAll(queueTitle, filaList);

        // Right Medical Record Form
        VBox rightPane = UIUtils.createCard("-fx-padding: 20;");
        VBox.setVgrow(rightPane, Priority.ALWAYS);

        lblPacienteInfo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblPacienteInfo.setTextFill(Color.web("#064e43"));
        lblPacienteInfo.setStyle("-fx-background-color: #f0fdf9; -fx-padding: 10 14; -fx-background-radius: 8; -fx-border-color: #cbf3ea; -fx-border-radius: 8;");

        txtDiagnostico.setPromptText("Descrição clínica, achados do exame físico e hipótese diagnóstica...");
        txtDiagnostico.setPrefRowCount(3);

        txtProcedimentos.setPromptText("Procedimentos realizados (ex: Consulta clínica geral, Curativo, Aplicação de vacina)...");
        txtProcedimentos.setPrefRowCount(2);

        txtPrescricoes.setPromptText("Receituário e orientações terapêuticas para o tutor...");
        txtPrescricoes.setPrefRowCount(2);

        txtObservacoes.setPromptText("Orientações pós-consulta, retorno preventivo ou cuidados extras...");
        txtObservacoes.setPrefRowCount(2);

        // Pharmacy / Stock selection
        HBox estoqueBar = new HBox(10);
        estoqueBar.setAlignment(Pos.CENTER_LEFT);

        Label lblMateriais = new Label("Materiais & Medicamentos da Farmácia:");
        lblMateriais.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblMateriais.setTextFill(Color.web("#0a362f"));

        Button btnAdicionarItem = UIUtils.createSecondaryButton("+ Adicionar Medicamento/Material");
        btnAdicionarItem.setOnAction(e -> abrirModalAdicionarMedicamento());

        estoqueBar.getChildren().addAll(lblMateriais, btnAdicionarItem);

        // Table of items used
        TableColumn<ItemUtilizado, String> colItemNome = new TableColumn<>("Item");
        colItemNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colItemNome.setPrefWidth(220);

        TableColumn<ItemUtilizado, String> colItemQtd = new TableColumn<>("Quantidade");
        colItemQtd.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getQuantidade())));
        colItemQtd.setPrefWidth(90);

        TableColumn<ItemUtilizado, String> colItemSub = new TableColumn<>("Subtotal");
        colItemSub.setCellValueFactory(c -> new SimpleStringProperty(String.format("R$ %.2f", c.getValue().getSubtotal())));
        colItemSub.setPrefWidth(100);

        TableColumn<ItemUtilizado, Void> colItemRemover = new TableColumn<>("");
        colItemRemover.setPrefWidth(70);
        colItemRemover.setCellFactory(p -> new TableCell<>() {
            private final Button btnRem = new Button("✕");
            {
                btnRem.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-cursor: hand; -fx-font-size: 10px;");
                btnRem.setOnAction(e -> {
                    itensData.remove(getIndex());
                    recalcularTotal();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnRem);
            }
        });

        itensTable.getColumns().addAll(colItemNome, colItemQtd, colItemSub, colItemRemover);
        itensTable.setItems(itensData);
        itensTable.setPrefHeight(120);

        // Pricing footer
        HBox footerBox = new HBox(16);
        footerBox.setAlignment(Pos.CENTER_RIGHT);

        Label lblProc = new Label("Valor dos Procedimentos (R$):");
        lblProc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        txtValorProcedimentos.setPrefWidth(100);
        txtValorProcedimentos.textProperty().addListener((obs, oldV, newV) -> recalcularTotal());

        lblTotal.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 16));
        lblTotal.setTextFill(Color.web("#047857"));

        Button btnConcluir = UIUtils.createPrimaryButton("Finalizar & Registrar Atendimento");
        btnConcluir.setOnAction(e -> salvarAtendimento());

        footerBox.getChildren().addAll(lblProc, txtValorProcedimentos, lblTotal, btnConcluir);

        ScrollPane formScroll = new ScrollPane();
        formScroll.setFitToWidth(true);
        formScroll.setStyle("-fx-background-color: transparent; -fx-background: white;");

        VBox formContent = new VBox(12);
        formContent.getChildren().addAll(
                lblPacienteInfo,
                new Label("Diagnóstico Clínico: *"), txtDiagnostico,
                new Label("Procedimentos Realizados: *"), txtProcedimentos,
                new Label("Prescrições & Receituário:"), txtPrescricoes,
                new Label("Observações e Recomendações:"), txtObservacoes,
                estoqueBar,
                itensTable,
                footerBox
        );
        formScroll.setContent(formContent);

        rightPane.getChildren().add(formScroll);

        splitPane.getItems().addAll(leftPane, rightPane);
        splitPane.setDividerPositions(0.32);

        setCenter(splitPane);
    }

    private void carregarFila() {
        filaData.setAll(atendimentoService.listarConsultasAguardando());
        if (!filaData.isEmpty()) {
            filaList.getSelectionModel().select(0);
        }
    }

    private void selecionarConsulta(Consulta c) {
        this.consultaSelecionada = c;
        Paciente p = pacienteService.buscarPorId(c.getPacienteId()).orElse(null);
        Tutor t = tutorService.buscarPorId(c.getTutorId()).orElse(null);

        if (p != null && t != null) {
            lblPacienteInfo.setText("🐾 Paciente: " + p.getNome() + " (" + p.getEspecie() + " - " + p.getRaca() + ") | Tutor: " + t.getNomeCompleto() + " (" + t.getTelefone() + ")");
        } else {
            lblPacienteInfo.setText("Consulta selecionada para atendimento.");
        }

        txtDiagnostico.clear();
        txtProcedimentos.setText("Consulta clínica veterinária de rotina");
        txtPrescricoes.clear();
        txtObservacoes.clear();
        itensData.clear();
        recalcularTotal();
    }

    private void recalcularTotal() {
        double proc = 0.0;
        try {
            proc = Double.parseDouble(txtValorProcedimentos.getText().replace(",", "."));
        } catch (Exception ignored) {}

        double itens = itensData.stream().mapToDouble(ItemUtilizado::getSubtotal).sum();
        double total = proc + itens;
        lblTotal.setText(String.format("Total: R$ %.2f", total));
    }

    private void abrirModalAdicionarMedicamento() {
        Dialog<ItemUtilizado> dialog = new Dialog<>();
        dialog.setTitle("Adicionar Medicamento ou Material");
        dialog.setHeaderText("Selecione um item do estoque farmacêutico e informe a dosagem/quantidade.");

        ButtonType btnAdd = new ButtonType("Adicionar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAdd, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<ItemEstoque> itemCombo = new ComboBox<>(FXCollections.observableArrayList(estoqueService.listarTodos()));
        if (!estoqueService.listarTodos().isEmpty()) itemCombo.setValue(estoqueService.listarTodos().get(0));

        Spinner<Integer> qtdSpinner = new Spinner<>(1, 100, 1);
        qtdSpinner.setEditable(true);

        grid.add(new Label("Item de Estoque:"), 0, 0);
        grid.add(itemCombo, 1, 0);
        grid.add(new Label("Quantidade Usada:"), 0, 1);
        grid.add(qtdSpinner, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnAdd && itemCombo.getValue() != null) {
                ItemEstoque est = itemCombo.getValue();
                int qtd = qtdSpinner.getValue();
                if (qtd > est.getQuantidade()) {
                    UIUtils.showError("Saldo Insuficiente", "O estoque atual possui apenas " + est.getQuantidade() + " " + est.getUnidade() + " disponíveis.");
                    return null;
                }
                return new ItemUtilizado(est.getId(), est.getNome(), qtd, est.getValorUnitario());
            }
            return null;
        });

        Optional<ItemUtilizado> res = dialog.showAndWait();
        res.ifPresent(item -> {
            itensData.add(item);
            recalcularTotal();
        });
    }

    private void salvarAtendimento() {
        if (consultaSelecionada == null) {
            UIUtils.showError("Atenção", "Selecione uma consulta na fila de espera.");
            return;
        }

        double proc = 0.0;
        try {
            proc = Double.parseDouble(txtValorProcedimentos.getText().replace(",", "."));
        } catch (Exception e) {
            UIUtils.showError("Valor Inválido", "Informe um valor numérico válido para os procedimentos.");
            return;
        }

        OperationResult<Atendimento> res = atendimentoService.registrarAtendimento(
                consultaSelecionada.getId(),
                txtDiagnostico.getText(),
                txtProcedimentos.getText(),
                txtPrescricoes.getText(),
                txtObservacoes.getText(),
                new ArrayList<>(itensData),
                proc
        );

        if (!res.isSuccess()) {
            UIUtils.showError("Erro no Atendimento", res.getMessage());
            return;
        }

        // Show alert if stock warning was triggered
        if (!res.getAlertas().isEmpty()) {
            UIUtils.showWarning("Aviso de Estoque", String.join("\n", res.getAlertas()));
        }

        UIUtils.showInfo("Sucesso", "Atendimento clínico concluído com sucesso!\nProntuário atualizado e cobrança enviada ao Faturamento.");
        carregarFila();
    }
}
