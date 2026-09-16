package com.myow.view;

import com.myow.model.Tutor;
import com.myow.service.OperationResult;
import com.myow.service.TutorService;
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

public class TutoresView extends VBox {
    private final TutorService tutorService = new TutorService();
    private final TableView<Tutor> table = new TableView<>();
    private final ObservableList<Tutor> data = FXCollections.observableArrayList();

    public TutoresView() {
        setSpacing(16);
        setPadding(new Insets(0, 24, 24, 24));
        setStyle("-fx-background-color: transparent;");
        initUI();
        carregarDados();
    }

    private void initUI() {
        // Top Header Card (Figma Style)
        Button btnNovo = UIUtils.createPrimaryButton("+ Novo Tutor");
        btnNovo.setOnAction(e -> abrirModalNovoTutor());

        HBox headerCard = UIUtils.createHeaderCard("👥  Gestão de Tutores", btnNovo);

        // Main Content Card
        VBox contentCard = UIUtils.createCard("-fx-padding: 20;");
        VBox.setVgrow(contentCard, Priority.ALWAYS);

        // Search Bar
        HBox searchBox = new HBox(12);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(0, 0, 14, 0));

        TextField searchField = new TextField();
        searchField.setPromptText("Buscar por nome, telefone ou CPF do tutor...");
        searchField.setPrefWidth(350);
        searchField.setStyle("-fx-padding: 8 12; -fx-background-radius: 8; -fx-border-color: #cbd5e1; -fx-border-radius: 8;");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            data.setAll(tutorService.filtrar(newVal));
        });

        searchBox.getChildren().add(searchField);

        // Coluna 1: TUTOR (Avatar circular com iniciais + Nome)
        TableColumn<Tutor, Void> colNome = new TableColumn<>("NOME DO TUTOR");
        colNome.setPrefWidth(240);
        colNome.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Tutor t = getTableView().getItems().get(getIndex());
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER_LEFT);

                    Label avatar = new Label(obterIniciais(t.getNomeCompleto()));
                    avatar.setStyle("-fx-background-color: #cbf3ea; -fx-text-fill: #064e43; -fx-font-weight: bold; -fx-font-size: 11px; -fx-min-width: 32px; -fx-min-height: 32px; -fx-max-width: 32px; -fx-max-height: 32px; -fx-alignment: center; -fx-background-radius: 16;");

                    VBox textBox = new VBox(2);
                    Label lblNome = new Label(t.getNomeCompleto());
                    lblNome.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0f172a;");

                    Label lblDoc = new Label("CPF: " + t.getCpf());
                    lblDoc.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

                    textBox.getChildren().addAll(lblNome, lblDoc);
                    box.getChildren().addAll(avatar, textBox);
                    setGraphic(box);
                }
            }
        });

        TableColumn<Tutor, String> colTelefone = new TableColumn<>("TELEFONE");
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        colTelefone.setPrefWidth(140);

        TableColumn<Tutor, String> colEmail = new TableColumn<>("E-MAIL");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(180);

        TableColumn<Tutor, String> colEndereco = new TableColumn<>("ENDEREÇO");
        colEndereco.setCellValueFactory(new PropertyValueFactory<>("endereco"));
        colEndereco.setPrefWidth(240);

        TableColumn<Tutor, Void> colAcoes = new TableColumn<>("AÇÕES");
        colAcoes.setPrefWidth(130);
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnSel = UIUtils.createSelectButton("🔍 Selecionar");
            {
                btnSel.setOnAction(e -> {
                    Tutor t = getTableView().getItems().get(getIndex());
                    UIUtils.showInfo("Tutor Selecionado", "Tutor: " + t.getNomeCompleto() + "\nTelefone: " + t.getTelefone());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    setGraphic(btnSel);
                }
            }
        });

        table.getColumns().addAll(colNome, colTelefone, colEmail, colEndereco, colAcoes);
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        HBox paginationBar = UIUtils.createPaginationBar(1, Math.min(10, data.size()), data.size(), "tutores", 1, 1);

        contentCard.getChildren().addAll(searchBox, table, paginationBar);

        getChildren().addAll(headerCard, contentCard);
    }

    private String obterIniciais(String nome) {
        if (nome == null || nome.trim().isEmpty()) return "TU";
        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 1) return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
        return ("" + partes[0].charAt(0) + partes[partes.length - 1].charAt(0)).toUpperCase();
    }

    private void carregarDados() {
        data.setAll(tutorService.listarTodos());
    }

    private void abrirModalNovoTutor() {
        Dialog<Tutor> dialog = new Dialog<>();
        dialog.setTitle("Cadastrar Novo Tutor");
        dialog.setHeaderText("Preencha os dados cadastrais do responsável.");

        ButtonType btnSalvarType = new ButtonType("Salvar Cadastro", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSalvarType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 20, 20));

        TextField nomeField = new TextField();
        nomeField.setPromptText("Ex: Mariana Souza");

        TextField cpfField = new TextField();
        cpfField.setPromptText("000.000.000-00");

        TextField telField = new TextField();
        telField.setPromptText("(11) 99999-9999");

        TextField emailField = new TextField();
        emailField.setPromptText("exemplo@email.com");

        TextField endField = new TextField();
        endField.setPromptText("Rua, Número, Bairro, Cidade");

        grid.add(new Label("Nome Completo: *"), 0, 0);
        grid.add(nomeField, 1, 0);
        grid.add(new Label("CPF: *"), 0, 1);
        grid.add(cpfField, 1, 1);
        grid.add(new Label("Telefone: *"), 0, 2);
        grid.add(telField, 1, 2);
        grid.add(new Label("E-mail:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Endereço: *"), 0, 4);
        grid.add(endField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnSalvarType) {
                OperationResult<Tutor> res = tutorService.cadastrar(
                        nomeField.getText(),
                        cpfField.getText(),
                        telField.getText(),
                        emailField.getText(),
                        endField.getText()
                );

                if (!res.isSuccess()) {
                    UIUtils.showError("Atenção no Cadastro", res.getMessage());
                    return null;
                }
                return res.getData();
            }
            return null;
        });

        Optional<Tutor> result = dialog.showAndWait();
        result.ifPresent(t -> {
            carregarDados();
            UIUtils.showInfo("Sucesso", "Tutor " + t.getNomeCompleto() + " cadastrado com sucesso!");
        });
    }
}
