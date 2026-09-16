package com.myow.view;

import com.myow.model.Paciente;
import com.myow.model.Tutor;
import com.myow.service.OperationResult;
import com.myow.service.PacienteService;
import com.myow.service.TutorService;
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

public class PacientesView extends VBox {
    private final PacienteService pacienteService = new PacienteService();
    private final TutorService tutorService = new TutorService();

    private final TableView<Paciente> table = new TableView<>();
    private final ObservableList<Paciente> data = FXCollections.observableArrayList();

    public PacientesView() {
        setSpacing(16);
        setPadding(new Insets(0, 24, 24, 24));
        setStyle("-fx-background-color: transparent;");
        initUI();
        carregarDados();
    }

    private void initUI() {
        // 1. Header Card (Figma Style)
        Button btnNovo = UIUtils.createPrimaryButton("+ Novo Paciente");
        btnNovo.setOnAction(e -> abrirModalNovoPaciente());

        HBox headerCard = UIUtils.createHeaderCard("🐾  Cadastro de Pacientes", btnNovo);

        // 2. Main Content Card
        VBox contentCard = UIUtils.createCard("-fx-padding: 20;");
        VBox.setVgrow(contentCard, Priority.ALWAYS);

        // Search & Filter Bar
        HBox searchBox = new HBox(12);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(0, 0, 14, 0));

