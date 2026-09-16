package com.myow.view;

import com.myow.model.Perfil;
import com.myow.model.Usuario;
import com.myow.service.FuncionarioService;
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

public class FuncionariosView extends VBox {
    private final FuncionarioService funcionarioService = new FuncionarioService();

    private final TableView<Usuario> table = new TableView<>();
    private final ObservableList<Usuario> data = FXCollections.observableArrayList();

    public FuncionariosView() {
        setSpacing(16);
        setPadding(new Insets(0, 24, 24, 24));
        setStyle("-fx-background-color: transparent;");
        initUI();
        carregarDados();
    }

    private void initUI() {
        // 1. Header Card (Figma Screenshot 4)
        Button btnNovo = UIUtils.createPrimaryButton("+ Novo Funcionário");
        btnNovo.setOnAction(e -> abrirModalNovo());

        HBox headerCard = UIUtils.createHeaderCard("👤  Cadastro de Funcionários", btnNovo);

        // 2. Main Content Card
        VBox contentCard = UIUtils.createCard("-fx-padding: 20;");
        VBox.setVgrow(contentCard, Priority.ALWAYS);

        // Filter Bar
        HBox filterBox = new HBox(12);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(0, 0, 14, 0));

        TextField searchField = new TextField();
        searchField.setPromptText("Buscar por nome, login, cargo ou CPF...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-padding: 8 12; -fx-background-radius: 8; -fx-border-color: #cbd5e1; -fx-border-radius: 8;");

