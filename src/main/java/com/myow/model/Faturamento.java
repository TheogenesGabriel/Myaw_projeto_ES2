package com.myow.model;

public class Faturamento {
    private String id;
    private String atendimentoId;
    private String consultaId;
    private String pacienteId;
    private String tutorId;
    private double valorTotal;
    private FormaPagamento formaPagamento;
    private Double valorRecebido;
    private Double troco;
    private String dataPagamento;
    private String horaPagamento;
    private String operadorId;

    public Faturamento() {}

    public Faturamento(String id, String atendimentoId, String consultaId, String pacienteId, String tutorId, double valorTotal, FormaPagamento formaPagamento, Double valorRecebido, Double troco, String dataPagamento, String horaPagamento, String operadorId) {
        this.id = id;
        this.atendimentoId = atendimentoId;
        this.consultaId = consultaId;
        this.pacienteId = pacienteId;
        this.tutorId = tutorId;
        this.valorTotal = valorTotal;
        this.formaPagamento = formaPagamento;
        this.valorRecebido = valorRecebido;
        this.troco = troco;
        this.dataPagamento = dataPagamento;
        this.horaPagamento = horaPagamento;
        this.operadorId = operadorId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAtendimentoId() { return atendimentoId; }
    public void setAtendimentoId(String atendimentoId) { this.atendimentoId = atendimentoId; }

    public String getConsultaId() { return consultaId; }
    public void setConsultaId(String consultaId) { this.consultaId = consultaId; }

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }

    public String getTutorId() { return tutorId; }
    public void setTutorId(String tutorId) { this.tutorId = tutorId; }

    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }

    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }

    public Double getValorRecebido() { return valorRecebido; }
    public void setValorRecebido(Double valorRecebido) { this.valorRecebido = valorRecebido; }

    public Double getTroco() { return troco; }
    public void setTroco(Double troco) { this.troco = troco; }

    public String getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(String dataPagamento) { this.dataPagamento = dataPagamento; }

    public String getHoraPagamento() { return horaPagamento; }
    public void setHoraPagamento(String horaPagamento) { this.horaPagamento = horaPagamento; }

    public String getOperadorId() { return operadorId; }
    public void setOperadorId(String operadorId) { this.operadorId = operadorId; }
}
