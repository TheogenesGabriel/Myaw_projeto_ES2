package com.myow.view;

import com.myow.model.ItemEstoque;
import com.myow.service.EstoqueService;
import com.myow.service.OperationResult;
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

import java.util.Optional;

public class EstoqueView extends VBox {
    private final EstoqueService estoqueService = new EstoqueService();
    private final TableView<ItemEstoque> table = new TableView<>();
    private final ObservableList<ItemEstoque> data = FXCollections.observableArrayList();
    private final VBox bannerCritico = new VBox();

    public EstoqueView() {
        setSpacing(16);
        setPadding(new Insets(0, 24, 24, 24));
        setStyle("-fx-background-color: transparent;");
        initUI();
        carregarDados();
    }

    private void initUI() {
        // Top Header Card (Figma Style)
        Button btnNovo = UIUtils.createPrimaryButton("+ Novo Item");
        btnNovo.setOnAction(e -> abrirModalNovoItem());

        HBox headerCard = UIUtils.createHeaderCard("📦  Controle de Estoque & Farmácia", btnNovo);

        // Low stock banner container
        bannerCritico.setSpacing(4);

        // Main Content Card
        VBox contentCard = UIUtils.createCard("-fx-padding: 20;");
        VBox.setVgrow(contentCard, Priority.ALWAYS);

        // Table Setup
        TableColumn<ItemEstoque, String> colNome = new TableColumn<>("ITEM / MEDICAMENTO");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colNome.setPrefWidth(220);

        TableColumn<ItemEstoque, String> colLote = new TableColumn<>("LOTE");
        colLote.setCellValueFactory(new PropertyValueFactory<>("lote"));
        colLote.setPrefWidth(100);

        TableColumn<ItemEstoque, String> colCat = new TableColumn<>("CATEGORIA");
        colCat.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colCat.setPrefWidth(110);

        TableColumn<ItemEstoque, String> colSaldo = new TableColumn<>("SALDO ATUAL");
        colSaldo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getQuantidade() + " " + c.getValue().getUnidade()));
        colSaldo.setPrefWidth(100);

        TableColumn<ItemEstoque, Integer> colMin = new TableColumn<>("ESTOQUE MÍN.");
        colMin.setCellValueFactory(new PropertyValueFactory<>("estoqueMinimo"));
        colMin.setPrefWidth(95);

        TableColumn<ItemEstoque, String> colVal = new TableColumn<>("VALIDADE");
        colVal.setCellValueFactory(new PropertyValueFactory<>("validade"));
        colVal.setPrefWidth(100);

        TableColumn<ItemEstoque, String> colPreco = new TableColumn<>("VALOR UNITÁRIO");
        colPreco.setCellValueFactory(c -> new SimpleStringProperty(String.format("R$ %.2f", c.getValue().getValorUnitario())));
        colPreco.setPrefWidth(110);

        TableColumn<ItemEstoque, Void> colStatus = new TableColumn<>("STATUS SALDO");
        colStatus.setPrefWidth(110);
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    ItemEstoque it = getTableView().getItems().get(getIndex());
                    boolean baixo = it.isEstoqueBaixo();
                    Label badge = new Label(baixo ? "Crítico" : "Normal");
                    badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                    if (baixo) {
                        badge.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-padding: 3 8; -fx-background-radius: 10;");
                    } else {
                        badge.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-padding: 3 8; -fx-background-radius: 10;");
                    }
                    setGraphic(badge);
                }
            }
        });

        TableColumn<ItemEstoque, Void> colAcoes = new TableColumn<>("AÇÕES");
        colAcoes.setPrefWidth(170);
        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btnAjustar = UIUtils.createSelectButton("Ajustar");
            private final Button btnEditar = new Button("Editar");
            private final HBox box = new HBox(6, btnAjustar, btnEditar);

            {
                btnEditar.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 5 10; -fx-background-radius: 6;");
                box.setAlignment(Pos.CENTER_LEFT);

                btnAjustar.setOnAction(e -> {
                    ItemEstoque item = getTableView().getItems().get(getIndex());
                    abrirModalAjustarSaldo(item);
                });

                btnEditar.setOnAction(e -> {
                    ItemEstoque item = getTableView().getItems().get(getIndex());
                    abrirModalEditar(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(colNome, colLote, colCat, colSaldo, colMin, colVal, colPreco, colStatus, colAcoes);
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        HBox paginationBar = UIUtils.createPaginationBar(1, Math.min(10, data.size()), data.size(), "itens", 1, 1);

        contentCard.getChildren().addAll(table, paginationBar);

        getChildren().addAll(headerCard, bannerCritico, contentCard);
    }

    private void carregarDados() {
        data.setAll(estoqueService.listarTodos());
        atualizarBanner();
    }

    private void atualizarBanner() {
        bannerCritico.getChildren().clear();
        long criticos = estoqueService.listarItensCriticos().size();
        if (criticos > 0) {
            HBox b = new HBox(12);
            b.setAlignment(Pos.CENTER_LEFT);
            b.setPadding(new Insets(12, 16, 12, 16));
            b.setStyle("-fx-background-color: #fff1f2; -fx-border-color: #fecdd3; -fx-border-radius: 8; -fx-background-radius: 8;");

            Label msg = new Label("⚠️ Alerta: Existem " + criticos + " item(ns) com saldo igual ou abaixo do estoque mínimo!");
            msg.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            msg.setTextFill(Color.web("#9f1239"));

            b.getChildren().add(msg);
            bannerCritico.getChildren().add(b);
        }
    }

    private void abrirModalNovoItem() {
        Dialog<ItemEstoque> dialog = new Dialog<>();
        dialog.setTitle("Cadastrar Item no Estoque");
        dialog.setHeaderText("Preencha as informações do medicamento ou insumo médico.");

        ButtonType btnSalvar = new ButtonType("Salvar no Estoque", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSalvar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField nomeField = new TextField();
        nomeField.setPromptText("Ex: Amoxicilina 250mg");

        TextField loteField = new TextField();
        loteField.setPromptText("Ex: LOT-2025-01");

        ComboBox<String> catCombo = new ComboBox<>(FXCollections.observableArrayList("Medicamento", "Vacina", "Material", "Ração/Nutrição", "Higiene"));
        catCombo.setValue("Medicamento");

        Spinner<Integer> qtdSpinner = new Spinner<>(0, 10000, 10);
        qtdSpinner.setEditable(true);

        Spinner<Integer> minSpinner = new Spinner<>(0, 1000, 5);
        minSpinner.setEditable(true);

        DatePicker valPicker = new DatePicker();

        TextField precoField = new TextField("25.00");
        precoField.setPromptText("Valor unitário (R$)");

        ComboBox<String> undCombo = new ComboBox<>(FXCollections.observableArrayList("unidade", "frasco", "caixa", "dose", "ampola", "comprimido", "par"));
        undCombo.setValue("frasco");

        grid.add(new Label("Nome do Item: *"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(new Label("Lote: *"), 0, 1);
        grid.add(loteField, 1, 1);
        grid.add(new Label("Categoria: *"), 0, 2);
        grid.add(catCombo, 1, 2);
        grid.add(new Label("Quantidade Inicial: *"), 0, 3);
        grid.add(qtdSpinner, 1, 3);
        grid.add(new Label("Estoque Mínimo: *"), 0, 4);
        grid.add(minSpinner, 1, 4);
        grid.add(new Label("Data de Validade: *"), 0, 5);
        grid.add(valPicker, 1, 5);
        grid.add(new Label("Valor Unitário (R$): *"), 0, 6);
        grid.add(precoField, 1, 6);
        grid.add(new Label("Unidade de Medida:"), 0, 7);
        grid.add(undCombo, 1, 7);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnSalvar) {
                if (nomeField.getText().trim().isEmpty() || loteField.getText().trim().isEmpty() || valPicker.getValue() == null) {
                    UIUtils.showError("Campos Obrigatórios", "Nome, Lote e Validade são campos obrigatórios.");
                    return null;
                }

                double preco = 0.0;
                try {
                    preco = Double.parseDouble(precoField.getText().replace(",", "."));
                } catch (Exception e) {
                    UIUtils.showError("Valor Inválido", "Informe um valor unitário válido.");
                    return null;
                }

                String valStr = valPicker.getValue().toString();
                OperationResult<ItemEstoque> res = estoqueService.cadastrarItem(
                        nomeField.getText(), loteField.getText(), catCombo.getValue(),
                        qtdSpinner.getValue(), minSpinner.getValue(), valStr, preco, undCombo.getValue(), false
                );

                if (!res.isSuccess()) {
                    if (res.getMessage().contains("DUPLICADO")) {
                        boolean somar = UIUtils.showConfirm(
                                "Item já existente",
                                "Já existe um item cadastrado com este mesmo nome e lote.",
                                "Deseja somar a quantidade informada ao saldo do item existente?"
                        );
                        if (somar) {
                            return estoqueService.cadastrarItem(
                                    nomeField.getText(), loteField.getText(), catCombo.getValue(),
                                    qtdSpinner.getValue(), minSpinner.getValue(), valStr, preco, undCombo.getValue(), true
                            ).getData();
                        }
                    } else {
                        UIUtils.showError("Erro", res.getMessage());
                    }
                    return null;
                }
                return res.getData();
            }
            return null;
        });

        Optional<ItemEstoque> res = dialog.showAndWait();
        res.ifPresent(i -> {
            carregarDados();
            UIUtils.showInfo("Sucesso", "Item cadastrado com sucesso no estoque!");
        });
    }

    private void abrirModalAjustarSaldo(ItemEstoque item) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Ajustar Saldo de Estoque");
        dialog.setHeaderText("Item: " + item.getNome() + " (Lote: " + item.getLote() + ")\nSaldo Atual: " + item.getQuantidade() + " " + item.getUnidade());

        ButtonType btnConfirmar = new ButtonType("Confirmar Ajuste", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnConfirmar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> tipoCombo = new ComboBox<>(FXCollections.observableArrayList("Entrada (+)", "Saída / Ajuste (-)"));
        tipoCombo.setValue("Entrada (+)");

        Spinner<Integer> qtdSpinner = new Spinner<>(1, 500, 5);
        qtdSpinner.setEditable(true);

        grid.add(new Label("Tipo de Movimentação:"), 0, 0);
        grid.add(tipoCombo, 1, 0);
        grid.add(new Label("Quantidade:"), 0, 1);
        grid.add(qtdSpinner, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnConfirmar) {
                int delta = qtdSpinner.getValue();
                if (tipoCombo.getValue().startsWith("Saída")) {
                    delta = -delta;
                }
                return delta;
            }
            return null;
        });

        Optional<Integer> res = dialog.showAndWait();
        res.ifPresent(delta -> {
            OperationResult<ItemEstoque> op = estoqueService.ajustarQuantidade(item.getId(), delta);
            if (op.isSuccess()) {
                carregarDados();
                if (!op.getAlertas().isEmpty()) {
                    UIUtils.showWarning("Aviso", String.join("\n", op.getAlertas()));
                } else {
                    UIUtils.showInfo("Sucesso", "Saldo atualizado para " + op.getData().getQuantidade() + " " + op.getData().getUnidade() + "!");
                }
            } else {
                UIUtils.showError("Erro", op.getMessage());
            }
        });
    }

    private void abrirModalEditar(ItemEstoque item) {
        Dialog<ItemEstoque> dialog = new Dialog<>();
        dialog.setTitle("Editar Item");
        dialog.setHeaderText("Editar informações cadastrais do item " + item.getNome());

        ButtonType btnSalvar = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSalvar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nomeField = new TextField(item.getNome());
        TextField catField = new TextField(item.getCategoria());
        Spinner<Integer> minSpinner = new Spinner<>(0, 1000, item.getEstoqueMinimo());
        minSpinner.setEditable(true);
        TextField precoField = new TextField(String.valueOf(item.getValorUnitario()));

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(new Label("Categoria:"), 0, 1);
        grid.add(catField, 1, 1);
        grid.add(new Label("Estoque Mínimo:"), 0, 2);
        grid.add(minSpinner, 1, 2);
        grid.add(new Label("Valor Unitário (R$):"), 0, 3);
        grid.add(precoField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnSalvar) {
                double preco = Double.parseDouble(precoField.getText().replace(",", "."));
                estoqueService.editarItem(item.getId(), nomeField.getText(), catField.getText(), minSpinner.getValue(), preco);
                return item;
            }
            return null;
        });

        Optional<ItemEstoque> res = dialog.showAndWait();
        res.ifPresent(i -> carregarDados());
    }
}
