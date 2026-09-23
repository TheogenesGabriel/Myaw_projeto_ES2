package com.myow.model;

public enum Perfil {
    ADMINISTRADOR("Administrador"),
    VETERINARIO("Veterinário"),
    FUNCIONARIO("Funcionário");

    private final String descricao;

    Perfil(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static Perfil fromString(String text) {
        for (Perfil p : Perfil.values()) {
            if (p.name().equalsIgnoreCase(text) || p.descricao.equalsIgnoreCase(text)) {
                return p;
            }
        }
        return FUNCIONARIO;
    }
}
