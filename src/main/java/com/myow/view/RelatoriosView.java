package com.myow.view;

import com.myow.model.*;
import com.myow.service.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class RelatoriosView extends VBox {
    private final RelatorioService relatorioService = new RelatorioService();
    private final PacienteService pacienteService = new PacienteService();

    private final DatePicker dtInicio = new DatePicker(LocalDate.now().minusMonths(1));
    private final DatePicker dtFim = new DatePicker(LocalDate.now());

    // KPI labels
    private final Label lblTotalFaturado = new Label("R$ 0,00");
    private final Label lblTicketMedio = new Label("R$ 0,00");
    private final Label lblQtdPagamentos = new Label("0");
    private final Label lblTotalCartao = new Label("R$ 0,00");
    private final Label lblTotalPix = new Label("R$ 0,00");
    private final Label lblTotalDinheiro = new Label("R$ 0,00");

    private final TableView<Faturamento> faturamentoTable = new TableView<>();
    private final ObservableList<Faturamento> faturamentoData = FXCollections.observableArrayList();

    public RelatoriosView() {
        setSpacing(16);
        setPadding(new Insets(0, 24, 24, 24));
        setStyle("-fx-background-color: transparent;");
        initUI();
        atualizarRelatorios();
    }

    private void initUI() {
        // Top Header Card (Figma Style)
        Button btnFiltrar = UIUtils.createPrimaryButton("Aplicar Filtro");
        btnFiltrar.setOnAction(e -> atualizarRelatorios());

        HBox headerCard = UIUtils.createHeaderCard("📈  Relatórios & Gestão Financeira", btnFiltrar);

        // Date Filter Box + Export Buttons
        HBox filterBox = new HBox(12);
        filterBox.getStyleClass().add("card");
        filterBox.setStyle("-fx-padding: 14 18; -fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12;");
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Label l1 = new Label("Período de:");
        l1.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        l1.setTextFill(Color.web("#0a362f"));

        Label l2 = new Label("até:");
        l2.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        l2.setTextFill(Color.web("#0a362f"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Botões de Exportação (UC11)
        Button btnExportPdf = new Button("📄 Exportar PDF");
        btnExportPdf.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand;");
        btnExportPdf.setOnAction(e -> exportarParaPdf());

        Button btnExportExcel = new Button("📊 Exportar Excel (.xlsx)");
        btnExportExcel.setStyle("-fx-background-color: #064e43; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand;");
        btnExportExcel.setOnAction(e -> exportarParaExcel());

        filterBox.getChildren().addAll(l1, dtInicio, l2, dtFim, spacer, btnExportPdf, btnExportExcel);

        // KPIs Row
        HBox kpiRow = new HBox(14);
        VBox card1 = criarKpiCard("Total Faturado", lblTotalFaturado, "#064e43");
        VBox card2 = criarKpiCard("Ticket Médio", lblTicketMedio, "#0d5c4d");
        VBox card3 = criarKpiCard("Qtd. Transações", lblQtdPagamentos, "#334155");
        VBox card4 = criarKpiCard("Cartão", lblTotalCartao, "#0a362f");
        VBox card5 = criarKpiCard("Pix", lblTotalPix, "#059669");
        VBox card6 = criarKpiCard("Dinheiro", lblTotalDinheiro, "#d97706");

        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);
        HBox.setHgrow(card4, Priority.ALWAYS);
        HBox.setHgrow(card5, Priority.ALWAYS);
        HBox.setHgrow(card6, Priority.ALWAYS);

        kpiRow.getChildren().addAll(card1, card2, card3, card4, card5, card6);

        // Table Card
        VBox tableCard = UIUtils.createCard("-fx-padding: 20;");
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        Label tableTitle = new Label("Detalhamento dos Recebimentos no Período");
        tableTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        tableTitle.setTextFill(Color.web("#0a362f"));

        TableColumn<Faturamento, String> colData = new TableColumn<>("DATA");
        colData.setCellValueFactory(new PropertyValueFactory<>("dataPagamento"));
        colData.setPrefWidth(110);

        TableColumn<Faturamento, String> colHora = new TableColumn<>("HORA");
        colHora.setCellValueFactory(new PropertyValueFactory<>("horaPagamento"));
        colHora.setPrefWidth(90);

        TableColumn<Faturamento, String> colPac = new TableColumn<>("PACIENTE");
        colPac.setCellValueFactory(c -> {
            String n = pacienteService.buscarPorId(c.getValue().getPacienteId()).map(Paciente::getNome).orElse("Paciente");
            return new SimpleStringProperty(n);
        });
        colPac.setPrefWidth(180);

        TableColumn<Faturamento, Void> colForma = new TableColumn<>("FORMA DE PAGTO.");
        colForma.setPrefWidth(140);
        colForma.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Faturamento f = getTableView().getItems().get(getIndex());
                    Label badge = new Label(f.getFormaPagamento().getDescricao());
                    badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                    badge.setStyle("-fx-background-color: #cbf3ea; -fx-text-fill: #064e43; -fx-padding: 3 8; -fx-background-radius: 10;");
                    setGraphic(badge);
                }
            }
        });

        TableColumn<Faturamento, String> colValor = new TableColumn<>("VALOR TOTAL");
        colValor.setCellValueFactory(c -> new SimpleStringProperty(String.format("R$ %.2f", c.getValue().getValorTotal())));
        colValor.setPrefWidth(120);

        faturamentoTable.getColumns().addAll(colData, colHora, colPac, colForma, colValor);
        faturamentoTable.setItems(faturamentoData);
        faturamentoTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(faturamentoTable, Priority.ALWAYS);

        HBox paginationBar = UIUtils.createPaginationBar(1, Math.min(10, faturamentoData.size()), faturamentoData.size(), "transações", 1, 1);

        tableCard.getChildren().addAll(tableTitle, faturamentoTable, paginationBar);

        getChildren().addAll(headerCard, filterBox, kpiRow, tableCard);
    }

    private VBox criarKpiCard(String title, Label valLbl, String colorHex) {
        VBox card = UIUtils.createCard("-fx-padding: 12 16;");
        Label t = new Label(title);
        t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        t.setTextFill(Color.web("#64748b"));

        valLbl.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 17));
        valLbl.setTextFill(Color.web(colorHex));

        card.getChildren().addAll(t, valLbl);
        return card;
    }

    private void atualizarRelatorios() {
        LocalDate start = dtInicio.getValue();
        LocalDate end = dtFim.getValue();

        String sStr = start != null ? start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
        String eStr = end != null ? end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";

        if (!relatorioService.isPeriodoValido(sStr, eStr)) {
            UIUtils.showError("Período Inválido", "A data inicial não pode ser posterior à data final.");
            return;
        }

        Map<String, Object> resumo = relatorioService.gerarResumoFinanceiro(sStr, eStr);
        lblTotalFaturado.setText(String.format("R$ %.2f", (Double) resumo.get("total")));
        lblTicketMedio.setText(String.format("R$ %.2f", (Double) resumo.get("ticketMedio")));
        lblQtdPagamentos.setText(String.valueOf(resumo.get("quantidade")));
        lblTotalDinheiro.setText(String.format("R$ %.2f", (Double) resumo.get("totalDinheiro")));
        lblTotalCartao.setText(String.format("R$ %.2f", (Double) resumo.get("totalCartao")));
        lblTotalPix.setText(String.format("R$ %.2f", (Double) resumo.get("totalPix")));

        @SuppressWarnings("unchecked")
        List<Faturamento> list = (List<Faturamento>) resumo.get("registros");
        faturamentoData.setAll(list);
    }

    private void exportarParaPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Relatório em PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documento PDF (*.pdf)", "*.pdf"));
        fileChooser.setInitialFileName("relatorio_myow_" + LocalDate.now() + ".pdf");

        File file = fileChooser.showSaveDialog(getScene() != null ? getScene().getWindow() : null);
        if (file == null) {
            return;
        }

        LocalDate start = dtInicio.getValue();
        LocalDate end = dtFim.getValue();
        String sStr = start != null ? start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
        String eStr = end != null ? end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";

        OperationResult<File> res = relatorioService.exportarPdf(file, sStr, eStr);
        if (res.isSuccess()) {
            UIUtils.showInfo("Exportação Concluída", res.getMessage());
        } else {
            UIUtils.showError("Erro na Exportação", res.getMessage());
        }
    }

    private void exportarParaExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Relatório em Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Planilha Excel (*.xlsx)", "*.xlsx"));
        fileChooser.setInitialFileName("relatorio_myow_" + LocalDate.now() + ".xlsx");

        File file = fileChooser.showSaveDialog(getScene() != null ? getScene().getWindow() : null);
        if (file == null) {
            return;
        }

        LocalDate start = dtInicio.getValue();
        LocalDate end = dtFim.getValue();
        String sStr = start != null ? start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
        String eStr = end != null ? end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";

        OperationResult<File> res = relatorioService.exportarExcel(file, sStr, eStr);
        if (res.isSuccess()) {
            UIUtils.showInfo("Exportação Concluída", res.getMessage());
        } else {
            UIUtils.showError("Erro na Exportação", res.getMessage());
        }
    }
}
