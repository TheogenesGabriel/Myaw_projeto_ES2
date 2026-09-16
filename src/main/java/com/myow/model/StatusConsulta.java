package com.myow.model;

public enum StatusConsulta {
    AGENDADA("Agendada"),
    AGUARDANDO_ATENDIMENTO("Aguardando Atendimento"),
    EM_ANDAMENTO("Em Andamento"),
    REALIZADA("Realizada"),
    CANCELADA("Cancelada");

    private final String descricao;

    StatusConsulta(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static StatusConsulta fromString(String text) {
        for (StatusConsulta s : StatusConsulta.values()) {
            if (s.name().equalsIgnoreCase(text) || s.descricao.equalsIgnoreCase(text)) {
                return s;
            }
        }
        return AGENDADA;
    }
}
