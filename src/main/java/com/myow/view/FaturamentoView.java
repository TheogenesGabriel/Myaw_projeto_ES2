package com.myow.view;

import com.myow.model.*;
import com.myow.service.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class FaturamentoView extends BorderPane {
    private final FaturamentoService faturamentoService = new FaturamentoService();
    private final PacienteService pacienteService = new PacienteService();
    private final TutorService tutorService = new TutorService();

    private final ListView<Atendimento> pendentesList = new ListView<>();
    private final ObservableList<Atendimento> pendentesData = FXCollections.observableArrayList();

    private Atendimento atendimentoSelecionado = null;

    // Billing form components
    private final Label lblTituloAtendimento = new Label("Selecione um atendimento pendente para faturamento");
    private final Label lblValorProcedimentos = new Label("R$ 0,00");
    private final VBox itensBox = new VBox(4);
    private final Label lblValorTotal = new Label("R$ 0,00");

    private final ComboBox<FormaPagamento> formaPagamentoCombo = new ComboBox<>(FXCollections.observableArrayList(FormaPagamento.values()));
    private final VBox dinheiroBox = new VBox(6);
    private final TextField txtValorRecebido = new TextField();
    private final Label lblTroco = new Label("Troco: R$ 0,00");

    public FaturamentoView() {
        setStyle("-fx-background-color: transparent;");
        setPadding(new Insets(0, 24, 24, 24));
        initUI();
        carregarPendentes();
    }

    private void initUI() {
        // Top Header Card (Figma Style)
        Button btnAtualizar = UIUtils.createSecondaryButton("🔄 Atualizar Caixa");
        btnAtualizar.setOnAction(e -> carregarPendentes());

        HBox headerCard = UIUtils.createHeaderCard("💳  Caixa & Faturamento", btnAtualizar);
        setTop(headerCard);

        // Center split
        SplitPane split = new SplitPane();
        split.setStyle("-fx-box-border: transparent; -fx-background-color: transparent; -fx-padding: 16 0 0 0;");

        // Left Pane: Pending Attendances
        VBox leftPane = UIUtils.createCard("-fx-padding: 18;");
        leftPane.setPrefWidth(320);
        leftPane.setMaxWidth(360);

        Label leftTitle = new Label("Atendimentos Pendentes");
        leftTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        leftTitle.setTextFill(Color.web("#0a362f"));

        pendentesList.setItems(pendentesData);
        pendentesList.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");
        pendentesList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Atendimento a, boolean empty) {
                super.updateItem(a, empty);
                if (empty || a == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String pac = pacienteService.buscarPorId(a.getPacienteId()).map(Paciente::getNome).orElse("Paciente");
                    String tut = tutorService.buscarPorId(a.getTutorId()).map(Tutor::getNomeCompleto).orElse("Tutor");

                    VBox box = new VBox(3);
                    box.setPadding(new Insets(4, 2, 4, 2));

                    Label l1 = new Label("🐾 " + pac + " • " + tut);
                    l1.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                    l1.setTextFill(Color.web("#0f172a"));

                    Label l2 = new Label(String.format("Data: %s %s | Total: R$ %.2f", a.getData(), a.getHora(), a.getValorTotal()));
                    l2.setFont(Font.font("Segoe UI", 11));
                    l2.setTextFill(Color.web("#064e43"));

                    box.getChildren().addAll(l1, l2);
                    setGraphic(box);
                }
            }
        });

        pendentesList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                selecionarAtendimento(newV);
            }
        });

        VBox.setVgrow(pendentesList, Priority.ALWAYS);
        leftPane.getChildren().addAll(leftTitle, pendentesList);

        // Right Pane: Checkout
        VBox rightPane = UIUtils.createCard("-fx-padding: 24;");
        lblTituloAtendimento.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblTituloAtendimento.setTextFill(Color.web("#064e43"));

        // Items details card
        VBox detailsCard = new VBox(8);
        detailsCard.setStyle("-fx-background-color: #f8fafc; -fx-padding: 16; -fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8;");

        HBox procRow = new HBox(10);
        procRow.setAlignment(Pos.CENTER_LEFT);
        Label lblProcTitle = new Label("Procedimentos Clínicos Realizados:");
        lblProcTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        Region sp1 = new Region();
        HBox.setHgrow(sp1, Priority.ALWAYS);
        lblValorProcedimentos.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        procRow.getChildren().addAll(lblProcTitle, sp1, lblValorProcedimentos);

        Label lblMatTitle = new Label("Materiais e Medicamentos Utilizados:");
        lblMatTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));

        HBox totalRow = new HBox(10);
        totalRow.setAlignment(Pos.CENTER_LEFT);
        Label lblTotTitle = new Label("VALOR TOTAL A PAGAR:");
        lblTotTitle.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 16));
        lblTotTitle.setTextFill(Color.web("#0f172a"));
        Region sp2 = new Region();
        HBox.setHgrow(sp2, Priority.ALWAYS);
        lblValorTotal.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 22));
        lblValorTotal.setTextFill(Color.web("#064e43"));
        totalRow.getChildren().addAll(lblTotTitle, sp2, lblValorTotal);

        detailsCard.getChildren().addAll(procRow, new Separator(), lblMatTitle, itensBox, new Separator(), totalRow);

        // Payment method selector
        Label lblForma = new Label("Selecione a Forma de Pagamento:");
        lblForma.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        formaPagamentoCombo.setValue(FormaPagamento.CARTAO);
        formaPagamentoCombo.setPrefWidth(250);
        formaPagamentoCombo.valueProperty().addListener((obs, oldV, newV) -> {
            dinheiroBox.setVisible(newV == FormaPagamento.DINHEIRO);
            dinheiroBox.setManaged(newV == FormaPagamento.DINHEIRO);
            atualizarTroco();
        });

        // Cash change section
        Label lblRec = new Label("Valor Recebido em Dinheiro (R$):");
        lblRec.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        txtValorRecebido.setPromptText("Ex: 200.00");
        txtValorRecebido.setPrefWidth(200);
        txtValorRecebido.textProperty().addListener((obs, oldV, newV) -> atualizarTroco());

        lblTroco.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblTroco.setTextFill(Color.web("#15803d"));

        dinheiroBox.getChildren().addAll(lblRec, txtValorRecebido, lblTroco);
        dinheiroBox.setVisible(false);
        dinheiroBox.setManaged(false);

        Button btnConfirmar = UIUtils.createPrimaryButton("Confirmar Pagamento & Emitir Comprovante");
        btnConfirmar.setStyle("-fx-background-color: #047857; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand;");
        btnConfirmar.setOnAction(e -> registrarPagamento());

        rightPane.getChildren().addAll(
                lblTituloAtendimento,
                detailsCard,
                lblForma,
                formaPagamentoCombo,
                dinheiroBox,
                btnConfirmar
        );

        split.getItems().addAll(leftPane, rightPane);
        split.setDividerPositions(0.32);

        setCenter(split);
    }

    private void carregarPendentes() {
        pendentesData.setAll(faturamentoService.listarPendentesFaturamento());
        if (!pendentesData.isEmpty()) {
            pendentesList.getSelectionModel().select(0);
        } else {
            lblTituloAtendimento.setText("Não há atendimentos pendentes de faturamento no momento.");
        }
    }

    private void selecionarAtendimento(Atendimento a) {
        this.atendimentoSelecionado = a;
        Paciente p = pacienteService.buscarPorId(a.getPacienteId()).orElse(null);
        Tutor t = tutorService.buscarPorId(a.getTutorId()).orElse(null);

        String pacNome = p != null ? p.getNome() : "Paciente";
        String tutNome = t != null ? t.getNomeCompleto() : "Tutor";

        lblTituloAtendimento.setText("Faturando Atendimento: " + pacNome + " | Tutor: " + tutNome);
        lblValorProcedimentos.setText(String.format("R$ %.2f", a.getValorProcedimentos()));
        lblValorTotal.setText(String.format("R$ %.2f", a.getValorTotal()));

        itensBox.getChildren().clear();
        if (a.getItensUtilizados().isEmpty()) {
            Label semItens = new Label("Nenhum medicamento adicional utilizado.");
            semItens.setFont(Font.font("Segoe UI", 12));
            semItens.setTextFill(Color.web("#64748b"));
            itensBox.getChildren().add(semItens);
        } else {
            for (ItemUtilizado it : a.getItensUtilizados()) {
                HBox r = new HBox();
                Label n = new Label(it.getNome() + " (" + it.getQuantidade() + "x)");
                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);
                Label v = new Label(String.format("R$ %.2f", it.getSubtotal()));
                r.getChildren().addAll(n, sp, v);
                itensBox.getChildren().add(r);
            }
        }

        txtValorRecebido.setText(String.format("%.2f", a.getValorTotal()));
        atualizarTroco();
    }

    private void atualizarTroco() {
        if (atendimentoSelecionado == null) return;
        try {
            double rec = Double.parseDouble(txtValorRecebido.getText().replace(",", "."));
            double troco = rec - atendimentoSelecionado.getValorTotal();
            if (troco < 0) {
                lblTroco.setText("Valor insuficiente! Faltam R$ " + String.format("%.2f", -troco));
                lblTroco.setTextFill(Color.web("#dc2626"));
            } else {
                lblTroco.setText("Troco a Devolver: R$ " + String.format("%.2f", troco));
                lblTroco.setTextFill(Color.web("#15803d"));
            }
        } catch (Exception ignored) {
            lblTroco.setText("Informe o valor em dinheiro recebido.");
            lblTroco.setTextFill(Color.web("#64748b"));
        }
    }

    private void registrarPagamento() {
        if (atendimentoSelecionado == null) {
            UIUtils.showError("Atenção", "Selecione um atendimento pendente.");
            return;
        }

        FormaPagamento fp = formaPagamentoCombo.getValue();
        Double recebido = null;

        if (fp == FormaPagamento.DINHEIRO) {
            try {
                recebido = Double.parseDouble(txtValorRecebido.getText().replace(",", "."));
            } catch (Exception e) {
                UIUtils.showError("Valor Inválido", "Informe um valor numérico válido para o dinheiro recebido.");
                return;
            }
        }

        OperationResult<Faturamento> res = faturamentoService.registrarPagamento(
                atendimentoSelecionado.getId(), fp, recebido
        );

        if (!res.isSuccess()) {
            UIUtils.showError("Erro no Faturamento", res.getMessage());
            return;
        }

        Faturamento fat = res.getData();
        mostrarComprovante(fat);
        carregarPendentes();
    }

    private void mostrarComprovante(Faturamento fat) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Comprovante de Pagamento");
        alert.setHeaderText("🐾 CLÍNICA VETERINÁRIA MYOW - COMPROVANTE FISCAL");

        String trocoMsg = fat.getTroco() != null ? String.format("\nTroco: R$ %.2f", fat.getTroco()) : "";
        String msg = String.format(
                "ID Faturamento: %s\nData / Hora: %s às %s\nForma de Pagamento: %s\nValor Total: R$ %.2f%s\n\nPagamento confirmado e registrado no caixa com sucesso!",
                fat.getId(), fat.getDataPagamento(), fat.getHoraPagamento(), fat.getFormaPagamento().getDescricao(), fat.getValorTotal(), trocoMsg
        );

        alert.setContentText(msg);
        alert.showAndWait();
    }
}