        TextField searchField = new TextField();
        searchField.setPromptText("Buscar paciente por nome, espécie ou raça...");
        searchField.setPrefWidth(350);
        searchField.setStyle("-fx-padding: 8 12; -fx-background-radius: 8; -fx-border-color: #cbd5e1; -fx-border-radius: 8;");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            data.setAll(pacienteService.filtrar(newVal));
        });

        searchBox.getChildren().add(searchField);

        // Coluna 1: PACIENTE (Avatar com ícone de pet + Nome + Raça)
        TableColumn<Paciente, Void> colNome = new TableColumn<>("PACIENTE");
        colNome.setPrefWidth(220);
        colNome.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Paciente p = getTableView().getItems().get(getIndex());
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER_LEFT);

                    String emoji = "🐾";
                    if (p.getEspecie() != null) {
                        String esp = p.getEspecie().toLowerCase();
                        if (esp.contains("can")) emoji = "🐶";
                        else if (esp.contains("fel")) emoji = "🐱";
                        else if (esp.contains("ave")) emoji = "🦜";
                    }

                    Label iconBadge = new Label(emoji);
                    iconBadge.setStyle("-fx-background-color: #cbf3ea; -fx-min-width: 32px; -fx-min-height: 32px; -fx-max-width: 32px; -fx-max-height: 32px; -fx-alignment: center; -fx-background-radius: 16; -fx-font-size: 14px;");

                    VBox textBox = new VBox(2);
                    Label lblNome = new Label(p.getNome());
                    lblNome.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0f172a;");

                    Label lblRaca = new Label(p.getRaca() != null && !p.getRaca().isEmpty() ? p.getRaca() : "SRD");
                    lblRaca.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

                    textBox.getChildren().addAll(lblNome, lblRaca);
                    box.getChildren().addAll(iconBadge, textBox);
                    setGraphic(box);
                }
            }
        });

        // Coluna 2: ESPÉCIE (Badge)
        TableColumn<Paciente, Void> colEspecie = new TableColumn<>("ESPÉCIE");
        colEspecie.setPrefWidth(120);
        colEspecie.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Paciente p = getTableView().getItems().get(getIndex());
                    Label badge = new Label(p.getEspecie());
                    badge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                    badge.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-padding: 3 8; -fx-background-radius: 10;");
                    setGraphic(badge);
                }
            }
        });

        // Coluna 3: IDADE & SEXO
        TableColumn<Paciente, Void> colInfo = new TableColumn<>("IDADE / SEXO");
        colInfo.setPrefWidth(130);
        colInfo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Paciente p = getTableView().getItems().get(getIndex());
                    Label lbl = new Label(p.getIdade() + " anos • " + p.getSexo());
                    lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");
                    setGraphic(lbl);
                }
            }
        });

        // Coluna 4: TUTOR RESPONSÁVEL
        TableColumn<Paciente, String> colTutor = new TableColumn<>("TUTOR RESPONSÁVEL");
        colTutor.setCellValueFactory(cellData -> {
            String tutorNome = tutorService.buscarPorId(cellData.getValue().getTutorId())
                    .map(Tutor::getNomeCompleto)
                    .orElse("Tutor não vinculado");
            return new SimpleStringProperty(tutorNome);
        });
        colTutor.setPrefWidth(200);

        // Coluna 5: AÇÕES (Botão Selecionar)
        TableColumn<Paciente, Void> colAcoes = new TableColumn<>("AÇÕES");
        colAcoes.setPrefWidth(140);
        colAcoes.setCellFactory(col -> new TableCell<>() {
            private final Button btnSel = UIUtils.createSelectButton("🔍 Selecionar");
            {
                btnSel.setOnAction(e -> {
                    Paciente p = getTableView().getItems().get(getIndex());
                    UIUtils.showInfo("Paciente Selecionado", "Paciente: " + p.getNome() + "\nEspécie: " + p.getEspecie() + "\nRaça: " + p.getRaca());
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

        table.getColumns().addAll(colNome, colEspecie, colInfo, colTutor, colAcoes);
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        // Pagination Bar
        HBox paginationBar = UIUtils.createPaginationBar(1, Math.min(10, data.size()), data.size(), "pacientes", 1, 1);

        contentCard.getChildren().addAll(searchBox, table, paginationBar);

        getChildren().addAll(headerCard, contentCard);
    }

    private void carregarDados() {
        data.setAll(pacienteService.listarTodos());
    }

    private void abrirModalNovoPaciente() {
        Dialog<Paciente> dialog = new Dialog<>();
        dialog.setTitle("Cadastrar Novo Paciente");
        dialog.setHeaderText("Informe os dados do animal e selecione o tutor responsável.");

        ButtonType btnSalvarType = new ButtonType("Salvar Paciente", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSalvarType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 20, 20));

        TextField nomeField = new TextField();
        nomeField.setPromptText("Ex: Thor");

        ComboBox<String> especieCombo = new ComboBox<>(FXCollections.observableArrayList("Canina", "Felina", "Ave", "Outro"));
        especieCombo.setValue("Canina");

        TextField racaField = new TextField();
        racaField.setPromptText("Ex: Golden Retriever ou SRD");

        Spinner<Integer> idadeSpinner = new Spinner<>(0, 30, 2);
        idadeSpinner.setEditable(true);

        ComboBox<String> sexoCombo = new ComboBox<>(FXCollections.observableArrayList("Macho", "Fêmea"));
        sexoCombo.setValue("Macho");

        ComboBox<Tutor> tutorCombo = new ComboBox<>(FXCollections.observableArrayList(tutorService.listarTodos()));
        if (!tutorService.listarTodos().isEmpty()) {
            tutorCombo.setValue(tutorService.listarTodos().get(0));
        }

        TextArea histArea = new TextArea();
        histArea.setPromptText("Alergias conhecidas, histórico de vacinas, etc.");
        histArea.setPrefRowCount(3);

        grid.add(new Label("Tutor Responsável: *"), 0, 0);
        grid.add(tutorCombo, 1, 0);
        grid.add(new Label("Nome do Animal: *"), 0, 1);
        grid.add(nomeField, 1, 1);
        grid.add(new Label("Espécie: *"), 0, 2);
        grid.add(especieCombo, 1, 2);
        grid.add(new Label("Raça: *"), 0, 3);
        grid.add(racaField, 1, 3);
        grid.add(new Label("Idade (anos): *"), 0, 4);
        grid.add(idadeSpinner, 1, 4);
        grid.add(new Label("Sexo: *"), 0, 5);
        grid.add(sexoCombo, 1, 5);
        grid.add(new Label("Histórico Clínico Inicial:"), 0, 6);
        grid.add(histArea, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnSalvarType) {
                Tutor selTutor = tutorCombo.getValue();
                if (selTutor == null) {
                    UIUtils.showError("Atenção", "Selecione o tutor responsável.");
                    return null;
                }

                String nome = nomeField.getText();
                boolean existeDuplicado = pacienteService.verificarDuplicidade(selTutor.getId(), nome);
                boolean forcar = false;

                if (existeDuplicado) {
                    boolean confirmar = UIUtils.showConfirm(
                            "Animal com Mesmo Nome",
                            "Já existe um animal cadastrado com o nome '" + nome + "' para este tutor.",
                            "Deseja cadastrar mesmo assim?"
                    );
                    if (!confirmar) {
                        return null;
                    }
                    forcar = true;
                }

                OperationResult<Paciente> res = pacienteService.cadastrar(
                        nome,
                        especieCombo.getValue(),
                        racaField.getText(),
                        idadeSpinner.getValue(),
                        sexoCombo.getValue(),
                        selTutor.getId(),
                        histArea.getText(),
                        forcar
                );

                if (!res.isSuccess()) {
                    UIUtils.showError("Erro no Cadastro", res.getMessage());
                    return null;
                }
                return res.getData();
            }
            return null;
        });

        Optional<Paciente> result = dialog.showAndWait();
        result.ifPresent(p -> {
            carregarDados();
            UIUtils.showInfo("Sucesso", "Paciente " + p.getNome() + " registrado com sucesso!");
        });
    }
}
