package com.myow.model;

public enum FormaPagamento {
    DINHEIRO("Dinheiro"),
    CARTAO("Cartão"),
    PIX("Pix");

    private final String descricao;

    FormaPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static FormaPagamento fromString(String text) {
        for (FormaPagamento fp : FormaPagamento.values()) {
            if (fp.name().equalsIgnoreCase(text) || fp.descricao.equalsIgnoreCase(text)) {
                return fp;
            }
        }
        return CARTAO;
    }
}
