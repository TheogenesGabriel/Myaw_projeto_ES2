package com.myow.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.io.InputStream;
import java.util.Optional;

public class UIUtils {

    // Paleta Oficial Figma Myow
    public static final String COLOR_CANVAS = "#062e28";
    public static final String COLOR_PRIMARY = "#064e43";
    public static final String COLOR_PRIMARY_HOVER = "#043c34";
    public static final String COLOR_PRIMARY_DARK = "#04241f";
    public static final String COLOR_MINT = "#10b981";
    public static final String COLOR_MINT_LIGHT = "#d1fae5";
    public static final String COLOR_TEXT_DARK = "#0f172a";
    public static final String COLOR_TEXT_MUTED = "#64748b";
    public static final String COLOR_BORDER = "#e2e8f0";

    public static Button createPrimaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + COLOR_PRIMARY + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 9 18; -fx-background-radius: 8; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + COLOR_PRIMARY_HOVER + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 9 18; -fx-background-radius: 8; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + COLOR_PRIMARY + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 9 18; -fx-background-radius: 8; -fx-cursor: hand;"));
        return btn;
    }

    public static Button createWhiteButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: white; -fx-text-fill: " + COLOR_PRIMARY + "; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 2);");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: " + COLOR_PRIMARY_HOVER + "; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.20), 12, 0, 0, 3);"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: white; -fx-text-fill: " + COLOR_PRIMARY + "; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 2);"));
        return btn;
    }

    public static Button createSecondaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 8; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-border-width: 1; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #0f172a; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 8; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-border-width: 1; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #f8fafc; -fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 8; -fx-border-color: #cbd5e1; -fx-border-radius: 8; -fx-border-width: 1; -fx-cursor: hand;"));
        return btn;
    }

    public static Button createSelectButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + COLOR_PRIMARY + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 6 14; -fx-background-radius: 6; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + COLOR_PRIMARY_HOVER + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 6 14; -fx-background-radius: 6; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + COLOR_PRIMARY + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 6 14; -fx-background-radius: 6; -fx-cursor: hand;"));
        return btn;
    }

    public static Button createDangerButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 8; -fx-border-color: #fecaca; -fx-border-radius: 8; -fx-border-width: 1; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #fecaca; -fx-text-fill: #7f1d1d; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 8; -fx-border-color: #fca5a5; -fx-border-radius: 8; -fx-border-width: 1; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 8; -fx-border-color: #fecaca; -fx-border-radius: 8; -fx-border-width: 1; -fx-cursor: hand;"));
        return btn;
    }

    public static Label createHeaderTitle(String title, String subtitle) {
        Label lbl = new Label(title);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lbl.setTextFill(Color.web(COLOR_TEXT_DARK));
        return lbl;
    }

    public static VBox createCard(String paddingStyle) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 16, 0, 0, 4); " + paddingStyle);
        return card;
    }

    public static HBox createHeaderCard(String title, Node rightAction) {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 20, 14, 20));
        header.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 12, 0, 0, 3);");

        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        lblTitle.setTextFill(Color.web(COLOR_TEXT_DARK));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (rightAction != null) {
            header.getChildren().addAll(lblTitle, spacer, rightAction);
        } else {
            header.getChildren().addAll(lblTitle, spacer);
        }
        return header;
    }

    /**
     * O topo icônico do Figma: Cúpula / Arco curvo branco centralizado contendo o Logo Myow
     */
    public static StackPane createBrandArchHeader() {
        StackPane container = new StackPane();
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(0, 0, 12, 0));

        HBox arch = new HBox();
        arch.setAlignment(Pos.CENTER);
        arch.setPrefWidth(420);
        arch.setMaxWidth(440);
        arch.setPrefHeight(75);
        arch.setMaxHeight(78);
        arch.setStyle("-fx-background-color: white; -fx-background-radius: 0 0 110 110; -fx-padding: 6 30 10 30; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 14, 0, 0, 3);");

        Node logoNode = getBrandLogoNode(52);
        arch.getChildren().add(logoNode);

        container.getChildren().add(arch);
        return container;
    }

    public static Node getBrandLogoNode(double height) {
        try {
            InputStream is = UIUtils.class.getResourceAsStream("/images/myow-logo.png");
            if (is != null) {
                Image img = new Image(is);
                ImageView imageView = new ImageView(img);
                imageView.setFitWidth(300.0);
                imageView.setFitHeight(200.0);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                return imageView;
            }
        } catch (Exception ignored) {
        }

        // Fallback vetorial limpo caso o recurso não seja lido
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER);

        Label mark = new Label("🐾");
        mark.setFont(Font.font("Segoe UI", FontWeight.BOLD, height * 0.45));
        mark.setTextFill(Color.web(COLOR_PRIMARY));

        VBox textBox = new VBox(0);
        textBox.setAlignment(Pos.CENTER_LEFT);

        Label lblMyow = new Label("Myow");
        lblMyow.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, height * 0.42));
        lblMyow.setTextFill(Color.web(COLOR_PRIMARY));

        Label lblSub = new Label("GESTÃO VETERINÁRIA INTELIGENTE");
        lblSub.setFont(Font.font("Segoe UI", FontWeight.BOLD, height * 0.16));
        lblSub.setTextFill(Color.web(COLOR_TEXT_MUTED));

        textBox.getChildren().addAll(lblMyow, lblSub);
        box.getChildren().addAll(mark, textBox);
        return box;
    }

    public static HBox createPaginationBar(int start, int end, int total, String itemLabel, int currentPage, int totalPages) {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 4, 4, 4));

        Label lblInfo = new Label("Mostrando " + start + " a " + end + " de " + total + " " + itemLabel);
        lblInfo.setFont(Font.font("Segoe UI", 12));
        lblInfo.setTextFill(Color.web(COLOR_TEXT_MUTED));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox btnsBox = new HBox(6);
        btnsBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnAnt = new Button("< Anterior");
        btnAnt.setStyle("-fx-background-color: white; -fx-text-fill: #475569; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-cursor: hand;");

        Button btn1 = new Button("1");
        btn1.setStyle("-fx-background-color: " + COLOR_PRIMARY + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 6; -fx-cursor: hand;");

        Button btn2 = new Button("2");
        btn2.setStyle("-fx-background-color: white; -fx-text-fill: #475569; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-cursor: hand;");

        Button btn3 = new Button("3");
        btn3.setStyle("-fx-background-color: white; -fx-text-fill: #475569; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-cursor: hand;");

        Button btnProx = new Button("Próxima >");
        btnProx.setStyle("-fx-background-color: white; -fx-text-fill: #475569; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 6; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-cursor: hand;");

        btnsBox.getChildren().addAll(btnAnt, btn1, btn2, btn3, btnProx);
        bar.getChildren().addAll(lblInfo, spacer, btnsBox);
        return bar;
    }

    public static Label createAdminBadge() {
        Label badge = new Label("🛡️ ADMINISTRADOR");
        badge.setStyle("-fx-background-color: #e6fffa; -fx-text-fill: #0d9488; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 12; -fx-border-color: #0d9488; -fx-border-radius: 12; -fx-border-width: 1;");
        return badge;
    }

    public static Label createVetBadge() {
        Label badge = new Label("🛡️ VETERINÁRIO");
        badge.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #0284c7; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 12; -fx-border-color: #0284c7; -fx-border-radius: 12; -fx-border-width: 1;");
        return badge;
    }

    public static Label createRecepcaoBadge() {
        Label badge = new Label("🛡️ RECEPÇÃO");
        badge.setStyle("-fx-background-color: #f3e8ff; -fx-text-fill: #7c3aed; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 12; -fx-border-color: #7c3aed; -fx-border-radius: 12; -fx-border-width: 1;");
        return badge;
    }

    public static Label createAtivoBadge(boolean ativo) {
        Label badge = new Label(ativo ? "● Ativo" : "● Inativo");
        if (ativo) {
            badge.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 12;");
        } else {
            badge.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 12;");
        }
        return badge;
    }

    public static Label createCrmvBadge(String crmv) {
        Label badge = new Label("🪪 " + crmv);
        badge.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #0f766e; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 12; -fx-border-color: #cbd5e1; -fx-border-radius: 12; -fx-border-width: 1;");
        return badge;
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean showConfirm(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}

