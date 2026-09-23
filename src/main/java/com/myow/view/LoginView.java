package com.myow.view;

import com.myow.model.Usuario;
import com.myow.service.AuthService;
import com.myow.service.OperationResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.Optional;
import java.util.function.Consumer;

public class LoginView extends HBox {
    private final AuthService authService = new AuthService();
    private final Consumer<Usuario> onLoginSuccess;

    public LoginView(Consumer<Usuario> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        initUI();
    }

    private void initUI() {
        // Layout Split-Screen 50/50 fiel ao Figma
        setAlignment(Pos.CENTER);

        // --- LADO ESQUERDO: Fundo Branco com o Logo Myow ampliado ---
        VBox leftPane = new VBox(20);
        leftPane.setAlignment(Pos.CENTER);
        leftPane.setStyle("-fx-background-color: #ffffff;");
        HBox.setHgrow(leftPane, Priority.ALWAYS);
        leftPane.setPadding(new Insets(40));

        Node logoNode = UIUtils.getBrandLogoNode(105);
        leftPane.getChildren().add(logoNode);

        // --- LADO DIREITO: Fundo Dark Pine Teal (#062e28) com o Formulário ---
        VBox rightPane = new VBox();
        rightPane.setAlignment(Pos.CENTER);
        rightPane.setStyle("-fx-background-color: #062e28;");
        HBox.setHgrow(rightPane, Priority.ALWAYS);
        rightPane.setPadding(new Insets(40));

        VBox formContainer = new VBox(18);
        formContainer.setMaxWidth(400);
        formContainer.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Acesso ao Sistema");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.WHITE);

        Label subLabel = new Label("Informe suas credenciais para entrar no hospital veterinário.");
        subLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        subLabel.setTextFill(Color.web("#94a3b8"));
        subLabel.setWrapText(true);

        VBox headerBox = new VBox(6, titleLabel, subLabel);

        // Campo Usuário
        Label userLabel = new Label("Usuário / Login *");
        userLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        userLabel.setTextFill(Color.WHITE);

        TextField userField = new TextField("admin");
        userField.setPromptText("Ex: admin");
        userField.setStyle("-fx-padding: 10 14; -fx-background-color: white; -fx-background-radius: 8; -fx-text-fill: #0f172a; -fx-font-size: 13px;");

        VBox userBox = new VBox(6, userLabel, userField);

        // Campo Senha
        Label passLabel = new Label("Senha *");
        passLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        passLabel.setTextFill(Color.WHITE);

        PasswordField passField = new PasswordField();
        passField.setText("admin123");
        passField.setPromptText("Digite sua senha");
        passField.setStyle("-fx-padding: 10 14; -fx-background-color: white; -fx-background-radius: 8; -fx-text-fill: #0f172a; -fx-font-size: 13px;");

        Hyperlink linkEsqueciSenha = new Hyperlink("Esqueci minha senha");
        linkEsqueciSenha.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        linkEsqueciSenha.setTextFill(Color.web("#6ee7b7"));
        linkEsqueciSenha.setOnAction(e -> abrirModalRecuperacaoSenha(userField.getText()));

        HBox passHeader = new HBox(passLabel, new Region(), linkEsqueciSenha);
        HBox.setHgrow(passHeader.getChildren().get(1), Priority.ALWAYS);
        passHeader.setAlignment(Pos.CENTER_LEFT);

        VBox passBox = new VBox(6, passHeader, passField);

        // Mensagem de Erro
        Label msgLabel = new Label();
        msgLabel.setWrapText(true);
        msgLabel.setTextFill(Color.web("#fca5a5"));
        msgLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        msgLabel.setStyle("-fx-background-color: rgba(239, 68, 68, 0.15); -fx-padding: 8 12; -fx-background-radius: 8; -fx-border-color: #ef4444; -fx-border-radius: 8;");
        msgLabel.setVisible(false);
        msgLabel.managedProperty().bind(msgLabel.visibleProperty());

        // Botão Branco estilo Figma Cadastrar ->
        Button btnEntrar = new Button("Entrar no Sistema →");
        btnEntrar.setMaxWidth(Double.MAX_VALUE);
        btnEntrar.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #064e43; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 10, 0, 0, 2);");
        btnEntrar.setOnMouseEntered(e -> btnEntrar.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #043c34; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 12, 0, 0, 3);"));
        btnEntrar.setOnMouseExited(e -> btnEntrar.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #064e43; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 20; -fx-background-radius: 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 10, 0, 0, 2);"));

        Runnable doLogin = () -> {
            msgLabel.setVisible(false);
            OperationResult<Usuario> result = authService.login(userField.getText(), passField.getText());
            if (result.isSuccess()) {
                onLoginSuccess.accept(result.getData());
            } else {
                msgLabel.setText(result.getMessage());
                msgLabel.setVisible(true);
            }
        };

        btnEntrar.setOnAction(e -> doLogin.run());
        passField.setOnAction(e -> doLogin.run());
        userField.setOnAction(e -> doLogin.run());

        // Box de credenciais de teste
        VBox helperBox = new VBox(4);
        helperBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.06); -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: rgba(255, 255, 255, 0.12); -fx-border-radius: 8;");
        Label helperTitle = new Label("Credenciais de Acesso (SQLite Local):");
        helperTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        helperTitle.setTextFill(Color.web("#cbd5e1"));

        Label h1 = new Label("• Administrador: admin / admin123");
        Label h2 = new Label("• Veterinária: camila.vet / 123456");
        Label h3 = new Label("• Recepção: bia.recepcao / 123456");
        h1.setFont(Font.font("Segoe UI", 11));
        h2.setFont(Font.font("Segoe UI", 11));
        h3.setFont(Font.font("Segoe UI", 11));
        h1.setTextFill(Color.web("#94a3b8"));
        h2.setTextFill(Color.web("#94a3b8"));
        h3.setTextFill(Color.web("#94a3b8"));
        helperBox.getChildren().addAll(helperTitle, h1, h2, h3);

        formContainer.getChildren().addAll(headerBox, userBox, passBox, msgLabel, btnEntrar, helperBox);
        rightPane.getChildren().add(formContainer);

        getChildren().addAll(leftPane, rightPane);
    }

    private void abrirModalRecuperacaoSenha(String usuarioSugerido) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Recuperação de Senha");
        dialog.setHeaderText("Redefinição Segura de Senha (Local)");

        ButtonType btnRedefinirTipo = new ButtonType("Redefinir Senha", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnRedefinirTipo, ButtonType.CANCEL);

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        content.setPrefWidth(380);

        Label lblInstrucao = new Label("Informe seu login ou CPF e a resposta de segurança para cadastrar uma nova senha:");
        lblInstrucao.setWrapText(true);
        lblInstrucao.setFont(Font.font("Segoe UI", 12));
        lblInstrucao.setTextFill(Color.web("#475569"));

        TextField txtLoginCpf = new TextField(usuarioSugerido != null ? usuarioSugerido : "");
        txtLoginCpf.setPromptText("Login ou CPF");
        txtLoginCpf.setStyle("-fx-padding: 8 12; -fx-background-radius: 6;");

        Label lblPergunta = new Label("Pergunta de Segurança:");
        lblPergunta.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        Label lblPerguntaTexto = new Label("Qual o nome da clínica?");
        lblPerguntaTexto.setFont(Font.font("Segoe UI", 12));
        lblPerguntaTexto.setTextFill(Color.web("#064e43"));

        txtLoginCpf.textProperty().addListener((obs, oldV, newV) -> {
            Optional<String> perg = authService.obterPerguntaSeguranca(newV);
            lblPerguntaTexto.setText(perg.orElse("Qual o nome da clínica?"));
        });

        TextField txtResposta = new TextField();
        txtResposta.setPromptText("Sua resposta de segurança (padrão: myow)");
        txtResposta.setStyle("-fx-padding: 8 12; -fx-background-radius: 6;");

        PasswordField txtNovaSenha = new PasswordField();
        txtNovaSenha.setPromptText("Nova senha (mínimo 4 caracteres)");
        txtNovaSenha.setStyle("-fx-padding: 8 12; -fx-background-radius: 6;");

        PasswordField txtConfirmaSenha = new PasswordField();
        txtConfirmaSenha.setPromptText("Confirme a nova senha");
        txtConfirmaSenha.setStyle("-fx-padding: 8 12; -fx-background-radius: 6;");

        Label lblErro = new Label();
        lblErro.setWrapText(true);
        lblErro.setTextFill(Color.web("#dc2626"));
        lblErro.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblErro.setVisible(false);

        content.getChildren().addAll(
                lblInstrucao,
                new Label("Login ou CPF:"), txtLoginCpf,
                lblPergunta, lblPerguntaTexto,
                new Label("Resposta:"), txtResposta,
                new Label("Nova Senha:"), txtNovaSenha,
                new Label("Confirmação:"), txtConfirmaSenha,
                lblErro
        );

        dialog.getDialogPane().setContent(content);

        Button btnConfirmar = (Button) dialog.getDialogPane().lookupButton(btnRedefinirTipo);
        btnConfirmar.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            OperationResult<Void> res = authService.redefinirSenha(
                    txtLoginCpf.getText(),
                    txtResposta.getText(),
                    txtNovaSenha.getText(),
                    txtConfirmaSenha.getText()
            );

            if (!res.isSuccess()) {
                event.consume();
                lblErro.setText(res.getMessage());
                lblErro.setVisible(true);
            } else {
                Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
                sucesso.setTitle("Senha Redefinida");
                sucesso.setHeaderText(null);
                sucesso.setContentText("Senha redefinida com sucesso! Agora você já pode entrar com a nova credencial.");
                sucesso.showAndWait();
            }
        });

        dialog.showAndWait();
    }
}
