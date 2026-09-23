package com.myow.view;

import com.myow.model.Perfil;
import com.myow.model.Usuario;
import com.myow.service.AuthService;
import com.myow.service.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.LinkedHashMap;
import java.util.Map;

public class MainLayout extends BorderPane {
    private final AuthService authService = new AuthService();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final Runnable onLogout;

    private final StackPane viewContainer = new StackPane();
    private final Map<String, Button> navButtons = new LinkedHashMap<>();

    public MainLayout(Runnable onLogout) {
        this.onLogout = onLogout;
        setStyle("-fx-background-color: #062e28;");
        initUI();
    }

    private void initUI() {
        // --- Sidebar Left (Figma Dark Pine Teal #04241f) ---
        VBox sidebar = new VBox(6);
        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(240);
        sidebar.setPadding(new Insets(18, 12, 18, 12));
        sidebar.setStyle("-fx-background-color: #04241f; -fx-border-color: rgba(255, 255, 255, 0.06); -fx-border-width: 0 1 0 0;");

        // Brand no topo da sidebar
        HBox brandBox = new HBox(10);
        brandBox.setAlignment(Pos.CENTER_LEFT);
        brandBox.setPadding(new Insets(4, 8, 18, 8));

        Label brandIcon = new Label("🐾");
        brandIcon.setFont(Font.font(24));

        VBox brandTexts = new VBox(1);
        Label brandTitle = new Label("MYOW");
        brandTitle.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, 20));
        brandTitle.setTextFill(Color.WHITE);

        Label brandSub = new Label("Hospital Veterinário");
        brandSub.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        brandSub.setTextFill(Color.web("#34d399"));

        brandTexts.getChildren().addAll(brandTitle, brandSub);
        brandBox.getChildren().addAll(brandIcon, brandTexts);
        sidebar.getChildren().add(brandBox);

        // Navigation Items
        Usuario user = sessionManager.getUsuarioLogado();
        Perfil perfil = user != null ? user.getPerfil() : Perfil.FUNCIONARIO;

        criarItemMenu(sidebar, "Dashboard", "📊  Painel Geral");

        if (perfil == Perfil.ADMINISTRADOR || perfil == Perfil.FUNCIONARIO) {
            criarItemMenu(sidebar, "Tutores", "👥  Tutores");
        }

        criarItemMenu(sidebar, "Pacientes", "🐾  Pacientes");
        criarItemMenu(sidebar, "Agendamentos", "📅  Agendamentos");

        if (perfil == Perfil.ADMINISTRADOR || perfil == Perfil.VETERINARIO) {
            criarItemMenu(sidebar, "Atendimento Clínico", "🩺  Atendimento Clínico");
            criarItemMenu(sidebar, "Prontuário", "📋  Prontuário");
        }

        criarItemMenu(sidebar, "Estoque", "📦  Estoque & Farmácia");

        if (perfil == Perfil.ADMINISTRADOR || perfil == Perfil.FUNCIONARIO) {
            criarItemMenu(sidebar, "Faturamento", "💳  Caixa & Faturamento");
        }

        if (perfil == Perfil.ADMINISTRADOR) {
            criarItemMenu(sidebar, "Relatórios", "📈  Relatórios");
            criarItemMenu(sidebar, "Funcionários", "⚙️  Equipe & Acessos");
        }

        Region navSpacer = new Region();
        VBox.setVgrow(navSpacer, Priority.ALWAYS);
        sidebar.getChildren().add(navSpacer);

        // User info card at bottom of sidebar (Figma #063931)
        VBox userCard = new VBox(6);
        userCard.setPadding(new Insets(12));
        userCard.setStyle("-fx-background-color: #063931; -fx-background-radius: 10; -fx-border-color: rgba(255, 255, 255, 0.08); -fx-border-radius: 10;");

        HBox userHeader = new HBox(8);
        userHeader.setAlignment(Pos.CENTER_LEFT);

        Label avatarCircle = new Label(obterIniciais(user != null ? user.getNome() : "US"));
        avatarCircle.setStyle("-fx-background-color: #0d9488; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-min-width: 28px; -fx-min-height: 28px; -fx-max-width: 28px; -fx-max-height: 28px; -fx-alignment: center; -fx-background-radius: 14;");

        VBox userTexts = new VBox(1);
        Label uName = new Label(user != null ? user.getNome() : "Usuário");
        uName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        uName.setTextFill(Color.WHITE);

        Label uCargo = new Label(user != null ? user.getCargo() : "Colaborador");
        uCargo.setFont(Font.font("Segoe UI", 11));
        uCargo.setTextFill(Color.web("#a7f3d0"));

        userTexts.getChildren().addAll(uName, uCargo);
        userHeader.getChildren().addAll(avatarCircle, userTexts);

        Button btnLogout = new Button("Encerrar Sessão 🚪");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setStyle("-fx-background-color: #04241f; -fx-text-fill: #cbd5e1; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 7; -fx-background-radius: 6; -fx-cursor: hand;");
        btnLogout.setOnMouseEntered(e -> btnLogout.setStyle("-fx-background-color: #991b1b; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 7; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnLogout.setOnMouseExited(e -> btnLogout.setStyle("-fx-background-color: #04241f; -fx-text-fill: #cbd5e1; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 7; -fx-background-radius: 6; -fx-cursor: hand;"));

        btnLogout.setOnAction(e -> {
            authService.logout();
            onLogout.run();
        });

        userCard.getChildren().addAll(userHeader, btnLogout);
        sidebar.getChildren().add(userCard);

        setLeft(sidebar);

        // --- Área Central com Fundo Dark Pine Teal e Cúpula do Logo ---
        VBox centerArea = new VBox(0);
        centerArea.setStyle("-fx-background-color: #062e28;");

        // Cúpula curva branca no topo central (Arco do Figma)
        StackPane topArch = UIUtils.createBrandArchHeader();

        VBox.setVgrow(viewContainer, Priority.ALWAYS);
        viewContainer.setStyle("-fx-background-color: transparent;");

        centerArea.getChildren().addAll(topArch, viewContainer);
        setCenter(centerArea);

        // Inicia na tela Dashboard
        navegarPara("Dashboard");
    }

    private String obterIniciais(String nome) {
        if (nome == null || nome.trim().isEmpty()) return "US";
        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 1) return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
        return ("" + partes[0].charAt(0) + partes[partes.length - 1].charAt(0)).toUpperCase();
    }

    private void criarItemMenu(VBox container, String id, String texto) {
        Button btn = new Button(texto);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-padding: 10 14; -fx-background-radius: 8; -fx-cursor: hand;");

        btn.setOnMouseEntered(e -> {
            if (!btn.getStyle().contains("#064e43")) {
                btn.setStyle("-fx-background-color: #063931; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 14; -fx-background-radius: 8; -fx-cursor: hand;");
            }
        });

        btn.setOnMouseExited(e -> {
            if (!btn.getStyle().contains("#064e43")) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-padding: 10 14; -fx-background-radius: 8; -fx-cursor: hand;");
            }
        });

        btn.setOnAction(e -> navegarPara(id));

        navButtons.put(id, btn);
        container.getChildren().add(btn);
    }

    public boolean verificarPermissaoNavegacao(Perfil perfil, String viewName) {
        if (perfil == Perfil.ADMINISTRADOR) return true;

        switch (viewName) {
            case "Dashboard":
            case "Pacientes":
            case "Agendamentos":
            case "Estoque":
                return true;
            case "Tutores":
            case "Faturamento":
                return perfil == Perfil.FUNCIONARIO;
            case "Atendimento Clínico":
            case "Prontuário":
                return perfil == Perfil.VETERINARIO;
            case "Relatórios":
            case "Funcionários":
                return false; // Apenas administrador
            default:
                return true;
        }
    }

    public void navegarPara(String viewName) {
        navButtons.forEach((name, btn) -> {
            if (name.equalsIgnoreCase(viewName)) {
                btn.setStyle("-fx-background-color: #064e43; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 14; -fx-background-radius: 8; -fx-border-color: #10b981; -fx-border-width: 0 0 0 3;");
            } else {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-padding: 10 14; -fx-background-radius: 8; -fx-cursor: hand;");
            }
        });

        Usuario user = sessionManager.getUsuarioLogado();
        Perfil perfil = user != null ? user.getPerfil() : Perfil.FUNCIONARIO;

        if (!verificarPermissaoNavegacao(perfil, viewName)) {
            viewContainer.getChildren().setAll(criarTelaAcessoNegado(viewName, perfil));
            return;
        }

        Node viewNode;
        switch (viewName) {
            case "Dashboard":
                viewNode = new DashboardView(this::navegarPara);
                break;
            case "Tutores":
                viewNode = new TutoresView();
                break;
            case "Pacientes":
                viewNode = new PacientesView();
                break;
            case "Agendamentos":
                viewNode = new AgendamentosView();
                break;
            case "Atendimento Clínico":
                viewNode = new AtendimentoView();
                break;
            case "Prontuário":
                viewNode = new ProntuarioView();
                break;
            case "Estoque":
                viewNode = new EstoqueView();
                break;
            case "Faturamento":
                viewNode = new FaturamentoView();
                break;
            case "Relatórios":
                viewNode = new RelatoriosView();
                break;
            case "Funcionários":
                viewNode = new FuncionariosView();
                break;
            default:
                viewNode = new DashboardView(this::navegarPara);
                break;
        }

        viewContainer.getChildren().setAll(viewNode);
    }

    private Node criarTelaAcessoNegado(String tela, Perfil perfil) {
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));

        VBox card = UIUtils.createCard("-fx-padding: 40; -fx-alignment: center; -fx-max-width: 480;");

        Label icon = new Label("🔒");
        icon.setFont(Font.font(48));

        Label title = new Label("Acesso Não Autorizado");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#991b1b"));

        Label msg = new Label("O seu perfil de acesso atual (" + perfil.name() + ") não possui permissão para visualizar o módulo '" + tela + "'.");
        msg.setFont(Font.font("Segoe UI", 13));
        msg.setTextFill(Color.web("#64748b"));
        msg.setWrapText(true);

        Button btnVoltar = UIUtils.createPrimaryButton("Voltar ao Painel Geral");
        btnVoltar.setOnAction(e -> navegarPara("Dashboard"));

        card.getChildren().addAll(icon, title, msg, btnVoltar);
        box.getChildren().add(card);
        return box;
    }
}