        ComboBox<String> perfilCombo = new ComboBox<>(FXCollections.observableArrayList("Todos os Perfis", "ADMINISTRADOR", "VETERINARIO", "FUNCIONARIO"));
        perfilCombo.setValue("Todos os Perfis");

        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList("Todos os Status", "Ativo", "Inativo"));
        statusCombo.setValue("Todos os Status");

        searchField.textProperty().addListener((obs, o, n) -> aplicarFiltro(searchField.getText(), perfilCombo.getValue(), statusCombo.getValue()));
        perfilCombo.valueProperty().addListener((obs, o, n) -> aplicarFiltro(searchField.getText(), perfilCombo.getValue(), statusCombo.getValue()));
        statusCombo.valueProperty().addListener((obs, o, n) -> aplicarFiltro(searchField.getText(), perfilCombo.getValue(), statusCombo.getValue()));

        filterBox.getChildren().addAll(searchField, new Label("Perfil:"), perfilCombo, new Label("Status:"), statusCombo);

        // Coluna 1: NOME DO PROFISSIONAL (Avatar circular com iniciais + Nome + Email/Login)
        TableColumn<Usuario, Void> colNome = new TableColumn<>("NOME DO PROFISSIONAL");
        colNome.setPrefWidth(240);
        colNome.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Usuario u = getTableView().getItems().get(getIndex());
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER_LEFT);

                    Label avatar = new Label(obterIniciais(u.getNome()));
                    avatar.setStyle("-fx-background-color: #cbf3ea; -fx-text-fill: #064e43; -fx-font-weight: bold; -fx-font-size: 11px; -fx-min-width: 32px; -fx-min-height: 32px; -fx-max-width: 32px; -fx-max-height: 32px; -fx-alignment: center; -fx-background-radius: 16;");

                    VBox textBox = new VBox(2);
                    Label lblNome = new Label(u.getNome());
                    lblNome.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0f172a;");

                    Label lblEmail = new Label(u.getLogin() + "@myow.vet.br");
                    lblEmail.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

                    textBox.getChildren().addAll(lblNome, lblEmail);
                    box.getChildren().addAll(avatar, textBox);
                    setGraphic(box);
                }
            }
        });

        // Coluna 2: CARGO / FUNÇÃO
        TableColumn<Usuario, String> colCargo = new TableColumn<>("CARGO / FUNÇÃO");
        colCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        colCargo.setPrefWidth(160);

        // Coluna 3: DOCUMENTO / CONSELHO
        TableColumn<Usuario, Void> colDoc = new TableColumn<>("DOCUMENTO / CONSELHO");
        colDoc.setPrefWidth(150);
        colDoc.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Usuario u = getTableView().getItems().get(getIndex());
                    String doc = (u.getCpf() != null && !u.getCpf().isEmpty()) ? u.getCpf() : "CRMV-SP Ativo";
                    Label lbl = new Label(doc);
                    lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");
                    setGraphic(lbl);
                }
            }
        });

        // Coluna 4: PERFIL DE ACESSO (Badge com cores Figma)
        TableColumn<Usuario, Void> colPerfil = new TableColumn<>("PERFIL DE ACESSO");
        colPerfil.setPrefWidth(150);
        colPerfil.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Usuario u = getTableView().getItems().get(getIndex());
                    Label badge = new Label(u.getPerfil().getDescricao());
                    badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));

                    switch (u.getPerfil()) {
                        case VETERINARIO:
                            badge.setStyle("-fx-background-color: #cbf3ea; -fx-text-fill: #064e43; -fx-padding: 3 8; -fx-background-radius: 10;");
                            break;
                        case ADMINISTRADOR:
                            badge.setStyle("-fx-background-color: #ede9fe; -fx-text-fill: #6d28d9; -fx-padding: 3 8; -fx-background-radius: 10;");
                            break;
                        case FUNCIONARIO:
                        default:
                            badge.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1; -fx-padding: 3 8; -fx-background-radius: 10;");
                            break;
                    }
                    setGraphic(badge);
                }
            }
        });

        // Coluna 5: STATUS
        TableColumn<Usuario, Void> colStatus = new TableColumn<>("STATUS");
        colStatus.setPrefWidth(100);
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Usuario u = getTableView().getItems().get(getIndex());
                    Label badge = new Label(u.isAtivo() ? "Ativo" : "Inativo");
                    badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                    if (u.isAtivo()) {
                        badge.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-padding: 3 8; -fx-background-radius: 10;");
                    } else {
                        badge.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-padding: 3 8; -fx-background-radius: 10;");
                    }
                    setGraphic(badge);
                }
            }
        });

        // Coluna 6: AÇÕES
        TableColumn<Usuario, Void> colAcoes = new TableColumn<>("AÇÕES");
        colAcoes.setPrefWidth(210);
        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btnSelecionar = UIUtils.createSelectButton("🔍 Selecionar");
            private final Button btnEditar = new Button("Editar");
            private final Button btnAlternar = new Button();
            private final HBox box = new HBox(6, btnSelecionar, btnEditar, btnAlternar);

            {
                box.setAlignment(Pos.CENTER_LEFT);
                btnEditar.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 5 10; -fx-background-radius: 6;");

                btnSelecionar.setOnAction(e -> {
                    Usuario u = getTableView().getItems().get(getIndex());
                    abrirModalEditar(u);
                });

                btnAlternar.setOnAction(e -> {
                    Usuario u = getTableView().getItems().get(getIndex());
                    OperationResult<Usuario> res = funcionarioService.alternarStatus(u.getId());
                    if (res.isSuccess()) {
                        carregarDados();
                    } else {
                        UIUtils.showError("Ação Bloqueada", res.getMessage());
                    }
                });

                btnEditar.setOnAction(e -> {
                    Usuario u = getTableView().getItems().get(getIndex());
                    abrirModalEditar(u);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Usuario u = getTableView().getItems().get(getIndex());
                    if (u.isAtivo()) {
                        btnAlternar.setText("Inativar");
                        btnAlternar.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 5 10; -fx-background-radius: 6;");
                    } else {
                        btnAlternar.setText("Ativar");
                        btnAlternar.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 5 10; -fx-background-radius: 6;");
                    }
                    setGraphic(box);
                }
            }
        });

        table.getColumns().addAll(colNome, colCargo, colDoc, colPerfil, colStatus, colAcoes);
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        // Barra de Paginação idêntica ao Figma Screenshot 4
        HBox paginationBar = UIUtils.createPaginationBar(1, Math.min(9, data.size()), data.size(), "funcionários", 1, 2);

        contentCard.getChildren().addAll(filterBox, table, paginationBar);

        getChildren().addAll(headerCard, contentCard);
    }

    private String obterIniciais(String nome) {
        if (nome == null || nome.trim().isEmpty()) return "US";
        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 1) return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
        return ("" + partes[0].charAt(0) + partes[partes.length - 1].charAt(0)).toUpperCase();
    }

    private void carregarDados() {
        data.setAll(funcionarioService.listarTodos());
    }

    private void aplicarFiltro(String termo, String perfil, String status) {
        String p = perfil.equals("Todos os Perfis") ? "TODOS" : perfil;
        String s = status.equals("Todos os Status") ? "TODOS" : status;
        data.setAll(funcionarioService.filtrar(termo, p, s));
    }

    private void abrirModalNovo() {
        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle("Cadastrar Novo Colaborador");
        dialog.setHeaderText("Informe as credenciais e permissões do funcionário.");

        ButtonType btnSalvar = new ButtonType("Salvar Colaborador", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSalvar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nomeField = new TextField();
        TextField cpfField = new TextField();
        TextField telField = new TextField();
        TextField cargoField = new TextField("Recepcionista");

        ComboBox<Perfil> perfilCombo = new ComboBox<>(FXCollections.observableArrayList(Perfil.values()));
        perfilCombo.setValue(Perfil.FUNCIONARIO);

        TextField loginField = new TextField();
        PasswordField senhaField = new PasswordField();

        grid.add(new Label("Nome Completo: *"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(new Label("CPF: *"), 0, 1);
        grid.add(cpfField, 1, 1);
        grid.add(new Label("Telefone:"), 0, 2);
        grid.add(telField, 1, 2);
        grid.add(new Label("Cargo: *"), 0, 3);
        grid.add(cargoField, 1, 3);
        grid.add(new Label("Perfil de Acesso: *"), 0, 4);
        grid.add(perfilCombo, 1, 4);
        grid.add(new Label("Login de Acesso: *"), 0, 5);
        grid.add(loginField, 1, 5);
        grid.add(new Label("Senha Inicial: *"), 0, 6);
        grid.add(senhaField, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnSalvar) {
                OperationResult<Usuario> res = funcionarioService.cadastrar(
                        nomeField.getText(), cpfField.getText(), telField.getText(),
                        cargoField.getText(), perfilCombo.getValue(), loginField.getText(), senhaField.getText()
                );

                if (!res.isSuccess()) {
                    UIUtils.showError("Erro no Cadastro", res.getMessage());
                    return null;
                }
                return res.getData();
            }
            return null;
        });

        Optional<Usuario> res = dialog.showAndWait();
        res.ifPresent(u -> {
            carregarDados();
            UIUtils.showInfo("Sucesso", "Colaborador cadastrado com sucesso!");
        });
    }

    private void abrirModalEditar(Usuario u) {
        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle("Editar Colaborador");
        dialog.setHeaderText("Editar informações de " + u.getNome());

        ButtonType btnSalvar = new ButtonType("Salvar Alterações", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSalvar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nomeField = new TextField(u.getNome());
        TextField cpfField = new TextField(u.getCpf());
        TextField telField = new TextField(u.getTelefone());
        TextField cargoField = new TextField(u.getCargo());

        ComboBox<Perfil> perfilCombo = new ComboBox<>(FXCollections.observableArrayList(Perfil.values()));
        perfilCombo.setValue(u.getPerfil());

        TextField loginField = new TextField(u.getLogin());
        PasswordField senhaField = new PasswordField();
        senhaField.setPromptText("Deixe em branco para manter a atual");

        grid.add(new Label("Nome Completo: *"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(new Label("CPF: *"), 0, 1);
        grid.add(cpfField, 1, 1);
        grid.add(new Label("Telefone:"), 0, 2);
        grid.add(telField, 1, 2);
        grid.add(new Label("Cargo: *"), 0, 3);
        grid.add(cargoField, 1, 3);
        grid.add(new Label("Perfil de Acesso: *"), 0, 4);
        grid.add(perfilCombo, 1, 4);
        grid.add(new Label("Login de Acesso: *"), 0, 5);
        grid.add(loginField, 1, 5);
        grid.add(new Label("Nova Senha:"), 0, 6);
        grid.add(senhaField, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnSalvar) {
                OperationResult<Usuario> res = funcionarioService.editar(
                        u.getId(), nomeField.getText(), cpfField.getText(), telField.getText(),
                        cargoField.getText(), perfilCombo.getValue(), loginField.getText(), senhaField.getText()
                );

                if (!res.isSuccess()) {
                    UIUtils.showError("Erro na Atualização", res.getMessage());
                    return null;
                }
                return res.getData();
            }
            return null;
        });

        Optional<Usuario> res = dialog.showAndWait();
        res.ifPresent(updated -> carregarDados());
    }
}
